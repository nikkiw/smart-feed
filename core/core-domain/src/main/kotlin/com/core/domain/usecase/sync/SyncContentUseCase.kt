package com.core.domain.usecase.sync

import com.core.domain.repository.ContentItemRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Use case для синхронизации контента из сети
 */
@Singleton
class SyncContentUseCase @Inject constructor(
    private val contentItemRepository: ContentItemRepository
) {
    suspend operator fun invoke(): Result<Unit> {
        return contentItemRepository.syncContent()
    }
}
