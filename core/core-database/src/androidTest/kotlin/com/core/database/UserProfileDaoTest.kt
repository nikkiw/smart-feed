package com.core.database

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.core.database.userprofile.UserProfileDao
import com.core.database.userprofile.UserProfileEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.runner.RunWith
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@RunWith(AndroidJUnit4::class)
class UserProfileDaoTest {

    private lateinit var db: AppDatabase
    private lateinit var userProfileDao: UserProfileDao


    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = AppDatabase.getTestDatabase(context)
        userProfileDao = db.userProfileDao()
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun testGetProfile_returnsNullWhenNotExists() = runTest {
        val profile = userProfileDao.getProfile(1).first()
        assertNull(profile)
    }

    @Test
    fun testInsertAndGetProfile() = runTest {
        val profile = UserProfileEntity(
            userId = 1,
            embedding = floatArrayOf(1f, 2f, 3f),
            visitsCount = 5
        )
        userProfileDao.upsert(profile)
        val result = userProfileDao.getProfile(1).first()
        assertNotNull(result)
        assertEquals(profile, result)
    }

    @Test
    fun testUpdateProfile_updatesExistingEntry() = runTest {
        // Insert initial profile
        val initialProfile = UserProfileEntity(
            userId = 1,
            embedding = floatArrayOf(0f, 0f),
            visitsCount = 0
        )
        userProfileDao.upsert(initialProfile)

        // Update profile
        val updatedProfile = UserProfileEntity(
            userId = 1,
            embedding = floatArrayOf(1f, 2f, 3f),
            visitsCount = 10
        )
        userProfileDao.upsert(updatedProfile)

        // Verify update
        val result = userProfileDao.getProfile(1).first()
        assertNotNull(result)
        assertEquals(updatedProfile, result)
    }

    @Test
    fun testVisitsCount_defaultValue() = runTest {
        val profile = UserProfileEntity(
            userId = 1,
            embedding = floatArrayOf()
        )
        userProfileDao.upsert(profile)
        val result = userProfileDao.getProfile(1).first()
        assertNotNull(result)
        assertEquals(0, result.visitsCount)
    }

    @Test
    fun testMultipleProfiles_withDifferentIds() = runTest {
        val profile1 = UserProfileEntity(
            userId = 1,
            embedding = floatArrayOf(1f)
        )
        val profile2 = UserProfileEntity(
            userId = 2,
            embedding = floatArrayOf(2f)
        )

        userProfileDao.upsert(profile1)
        userProfileDao.upsert(profile2)

        val result1 = userProfileDao.getProfile(1).first()
        val result2 = userProfileDao.getProfile(2).first()

        assertNotNull(result1)
        assertNotNull(result2)
        assertEquals(profile1, result1)
        assertEquals(profile2, result2)
    }

    @Test
    fun testEmptyEmbeddingArray() = runTest {
        val profile = UserProfileEntity(
            userId = 1,
            embedding = floatArrayOf(),
            visitsCount = 0
        )
        userProfileDao.upsert(profile)
        val result = userProfileDao.getProfile(1).first()
        assertNotNull(result)
        assertTrue(result.embedding.contentEquals(floatArrayOf()))
    }

    @Test
    fun testMaxValues() = runTest {
        val profile = UserProfileEntity(
            userId = Long.MAX_VALUE,
            embedding = floatArrayOf(Float.MAX_VALUE, -Float.MAX_VALUE),
            visitsCount = Int.MAX_VALUE
        )
        userProfileDao.upsert(profile)
        val result = userProfileDao.getProfile(Long.MAX_VALUE).first()
        assertNotNull(result)
        assertEquals(profile, result)
    }

    @Test
    fun testMinValues() = runTest {
        val profile = UserProfileEntity(
            userId = Long.MIN_VALUE,
            embedding = floatArrayOf(Float.MIN_VALUE, -Float.MIN_VALUE),
            visitsCount = Int.MIN_VALUE
        )
        userProfileDao.upsert(profile)
        val result = userProfileDao.getProfile(Long.MIN_VALUE).first()
        assertNotNull(result)
        assertEquals(profile, result)
    }

    @Test
    fun testNegativeUserId() = runTest {
        val profile = UserProfileEntity(
            userId = -1,
            embedding = floatArrayOf(1f)
        )
        userProfileDao.upsert(profile)
        val result = userProfileDao.getProfile(-1).first()
        assertNotNull(result)
        assertEquals(profile, result)
    }

    @Test
    fun testConcurrentUpdates() = runTest {
        val initialProfile = UserProfileEntity(
            userId = 1,
            embedding = floatArrayOf(0f)
        )
        userProfileDao.upsert(initialProfile)

        // Simulate concurrent updates
        val job1 = launch {
            val profile = userProfileDao.getProfile(1).first()!!
            val updated = profile.copy(embedding = floatArrayOf(1f))
            userProfileDao.upsert(updated)
        }

        val job2 = launch {
            val profile = userProfileDao.getProfile(1).first()!!
            val updated = profile.copy(embedding = floatArrayOf(2f))
            userProfileDao.upsert(updated)
        }

        job1.join()
        job2.join()

        // Final state should be one of the two updates due to REPLACE strategy
        val finalProfile = userProfileDao.getProfile(1).first()!!
        assertTrue(
            finalProfile.embedding.contentEquals(floatArrayOf(1f)) ||
                    finalProfile.embedding.contentEquals(floatArrayOf(2f))
        )
    }
}