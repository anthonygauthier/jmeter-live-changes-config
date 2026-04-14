## Relevant Files

| File | Why It Is Relevant |
| --- | --- |
| `src/main/java/io/github/delirius325/jmeter/config/livechanges/LiveChanges.java` | Main JMeter lifecycle entry point and the current single-node runtime state holder. |
| `src/main/java/io/github/delirius325/jmeter/config/livechanges/api/App.java` | Embedded Jetty bootstrap that will need distributed-mode startup and routing support. |
| `src/main/java/io/github/delirius325/jmeter/config/livechanges/api/resources/HealthCheckResource.java` | Existing health endpoint that should remain reachable through the controller-facing API. |
| `src/main/java/io/github/delirius325/jmeter/config/livechanges/api/resources/ThreadsResource.java` | Handles thread read and write endpoints that must support distributed fanout and aggregation. |
| `src/main/java/io/github/delirius325/jmeter/config/livechanges/api/resources/VariablesResource.java` | Handles variable read and write endpoints that must support distributed behavior. |
| `src/main/java/io/github/delirius325/jmeter/config/livechanges/api/resources/PropertiesResource.java` | Handles property read and write endpoints that must support distributed behavior. |
| `src/main/java/io/github/delirius325/jmeter/config/livechanges/api/resources/TestResource.java` | Handles status, summary, errors, and stop endpoints that must support distributed responses. |
| `src/main/java/io/github/delirius325/jmeter/config/livechanges/helpers/ThreadGroupHelper.java` | Current thread-group discovery and response-shaping helper used by thread endpoints. |
| `src/main/java/io/github/delirius325/jmeter/config/livechanges/helpers/JSONHelper.java` | Shared JSON response helper likely needed for consistent distributed success and failure reporting. |
| `src/main/java/io/github/delirius325/jmeter/config/livechanges/SamplerMap.java` | Existing per-sampler aggregation logic that may need worker-aware aggregation support. |
| `src/main/java/io/github/delirius325/jmeter/config/livechanges/ResultHolder.java` | Existing summary metric holder that may need worker-aware aggregation coverage. |
| `src/test/java/io/github/delirius325/jmeter/config/livechanges/TestAppServer.java` | Current server test that can be extended or complemented with distributed-lifecycle tests. |
| `docs/swagger.yaml` | API contract source that must document any distributed-mode response additions or changes. |
| `examples/sample-testplan.jmx` | Useful reference asset for manual distributed-run validation and proof artifact generation. |
| `docs/specs/01-spec-distributed-mode-support/01-spec-distributed-mode-support.md` | Source spec that defines the functional requirements this task plan must cover. |

### Notes

- Unit and integration-style tests should stay in `src/test/java/` and run through the repository’s existing Maven test command: `mvn test --file pom.xml`.
- Preserve the current Maven, Jetty, Jersey, and JMeter-based structure rather than introducing unrelated framework changes.
- Keep API documentation aligned with `docs/swagger.yaml` when distributed-mode responses or behavior change.
- PR titles should continue following the repository’s conventional-commit workflow.

## Tasks

### [x] 1.0 Establish Distributed Run Lifecycle and Controller Activation

#### 1.0 Proof Artifact(s)

- Test: `mvn test --file pom.xml` including new lifecycle-focused tests demonstrates distributed-run detection, startup path selection, and single-node backward compatibility are covered.
- CLI: `curl http://<controller-host>:7566/v1/healthcheck` during a distributed JMeter run returns `connected` demonstrates the controller-facing API is reachable.
- Log: sanitized controller and worker startup log excerpts showing distributed-mode activation demonstrates the runtime selected the distributed path successfully.

#### 1.0 Tasks

- [x] 1.1 Document the current single-node lifecycle in `LiveChanges.java` and identify which callbacks are unsafe to rely on for distributed execution.
- [x] 1.2 Design the runtime state model needed to distinguish single-node execution, controller execution, and worker participation without breaking the current plugin boot path.
- [x] 1.3 Implement distributed-run detection and controller activation in `LiveChanges.java` and `api/App.java`, preserving current single-node startup behavior.
- [x] 1.4 Add clear API-visible startup failure reporting for distributed activation so operators can tell when the distributed path is unavailable.
- [x] 1.5 Add or extend automated tests in `src/test/java/` to cover lifecycle-path selection and controller API startup behavior.

