# 📘 Content Delta Sync API Specification

## 🔄 Data Exchange Scheme

```text
   Android Client                           Server
        |                                       |
        | --- GET /updates?since=...&limit= --> |
        |                                       |
        | <-- JSON { data, meta } ------------- |
        |                                       |
        | processes upsert/delete               |
        | stores meta.nextSince                 |
        |                                       |
  (if necessary)
        | --- GET /contents/{type}/{id} ------> |
        | <------- JSON full content ---------- |
```

---

## General Information

This API provides a mechanism for retrieving updated content (articles, quizzes, etc.) for the Android application. Updates include both new or modified objects and deleted ones.

Communication is carried out over HTTPS with Bearer token authorization.

---

## 🔐 Authorization

**Request header:**

```
Authorization: Bearer <access_token>
```

---

## 📥 GET `/api/v1/contents/updates`

Returns a list of content updates starting from the specified timestamp (`since`).

### Query Parameters

| Parameter | Type              | Required | Description                                        |
|-----------|-------------------|----------|----------------------------------------------------|
| `since`   | string (ISO 8601) | ✅ Yes    | Date-time of the last synchronization              |
| `limit`   | integer           | ❌ No     | Maximum number of items per request (default: 100) |

---

### Example Request

```
GET /api/v1/contents/updates?since=2024-05-01T10:00:00Z&limit=100
Authorization: Bearer eyJhbGci...
```

---

### Example Response (200 OK)

```json
{
  "data": [
    {
      "id": "abc123",
      "type": "article",
      "action": "upsert",
      "updatedAt": "2024-05-01T12:30:00Z", 
      "mainImageUrl": "https://example.com/images/article/art_1.jpg",
      "attributes": {
        "title": "How to improve sleep",
        "content": "..."
      }
    },
    {
      "id": "xyz789",
      "type": "quiz",
      "action": "delete",
      "updatedAt": "2024-05-01T11:00:00Z",
      "mainImageUrl": "https://example.com/images/quiz/3.jpg",
      "attributes": null
    }
  ],
  "meta": {
    "nextSince": "2024-05-01T12:31:00Z",
    "hasMore": false
  }
}
```

---

### Response Fields

#### `data[]`

| Field          | Type              | Description                                   |
|----------------|-------------------|-----------------------------------------------|
| `id`           | string            | Unique identifier of the object               |
| `type`         | string            | Content type (`article`, `quiz`, `tip`, etc.) |
| `action`       | string            | `upsert` or `delete`                          |
| `updatedAt`    | string (ISO 8601) | Date and time of the last modification        |
| `mainImageUrl` | string (URL)      | URL to the image for the item                 |
| `attributes`   | object/null       | Content fields (absent for `delete` actions)  |

#### `meta`

| Field       | Type              | Description                                                  |
|-------------|-------------------|--------------------------------------------------------------|
| `nextSince` | string (ISO 8601) | Value for the next synchronization (new timestamp)           |
| `hasMore`   | boolean           | Indicates whether there are more updates after this response |

---

## 📄 GET `/api/v1/contents/{type}/{id}`

Get full information about an object by its type and ID.

### Path Parameters

* `{type}` — type of content
* `{id}` — object ID

---

## 🔁 Recommended Client Algorithm

1. Store `lastSyncAt` in `SharedPreferences` or the database.
2. On pull-to-refresh or during a background update:

   * Send a request `GET /contents/updates?since=lastSyncAt`.
   * Process `upsert` and `delete` for each item.
   * Update `lastSyncAt` to the value of `meta.nextSince`.

---

## 📌 Notes

* The server always returns `action: delete` for deleted objects.
* `nextSince` is an ISO 8601 string corresponding to the `max(updatedAt)` in the response.
* All dates are in UTC.
