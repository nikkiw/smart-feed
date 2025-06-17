# Technical Specification for MVP `smart-feed`

## 1. General Overview

**Project Name:** smart-feed
**Description:** A demo Android application featuring an article feed with an on-device recommendation system.
**Technology Stack:** Kotlin, Android MVVM, Coroutines, Room, WorkManager, Hilt, Retrofit, Decompose (for BLoC-style screen composition and navigation), GitHub Actions CI.

## 2. Objectives

* Implement a dynamic feed with personalized recommendations computed locally.
* Provide a modular architecture with support for dynamic content modules.
* Maintain high code quality, test coverage, and CI/CD integration.
* Deliver clear documentation and licensing suitable for portfolio inclusion.

## 3. Functional Requirements

1. **Dynamic Feed from Server**

   * Fetch articles via REST API.
   * Store articles in Room with synchronization timestamp.
   * Support pull-to-refresh and periodic sync using WorkManager.

2. **Recommendation System (On-Device)**

   * Build a user profile by weighting embeddings of read articles.
   * Generate “Recommended Reading” suggestions for each article.

3. **User Interaction Events**

   * Log events: `read` (content ID, read count, read percentage, and read time).
   * Save events in the `event_logs` table for analytics and aggregate into `content_interaction_stats`.

4. **UI/UX**

   * Bottom navigation: Feed | Recommendations.
   * Pull-to-refresh and infinite scroll.
   * Periodic content updates.

5. **Testing and CI**

   * Android instrumented tests for SQLite and other platform-specific logic.
   * Unit tests for ViewModels, UseCases, and DAOs.
   * GitHub Actions for linting, building, and test execution.

## 4. Non-Functional Requirements

* **Scalability:** Modular structure allowing future support for video, podcasts, and server-side collaborative filtering.
* **Maintainability:** Kotlin style guide compliance, code comments, and documentation in the `docs/` directory.
* **Compatibility:** Android SDK 23+, minimum JDK 17.

## 5. Architecture and Modules

```plaintext
smart-feed/
├── app/             # App module: UI, navigation, ViewModels, DI (via Decompose)
├── build-logic/     # Custom Gradle conventions and plugins
├── buildSrc/        # App configuration and global constants
├── core/            # Shared models and utilities used across the project
├── docs/            # Documentation in Markdown
├── feature/         # Decompose components and UI features
├── mock-server/     # Mock server for local development and testing
└── scripts/         # Python scripts for test data generation
```

* **Navigation & State Management:** Decompose replaces Android Navigation Component for lifecycle management, state handling, and multiplatform support.
* **Modularity:** Each module exposes its components via Decompose.
* **Dependency Injection:** Hilt provides components to ViewModels and UseCases.

## 6. API Specification

The full API description is available in the file [content\_delta\_sync\_spec\_rus.md](/docs/content_delta_sync_spec_rus.md).

## 7. Screens & User Flow

1. **FeedRoot**

   * Displays either FeedScreen or RecommendationScreen based on app state.
   * BottomBar enables switching between FeedScreen and RecommendationScreen.

2. **FeedScreen**

   * FilterSortScreen allows sorting and filtering by tags.
   * Vertical RecyclerView with multiple card types.

3. **ArticleDetailScreen**

   * Header, banner, full article text.
   * Recommended Reading list.

4. **RecommendationScreen**

   * Displays a list of recommended content (Recommended Reading).

## 9. WorkManager Configuration

* **ContentFetchScheduler:** Configures schedule and execution constraints for `ContentFetchWorker`.
* **ContentFetchWorker:** Periodic task that fetches new articles and updates recommendations.
* **Constraints:** Requires `NetworkType.CONNECTED`; `deviceIdle` is optional.

## 10. Dependency Injection

* Implemented via Hilt.

## 11. Testing Strategy

* **Unit Tests:** JUnit + MockK for ViewModels and UseCases.
* **Instrumented Tests:** Room, SQLite behavior, and Worker testing.
* **CI:** GitHub Actions (`.github/workflows/android-ci.yml`) triggered on push and pull requests.

## 12. Roadmap & Future Enhancements

* Build and publish project documentation.
* Aggregate and report test coverage.
* Support for various screen sizes.
* New content types: video, audio, polls.
* Analytics: engagement metrics dashboard.
