package com.core.data.service

import com.core.data.embedding.EmbeddingIndex
import com.core.database.content.ContentDao
import com.core.database.embeding.ArticleEmbeddingDao
import com.core.database.event.ContentInteractionStatsDao
import com.core.database.recommendation.entity.ContentRecommendationEntity
import com.core.database.recommendation.RecommendationDao
import com.core.database.recommendation.entity.UserRecommendationEntity
import com.core.di.ApplicationScope
import com.core.di.DefaultDispatcher
import com.core.di.IoDispatcher
import com.core.domain.model.ContentId
import com.core.domain.model.Recommendation
import com.core.domain.repository.UserProfileRepository
import com.core.domain.service.Recommender
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

typealias ArticleEmbeddings = Pair<String, FloatArray>

/**
 * Implementation of the [Recommender] interface using content embeddings and MMR diversification.
 *
 * This service listens for changes in the user's profile embeddings and triggers updates
 * for personalized recommendations. It also generates content-to-content recommendations
 * based on embedding similarity and diversification.
 *
 * ### Usage Example
 * ```kotlin
 * // Injected via DI
 * val recommender: Recommender = RecommenderImpl(
 *     userProfileRepository,
 *     contentInteractionStatsDao,
 *     contentDao,
 *     articleEmbeddingDao,
 *     recommendationDao,
 *     ioDispatcher,
 *     defaultDispatcher,
 *     applicationScope
 * )
 *
 * // Manually trigger update for the current user
 * CoroutineScope(defaultDispatcher).launch {
 *     recommender.updateRecommendationsForUser()
 * }
 *
 * // Update recommendations for all articles (e.g. batch job)
 * CoroutineScope(defaultDispatcher).launch {
 *     recommender.updateRecommendationsForArticles()
 * }
 * ```
 *
 * @property userProfileRepository repository to fetch user profile embeddings
 * @property contentInteractionStatsDao DAO to query user read history
 * @property contentDao DAO to fetch recent content for cold-start
 * @property articleEmbeddingDao DAO to retrieve pre-computed article embeddings
 * @property recommendationDao DAO to save recommendations
 * @property ioDispatcher dispatcher for IO-bound operations
 * @property defaultDispatcher dispatcher for CPU-bound or default operations
 * @property applicationScope application-wide coroutine scope for initialization logic
 * @param topK number of most similar items to consider
 * @param coldK number of "cold" items (least similar) to diversify
 * @param mmrK number of recommendations after diversification
 * @param lambda weight between relevance and diversity in MMR
 */
