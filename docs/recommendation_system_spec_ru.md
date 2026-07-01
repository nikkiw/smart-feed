**Техническая спецификация**: **Система рекомендаций статей**

> **Актуальность**: Документ обновлён с учётом текущей модульной архитектуры
> (Feature-Driven Vertical Slice). Старые имена модулей (`data-module`, `domain-module`,
> `di-module`, `app-module`, `:data`, `:domain`) заменены на актуальные.

---

## 1. Введение

**Цель:**

- Построить гибкую рекомендательную систему для Android-приложения с возможностью:
  - Учёта множества сигналов (эмбеддинги, engagement, просмотры).
  - Периодической генерации рекомендаций в фоне (WorkManager).
  - Внедрения через Hilt.
  - Хранения всех данных в Room.

**Область применения:** рекомендации для ленты пользователя и для страницы конкретной статьи.

---

## 2. Нефункциональные требования

| Категория          | Показатель                                                                       |
|--------------------|----------------------------------------------------------------------------------|
| Производительность | Генерация рекомендаций < 500 мс                                                  |
| Расширяемость      | Добавление новых сигналов без правок в ядре                                      |
| Тестируемость      | Unit- и integration-tests для каждого модуля                                     |
| Надёжность         | Рекомендации генерируются регулярно (каждые 6ч) и при старте приложения          |
| Приватность        | Все данные хранятся локально, никакие поведенческие данные не покидают устройство |

---

## 3. Компоненты системы

### 3.1 `:feature:recommendation:api`

Публичный контракт рекомендательной фичи. Не содержит Room, Hilt, Retrofit или
implementation-классы:

- `Recommendation` — доменная модель рекомендации (`articleId`, `score`)
- `RecommendationRepository` — интерфейс получения рекомендаций для пользователя и статьи
- `Recommender` — сервис, управляющий обновлением рекомендаций
- `RecommendForUserUseCase` — use case: рекомендации для текущего пользователя
- `RecommendForArticleUseCase` — use case: статьи, похожие на данную

### 3.2 `:feature:recommendation:local`

Локальная Room-схема хранения рекомендательной фичи:

| Entity | Таблица | Поля |
|--------|---------|------|
| `ArticleEmbedding` | — | `articleId`, `unitEmbedding: FloatArray` |
| `ContentInteractionStats` | `content_interaction_stats` | `contentId`, `readCount`, `avgReadingTime`, `avgReadPercentage` |
| `UserRecommendationEntity` | `user_recommendations` | `recommendedContentId`, `score` |
| `ContentRecommendationEntity` | — | `contentId`, `recommendedContentId`, `score` |

DAOs: `ArticleEmbeddingDao`, `ContentInteractionStatsDao`, `RecommendationDao`

Модуль хранит данные, но **не содержит** runtime-алгоритм ранжирования.

### 3.3 `:feature:recommendation:impl`

Runtime-реализация рекомендательной системы:

- `RecommenderImpl` — основная реализация `Recommender`. Выполняет cosine similarity,
  MMR-диверсификацию, cold-picks и сохранение результатов.
- `EmbeddingIndex` — in-memory индекс для поиска по векторам (`dot`, `opposite`, `search`).
- `RecommendationRepositoryImpl` — читает закешированные рекомендации из Room.
- `RecommendForUserUseCaseImpl`, `RecommendForArticleUseCaseImpl` — реализации use cases.
- `RecommendationDataModule` — Hilt-модуль с биндингами интерфейсов.

Именно здесь выполняются cosine similarity, MMR и сборка финального списка рекомендаций.

### 3.4 `:feature:userprofile:api` / `:feature:userprofile:impl`

`UserProfileRepository` и `Embeddings` — контракт для получения и обновления профиля пользователя.

`UserProfileRepositoryImpl` реализует построение и обновление вектора интересов пользователя
(см. раздел 5.1).

### 3.5 `:core:core-database`

Room-агрегатор:

- Содержит `AppDatabase` — регистрирует все entities из `:feature:*:local`.
- Управляет миграциями схемы.
- **Не зависит** от `:feature:*:api` или `:feature:*:impl`.

### 3.6 `:app`

Composition root:

- Запускает `AppStartupCoordinator` при старте.
- Подключает top-level feature factories через Hilt.
- Не импортирует implementation-классы рекомендательной фичи напрямую.

---

## 4. Сигналы и хранение

| Сигнал                   | Entity                    | Поля                                              |
|--------------------------|---------------------------|---------------------------------------------------|
| Статистика чтения        | `ContentInteractionStats` | `contentId`, `readCount`, `avgReadingTime` (мс), `avgReadPercentage` (0.0–1.0) |
| Эмбеддинг статьи         | `ArticleEmbedding`        | `articleId`, `unitEmbedding: FloatArray` (unit-norm) |
| Профиль пользователя     | `UserProfileEntity`       | `userId`, `embedding: FloatArray`, `visitsCount`  |
| Рекомендации для юзера   | `UserRecommendationEntity`| `recommendedContentId`, `score`                   |
| Рекомендации для статьи  | `ContentRecommendationEntity` | `contentId`, `recommendedContentId`, `score`  |

