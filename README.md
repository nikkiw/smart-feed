# Smart Feed (Senior Android Architecture Showcase)

[![Android CI](https://github.com/nikkiw/smart-feed/actions/workflows/android_ci.yml/badge.svg)](https://github.com/nikkiw/smart-feed/actions)
[![License](https://img.shields.io/badge/License-Apache--2.0-blue.svg)](LICENSE)
[![Decompose](https://img.shields.io/badge/Navigation-Decompose%203.3.0-orange.svg)](https://github.com/arkivanov/Decompose)

**Smart Feed** is a showcase Android application demonstrating modern, production-grade architectural patterns. It features a modular, offline-first article feed, dynamic filtering/sorting, and an on-device recommendation engine powered by text embeddings.

This repository serves as a showcase of senior-level engineering skills, focusing on **Feature-Driven Vertical Slice Architecture**, **decoupled navigation (Decompose)**, **strict 3-module feature boundaries (API / Local / Impl)**, **executable architecture guards (Konsist)**, and **composable static quality gates (Detekt/Spotless)**.

---

## 🎥 Demo

![Demo](https://github.com/user-attachments/assets/0d65b014-050b-4733-8d55-9ae5655efd8d)

---

## 🏗 Architecture & Design Highlights

The project is structured under **Clean Architecture** guidelines with **Feature-Driven Vertical Slice** decomposition and **Component-Driven UI** navigation:

1. **Vertical Feature Slices**: Each feature owns its full stack — domain models, repository contracts (`:api`), Room entities and DAOs (`:local`), and all UI and infrastructure implementations (`:impl`). This eliminates the classic "horizontal monolith" anti-pattern where all domain code lives in a shared global `core-domain` module.
2. **3-Module Feature Structure**: Features are split into `api` (pure Kotlin contracts), `local` (Room entities/DAOs — a deliberate Room constraint workaround to prevent circular Gradle dependencies), and `impl` (UI, repositories, Hilt modules). See [Architecture Documentation](docs/architecture.md).
3. **Decompose Navigation**: Pure Kotlin component tree controlling lifecycle, state preservation, and back-stack handling, completely decoupled from the Android framework. Read more in [ADR 0001: Adopting Decompose](docs/adr/0001-why-decompose.md).
4. **Executable Architecture Guards (Konsist)**: A dedicated `:architecture-tests` JVM module runs on every CI build to enforce module boundary rules — preventing domain models from importing Android platform APIs, and features from leaking implementation details across boundaries.
5. **Consolidated Gradle Build-Logic**: Eliminates old `buildSrc` duplication. Uses a composite build logic (`build-logic`) running Android Gradle Plugin **`9.2.1`**, Kotlin **`2.2.10`**, Java **`17`**, **Detekt** (`1.23.8`), and **Spotless/Ktlint** (`6.25.0`).
6. **Incremental Compose Migration**: Rather than a full rewrite, Compose is introduced using "Compose Islands" inside existing layout ViewHolders. Read more in [ADR 0002: XML to Jetpack Compose Migration](docs/adr/0002-xml-to-compose-migration.md).

For a complete breakdown, see the [Architecture Documentation](docs/architecture.md).

---

## 🛠 Tech Stack

| Layer           | Technologies                                                                                    |
|-----------------|-------------------------------------------------------------------------------------------------|
| **Core**        | Kotlin Coroutines & Flows, Serialization, shared pure Kotlin utilities in `:core:common`       |
| **Navigation**  | [Decompose](https://github.com/arkivanov/Decompose) with Android ViewContext extensions         |
| **State**       | MVI Kotlin Store architecture (next milestone)                                                  |
| **Database**    | Room with custom SQLite converters for float embedding vectors, per-feature entity/DAO modules   |
| **Background**  | WorkManager with Hilt worker scheduling                                                         |
| **DI**          | Dagger Hilt (assisted factories, interface binds, per-feature Hilt modules)                     |
| **Linter**      | Spotless, Ktlint, Detekt with layered rule profiles                                             |
| **Arch Testing**| [Konsist](https://github.com/LemonAppDev/konsist) — executable architectural consistency checks |
| **Images**      | Glide (behind `ImageLoader` contract in `:core:image:api`)                                      |

---

## 📁 Project Structure

```plaintext
smart-feed/
├── app/                    # Composition root (Hilt, AppBootstrapper, MainActivity)
├── architecture-tests/     # Konsist architecture enforcement tests (pure JVM module)
├── build-logic/            # Composite build logic (Convention plugins, Config.kt)
├── core/
│   ├── analytics/          # Analytics service API + implementation
│   │   ├── api/            #   AnalyticsService interface
│   │   └── impl/           #   Firebase / logging implementation
│   ├── connectivity/       # ConnectivityRepository (network status monitoring)
│   ├── content/            # Shared content value objects (cross-feature data types)
│   │   └── api/
    ├── common/             # Shared pure Kotlin utilities (`:core:common`)
│   ├── core-database/      # RoomDatabase orchestrator, schema migrations
│   ├── core-networks/      # Retrofit / Ktor config, network data sources
│   ├── core-paging/        # PagingData infrastructure abstractions
│   ├── coroutines/         # Coroutine Dispatchers DI module, Flow extensions
│   ├── image/
│   │   ├── api/            # ImageLoader contract (KMP-ready pure Kotlin)
│   │   └── (glide/)        # Glide implementation (moved to :core:image-glide)
│   ├── image-glide/        # Glide ImageLoader implementation
│   └── lifecycle/          # AppLifecycleObserver
├── docs/                   # ADRs, architecture docs, tech specs, plans
│   ├── adr/                # Architectural Decision Records
│   └── plans/              # Refactoring and implementation plans
└── feature/
    ├── feed/               # Article feed feature (3-module slice)
    │   ├── api/            #   Component contracts, domain models, repository interfaces
    │   ├── local/          #   Room Entities, DAOs (feed-owned storage)
    │   └── impl/           #   UI views, XML layouts, Hilt modules, repository impls
    ├── recommendation/     # Recommendation engine feature (3-module slice)
    │   ├── api/            #   Recommendation domain models, RecommendationRepository
    │   ├── local/          #   Room Entities, DAOs (recommendation-owned storage)
    │   └── impl/           #   Recommender, EmbeddingIndex, Hilt modules
    └── userprofile/        # User identity feature
        ├── api/            #   UserProfile model, UserProfileRepository
        ├── impl/           #   Repository impl, Hilt module
        └── local/          #   User profile storage
```

---

## 📅 Modernization Roadmap

| Phase | Description | Status |
|-------|-------------|--------|
| **0** | Stash experimental WIP changes (`create-post-wip`) | ✅ Done |
| **1** | Architecture docs, ADRs, diagrams | ✅ Done |
| **2** | Build logic consolidation (`buildSrc` → `build-logic`), AGP 9.2.1, Kotlin 2.2.10, Detekt/Spotless | ✅ Done |
| **3** | Konsist architecture enforcement module (`:architecture-tests`) | ✅ Done |
| **4** | `MainActivity` decoupling — `AppStartupCoordinator`, `SystemBarsController`, `FeedRootViewHost` | ✅ Done |
| **5** | AndroidX Paging dependency inversion — extract `:core:core-paging` | ✅ Done |
| **6** | Feature API/Impl split — `:feature:feed:api` and `:feature:feed:impl` | ✅ Done |
| **7** | **Core Layer Modularization** — 3-module feature slices (`api/local/impl`), vertical feature ownership of domain and data, `core` reduced to pure infrastructure | ✅ **Done** |
| **8** | MVI slice — `FeedState`, `FeedIntent`, `FeedEffect`, pure JVM Reducer tests | 🔜 Next |
| **9** | Jetpack Compose Card Island — `ArticleCard` in XML RecyclerView ViewHolder | 🔜 Planned |

---

## 📦 Local Development

1. Clone the repository:
   ```bash
   git clone https://github.com/nikkiw/smart-feed.git
   cd smart-feed
   ```
2. Build and run full quality gate:
   ```bash
   # Auto-format code
   ./gradlew spotlessApply
   # Run static analysis
   ./gradlew detekt
   # Run architecture consistency tests
   ./gradlew :architecture-tests:test
   # Run all unit tests
   ./gradlew test
   # Assemble developer debug build
   ./gradlew assembleDevDebug
   ```
3. Run the complete CI verification matrix locally:
   ```bash
   ./gradlew spotlessCheck detekt :architecture-tests:test test
   ```

---

## 📄 License

Licensed under the Apache License 2.0. See [LICENSE](LICENSE) for details.

---

## 🙋‍♂️ Author

Made by [Nikolay Vlasov](https://www.linkedin.com/in/nikolay-vlasov-dev) – Android Architect.
