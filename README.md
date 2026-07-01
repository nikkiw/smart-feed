# Smart Feed (Senior Android Architecture Showcase)

[![Android CI](https://github.com/nikkiw/smart-feed/actions/workflows/android_ci.yml/badge.svg)](https://github.com/nikkiw/smart-feed/actions)
[![License](https://img.shields.io/badge/License-Apache--2.0-blue.svg)](LICENSE)
[![Decompose](https://img.shields.io/badge/Navigation-Decompose%203.3.0-orange.svg)](https://github.com/arkivanov/Decompose)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.3.21-purple.svg)](https://kotlinlang.org)

**Smart Feed** is a showcase Android application demonstrating modern, production-grade architectural patterns. It features a modular, offline-first article feed, dynamic filtering/sorting, and an on-device recommendation engine powered by text embeddings.

This repository serves as a showcase of senior-level engineering skills, focusing on **Feature-Driven Vertical Slice Architecture**, **decoupled navigation (Decompose)**, **strict 3-module feature boundaries (API / Local / Impl)**, **executable architecture guards (Konsist)**, and **composable static quality gates (Detekt 2 / Spotless)**.

---

## 🎥 Demo

![Demo](https://github.com/user-attachments/assets/0d65b014-050b-4733-8d55-9ae5655efd8d)

---

## 🏗 Architecture & Design Highlights

The project is structured under **Clean Architecture** guidelines with **Feature-Driven Vertical Slice** decomposition and **Component-Driven UI** navigation:

1. **Vertical Feature Slices**: Each feature owns its full stack — domain contracts (`:api`), Room entities and DAOs (`:local`), and all UI and infrastructure implementations (`:impl`). This eliminates the "horizontal monolith" anti-pattern.
2. **3-Module Feature Structure**: The `:local` module is a deliberate architectural solution to prevent circular Gradle dependencies caused by Room's `@Database` entity registration requirement. See [Architecture Documentation](docs/architecture.md).
3. **Decompose Navigation**: Pure Kotlin component tree controlling lifecycle, state preservation, and back-stack handling — completely decoupled from the Android framework. See [ADR 0001](docs/adr/0001-why-decompose.md).
4. **Executable Architecture Guards (Konsist)**: A dedicated `:architecture-tests` JVM module enforces module boundary rules on every CI build — preventing domain leakage, platform imports in API modules, and naming violations.
5. **Consolidated Gradle Build-Logic**: Modern composite `build-logic` eliminating old `buildSrc`. Convention plugins handle per-module Detekt profiles, Spotless formatting, and toolchain configuration.
6. **Incremental Compose Migration**: Compose is introduced via "Compose Islands" inside existing XML RecyclerView ViewHolders — no big-bang rewrite. See [ADR 0002](docs/adr/0002-xml-to-compose-migration.md).

For a complete breakdown, see the [Architecture Documentation](docs/architecture.md).

---

## 🛠 Tech Stack

| Layer            | Technologies                                                                                      |
|------------------|---------------------------------------------------------------------------------------------------|
| **Language**     | Kotlin **2.3.21**, JVM 17 target                                                                  |
| **Build**        | Android Gradle Plugin **9.2.1**, Gradle **9.4.1**, KSP **2.3.9**, composite `build-logic`        |
| **Navigation**   | [Decompose](https://github.com/arkivanov/Decompose) **3.3.0** with Android ViewContext extensions |
| **State**        | MVIKotlin **4.2.0** (next milestone: MVI feed slice)                                              |
| **Database**     | Room **2.7.1** with float-array embedding converters, per-feature entity/DAO modules (`:local`), Paging 3 (`PagingData`, `GetPagedContentUseCase`) owned by `:feature:feed:impl` |
| **Background**   | WorkManager with Hilt worker scheduling                                                           |
| **DI**           | Dagger Hilt **2.60** (assisted factories, interface binds, per-feature Hilt modules)              |
| **Images**       | Glide behind a pure Kotlin `ImageLoader` contract (`:core:image:api`)                             |
| **Static Lint**  | Detekt **2.0.0-alpha.5** (layered profiles), Spotless **6.25.0** / Ktlint                        |
| **Arch Testing** | [Konsist](https://github.com/LemonAppDev/konsist) **0.17.3** — executable architecture guards    |
| **Networking**   | Retrofit + OkHttp, Ktor local mock server for dev flavour                                         |

---

## 📁 Project Structure

```plaintext
smart-feed/
├── app/                    # Composition root (Hilt, AppBootstrapper, MainActivity)
├── architecture-tests/     # Konsist architecture enforcement tests (pure JVM, no Android)
├── build-logic/            # Convention plugins: toolchain, Detekt, Spotless, feature config
├── config/detekt/          # Layered Detekt rule profiles (domain, data, ui, test, common)
├── core/
│   ├── common/             # Pure Kotlin shared utilities: coroutine helpers, embedding math,
│   │                       #   time converters (formerly :core:core → renamed)
│   ├── analytics/
│   │   ├── api/            #   AnalyticsService interface (pure Kotlin)
│   │   └── impl/           #   Analytics implementation
│   ├── connectivity/       # ConnectivityRepository (network state, modern observer-based)
│   ├── content/api/        # Shared content value objects used across features
│   ├── core-database/      # RoomDatabase orchestrator, cross-feature schema migrations
│   ├── core-networks/      # Retrofit/Ktor config, prod & dev network data sources
│   ├── coroutines/         # Coroutine Dispatchers DI module
│   ├── image/api/          # Pure Kotlin ImageLoader contract (KMP-portable)
│   ├── image-glide/        # Glide implementation of ImageLoader
│   └── lifecycle/          # AppLifecycleObserver
├── docs/
│   ├── adr/                # Architectural Decision Records
│   └── plans/              # Implementation and refactoring plans
└── feature/
    ├── feed/               # Article feed — full vertical slice
    │   ├── api/            #   Component contracts, ContentItem domain model, repository API
    │   ├── local/          #   ContentEntity, ContentDao (feed-owned Room storage)
    │   └── impl/           #   UI views, XML layouts, Hilt modules, repository impls,
    │                       #   Paging 3 (ContentPagingRepository, GetPagedContentUseCase)
    ├── recommendation/     # Recommendation engine — full vertical slice
    │   ├── api/            #   Recommendation model, RecommendationRepository contract
    │   ├── local/          #   Recommendation Room entities and DAOs
    │   └── impl/           #   Recommender, EmbeddingIndex (cosine similarity), Hilt modules
    └── userprofile/        # User identity — vertical slice
        ├── api/            #   UserProfile model, UserProfileRepository contract
        ├── impl/           #   Repository impl, Hilt module
        └── local/          #   User profile Room storage
```

---

## 📅 Modernization Roadmap

| Phase | Description | Status |
|-------|-------------|--------|
| **0** | Stash WIP (`create-post-wip`), isolate polish steps | ✅ Done |
| **1** | Architecture docs, ADRs, diagrams | ✅ Done |
| **2** | Build logic consolidation (`buildSrc` → `build-logic`), AGP 9.2.1, Kotlin 2.3.21, KSP 2.3.9, Detekt 2 | ✅ Done |
| **3** | Konsist architecture enforcement module (`:architecture-tests`) | ✅ Done |
| **4** | `MainActivity` decoupling — `AppStartupCoordinator`, `SystemBarsController`, `FeedRootViewHost` | ✅ Done |
| **5** | AndroidX Paging dependency inversion — extracted to `:core:core-paging`, then **co-located into `:feature:feed:impl`** (sole consumer) | ✅ Done |
| **6** | Feature API/Impl split — `:feature:feed:api` and `:feature:feed:impl` | ✅ Done |
| **7** | **Core Layer Modularization** — 3-module feature slices (`api/local/impl`), `:core:core` → `:core:common`, eliminated `core-domain` / `core-data` / `core-paging` monoliths, build noise cleanup | ✅ **Done** |
| **8** | MVI slice — `FeedState`, `FeedIntent`, `FeedEffect`, pure JVM Reducer tests | 🔜 Next |
| **9** | Jetpack Compose Card Island — `ArticleCard` in XML RecyclerView ViewHolder | 🔜 Planned |

---

## 📦 Local Development

1. Clone the repository:
   ```bash
   git clone https://github.com/nikkiw/smart-feed.git
   cd smart-feed
   ```
2. Run the full quality gate locally:
   ```bash
   # Auto-format code
   ./gradlew spotlessApply
   # Static analysis (layered Detekt profiles)
   ./gradlew detekt
   # Architecture consistency tests (Konsist)
   ./gradlew :architecture-tests:test
   # All unit tests
   ./gradlew test
   # Assemble developer debug build
   ./gradlew assembleDevDebug
   ```
3. Full CI verification matrix in one command:
   ```bash
   ./gradlew spotlessCheck detekt :architecture-tests:test test
   ```

---

## 📄 License

Licensed under the Apache License 2.0. See [LICENSE](LICENSE) for details.

---

## 🙋‍♂️ Author

Made by [Nikolay Vlasov](https://www.linkedin.com/in/nikolay-vlasov-dev) – Android Architect.
