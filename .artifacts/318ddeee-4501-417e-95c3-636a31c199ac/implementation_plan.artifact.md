# Implementation Plan - Fix Refresh Timeout Test Failure

The test `refresh timeout stops spinner and emits a stable error` in `FeedListComponentImplTest.kt` is failing because the `Refreshing` state does not transition to `Failed` when a timeout occurs.

## User Review Required

> [!IMPORTANT]
> The fix involves modifying the core utility `runSuspendCatching`. This change will cause `TimeoutCancellationException` (thrown by `withTimeout`) to be caught and returned as a `Result.failure`, while other `CancellationException`s (like job cancellation) will still be rethrown to preserve structured concurrency.

## Research Findings

1.  **Symptom**: The test fails with `expected: Failed(...) but was: Refreshing`.
2.  **Root Cause**: In `FeedStoreFactory.kt`, `startRefresh()` uses `withTimeout` inside `runSuspendCatching`.
3.  **Core Issue**: `runSuspendCatching` in `CoroutineExtensions.kt` explicitly rethrows all `CancellationException`s. Since `TimeoutCancellationException` inherits from `CancellationException`, it is rethrown and never reaches the `onFailure` block in `FeedStoreFactory.kt`, causing the coroutine to terminate without dispatching a failure message.
4.  **Regression Analysis**: `FeedStoreFactory.kt` contains explicit logic to handle `TimeoutCancellationException` inside the `Result` failure block, which strongly indicates that `runSuspendCatching` previously caught this exception.

## Proposed Changes

### Core Common Component

#### [MODIFY] [CoroutineExtensions.kt](file:///Users/dev/Developer/@PortfolioProjects/@android/smart-feed/core/common/src/main/kotlin/com/core/common/coroutines/CoroutineExtensions.kt)

- Update `runSuspendCatching` to catch `TimeoutCancellationException` specifically and return it as `Result.failure`.
- Keep rethrowing other `CancellationException`s to maintain correct coroutine behavior.
- Update KDoc to reflect this change.

## Verification Plan

### Automated Tests
- Run the specific failing test:
  ```bash
  ./gradlew :feature:feed:impl:test --tests "com.feature.feed.list.FeedListComponentImplTest.refresh timeout stops spinner and emits a stable error"
  ```
- Run all tests in `feature:feed:impl` to ensure no regressions:
  ```bash
  ./gradlew :feature:feed:impl:test
  ```

### Manual Verification
- None required beyond the automated tests as this is a logic fix for a background process.
