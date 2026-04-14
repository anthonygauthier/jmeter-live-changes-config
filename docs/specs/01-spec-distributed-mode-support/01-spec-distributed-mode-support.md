# 01-spec-distributed-mode-support.md

## Introduction/Overview

This feature adds JMeter distributed-mode support to the Live Changes plugin. Today, the plugin works in single-node execution because it relies on local JMeter lifecycle behavior, but issue `#36` documents that this breaks in distributed runs. The goal of this spec is to support the full existing API surface during distributed execution while presenting one controller-facing API that fans out commands and aggregates worker-visible state.

## Goals

- Enable the Live Changes plugin to function during JMeter distributed test runs instead of only single-node runs.
- Preserve the existing API capabilities for thread changes, variables, properties, test status, summary, errors, and graceful test stop.
- Expose one controller-facing API entry point for distributed runs so users do not need to call each worker manually.
- Provide observable proof that distributed-mode behavior works in a real JMeter remote test, not only in local automated tests.
- Keep single-node behavior working as it does today unless a distributed-mode-specific change is required.

## User Stories

- **As a performance engineer**, I want the Live Changes API to work during distributed JMeter runs so that I can adjust load and test settings without stopping the test.
- **As a test operator**, I want one controller-facing API for a distributed run so that I do not need to track and call each worker node separately.
- **As a maintainer**, I want distributed-mode support to preserve the current REST API contract as much as possible so that existing automation can keep working with minimal changes.
- **As a contributor**, I want clear proof artifacts for distributed-mode behavior so that future regressions in JMeter lifecycle handling are easy to detect.

## Demoable Units of Work

### Unit 1: Distributed Run Detection and Controller API Activation

**Purpose:** Establish a reliable execution path for distributed JMeter runs and make the Live Changes API available from the controller-side workflow selected by the user.

**Functional Requirements:**
- The system shall detect when the plugin is executing in a distributed JMeter test run versus a single-node run.
- The system shall activate one controller-facing Live Changes API entry point for a distributed run.
- The system shall avoid depending only on JMeter listener callbacks that do not fire consistently in remote worker execution.
- The system shall preserve current single-node startup behavior when the test is not distributed.
- The system shall fail with a clear API-visible error message if distributed-mode activation cannot be completed.

**Proof Artifacts:**
- Test: automated coverage for distributed-run detection and startup path demonstrates the plugin selects the correct lifecycle path.
- CLI: a controller-side API health request during a distributed run returns a successful response demonstrates the distributed API is reachable.
- Screenshot or log excerpt: controller and worker startup evidence demonstrates distributed-mode activation occurred.

### Unit 2: Distributed Fanout for Live Changes Commands

**Purpose:** Allow a user to send one API request to the controller path and have the requested live changes applied consistently across the distributed test workers.

**Functional Requirements:**
- The system shall accept distributed-mode API requests through the existing controller-facing API surface.
- The system shall fan out thread-count change requests to all active distributed test workers participating in the run.
- The system shall fan out variable update requests to all active distributed test workers participating in the run.
- The system shall fan out property update requests to all active distributed test workers participating in the run.
- The system shall fan out test-stop requests to all active distributed test workers participating in the run.
- The system shall return a response that identifies whether the distributed command succeeded fully, partially, or failed.
- The system shall report worker-level failures without hiding successful results from other workers.

**Proof Artifacts:**
- CLI: one controller API request that changes a thread group during a distributed run and results in changed worker thread activity demonstrates distributed thread fanout works.
- CLI: one controller API request that updates variables or properties and is observable on multiple workers demonstrates distributed configuration fanout works.
- Test: automated coverage for fanout result handling demonstrates success, partial success, and failure responses are reported correctly.

### Unit 3: Distributed Read Aggregation and Operator Visibility

**Purpose:** Make the read endpoints useful in distributed mode by returning a controller-visible view of worker state instead of silently exposing incomplete local-only data.

