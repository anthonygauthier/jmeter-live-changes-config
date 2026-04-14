# Distributed Validation Notes

## Purpose

This note provides a sanitized manual validation flow for distributed-mode support using `examples/sample-testplan.jmx` and a controller-facing Live Changes API.

## Preconditions

- JMeter controller host can reach every worker host on the configured Live Changes port.
- The same plugin build is installed on the controller and worker nodes.
- The test plan is based on `examples/sample-testplan.jmx` or an equivalent non-sensitive test plan.
- Replace worker names, controller hostnames, and any environment-specific identifiers with placeholders before sharing evidence.

## Suggested Environment Shape

- Controller host: `[CONTROLLER_HOST]`
- Worker hosts: `[WORKER_A]`, `[WORKER_B]`
- Live Changes port: `7566`
- Test plan: `examples/sample-testplan.jmx`

## Validation Steps

### 1. Start the distributed test from the controller

Run the distributed JMeter test using your normal controller-to-worker setup.

Expected result:

- The controller starts the Live Changes API.
- Worker hosts are registered in the controller runtime state.
- No secrets, internal tokens, or private addresses appear in stored notes.

### 2. Confirm controller health

```bash
curl http://[CONTROLLER_HOST]:7566/v1/healthcheck
```

Expected result:

- Response body is `connected` when startup succeeds.
- If startup fails, capture the sanitized JSON error body and HTTP `503`.

### 3. Validate distributed write fanout

```bash
curl -X POST http://[CONTROLLER_HOST]:7566/v1/threads/Thread%20Group \
  -H "Content-Type: application/json" \
  -d "{\"threadNum\":3}"

curl -X POST http://[CONTROLLER_HOST]:7566/v1/variables \
  -H "Content-Type: application/json" \
  -d "{\"exampleVar\":\"new-value\"}"

curl -X POST http://[CONTROLLER_HOST]:7566/v1/properties \
  -H "Content-Type: application/json" \
  -d "{\"example.property\":\"new-value\"}"

curl http://[CONTROLLER_HOST]:7566/v1/test/end
```

Expected result:

- Responses include `executionMode: "DISTRIBUTED_CONTROLLER"`.
- Responses include a `workers` object keyed by sanitized worker names.
- Aggregate `info` is `success`, `partial_success`, or `error` based on worker-level results.

### 4. Validate distributed read aggregation

```bash
curl http://[CONTROLLER_HOST]:7566/v1/threads
curl http://[CONTROLLER_HOST]:7566/v1/test/status
curl http://[CONTROLLER_HOST]:7566/v1/test/summary
curl http://[CONTROLLER_HOST]:7566/v1/test/errors
```

Expected result:

- `GET /threads` returns a `threads` array with worker identity attached to each aggregated entry.
- `GET /test/status` returns `statusByWorker`.
- `GET /test/summary` returns `summaryByWorker`.
- `GET /test/errors` returns `errorsByWorker`.
- Partial worker failures remain visible in the top-level `workers` object.

## Sanitization Rules

- Replace real hosts with placeholders such as `[CONTROLLER_HOST]`, `[WORKER_A]`, and `[WORKER_B]`.
- Remove secrets, access tokens, internal URLs, and any customer or production identifiers.
- If logs include machine-specific filesystem paths, trim or replace them with `[PATH]` where they are not important to the proof.

## Reviewer Checklist

- The controller-facing API was reachable during a distributed run.
- Write responses showed worker-aware aggregated outcomes.
- Read responses showed worker-aware aggregated visibility.
- Any partial failure remained visible without hiding successful worker data.
- All captured evidence was sanitized before commit or PR attachment.
