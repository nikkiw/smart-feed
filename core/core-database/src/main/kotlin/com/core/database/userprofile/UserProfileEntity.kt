package com.core.database.userprofile

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Represents a user profile entity stored in the database.
 *
 * @property userId Unique identifier of the user (primary key).
 * @property embedding A numeric vector (FloatArray) representing user features or preferences, used for personalization or recommendations.
 * @property visitsCount The number of times the user has visited or interacted with the app; defaults to 0.
 *
 * Note: The [equals] and [hashCode] methods are overridden to consider
 * only the [userId] and [visitsCount] fields for equality checks.
 */
@Entity(
    tableName = "user_profile"
)
data class UserProfileEntity(
    @PrimaryKey val userId: Long,
    val embedding: FloatArray,
    val visitsCount: Int = 0
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as UserProfileEntity

        if (userId != other.userId) return false
        if (visitsCount != other.visitsCount) return false

        return true
    }

    override fun hashCode(): Int {
        var result = userId.hashCode()
        result = 31 * result + visitsCount
        return result
    }
}
