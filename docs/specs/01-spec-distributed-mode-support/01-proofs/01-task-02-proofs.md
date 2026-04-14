# Task 02 Proofs - Distributed fanout for mutating API commands

## Task Summary

This task adds a controller-side distributed command router for thread updates, variable updates, property updates, and graceful stop requests. The router forwards commands to registered worker hosts, aggregates worker-level outcomes, and preserves the original single-node behavior when the plugin is not acting as a distributed controller.

## What This Task Proves

- The controller-facing write endpoints now return worker-aware distributed responses instead of single-node-only success payloads during distributed runs.
- Thread, variable, property, and stop commands all share one command-routing contract and one aggregation model.
- Distributed fanout reports `success`, `partial_success`, and `error` based on worker-level outcomes.
- Existing single-node write behavior remains available outside distributed-controller mode.

## Evidence Summary

- A controller-facing API test hit `/v1/threads/{name}`, `/v1/variables`, `/v1/properties`, and `/v1/test/end` and returned worker-aware JSON responses for all four commands.
- The router aggregation test covered all-success, partial-success, and all-failure worker result combinations.
- The resource-level mutating endpoint tests verified both distributed routing and single-node backward compatibility.
- `mvn test --file pom.xml` passed with 15 tests green on April 14, 2026.

## Artifact: Controller-facing distributed command responses

**What it proves:** The controller path returns worker-aware responses for thread, variable, property, and stop commands.

**Why it matters:** This is the user-visible behavior required by the spec for distributed mutating endpoints.

**Command:**

```bash
mvn test --file pom.xml
```

**Artifact path:** `src/test/java/io/github/delirius325/jmeter/config/livechanges/DistributedControllerApiTest.java`

**Result summary:** The API-level test started the embedded server in distributed-controller mode and printed worker-aware responses for all four mutating endpoints, each showing per-worker results and an aggregated command outcome.

```text
THREADS_RESPONSE={"workerCount":2,"successfulWorkers":2,"executionMode":"DISTRIBUTED_CONTROLLER","description":"Distributed command succeeded on all 2 workers.","failedWorkers":0,"workers":{"worker-b":{"response":{"path":"/threads/checkout","requestBody":{"threadNum":3},"host":"worker-b","info":"success"},"info":"success","statusCode":200},"worker-a":{"response":{"path":"/threads/checkout","requestBody":{"threadNum":3},"host":"worker-a","info":"success"},"info":"success","statusCode":200}},"command":"threads.update","info":"success"}
VARIABLES_RESPONSE={"workerCount":2,"successfulWorkers":2,"executionMode":"DISTRIBUTED_CONTROLLER","description":"Distributed command succeeded on all 2 workers.","failedWorkers":0,"workers":{"worker-b":{"response":{"path":"/variables","requestBody":{"exampleVar":"new-value"},"host":"worker-b","info":"success"},"info":"success","statusCode":200},"worker-a":{"response":{"path":"/variables","requestBody":{"exampleVar":"new-value"},"host":"worker-a","info":"success"},"info":"success","statusCode":200}},"command":"variables.update","info":"success"}
PROPERTIES_RESPONSE={"workerCount":2,"successfulWorkers":2,"executionMode":"DISTRIBUTED_CONTROLLER","description":"Distributed command succeeded on all 2 workers.","failedWorkers":0,"workers":{"worker-b":{"response":{"path":"/properties","requestBody":{"example.property":"new-value"},"host":"worker-b","info":"success"},"info":"success","statusCode":200},"worker-a":{"response":{"path":"/properties","requestBody":{"example.property":"new-value"},"host":"worker-a","info":"success"},"info":"success","statusCode":200}},"command":"properties.update","info":"success"}
STOP_RESPONSE={"workerCount":2,"successfulWorkers":2,"executionMode":"DISTRIBUTED_CONTROLLER","description":"Distributed command succeeded on all 2 workers.","failedWorkers":0,"workers":{"worker-b":{"response":{"path":"/test/end","host":"worker-b","info":"success"},"info":"success","statusCode":200},"worker-a":{"response":{"path":"/test/end","host":"worker-a","info":"success"},"info":"success","statusCode":200}},"command":"test.stop","info":"success"}
```

## Artifact: Fanout aggregation coverage

**What it proves:** The distributed router reports full success, partial success, and full failure correctly from worker-level outcomes.

**Why it matters:** Operators need an accurate aggregate result instead of a misleading binary success signal when some workers fail.

**Artifact path:** `target/surefire-reports/io.github.delirius325.jmeter.config.livechanges.DistributedCommandRouterTest.txt`

**Result summary:** The dedicated router suite passed all three aggregation scenarios.

```text
-------------------------------------------------------------------------------
Test set: io.github.delirius325.jmeter.config.livechanges.DistributedCommandRouterTest
-------------------------------------------------------------------------------
Tests run: 3, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.023 sec
```

## Artifact: Resource-level routing and backward compatibility

**What it proves:** The write resources route through the distributed command router in controller mode and keep the original single-node write behavior outside controller mode.

**Why it matters:** This prevents distributed support from regressing existing single-node users.

**Artifact path:** `target/surefire-reports/io.github.delirius325.jmeter.config.livechanges.DistributedMutatingResourcesTest.txt`

**Result summary:** The resource suite passed distributed endpoint routing checks for threads, variables, properties, and stop, plus single-node preservation checks for variables, properties, and stop.

```text
-------------------------------------------------------------------------------
Test set: io.github.delirius325.jmeter.config.livechanges.DistributedMutatingResourcesTest
-------------------------------------------------------------------------------
Tests run: 7, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.004 sec
```

## Artifact: Repository test gate

**What it proves:** The new distributed write implementation integrates cleanly with the existing codebase and test suite.

**Why it matters:** Parent task completion requires the repository-standard test gate to pass after the feature work lands.

**Command:**

```bash
mvn test --file pom.xml
```

**Result summary:** The full suite passed with the new distributed write tests included.

```text
Results :

Tests run: 15, Failures: 0, Errors: 0, Skipped: 0

[INFO] BUILD SUCCESS
```

## Reviewer Conclusion

Task `2.0` is complete: the controller can now fan out mutating API commands across distributed workers, report worker-aware aggregate outcomes, and preserve the previous single-node behavior when distributed routing is not active.
