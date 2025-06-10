**Техническая спецификация**: **Система рекомендаций статей**

---

## 1. Введение

**Цель:**

* Построить гибкую рекомендательную систему для Android-приложения с возможностью:

    * Учёта множества сигналов (эмбеддинги, engagement, просмотры).
    * Периодической генерации рекомендаций в фоне (WorkManager).
    * Внедрения через Hilt.
    * Хранения всех данных в Room.

**Область применения:** рекомендации для ленты пользователя и для страницы конкретной статьи.

## 2. Нефункциональные требования

| Категория          | Показатель                                                                       |
|--------------------|----------------------------------------------------------------------------------|
| Производительность | Генерация рекомендаций < 500 мс                                                  |
| Расширяемость      | Добавление новых сигналов без правок в ядре                                      |
| Тестируемость      | Unit- и integration-tests для каждого модуля                                     |
| Надёжность         | Рекомендации должны генерироваться регулярно (каждые 6ч) и при старте приложения |

## 3. Компоненты системы

### 3.1 data-module

* **AppDatabase (Room)**

    * Entities: `ReadEvent`, `ShowEvent`, `EmbeddingEntity`, `UserProfileEntity`
    * DAOs: `ReadEventDao`, `ShowEventDao`, `EmbeddingDao`, `UserProfileDao`
    * Converters: `EmbeddingConverter` (FloatArray ↔ ByteArray)

### 3.2 domain-module

* **Интерфейс `Recommender`**

  ```kotlin
  interface Recommender {
    suspend fun recommendForUser(userId: String, limit: Int = 10): List<Recommendation>
    suspend fun recommendForArticle(articleId: String, userId: String? = null, limit: Int = 10): List<Recommendation>
  }
  ```
* **DTO `Recommendation`**: `{ category: String, articles: List<String> }`
* **UseCases**: `RecommendForUserUseCase`, `RecommendForArticleUseCase`

### 3.3 di-module (Hilt)

* Модули:

    * `DatabaseModule` → provides `AppDatabase` и DAOs
    * `RecommenderModule` → provides `RecommenderImpl` singleton

### 3.4 app-module

* **RecommendationWorker** (`CoroutineWorker`, @HiltWorker)

    * Запускается по расписанию (PeriodicWorkRequest каждые 6ч)
    * Вызывает `recommendForUser`, сохраняет события показа (`ShowEventDao`)
* **Scheduler**

    * Планирует работу WorkManager при логине/старте приложения

## 4. Сигналы и хранение

| Сигнал               | Entity              | Поля                                                 |
|----------------------|---------------------|------------------------------------------------------|
| Читать событие       | `ReadEvent`         | userId, articleId, percentRead, timeSpent, timestamp |
| Показ события        | `ShowEvent`         | userId, articleId, showCount, timestamp              |
| Эмбеддинг статьи     | `EmbeddingEntity`   | id, topic, embedding (BLOB)                          |
| Профиль пользователя | `UserProfileEntity` | userId, embedding, visitsCount                       |

Все сигналы пишутся в Room сразу по событиям UI.

## 5. Алгоритмы рекомендаций

### 5.1 Построение профиля

* **Первичный профиль**:

    1. Onboarding-теги → усреднение эмбеддингов популярных статей по темам.
    2. Фоллбэк: популярные статьи за последние 24ч.
    3. Кластерный подход (по демографии) → centroids.
* **Обновление профиля**:

    * Взвешенное среднее (`(old*count + emb*weight)/ (count+1)`), weight = f(percentRead, time)
    * EMA: `new = α * emb + (1-α) * old`

### 5.2 Ранжирование кандидатов

* **Cosine Similarity**: базовый скор.
* **MMR** (λ \* sim\_to\_profile − (1−λ) \* max\_sim\_to\_selected).
* **Cold Picks**: min(sim\_to\_profile).

### 5.3 Учет дополнительных сигналов

* **Engagement**: смешивание similarity и engagementWeight = α·sim + (1−α)·eng
* **Read penalty**: понижение скора для уже прочитанных.
* **Разнообразие тем**: группировка по `topic`, проброс остальных типов рекомендаций в MMR.

## 6. Поток данных (Sequence Diagrams)

1. **Событие чтения** → UI → `ReadEventDao.insert(ReadEvent)` → triggers `updateProfileWithEngagement` в `RecommenderImpl`.
2. **WorkManager** запускает `RecommendationWorker` → вызывает `Recommender.recommendForUser` → сохраняет `ShowEvent` и пушит LiveData/Notification.
3. При открытии статьи UI вызывает `recommendForArticle`, отображает блоки рекомендаций.

## 7. API интерфейсы

```kotlin
class RecommenderImpl @Inject constructor(
    private val readDao: ReadEventDao,
    private val showDao: ShowEventDao,
    private val embDao: EmbeddingDao,
    private val profileDao: UserProfileDao
) : Recommender { ... }
```

* **Методы**:

    * `recommendForUser(userId, limit)` → List<Recommendation>
    * `recommendForArticle(articleId, userId?, limit)` → List<Recommendation>

## 8. Best Practices

* **Separation of Concerns**: data, domain, app.
* **Dependency Injection** с Hilt: все зависимости проброшены через DI.
* **WorkManager** для фоновых задач.
* **Room + TypeConverters** для бинарных эмбеддингов.
* **Unit tests**: mock DAOs + тесты алгоритмов (cosine, MMR).
* **Configuration**: параметры (λ, α, период воркера) вынести в `Config`.
* **Observability**: логирование/метрики (Timber, Firebase Analytics).

## 9. Развёртывание и CI/CD

* **Gradle modules**: `:data`, `:domain`, `:app`.
* **Lint** и **Detekt** для качества кода.
* **Unit tests** + **Android Instrumentation tests**.
* **Release Pipeline**: GitHub Actions → сборка, тесты → Play Store.
