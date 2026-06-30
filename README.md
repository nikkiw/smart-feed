# Smart Feed (Senior Android Architecture Showcase)

[![Android CI](https://github.com/nikkiw/smart-feed/actions/workflows/android_ci.yml/badge.svg)](https://github.com/nikkiw/smart-feed/actions)
[![License](https://img.shields.io/badge/License-Apache--2.0-blue.svg)](LICENSE)
[![Decompose](https://img.shields.io/badge/Navigation-Decompose%203.3.0-orange.svg)](https://github.com/arkivanov/Decompose)

**Smart Feed** is a showcase Android application demonstrating modern, production-grade architectural patterns. It features a modular, offline-first article feed, dynamic filtering/sorting, and an on-device recommendation engine powered by text embeddings. 

This repository serves as a showcase of senior-level engineering skills, focusing on **decoupled navigation (Decompose)**, **strict feature boundary separation (API vs. Impl)**, **composable linter rules (Detekt/Spotless)**, and **incremental migration patterns (XML to Jetpack Compose)**.

---

## 🎥 Demo

![Demo](https://github.com/user-attachments/assets/0d65b014-050b-4733-8d55-9ae5655efd8d)

---

## 🏗 Architecture & Design Highlights

The project is structured under **Clean Architecture** guidelines with **Component-Driven UI** navigation, moving away from traditional Fragment-based or NavHost architectures:

1. **Decompose Navigation**: Pure Kotlin component tree controlling lifecycle, state preservation, and back-stack handling, completely decoupled from the Android framework. Read more in [ADR 0001: Adopting Decompose](docs/adr/0001-why-decompose.md).
2. **Feature API/Impl Separation**: Compiles feature modules into separate lightweight `:api` contracts and heavy `:impl` assets. This decouples features and speeds up build compilation.
3. **Consolidated Gradle Build-Logic**: Eliminates old `buildSrc` duplication. Uses a composite build logic (`build-logic`) running Android Gradle Plugin **`9.2.1`**, Java **`17`**, **Detekt** (`1.23.8`), and **Spotless/Ktlint** (`6.25.0`).
4. **Incremental Compose Migration**: Rather than a full rewrite, Compose is introduced using "Compose Islands" inside existing layout viewholders. Read more in [ADR 0002: XML to Jetpack Compose Migration](docs/adr/0002-xml-to-compose-migration.md).

For a complete breakdown, see the [Architecture Documentation](docs/architecture.md).

---

## 🛠 Tech Stack

| Layer        | Technologies                                                                                |
|--------------|---------------------------------------------------------------------------------------------|
| **Core**     | Kotlin Coroutines & Flows, Serialization                                                    |
| **Navigation**| [Decompose](https://github.com/arkivanov/Decompose) with Android ViewContext extensions    |
| **State**    | MVI Kotlin Store architecture (under migration)                                             |
| **Database** | Room with custom SQLite bundled converters for float embeddings vectors                     |
| **Background**| WorkManager with Hilt worker scheduling                                                     |
| **DI**       | Dagger Hilt (assisted factories, interface binds)                                            |
| **Linter**   | Spotless, Ktlint, Detekt static analysis                                                    |

---

## 📁 Project Structure

```plaintext
smart-feed/
├── app/             # Application entry point (Hilt components, AppInitializers, MainActivity)
├── build-logic/     # Composite build logic (Convention plugins for API, Impl, Detekt, Spotless)
├── core/            # Core horizontal layers
│   ├── core/        # Common utilities, base behaviors
│   ├── core-domain/ # Pure Kotlin domain models, interfaces, use cases
│   ├── core-data/   # Repository implementations and sync orchestrators
│   ├── core-database/# Room Database configuration and entities
│   ├── core-networks/# Retrofit clients and Ktor local mock server
│   └── image-glide/ # Glide configuration module
├── docs/            # ADRs and spec documentation
└── feature/         # Vertical features (e.g. split :feed:api and :feed:impl)
```

---

## 📅 Modernization Roadmap

The project is undergoing a staged modernization to demonstrate migration of legacy codebases:

* **Phase 0: Project Cleanup**
  * [x] Stash experimental changes (`create-post-wip`) to isolate polish steps.
* **Phase 1: Showcase Polish & Docs**
  * [x] Add system architecture diagram and module layouts.
  * [x] Write ADRs detailing Decompose choice and incremental Compose adoption.
* **Phase 2: Build Logic & Static Analysis**
  * [x] Consolidate `buildSrc` into `build-logic`.
  * [x] Upgrade to Android Gradle Plugin (AGP) **9.2.1**, Kotlin **2.1.10**, Java Toolchain **17**.
  * [x] Integrate Spotless, Ktlint formatting, and Detekt config rule sets.
* **Phase 3: MainActivity Refactoring**
  * [ ] Decouple app startup bootstrap from `MainActivity` lifecycle.
  * [ ] Clean up Hilt dependency injection to target interface `FeedRootComponent.Factory`.
* **Phase 4: Feature Module Separation**
  * [ ] Extract component interfaces from `:feature:feed` into `:feature:feed:api`.
  * [ ] Place UI Views and implementation classes in `:feature:feed:impl`.
* **Phase 5: Incremental MVI Slice Migration**
  * [ ] Migrate Feed list to MVI contract (`FeedState`, `FeedIntent`, `FeedEffect`).
  * [ ] Cover MVI Reducer transitions with pure JVM Unit Tests.
* **Phase 6: Jetpack Compose Card Island**
  * [ ] Build Compose-based `ArticleCard` and bind it inside XML RecyclerView list.

---

## 📦 Local Development

1. Clone the repository:
   ```bash
   git clone https://github.com/nikkiw/smart-feed.git
   cd smart-feed
   ```
2. Build the app and run checks:
   ```bash
   # Run detekt checks
   ./gradlew detekt
   # Run spotless checks and auto-format
   ./gradlew spotlessApply
   # Assemble developer debug build
   ./gradlew assembleDevDebug
   ```

---

## 📄 License

Licensed under the Apache License 2.0. See [LICENSE](LICENSE) for details.

---

## 🙋‍♂️ Author

Made by [Nikolay Vlasov](https://www.linkedin.com/in/nikolay-vlasov-dev) – Android Architect.