`ContentInteractionStats` обновляется через Room-триггер после каждого события чтения.

---

## 5. Алгоритмы рекомендаций

### 5.1 Построение профиля

**Инициализация (первый визит):**
```
profileVector = articleEmbedding × engagementWeight
```

**Обновление (последующие визиты) — взвешенное скользящее среднее:**
```
new_vec[i] = (old_vec[i] × visitCount + article_vec[i] × engagementWeight) / (visitCount + 1)
```

Где:
```
engagementWeight = 0.5 × avgReadPercentage + 0.5 × normalizedReadingTime
normalizedReadingTime = clamp(readingTimeSeconds / 600, 0.0, 1.0)
```

Обновление атомарно (защищено `Mutex`) и реактивно: `RecommenderImpl` наблюдает за
профилем через `Flow<Embeddings?>`.

### 5.2 Ранжирование кандидатов

**Cosine Similarity:**
```
score = dot(candidateEmbedding, profileVector)
```
(Эмбеддинги хранятся в unit-norm форме, поэтому dot product = cosine similarity.)

**Cold Picks** (диверсификация через противоположный вектор):
```
cold = search(opposite(profileVector), k = coldK)
```
Намеренно добавляет статьи, далёкие от текущих интересов — снижает эффект фильтр-пузыря.

**MMR (Maximal Marginal Relevance):**
```
MMR_score = λ × sim(candidate, profile) − (1 − λ) × max_{s ∈ selected} sim(candidate, s)
```
Применяется отдельно к top-K и cold-K спискам, результаты объединяются.

### 5.3 Учёт дополнительных сигналов

| Сигнал | Реализация |
|--------|-----------|
| **Прочитанные статьи** | Полностью исключаются из кандидатов (`getAllReadContentIds()`) |
| **Engagement weight** | Влияет на вклад статьи в профиль через `engagementWeight` |
| **Диверсификация** | Cold-picks + MMR обеспечивают разнообразие результатов |

> **Примечание**: Engagement-mixing (α·sim + (1−α)·eng) и topic-grouping описаны как
> возможное расширение. MMR и cold-picks, описанные выше, реализованы и активны в текущем коде.

---

## 6. Поток данных

1. **Событие чтения** → `ContentInteractionStatsDao` обновляется через Room-триггер →
   `UserProfileRepositoryImpl.onArticleVisited()` пересчитывает вектор профиля →
   `UserProfileDao` сохраняет новый `UserProfileEntity`.

2. **Реактивное обновление** → `RecommenderImpl` наблюдает
   `userProfileRepository.getUserProfileEmbeddings()` → при изменении профиля
   автоматически запускает `updateRecommendationsForUser()`.

3. **WorkManager** → `RecommendationWorker` по расписанию вызывает
   `updateRecommendationsForArticles()` — генерирует content-to-content рекомендации для
   всех статей батчем.

4. **Отображение** → `RecommendationRepositoryImpl` читает
   `UserRecommendationEntity` / `ContentRecommendationEntity` из Room →
   `RecommendationListComponent` и `ArticleItemComponent` отображают результаты.

---

## 7. Архитектурные инварианты

```
Recommendation boundary rule:

- :feature:recommendation:api     — только контракты, никаких реализаций.
- :feature:recommendation:local   — только Room-схема, никакого алгоритма.
- :feature:recommendation:impl    — алгоритм ранжирования и Hilt DI.
- :core:core-database             — зависит от :feature:recommendation:local,
                                    НИКОГДА от :api или :impl.
```

Инварианты проверяются автоматически через Konsist (`:architecture-tests`) на каждом CI-билде.

---

## 8. Best Practices

- **Separation of Concerns**: api / local / impl — каждый слой имеет одну ответственность.
- **Dependency Injection** через Hilt: все зависимости проброшены через DI, нет прямых instantiation.
- **WorkManager** для фоновых задач (`RecommendationWorker`).
- **Room + TypeConverters** для хранения `FloatArray`-эмбеддингов в бинарном формате.
- **Реактивность**: `Flow<Embeddings?>` из `UserProfileRepository` связывает
  обновление профиля с немедленной регенерацией рекомендаций.
- **Thread safety**: `Mutex` в `UserProfileRepositoryImpl` защищает от race conditions.
- **Unit tests**: `RecommenderImplTest` с мок-DAOs, тесты алгоритмов cosine и MMR.

---

## 9. Развёртывание и CI/CD

- **Gradle модули**: `:feature:recommendation:api`, `:feature:recommendation:local`,
  `:feature:recommendation:impl`, `:feature:userprofile:api`, `:feature:userprofile:impl`.
- **Detekt** (профиль `detekt-data.yml`) + **Spotless/Ktlint** для качества кода.
- **Unit tests** + **Android Instrumentation tests** (`RecommenderImplTest`).
- **Architecture tests** (Konsist) проверяют границы модулей.
- **Release Pipeline**: GitHub Actions → сборка, тесты → Play Store.