### [x] 2.0 Implement Distributed Fanout for Mutating API Commands

#### 2.0 Proof Artifact(s)

- CLI: `curl -X POST http://<controller-host>:7566/v1/threads/<thread-group> -H "Content-Type: application/json" -d "{\"threadNum\":3}"` returns a worker-aware success or partial-success response demonstrates distributed thread updates fan out correctly.
- CLI: `curl -X POST http://<controller-host>:7566/v1/variables -H "Content-Type: application/json" -d "{\"exampleVar\":\"new-value\"}"` and `curl -X POST http://<controller-host>:7566/v1/properties -H "Content-Type: application/json" -d "{\"example.property\":\"new-value\"}"` return worker-aware results demonstrates distributed variable and property updates fan out correctly.
- CLI: `curl http://<controller-host>:7566/v1/test/end` returns a distributed stop response demonstrates graceful stop fanout works.
- Test: `mvn test --file pom.xml` including new command-fanout tests demonstrates full-success, partial-success, and failure aggregation are covered.

#### 2.0 Tasks

- [x] 2.1 Define the command-routing contract for distributed writes so thread, variable, property, and stop requests can be forwarded from the controller path to workers consistently.
- [x] 2.2 Update `ThreadsResource.java` and related runtime state so thread-count changes are dispatched to distributed workers and return worker-aware results.
- [x] 2.3 Update `VariablesResource.java` and `PropertiesResource.java` so distributed write requests fan out across workers and preserve existing single-node behavior.
- [x] 2.4 Update `TestResource.java` stop handling so a distributed test-stop request reports full success, partial success, or failure at the worker level.
- [x] 2.5 Add automated tests that exercise mutating-command fanout result handling for success, partial success, and failure cases.

### [x] 3.0 Aggregate Distributed Read Endpoints for Operator Visibility

#### 3.0 Proof Artifact(s)

- CLI: `curl http://<controller-host>:7566/v1/threads` during a distributed run returns worker-aware thread data demonstrates aggregated thread visibility.
- CLI: `curl http://<controller-host>:7566/v1/test/status`, `curl http://<controller-host>:7566/v1/test/summary`, and `curl http://<controller-host>:7566/v1/test/errors` return aggregated distributed responses demonstrates controller-visible read behavior.
- Test: `mvn test --file pom.xml` including new aggregation tests demonstrates distributed response shaping and single-node backward compatibility are covered.

#### 3.0 Tasks

- [x] 3.1 Define the distributed read-response shape, including where worker identity appears and where existing single-node response shapes remain unchanged.
- [x] 3.2 Update `ThreadGroupHelper.java` and `ThreadsResource.java` so distributed thread reads aggregate worker state into one controller-visible response.
- [x] 3.3 Update `TestResource.java`, `SamplerMap.java`, and `ResultHolder.java` so status, summary, and error endpoints can expose distributed, worker-aware data.
- [x] 3.4 Ensure read endpoints return useful distributed errors when one or more workers are unavailable without hiding successful worker data.
- [x] 3.5 Add automated tests for aggregated read endpoints, including distributed responses and backward-compatible single-node responses.

### [ ] 4.0 Update API Documentation and Distributed Validation Assets

#### 4.0 Proof Artifact(s)

- Diff: `docs/swagger.yaml` and related documentation changes demonstrate distributed-mode request and response behavior is documented.
- Markdown or screenshot: sanitized distributed-run validation notes showing controller requests and worker-aware responses demonstrates reviewers can independently verify the feature.
- Test: `mvn test --file pom.xml` passes after documentation-aligned code changes demonstrates the repository quality gate still passes.

#### 4.0 Tasks

- [ ] 4.1 Update `docs/swagger.yaml` so distributed-mode request and response behavior is documented for health, write, and read endpoints.
- [ ] 4.2 Add or update repository documentation that explains distributed-mode setup assumptions, controller-facing usage, and any response-shape differences.
- [ ] 4.3 Create a sanitized manual validation checklist or notes file for a real distributed JMeter run using `examples/sample-testplan.jmx` or an equivalent non-sensitive test plan.
- [ ] 4.4 Verify the full planned proof set is reproducible, sanitized, and linked to the task sections before implementation handoff.
