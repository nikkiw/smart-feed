# ADR 0001: Adopting Decompose for Navigation and Architecture

## Context

Traditionally, Android applications rely on Jetpack Navigation (Fragments, Activities, or Compose Navigation) to handle user flow and back stack management. However, this introduces several architectural challenges:
1. **Coupling to the Android OS**: Traditional navigation is tightly bound to platform classes (FragmentManager, NavController), making business logic harder to unit test without instrumentation.
2. **Lifecycle Synchronization**: Ensuring that business logic/viewmodel states are scoped correctly and garbage collected when screens are popped requires fragile lifecycle boilerplate.
3. **Multiplatform Readiness**: To prepare the architecture for Kotlin Multiplatform (KMP), navigation logic must reside in a pure Kotlin layer, completely decoupled from Android UI structures.

## Decision

We have decided to adopt **Decompose** by Arkadii Ivanov as the primary navigation and state management framework. 

Decompose breaks the application into a tree of component nodes. Each component is a pure Kotlin class that:
- Inherits `ComponentContext` which provides an independent, platform-agnostic `Lifecycle`.
- Manages its own child components using `childStack` or `childSlot`.
- Preserves state across configuration changes automatically (via `StateKeeper`).

## Consequences

### Positive
- **100% Platform Independent**: Navigation and component state can be unit-tested using JVM tests without robolectric or Android emulator.
- **Granular Scoping**: Instead of sharing singletons or large-scoped ViewModels, dependencies and parameters can be passed directly to sub-components via constructors, ensuring clean memory management.
- **Modular Compile Times**: Because components are declared in lightweight API modules, compilation of presentation contracts is extremely fast.
- **UI Framework Agnostic**: The same Decompose component tree can be bound to XML/ViewBinding layouts (using extensions-android) or Jetpack Compose UI (using standard Compose state observe bindings).

### Negative / Trade-offs
- **Learning Curve**: Team members must get accustomed to thinking in component trees, constructor injection, and assisted factories instead of standard ViewModels and Fragment transactions.
- **Serialization Overhead**: Component route configurations must be `@Serializable` so Decompose can restore the navigation stack across process death.
