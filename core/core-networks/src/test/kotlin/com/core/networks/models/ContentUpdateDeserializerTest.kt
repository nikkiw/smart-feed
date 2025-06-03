package com.core.networks.models

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class ContentUpdateDeserializerTest {
    private lateinit var gson: Gson

    @Before
    fun setUp() {
        gson = GsonBuilder()
            .registerTypeAdapter(ContentUpdate::class.java, contentUpdateDeserializer)
            .create()
    }

    @Test
    fun `deserializes article type correctly`() {
        val json = """
        {
          "id": "123",
          "type": "article",
          "action": "upsert",
          "updatedAt": "2025-06-01T12:00:00Z",
          "mainImageUrl": "https://res.soft24hours.com/images/tst/boxer.webp",
          "attributes": {
            "title": "Test Article",
            "content": "This is a test.",
            "tags": ["tag1", "tag2"]
          }
        }
        """.trimIndent()

        val update = gson.fromJson(json, ContentUpdate::class.java)
        Assert.assertNotNull(update)
        Assert.assertEquals("123", update.id)
        Assert.assertEquals("article", update.type)
        Assert.assertEquals("upsert", update.action)
        Assert.assertEquals("2025-06-01T12:00:00Z", update.updatedAt)
        Assert.assertTrue(update.attributes is ContentAttributes.Article)

        val attributes = update.attributes as ContentAttributes.Article
        Assert.assertEquals("Test Article", attributes.title)
        Assert.assertEquals("This is a test.", attributes.content)
        Assert.assertEquals(listOf("tag1", "tag2"), attributes.tags)
    }

    @Test
    fun `deserializes quiz type correctly`() {
        val json = """
        {
          "id": "456",
          "type": "quiz",
          "action": "upsert",
          "updatedAt": "2025-06-01T13:00:00Z",
          "mainImageUrl": "https://res.soft24hours.com/images/tst/boxer.webp",
          "attributes": {
            "questions": ["Q1", "Q2", "Q3"]
          }
        }
        """.trimIndent()

        val update = gson.fromJson(json, ContentUpdate::class.java)
        Assert.assertNotNull(update)
        Assert.assertEquals("456", update.id)
        Assert.assertEquals("quiz", update.type)
        Assert.assertEquals("upsert", update.action)
        Assert.assertEquals("2025-06-01T13:00:00Z", update.updatedAt)
        Assert.assertTrue(update.attributes is ContentAttributes.Quiz)

        val attributes = update.attributes as ContentAttributes.Quiz
        Assert.assertEquals(listOf("Q1", "Q2", "Q3"), attributes.questions)
    }

    @Test
    fun `handles unknown content type with null attributes`() {
        val json = """
        {
          "id": "789",
          "type": "unknown",
          "action": "delete",
          "updatedAt": "2025-06-01T14:00:00Z",
          "mainImageUrl": "https://res.soft24hours.com/images/tst/boxer.webp",
          "attributes": {
            "someField": "someValue"
          }
        }
        """.trimIndent()

        val update = gson.fromJson(json, ContentUpdate::class.java)
        Assert.assertNotNull(update)
        Assert.assertEquals("789", update.id)
        Assert.assertEquals("unknown", update.type)
        Assert.assertEquals("delete", update.action)
        Assert.assertEquals("2025-06-01T14:00:00Z", update.updatedAt)
        Assert.assertNull(update.attributes)
    }

    @Test
    fun `deserializes updates response containing data and meta`() {
        val json = """
        {
          "data": [
            {
              "id": "123",
              "type": "article",
              "action": "upsert",
              "updatedAt": "2025-06-01T12:00:00Z",
              "mainImageUrl": "https://res.soft24hours.com/images/tst/boxer.webp",
              "attributes": {
                "title": "Test Article",
                "content": "This is a test.",
                "tags": ["tag1", "tag2"]
              }
            },
            {
              "id": "456",
              "type": "quiz",
              "action": "upsert",
              "updatedAt": "2025-06-01T13:00:00Z",
              "mainImageUrl": "https://res.soft24hours.com/images/tst/boxer.webp",
              "attributes": {
                "questions": ["Q1", "Q2"]
              }
            }
          ],
          "meta": {
            "nextSince": "2025-06-02T00:00:00Z",
            "hasMore": true
          }
        }
        """.trimIndent()

        val response = gson.fromJson(json, UpdatesResponse::class.java)
        Assert.assertNotNull(response)
        Assert.assertEquals(2, response.data.size)

        // Validate first item
        val first = response.data[0]
        Assert.assertEquals("123", first.id)
        Assert.assertEquals("article", first.type)
        Assert.assertTrue(first.attributes is ContentAttributes.Article)

        // Validate second item
        val second = response.data[1]
        Assert.assertEquals("456", second.id)
        Assert.assertEquals("quiz", second.type)
        Assert.assertTrue(second.attributes is ContentAttributes.Quiz)

        // Validate meta
        val meta = response.meta
        Assert.assertEquals("2025-06-02T00:00:00Z", meta.nextSince)
        Assert.assertTrue(meta.hasMore)
    }
}