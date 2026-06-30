# ADR 0006: App Startup Lifecycle

## Status

Accepted

## Context

`MainActivity` is recreated during configuration changes, so any activity-scoped startup coordinator can also be recreated. Startup logic must therefore be safe to call more than once.

## Decision

Несмотря на то, что AppStartupCoordinator перезапускается при смене конфигурации (configuration changes), AppBootstrapper является Singleton-сервисом, а его операции (проверка пустоты БД, планирование WorkManager) спроектированы как строго идемпотентные, что исключает избыточные сетевые запросы.

We intentionally do not add a process-global "run once" boolean guard. Repeated calls are controlled by idempotent startup operations:

- duplicate initial network bootstrap is avoided by checking whether local content storage is empty;
- duplicate background scheduling is avoided by idempotent WorkManager scheduling in the scheduling use case;
- lifecycle subscription and coroutine lifetime are owned by `AppStartupCoordinator`;
- startup orchestration is owned by `AppBootstrapper`;
- child startup tasks are isolated with `supervisorScope`.

Startup failures are reported through `StartupErrorReporter`. A user-facing degraded startup UI can be added later when there is a product requirement for it.

## Consequences

- `MainActivity` remains a thin Android shell.
- Startup behavior is testable without launching an activity.
- Configuration changes do not require a global startup lock.
- Startup operations must remain idempotent when new work is added.