class RecommenderImpl @Inject constructor(
    private val userProfileRepository: UserProfileRepository,
    private val contentInteractionStatsDao: ContentInteractionStatsDao,
    private val contentDao: ContentDao,
    private val articleEmbeddingDao: ArticleEmbeddingDao,
    private val recommendationDao: RecommendationDao,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    @DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher,
    @ApplicationScope private val applicationScope: CoroutineScope,
    private val topK: Int = 10,
    private val coldK: Int = 4,
    private val mmrK: Int = 5,
    private val lambda: Float = 0.7f
) : Recommender {

    init {
        // Listen for profile changes and update user recommendations automatically
        applicationScope.launch(defaultDispatcher) {
            userProfileRepository.getUserProfileEmbeddings()
                .collect {
                    updateRecommendationsForUser()
                }
        }
    }

    /**
     * Updates personalized recommendations for the current user.
     *
     * If no profile embeddings are available (cold start), fetches the most recent content.
     * Otherwise, computes top-K similar articles, selects opposite (cold) items,
     * and applies MMR diversification to balance relevance and novelty.
     *
     * Results are saved to the [RecommendationDao].
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    override suspend fun updateRecommendationsForUser() = withContext(defaultDispatcher) {
        // Fetch latest user profile embedding (nullable for cold start)
        val userProfile = userProfileRepository.getUserProfileEmbeddings().first()

        val recommendations = if (userProfile == null) {
            // Cold-start: recommend most recent content with uniform score
            withContext(ioDispatcher) { contentDao.getRecentContent(mmrK) }
                .map {
                    Recommendation(
                        articleId = ContentId(it.contentUpdate.id),
                        score = 1f
                    )
                }
        } else {
            // Build in-memory index of all article embeddings
            val embeddingIndex = EmbeddingIndex().apply {
                withContext(ioDispatcher) { articleEmbeddingDao.allEmbeddings() }
                    .forEach { articleEmbedding ->
                        add(articleEmbedding.articleId, articleEmbedding.unitEmbedding)
                    }
            }
            // Exclude already read articles
            val allReadArticles = contentInteractionStatsDao.getAllReadContentIds()

            // Find most similar articles to user profile
            val similarList = embeddingIndex.search(
                userProfile.value,
                k = topK
            ).filter { it.first !in allReadArticles } // skip read
                .sortedByDescending { it.second }

            // Find "cold" items: embeddings farthest (opposite) from profile
            val cold = embeddingIndex.search(
                EmbeddingIndex.Companion.opposite(userProfile.value),
                k = coldK
            ).sortedByDescending { it.second }

            // Diversify both similar and cold lists via MMR
            val mmr = mmrDiversify(
                userProfile.value,
                similarList.map { it.first to embeddingIndex.get(it.first) },
                mmrK,
                lambda
            ) + mmrDiversify(
                userProfile.value,
                cold.map { it.first to embeddingIndex.get(it.first) },
                mmrK,
                lambda
            )

            // Sort final recommendations and take top mmrK
            mmr.sortedByDescending { it.score }.take(mmrK)
        }.map {
            // Convert to persistence entities
            UserRecommendationEntity(
                recommendedContentId = it.articleId.value,
                score = it.score
            )
        }

        // Persist user recommendations
        withContext(ioDispatcher) {
            recommendationDao.replaceUserRecommendations(recommendations)
        }
    }

    /**
     * Updates content-to-content recommendations for all articles.
     *
     * For each article, finds similar and cold items excluding itself and
     * diversifies with MMR before persisting.
     */
    override suspend fun updateRecommendationsForArticles() =
        withContext(defaultDispatcher) {
            // Load all embeddings once
            val allContentEmbeddings =
                withContext(ioDispatcher) { articleEmbeddingDao.allEmbeddings() }

            val embeddingIndex = EmbeddingIndex().apply {
                allContentEmbeddings.forEach { articleEmbedding ->
                    add(articleEmbedding.articleId, articleEmbedding.unitEmbedding)
                }
            }

            val allReadArticles =
                withContext(ioDispatcher) { contentInteractionStatsDao.getAllReadContentIds() }

            // For each article, compute recommendations
            allContentEmbeddings.forEach { currentContent ->
                val articleUnitEmbeddings = currentContent.unitEmbedding

                // Top similar excluding itself and read
                val similarList = embeddingIndex.search(
                    articleUnitEmbeddings,
                    k = topK
                ).filter { it.first != currentContent.articleId && it.first !in allReadArticles }
                    .sortedByDescending { it.second }

                // Cold items for diversity
                val cold = embeddingIndex.search(
                    EmbeddingIndex.Companion.opposite(articleUnitEmbeddings),
                    k = coldK
                ).filter { it.first != currentContent.articleId }
                    .sortedByDescending { it.second }

                // Apply MMR diversification
                val mmr = mmrDiversify(
                    articleUnitEmbeddings,
                    similarList.map { it.first to embeddingIndex.get(it.first) },
                    mmrK,
                    lambda
                ) + mmrDiversify(
                    articleUnitEmbeddings,
                    cold.map { it.first to embeddingIndex.get(it.first) },
                    mmrK,
                    lambda
                )

                // Persist top diversified recommendations
                val recommendations =
                    mmr.sortedByDescending { it.score }.take(mmrK)
                        .map {
                            ContentRecommendationEntity(
                                contentId = currentContent.articleId,
                                recommendedContentId = it.articleId.value,
                                score = it.score
                            )
                        }
                recommendationDao.replaceContentRecommendations(recommendations)
            }
        }

    /**
     * Applies Maximal Marginal Relevance (MMR) algorithm to diversify a list of candidates.
     *
     * @param profile query embedding (user or current item)
     * @param candidates list of Pair(contentId, embedding) to score
     * @param k number of items to select
     * @param lambda weight balancing relevance vs diversity (0..1)
     * @return list of [Recommendation] objects with MMR scores
     *
     * ### Algorithm Steps:
     * 1. Initialize empty `selected` list.
     * 2. While fewer than `k` items selected and candidates remain:
     *    - Compute for each remaining candidate:
     *      MMR score = λ * sim(candidate, profile) - (1 - λ) * max_{s in selected} sim(candidate, s)
     *    - Pick candidate with highest MMR score.
     *    - Move it to `selected` and record its score.
     */
    fun mmrDiversify(
        profile: FloatArray,
        candidates: List<ArticleEmbeddings>,
        k: Int,
        lambda: Float
    ): List<Recommendation> {
        val selected = mutableListOf<ArticleEmbeddings>()
        val remaining = candidates.toMutableList()
        val recommendations = mutableListOf<Recommendation>()

        while (selected.size < k && remaining.isNotEmpty()) {
            // Score each candidate by combining relevance and diversity
            val scored = remaining.map { candidate ->
                val simToQuery = EmbeddingIndex.Companion.dot(candidate.second, profile)
                // similarity to already selected set (max)
                val simToSelected = selected.maxOfOrNull {
                    EmbeddingIndex.Companion.dot(candidate.second, it.second)
                } ?: 0f
                val score = lambda * simToQuery - (1 - lambda) * simToSelected
                candidate to score
            }

            // Select the best candidate
            val (bestCandidate, bestScore) = scored.maxByOrNull { it.second }!!  // safe: at least one

            selected.add(bestCandidate)       // include in final set
            remaining.remove(bestCandidate)   // exclude from future

            recommendations.add(
                Recommendation(
                    articleId = ContentId(bestCandidate.first),
                    score = bestScore
                )
            )
        }

        return recommendations
    }
}
