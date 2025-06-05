package com.core.domain.usecase.sync

import com.core.domain.repository.ContentItemRepository
import javax.inject.Inject

/**
 * Use case для синхронизации контента из сети
 */
class SyncContentUseCase @Inject constructor(
    private val contentItemRepository: ContentItemRepository
) {
    suspend operator fun invoke(): Result<Unit> {
        return contentItemRepository.syncContent()
    }
}
