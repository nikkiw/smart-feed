# On-Device Recommendation Engine

Smart Feed contains a fully local, privacy-first recommendation engine based on article text
embeddings. No reading behavior is sent to a server. All personalization happens on the device.

---

## Goals

- Keep recommendations available **offline** — no server-side personalization at runtime.
- Protect **user privacy** — reading behavior never leaves the device.
- Keep the ranking algorithm isolated inside a **feature-owned vertical slice**, independently
  testable without Android UI dependencies.
- Make the recommendation feature **extensible** — new signals can be added to
  `:feature:recommendation:local` and `:feature:userprofile:impl` without touching the
  ranking core.

---

## Module Ownership

```
:feature:recommendation:api
  Public contracts only.
  Recommendation, RecommendationRepository, Recommender,
  RecommendForUserUseCase, RecommendForArticleUseCase.
  No Android, Room, Hilt, or Decompose imports.

:feature:recommendation:local
  Room schema owned by this feature.
  ArticleEmbedding + ArticleEmbeddingDao
  ContentInteractionStats + ContentInteractionStatsDao
  UserRecommendationEntity, ContentRecommendationEntity + RecommendationDao

:feature:recommendation:impl
  Runtime ranking algorithm.
  RecommenderImpl — cosine similarity, MMR diversification, cold-start picks.
  RecommendationRepositoryImpl — reads cached recommendations from Room.
  RecommendForUserUseCaseImpl, RecommendForArticleUseCaseImpl.
  Hilt bindings (RecommendationDataModule).

:feature:userprofile:api
  UserProfileRepository contract, Embeddings value object.

:feature:userprofile:impl
  UserProfileRepositoryImpl — maintains the user interest vector via
  weighted moving average over article visits.

:core:core-database
  AppDatabase orchestrator.
  Registers entities from :feature:recommendation:local and
  :feature:userprofile:local without depending on their api or impl.
```

---

## Ranking Pipeline

```
Article text embeddings (precomputed, stored in ArticleEmbedding via ArticleEmbeddingDao)
           +
User interaction signals (ContentInteractionStats: readCount, avgReadPercentage, avgReadingTime)
           ↓
User interest vector update
  Formula: new_vec = (old_vec × visitCount + article_vec × engagementWeight) / (visitCount + 1)
  engagementWeight = 0.5 × avgReadPercentage + 0.5 × normalizedReadingTime
  Stored in UserProfileEntity, observed as a Flow by RecommenderImpl.
           ↓
Candidate retrieval
  All article embeddings loaded from ArticleEmbeddingDao.
  Already-read article IDs excluded (ContentInteractionStatsDao.getAllReadContentIds()).
           ↓
Cosine similarity scoring
  Top-K candidates: highest dot(article_embedding, user_profile_vector).
  Cold-K candidates: highest dot(article_embedding, opposite(user_profile_vector))
  — intentionally adds diversity by surfacing distant but potentially interesting content.
           ↓
MMR diversification (Maximal Marginal Relevance)
  score = λ × sim(candidate, profile) − (1 − λ) × max_{s ∈ selected} sim(candidate, s)
  Applied separately to top-K and cold-K candidates, then merged.
           ↓
Ranked recommendation list
  Sorted by MMR score, top mmrK items persisted to UserRecommendationEntity.
  Content-to-content recommendations follow the same pipeline per article.
```

---

## Two Recommendation Modes

| Mode | Trigger | Algorithm |
|------|---------|-----------|
| **User recommendations** | Profile vector updated (article visit) | top-K + cold-K → MMR over user profile vector |
| **Content-to-content** | Batch update (WorkManager) | For each article, top-K + cold-K relative to its own embedding → MMR |

Content-to-content recommendations power the "You might also like" section inside the article
detail screen (`ArticleItemComponent`).

---

## Key Implementation Details

### Already-read exclusion
Articles where `ContentInteractionStatsDao.getAllReadContentIds()` returns a match are
filtered out **before** cosine scoring — not penalized, but fully excluded.

### Cold-start diversity
`EmbeddingIndex.opposite(vector)` negates the user vector, then finds embeddings most
similar to that negation — i.e., articles furthest from current interests. These are
included intentionally to avoid filter bubbles and provide serendipity.

### Profile vector update
Implemented as a **weighted moving average** (not EMA with fixed α):

```
new_vec[i] = (old_vec[i] × visitCount + article_vec[i] × engagementWeight) / (visitCount + 1)
```

The engagement weight is derived from reading stats:
```
engagementWeight = 0.5 × avgReadPercentage + 0.5 × (avgReadingTime / 600s, clipped to [0,1])
```

### Thread safety
`UserProfileRepositoryImpl` uses a `Mutex` to serialize concurrent profile updates when
multiple article visits are processed simultaneously.

---

## Architecture Invariant

| Boundary | Rule |
|----------|------|
| `:feature:recommendation:api` | Exposes contracts only. No Room, Hilt, Retrofit, Android SDK. |
| `:feature:recommendation:local` | Owns Room schema only. No ranking algorithm. |
| `:feature:recommendation:impl` | Owns ranking logic and Hilt DI. Never imported by `:app` directly. |
| `:core:core-database` | May depend on `:feature:recommendation:local`. Must never depend on `:api` or `:impl`. |

This invariant is enforced on every CI build by `Konsist` tests in `:architecture-tests`.

---

## Related Documentation

- [Architecture Overview](architecture.md) — module dependency diagram and feature slice pattern
- [Recommendation System Spec (RU)](recommendation_system_spec_ru.md) — detailed algorithm specification in Russian
