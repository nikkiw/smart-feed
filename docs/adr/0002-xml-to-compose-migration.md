# ADR 0002: Incremental XML to Jetpack Compose Migration Strategy

## Context

The current user interface of the application is built using Android XML layout resource files, ViewBinding, and standard RecyclerView adapters. While stable and performant, migrating completely to Jetpack Compose is desirable for faster UI development, declarative layouts, and modernized design aesthetics.

However, a full rewrite ("Big Bang") introduces significant risks:
1. **Regression Risks**: Layout behaviors, image-loading caches, and scrolling interactions might break.
2. **Feature Freeze**: Migrating all layouts at once halts product feature delivery.
3. **Architecture Instability**: Navigation and side-effects handling must be fully refactored simultaneously.

**Current status (2026-07-25):** The "Compose Island" approach has been implemented (`ComposeArticleCard` inside a RecyclerView). Performance parity has been achieved and verified via Android Macrobenchmark using **Baseline Profiles**. The XML and Compose paths coexist and can be swapped dynamically.

## Decision

We have decided to adopt an **incremental migration strategy** using a parallel **"Compose Island"**
inside the existing XML and RecyclerView structures. The first implementation must keep the XML
card available as a control path instead of replacing it immediately.

Instead of replacing entire screens, we will migrate individual UI components step-by-step:
1. **RecyclerView ViewHolder Compose Wrapper**: Integrate a `ComposeView` inside the current
   `ArticleViewHolder` to render a Compose `ArticleCard`, while RecyclerView scrolling and caching
   remain unchanged. Keep the XML card selectable for A/B comparison during the migration.
2. **State Sharing**: Decompose components will expose state using Decompose `Value<T>`, which can be easily adapted to Compose `State<T>` via the `subscribeAsState()` extension, or consumed as standard Kotlin `StateFlow`.
3. **Shared Element Transitions**: Retain XML shared element transitions for screen navigation until all target screens are fully migrated to Compose.
4. **Performance comparison**: compare the XML and Compose variants on the same device, dataset,
   and interaction script. Record cold/warm startup, frame timing/jank while scrolling, memory,
   and (for Compose) recomposition counts before deciding whether to continue the migration.

## Consequences

### Positive
- **Low Risk**: Legacy XML code remains stable, and migrations can be tested component-by-component.
- **Immediate Value**: High-frequency components (like article cards) can be modernized with Compose UI gradients and animations immediately.
- **Coexistence**: XML Views and Jetpack Compose Composable layouts will coexist gracefully under the same Decompose lifecycle management.
- **Evidence-based migration**: the XML path provides a baseline, so a broader migration is based
  on measured behavior rather than assumptions about performance.

### Negative / Trade-offs
- **Bridge Overhead**: Creating `ComposeView` inside RecyclerView lists introduces small memory and layout overhead, which must be mitigated by properly disposing of Composition lifecycles on ViewHolder recycle.
- **Tooling complexity**: Gradle modules must enable Compose compiler features and compile options, which adds configuration logic to build scripts.
