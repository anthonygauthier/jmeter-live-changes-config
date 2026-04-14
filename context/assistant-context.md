# Repository Context

## Purpose
`jmeter-live-changes-config` is a JMeter plugin that adds a `Live Changes Config` test element. When a test starts, it launches an embedded Jetty server with Jersey resources so a REST API can inspect and mutate parts of a running test plan.

Primary live-change capabilities from the current codebase:
- Change active thread counts per thread group
- Read/update JMeter variables
- Read/update JMeter properties
- Read test status, summary metrics, and recent error payloads
- End the test gracefully

## Project Shape
- `src/main/java/io/github/delirius325/jmeter/config/livechanges/`
  Contains the plugin entry point, BeanInfo metadata, result aggregation, and helpers.
- `src/main/java/io/github/delirius325/jmeter/config/livechanges/api/`
  Embedded server bootstrap.
- `src/main/java/io/github/delirius325/jmeter/config/livechanges/api/resources/`
  REST endpoints.
- `src/test/java/.../TestAppServer.java`
  Minimal connectivity test for `/v1/healthcheck`.
- `docs/swagger.yaml`
  API spec source for the published docs site.
- `examples/sample-testplan.jmx`
  Example JMeter plan.

## Build and Runtime
- Maven project, artifact: `io.github.delirius325:jmeter.config.livechanges:0.0.3`
- Java source/target: `1.9`
- Main runtime libraries: Jetty `9.4.35`, Jersey `2.31`
- JMeter dependencies are `provided` and target JMeter `5.3`
- Packaged as a shaded jar for placement in `$JMETER_HOME/lib/ext`

## Core Execution Model
- `LiveChanges` is the JMeter config element implementation.
- On `testStarted()`, it resets sampler state, loads the JMX test plan tree, initializes a per-thread-group change queue, and starts `App`.
- `App` exposes Jersey resources under `/v1/*`.
- `iterationStart()` is the key event loop:
  - captures `StandardJMeterEngine` on first iteration
  - records thread groups encountered during execution
  - applies pending thread-count changes
  - preserves shared variables/properties across iterations
- `sampleOccurred()` aggregates `SampleResult` data into `SamplerMap` for summary/error endpoints.
- `testEnded()` stops the test/server via `finalizeTest()`.

## API Surface
Base path: `/v1`

- `GET /healthcheck`
  Returns plain text `connected`.
- `GET /threads`
  Lists known thread groups as JSON.
- `GET /threads/{name}`
  Returns one thread group's metadata.
- `POST /threads/{name}`
  Queues a desired `threadNum` change for the named thread group.
- `GET /variables`
  Returns all JMeter variables.
- `GET /variables/{param}`
  Returns one variable if matched.
- `POST /variables`
  Updates provided variable values.
- `GET /properties`
  Returns all JMeter properties.
- `POST /properties`
  Updates provided property values.
- `GET /test/status`
  Returns start time, elapsed time, active thread count, total thread count.
- `GET /test/end`
  Signals graceful stop.
- `GET /test/summary`
  Returns aggregated metrics by sampler label.
- `GET /test/errors`
  Returns the latest failed `SampleResult` per sampler label.

## Important Classes
- `LiveChanges.java`
  Main plugin lifecycle and shared mutable state.
- `api/App.java`
  Embedded Jetty/Jersey bootstrap.
- `api/resources/*.java`
  REST handlers.
- `helpers/ThreadGroupHelper.java`
  Converts active thread groups into API JSON and resolves groups by name.
- `SamplerMap.java` / `ResultHolder.java`
  Runtime stats aggregation for summary endpoints.

## Current Design Assumptions
- Thread group names must be unique. The queue map is keyed by thread group name.
- Only enabled thread groups from the loaded test plan are tracked initially.
- Thread-group discovery for API reads depends on thread groups encountered and stored in `LiveChanges.testThreadGroups`.
- The plugin expects to run inside JMeter; tests only verify server startup and healthcheck.

## Notable Code Risks
- `VariablesResource.getVariables()` compares strings with `==` instead of `.equals()`, so single-variable lookup may fail.
- `VariablesResource` and `PropertiesResource` use `!=` against stringified values; change detection is reference-based, not value-based.
- `ResultHolder.calculate()` uses `this.totalErrors++` in a ternary assignment, which likely undercounts errors because the post-increment value is assigned back.
- `SamplerMap.rawMap` stores only the latest `SampleResult` per label, so `/test/errors` is not a full error history.
- Several shared structures are mutable statics accessed across request and JMeter threads; concurrency behavior is only partially guarded.
- `finalizeTest()` calls `jMeterEngine.stopTest(true)` during `testEnded()`, which may be redundant or brittle depending on shutdown ordering.

## Minimal Mental Model For Future Work
- If a change affects REST behavior, inspect `api/resources/` first.
- If a change affects runtime mutation of JMeter state, inspect `LiveChanges.java` and helpers.
- If a change affects metrics/summary output, inspect `SamplerMap.java` and `ResultHolder.java`.
- If a change affects JMeter UI exposure, inspect `LiveChangesBeanInfo.java` and `LiveChangesResources.properties`.

## Local Working Rules
- Follow the repository's existing comment style and comment density in new files and new functions.
- Do not introduce a new commenting pattern when extending this codebase; match the surrounding style first.
