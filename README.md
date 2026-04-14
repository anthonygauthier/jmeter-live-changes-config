# JMeter Live Changes Config

## Overview
This plugin allows users to manipulate (through a REST API) JMeter test plans *on-the-go*.
Here's a couple things that are modifiable:

* Throughput
* Number of active threads
* Variables
* Properties

Users can also:

* View a summary of the test results
* End the test remotely
* Retrieve a quick status of the test (time running, active threads, etc.)

## Why
There are some scenarios where this kind of tool becomes very handy. 
For example;

* Testing the impact of different load scenarios against a system
* Container sizing (on Kubernetes/OSE, for example)
* Correcting certain test parameters while the test is running


## Setup
**Java version 9+ is required**

1. Download the latest JAR from the release section
2. Move the file to your `$JMETER_HOME/lib/ext` folder
3. Start JMeter, load your test plan
4. Add the `Live Changes Config` element to the root of your test plan
5. Set the port you want to be able to communicate with (defaults to `7566`)
6. Start your test
7. By using any HTTP client (Postman, Insomnia, cURL, etc.) communicate with the REST API to change your test's values

## Distributed Mode
Distributed JMeter runs are now supported through one controller-facing API.

- Start the Live Changes plugin on the controller test plan as usual.
- Ensure the controller can reach each worker host on the configured Live Changes port.
- During a distributed run, write endpoints fan out from the controller to registered workers.
- During a distributed run, read endpoints aggregate worker responses into one controller-visible payload.
- Single-node runs keep the original response shapes.

Distributed controller responses now include worker-aware metadata when the runtime is in distributed-controller mode:

- Write endpoints may return `success`, `partial_success`, or `error` with a `workers` object keyed by worker host.
- Read endpoints return aggregated payloads plus worker-level details so partial worker failures remain visible.
- `GET /v1/healthcheck` returns HTTP `503` with a JSON error body if distributed startup fails and the API can surface the failure.

Example distributed commands:

```bash
curl -X POST http://controller-host:7566/v1/threads/Thread%20Group \
  -H "Content-Type: application/json" \
  -d "{\"threadNum\":3}"

curl -X POST http://controller-host:7566/v1/variables \
  -H "Content-Type: application/json" \
  -d "{\"exampleVar\":\"new-value\"}"

curl http://controller-host:7566/v1/test/status
```

## Distributed Validation
The repository includes a sanitized validation asset for distributed mode at `docs/specs/01-spec-distributed-mode-support/01-distributed-validation-notes.md`.
Use it with `examples/sample-testplan.jmx` to reproduce the expected controller-side API behavior without committing environment-specific worker names, secrets, or internal addresses.

## Documentation
An API documentation [is available here](https://anthonygauthier.github.io/jmeter-live-changes-config/), it is directly generated from the [swagger.yaml](docs/swagger.yaml).

---

## Contributing
This plugin is licensed under the MIT license. 

All contributions are welcomed and appreciated.
