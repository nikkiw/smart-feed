package com.core.database

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.core.database.content.entity.UpdatesMetaEntity
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@RunWith(AndroidJUnit4::class)
class UpdatesMetaDaoTest {

    private lateinit var db: AppDatabase

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = AppDatabase.getTestDatabase(context)
    }

    @After
    fun closeDb() {
        db.close()
    }


    @Test
    fun test_metadata_is_empty() = runTest {
        val meta = db.updatesMetaDao().getMeta()
        assertNull(meta)
    }

    @Test
    fun test_update_metadata() = runTest {
        val meta = UpdatesMetaEntity(
            lastSyncAt = "2024-01-01T10:00:00"
        )

        db.updatesMetaDao().saveMeta(meta)

        var actualMeta = db.updatesMetaDao().getMeta()

        assertEquals(meta, actualMeta)

        val exceptedMeta = UpdatesMetaEntity(
            lastSyncAt = "2025-01-01T10:00:00"
        )

        db.updatesMetaDao().saveMeta(exceptedMeta)

        actualMeta = db.updatesMetaDao().getMeta()

        assertEquals(exceptedMeta, actualMeta)
    }

    @Test
    fun test_save_other_id_does_not_affect_meta() = runTest {
        val metaOtherId = UpdatesMetaEntity(id = 2, lastSyncAt = "2024-01-01T10:00:00")
        db.updatesMetaDao().saveMeta(metaOtherId)

        val actualMeta = db.updatesMetaDao().getMeta()
        assertNull(actualMeta)
    }

    @Test
    fun test_save_empty_lastSyncAt() = runTest {
        val meta = UpdatesMetaEntity(lastSyncAt = "")
        db.updatesMetaDao().saveMeta(meta)

        val actual = db.updatesMetaDao().getMeta()
        assertNotNull(actual)
        assertEquals("", actual?.lastSyncAt)
    }

    @Test
    fun test_save_very_long_lastSyncAt() = runTest {
        val longString = "X".repeat(10_000)
        val meta = UpdatesMetaEntity(lastSyncAt = longString)
        db.updatesMetaDao().saveMeta(meta)

        val actual = db.updatesMetaDao().getMeta()
        assertNotNull(actual)
        assertEquals(longString, actual?.lastSyncAt)
    }

    @Test
    fun test_meta_always_has_id_1() = runTest {
        val meta = UpdatesMetaEntity(lastSyncAt = "2024")
        db.updatesMetaDao().saveMeta(meta)

        val actual = db.updatesMetaDao().getMeta()
        assertNotNull(actual)
        assertEquals(1, actual.id)
    }

    @Test
    fun test_table_has_only_one_record_after_multiple_saves() = runTest {
        val meta1 = UpdatesMetaEntity(lastSyncAt = "1")
        val meta2 = UpdatesMetaEntity(lastSyncAt = "2")
        val meta3 = UpdatesMetaEntity(lastSyncAt = "3")

        db.updatesMetaDao().saveMeta(meta1)
        db.updatesMetaDao().saveMeta(meta2)
        db.updatesMetaDao().saveMeta(meta3)

        val all = db.updatesMetaDao().getAll()
        assertEquals(1, all.size)
    }

}
