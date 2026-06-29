# ADR 0002: Incremental XML to Jetpack Compose Migration Strategy

## Context

The current user interface of the application is built using Android XML layout resource files, ViewBinding, and standard RecyclerView adapters. While stable and performant, migrating completely to Jetpack Compose is desirable for faster UI development, declarative layouts, and modernized design aesthetics.

However, a full rewrite ("Big Bang") introduces significant risks:
1. **Regression Risks**: Layout behaviors, image-loading caches, and scrolling interactions might break.
2. **Feature Freeze**: Migrating all layouts at once halts product feature delivery.
3. **Architecture Instability**: Navigation and side-effects handling must be fully refactored simultaneously.

## Decision

We have decided to adopt an **incremental migration strategy** using **"Compose Islands"** inside existing XML and RecyclerView structures.

Instead of replacing entire screens, we will migrate individual UI components step-by-step:
1. **RecyclerView ViewHolder Compose Wrapper**: Integrate a `ComposeView` inside standard XML-based ViewHolders (e.g. `ArticleViewHolder`) to render lists where individual cards are written in Compose, but the list scrolling and caching remains managed by RecyclerView.
2. **State Sharing**: Decompose components will expose state using Decompose `Value<T>`, which can be easily adapted to Compose `State<T>` via the `subscribeAsState()` extension, or consumed as standard Kotlin `StateFlow`.
3. **Shared Element Transitions**: Retain XML shared element transitions for screen navigation until all target screens are fully migrated to Compose.

## Consequences

### Positive
- **Low Risk**: Legacy XML code remains stable, and migrations can be tested component-by-component.
- **Immediate Value**: High-frequency components (like article cards) can be modernized with Compose UI gradients and animations immediately.
- **Coexistence**: XML Views and Jetpack Compose Composable layouts will coexist gracefully under the same Decompose lifecycle management.

### Negative / Trade-offs
- **Bridge Overhead**: Creating `ComposeView` inside RecyclerView lists introduces small memory and layout overhead, which must be mitigated by properly disposing of Composition lifecycles on ViewHolder recycle.
- **Tooling complexity**: Gradle modules must enable Compose compiler features and compile options, which adds configuration logic to build scripts.