**Functional Requirements:**
- The system shall provide distributed-mode responses for thread, status, summary, and error read endpoints through the controller-facing API.
- The system shall aggregate worker responses into one API response format that remains understandable to existing users.
- The system shall include worker identity in distributed responses when needed to explain differences between workers.
- The system shall preserve single-node response behavior when the plugin is not running in distributed mode.
- The system shall document any endpoint whose distributed response shape differs from single-node behavior.

**Proof Artifacts:**
- CLI: a distributed-mode `GET /threads` or equivalent response showing data from multiple workers demonstrates aggregated thread visibility.
- CLI: distributed-mode status, summary, and errors responses that include worker-aware data demonstrates read aggregation works.
- Screenshot or markdown example: updated API documentation demonstrates operators can understand distributed-mode responses.

## Non-Goals (Out of Scope)

1. **Cross-run orchestration platform**: this feature does not create a new external control plane for coordinating multiple separate JMeter test runs.
2. **Non-distributed API redesign**: this feature does not replace the existing single-node API with a brand-new protocol or unrelated endpoint set.
3. **Advanced worker management**: this feature does not provision worker nodes, manage JMeter RMI infrastructure, or automate remote host setup outside normal JMeter distributed-test configuration.

## Design Considerations

No specific visual design requirements identified. API behavior should remain easy to understand from HTTP clients such as cURL, Postman, and Insomnia. If distributed-mode responses include worker identity or partial-failure details, the JSON shape should stay simple and readable for operators during live test execution.

## Repository Standards

- Follow the existing Maven and Java project structure under `src/main/java/io/github/delirius325/jmeter/config/livechanges/` and `api/resources/`.
- Preserve the current embedded Jetty plus Jersey REST pattern used by `api/App.java` and the resource classes.
- Keep API behavior aligned with `docs/swagger.yaml`, and update the documentation source when distributed-mode behavior changes endpoint semantics.
- Follow the repository’s current testing style by adding focused Java tests under `src/test/java/`, while extending beyond the current minimal healthcheck coverage where needed.
- Preserve backward compatibility for existing single-node users unless the spec explicitly allows a distributed-mode-only response difference.

## Technical Considerations

- The repository currently targets Java 9+, Maven packaging, JMeter `5.3`, Jetty `9.4.35`, and Jersey `2.31`; the implementation should work within that stack unless a later spec revision changes dependencies.
- The current plugin starts its API server from `TestStateListener.testStarted()` and applies runtime changes from `LoopIterationListener.iterationStart()`. Issue `#36` and the JMeter API references indicate distributed execution does not trigger the same lifecycle path on remote engines, so distributed-mode support must use a lifecycle strategy that is valid for remote execution instead of assuming local listener parity.
- Official Apache JMeter distributed-testing guidance treats worker nodes as independently running JMeter engines coordinated by a controller node. Because the user selected a controller-side fanout model, the implementation must introduce an explicit worker-coordination path rather than relying only on the current in-process embedded-server design.
- Distributed fanout and read aggregation should be designed so partial worker failure is observable and does not corrupt successful worker responses.
- The implementation should avoid assumptions that all thread groups are discoverable only after loop iteration on a local engine, because that pattern is already fragile in the current code.
- Any distributed-mode API response changes should be minimized and documented so existing clients can adapt predictably.

## Security Considerations

- The feature shall not require committing secrets, tokens, or environment-specific worker addresses into the repository.
- Distributed-mode proof artifacts shall avoid exposing sensitive test data, credentials, internal hostnames, or production response payloads.
- If controller-to-worker communication is added, errors returned by worker nodes shall avoid leaking unnecessary internal details while still being useful for debugging.
- Documentation shall clearly state any network exposure introduced by distributed-mode support so operators can bind ports and firewall rules appropriately.

## Success Metrics

1. **Distributed startup**: a real JMeter distributed test run exposes a working controller-facing Live Changes API without manual code changes or per-worker manual API calls.
2. **Distributed parity**: the existing core API capabilities for thread changes, variables, properties, status, summary, errors, and graceful stop are demonstrably available during a distributed run.
3. **Proof quality**: the repository includes automated coverage for lifecycle and fanout logic plus manual distributed-run proof artifacts for at least one end-to-end successful run.

## Open Questions

1. No open questions at this time.
