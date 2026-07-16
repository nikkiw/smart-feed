# Technical Specification for MVP `smart-feed`

## 1. General Overview

**Project Name:** smart-feed
**Description:** A demo Android application featuring an article feed with an on-device recommendation system.
**Technology Stack:** Kotlin, Coroutines, Room, WorkManager, Hilt, Retrofit, Decompose for lifecycle-aware component navigation, and MVIKotlin 4.2.0 for stateful Feed components. The current UI uses XML/ViewBinding and RecyclerView; Compose is planned as a parallel rendering path for measured comparison.

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
   * Current rendering is XML/ViewBinding with RecyclerView. A Compose `ArticleCard` is planned
     as a parallel implementation for performance comparison before broader migration.

5. **Testing and CI**

   * Android instrumented tests for SQLite and other platform-specific logic.
   * Unit tests for reducers, Decompose components, use cases, and DAOs.
   * GitHub Actions for linting, building, and test execution.

## 4. Non-Functional Requirements

* **Scalability:** Modular structure allowing future support for video, podcasts, and server-side collaborative filtering.
* **Maintainability:** Kotlin style guide compliance, code comments, and documentation in the `docs/` directory.
* **Compatibility:** Android SDK 23+, minimum JDK 17.

## 5. Architecture and Modules

```plaintext
smart-feed/
├── app/             # Composition root, startup, navigation host, DI
├── architecture-tests/ # Konsist architecture tests
├── build-logic/     # Convention plugins, Detekt, Spotless, toolchain
├── core/            # Cross-cutting infrastructure and pure Kotlin contracts
├── docs/            # Documentation and ADRs
├── feature/         # api/local/impl vertical feature slices
├── mock-server/     # Mock server for local development and testing
└── scripts/         # Scripts for test data generation
```

* **Navigation & State Management:** Decompose owns the component tree and lifecycle. MVIKotlin stores are used only where Feed state and side effects are complex; simple coordinators stay plain Decompose components.
* **Modularity:** Each module exposes its components via Decompose.
* **Dependency Injection:** Hilt provides component factories, stores, repositories, and use cases.

## 6. API Specification

The full API description is available in [content_delta_sync_spec.md](content_delta_sync_spec.md).

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

* **Unit Tests:** JUnit + MockK for reducers, components, and UseCases.
* **Instrumented Tests:** Room, SQLite behavior, and Worker testing.
* **CI:** GitHub Actions (`.github/workflows/android_ci.yml`) triggered on push and pull requests.

## 12. Roadmap & Future Enhancements

* Build and publish project documentation.
* Aggregate and report test coverage.
* Support for various screen sizes.
* New content types: video, audio, polls.
* Analytics: engagement metrics dashboard.
