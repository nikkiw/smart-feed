# 📘 Content Delta Sync API Specification

## 🔄 Схема обмена данными

```text
   Android Client                           Server
        |                                       |
        | --- GET /updates?since=...&limit= --> |
        |                                       |
        | <-- JSON { data, meta } ------------- |
        |                                       |
        | обрабатывает upsert/delete            |
        | сохраняет meta.nextSince              |
        |                                       |
  (при необходимости)
        | --- GET /contents/{type}/{id} ------> |
        | <------- JSON full content ---------- |
````

---

## Общая информация

Этот API предоставляет механизм получения обновлённого контента (статей, тестов и пр.) для Android-приложения. Обновления включают как новые или изменённые объекты, так и удалённые.

Коммуникация осуществляется через HTTPS с авторизацией по Bearer-токену.

---

## 🔐 Авторизация

**Заголовок запроса:**

```
Authorization: Bearer <access_token>
```

---

## 📥 GET `/api/v1/contents/updates`

Возвращает список обновлений контента, начиная с заданной метки времени (`since`).

### Параметры запроса

| Параметр | Тип               | Обязательный | Описание                                                            |
|----------|-------------------|--------------|---------------------------------------------------------------------|
| `since`  | string (ISO 8601) | ✅ Да         | Дата-время последней синхронизации                                  |
| `limit`  | integer           | ❌ Нет        | Максимальное количество объектов за один запрос (по умолчанию: 100) |

---

### Пример запроса

```
GET /api/v1/contents/updates?since=2024-05-01T10:00:00Z&limit=100
Authorization: Bearer eyJhbGci...
```

---

### Пример ответа (200 OK)

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
      "mainImageUrl":	"https://example.com/images/quiz/3.jpg",
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

### Поля ответа

#### `data[]`

| Поле           | Тип               | Описание                                       |
|----------------|-------------------|------------------------------------------------|
| `id`           | string            | Уникальный идентификатор объекта               |
| `type`         | string            | Тип контента (`article`, `quiz`, `tip` и т.д.) |
| `action`       | string            | `upsert` или `delete`                          |
| `updatedAt`    | string (ISO 8601) | Дата и время последнего изменения              |
| `mainImageUrl` | string (URL)      | URl к изображению для элемента                 |
| `attributes`   | object/null       | Поля контента (отсутствуют для `delete`)       |

#### `meta`

| Поле        | Тип               | Описание                                                   |
|-------------|-------------------|------------------------------------------------------------|
| `nextSince` | string (ISO 8601) | Значение для следующей синхронизации (новая метка времени) |
| `hasMore`   | boolean           | Есть ли ещё обновления после текущего ответа               |

---

## 📄 GET `/api/v1/contents/{type}/{id}`

Получить полную информацию об объекте по его типу и ID.

### Параметры пути

* `{type}` — тип контента
* `{id}` — ID объекта

---

## 🔁 Рекомендованный алгоритм клиента

1. Хранить `lastSyncAt` в `SharedPreferences` или БД.
2. При pull-to-refresh или фоновом обновлении:

    * Отправить запрос `GET /contents/updates?since=lastSyncAt`.
    * Обработать `upsert` и `delete` по каждому элементу.
    * Обновить `lastSyncAt` до значения `meta.nextSince`.

---

## 📌 Примечания

* Сервер всегда возвращает `action: delete` для удалённых объектов.
* `nextSince` — это ISO 8601-строка, соответствующая `max(updatedAt)` в ответе.
* Все даты в UTC.

