package com.core.database.userprofile

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * DAO interface for accessing and modifying user profile data in the database.
 *
 * Provides methods to retrieve user profiles as reactive streams and to insert or update
 * user profile entities with conflict resolution.
 */
@Dao
interface UserProfileDao {

    /**
     * Retrieves the user profile for the given [id] as a [Flow].
     * Emits updates whenever the profile changes.
     *
     * @param id The unique identifier of the user.
     * @return A [Flow] emitting the [UserProfileEntity] if found, or null if no profile exists.
     */
    @Query("SELECT * FROM user_profile WHERE userId = :id")
    fun getProfile(id: Long): Flow<UserProfileEntity?>

    /**
     * Inserts or updates a [UserProfileEntity] in the database.
     * If a profile with the same [userId] already exists, it will be replaced.
     *
     * @param profile The user profile entity to insert or update.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(profile: UserProfileEntity)
}
