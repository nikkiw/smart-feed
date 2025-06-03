# Technical Specification for MVP `smart-feed`

## 1. General Description

**Project Name:** smart-feed
**Description:** MVP Android application with an article feed and a built-in on-device recommendation system.
**Tech Stack:** Kotlin, Android MVVM, Coroutines, Room, WorkManager, Hilt, Retrofit, Decompose (for navigation and component lifecycle on multiplatform), TensorFlow Lite (optional for embeddings), GitHub Actions CI.

## 2. Goals

* Implement a Minimum Viable Product (MVP) featuring a dynamic feed and personalized recommendations computed locally.
* Provide a modular architecture supporting both static and dynamic content modules.
* Maintain high code quality, test coverage, and CI/CD integration.
* Prepare clear documentation and a license for portfolio inclusion.

## 3. Functional Requirements

1. **Static Article Feed**

   * Display preloaded articles by category.
   * View article details, add to bookmarks, and share.

2. **Dynamic Server-Fetched Feed**

   * Retrieve articles via REST API.
   * Store articles in Room with a synchronization timestamp.
   * Support manual pull-to-refresh and periodic synchronization via WorkManager.

3. **Recommendation System (On-Device)**

   * Calculate embeddings for all articles.
   * Build a user profile by averaging embeddings of read articles.
   * Compute cosine similarity and select the top-N unread articles.
   * Display a horizontal “Recommended For You” list in the feed.

4. **User Interaction Events**

   * Log events: `view_article`, `bookmark`, `share`, `complete_test`, `read_time`.
   * Store events in the `event_logs` table for analytics.

5. **UI/UX**

   * Segmented control: All | Articles | Tests | Recommended.
   * “New” or “For You” badges on cards.
   * Pull-to-refresh and infinite scrolling.

6. **Testing and CI**

   * Unit tests for ViewModel, UseCase, and DAO.
   * GitHub Actions to run lint, build, and tests.

## 4. Non-Functional Requirements

* **Performance:** Feed rendering—≤ 300 ms; recommendation computation—≤ 200 ms on a midrange device.
* **Scalability:** Modular structure allowing addition of video, podcasts, and server-side CF.
* **Security:** Input sanitization, HTTPS.
* **Maintainability:** Follow Kotlin style guide, comment code, documentation in `docs/`.
* **Compatibility:** Android SDK 23+, minimum JDK 17.

## 5. Architecture and Modules

```plaintext
smart-feed/
├── app/             # Application module: UI, navigation, ViewModel, DI (via Decompose)
├── build-logic/     # Custom Gradle conventions and plugins
├── buildSrc/        # App configuration and global constants
├── core/            # Shared models and utilities used throughout the project
├── docs/            # Documentation in Markdown
└── mock-server/     # Mock server for local development and testing
```

* **Navigation and State Management:** Uses Decompose instead of Android Navigation Component—handles lifecycle, state, and multiplatform support.
* **Modularity:** Each module provides its components through Decompose.
* **Dependency Injection:** Pass `ComponentContext` from Hilt into ViewModel and UseCase.

## 6. Data Models

* **Article:** `id`, `title`, `summary`, `content`, `imageUrl`, `category`, `createdAt`
* **RemoteArticle:** inherits from `Article`, adds `fetchedAt` and `isRead`
* **Recommendation:** `articleId`, `score`, `generatedAt`
* **EventLog:** `eventId`, `userId`, `type`, `payload` (JSON), `timestamp`

## 7. API Specification

Full API description is provided in the file [content_delta_sync_spec.md](/docs/content_delta_sync_spec.md).

## 8. Screens and User Flow

1. **FeedFragment**

   * Title bar and search icon.
   * Segmented control.
   * Vertical RecyclerView with cards of various types.

2. **ArticleDetailFragment**

   * Title, banner, full article text.
   * “Bookmark” and “Share” buttons.

3. **TestFragment**

   * List of mini-tests.
   * Progress and results display.

## 9. WorkManager Configuration

* **SyncWorker:** periodic task (e.g., every 6 hours)—fetch new articles and update recommendations.
* **Constraints:** `NetworkType.CONNECTED`, device idle—optional.

## 10. Dependency Injection

* Provided via Hilt:

   * Singleton `RoomDatabase`
   * Retrofit instance `FeedApiService`
   * DAOs: `RemoteArticleDao`, `RecommendationDao`, `EventLogDao`
   * UseCase and Repository bindings

## 11. Testing Strategy

* **Unit Tests:** JUnit + Mockito (or MockK) for ViewModel and UseCase
* **Instrumented Tests:** Room behavior
* **CI:** GitHub Actions `.github/workflows/android-ci.yml`, triggered on push and PR

## 12. Roadmap and Improvements

* **Server-Side Collaborative Filtering**
* **Hybrid Recommendations:** CF + content-based
* **New Content Types:** video, audio, polls
* **Analytics:** engagement metrics dashboard
