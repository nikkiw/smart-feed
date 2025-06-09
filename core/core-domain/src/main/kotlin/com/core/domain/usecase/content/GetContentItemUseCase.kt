package com.core.domain.usecase.content

import com.core.domain.model.ContentItem
import com.core.domain.model.ContentItemId
import com.core.domain.repository.ContentItemRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GetContentItemUseCase @Inject constructor(
    private val contentItemRepository: ContentItemRepository
) {
    suspend operator fun invoke(itemId: ContentItemId): Result<ContentItem> {
        return contentItemRepository.getContentById(itemId)
    }
}