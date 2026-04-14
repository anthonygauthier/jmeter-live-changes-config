package io.github.delirius325.jmeter.config.livechanges.distributed;

import io.github.delirius325.jmeter.config.livechanges.ExecutionMode;
import io.github.delirius325.jmeter.config.livechanges.RuntimeState;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Aggregates distributed read responses into controller-visible payloads.
 */
public class DistributedReadAggregator {
    private final WorkerClient workerClient;

    /**
     * Constructor
     * @param workerClient WorkerClient
     */
    public DistributedReadAggregator(WorkerClient workerClient) {
        this.workerClient = workerClient;
    }

    /**
     * Aggregates distributed thread responses
     * @param runtimeState RuntimeState
     * @param port int
     * @param path String
     * @return JSONObject
     */
    public JSONObject aggregateThreads(RuntimeState runtimeState, int port, String path) {
        JSONObject response = baseResponse(runtimeState, "threads.read");
        JSONArray aggregatedThreads = new JSONArray();
        JSONObject workers = new JSONObject();
        int successes = 0;

        for (String host : runtimeState.getRemoteHosts()) {
            DistributedCommandResponse workerResponse = this.workerClient.get(host, port, path);
            JSONObject workerObject = workerObject(workerResponse);
            workers.put(host, workerObject);
            if (workerResponse.isSuccess()) {
                successes++;
                JSONObject workerBody = workerResponse.getBody();
                if (workerBody.has("items") && workerBody.get("items") instanceof JSONArray) {
                    JSONArray array = workerBody.getJSONArray("items");
                    for (int i = 0; i < array.length(); i++) {
                        JSONObject wrapped = new JSONObject();
                        wrapped.put("worker", host);
                        wrapped.put("threadGroup", array.get(i));
                        aggregatedThreads.put(wrapped);
                    }
                } else {
                    JSONObject wrapped = new JSONObject();
                    wrapped.put("worker", host);
                    wrapped.put("threadGroup", workerBody);
                    aggregatedThreads.put(wrapped);
                }
            }
        }

        finalizeResponse(response, workers, runtimeState.getRemoteHosts().size(), successes, "Distributed thread visibility");
        response.put("threads", aggregatedThreads);
        return response;
    }

    /**
     * Aggregates distributed test status responses
     * @param runtimeState RuntimeState
     * @param port int
     * @param path String
     * @return JSONObject
     */
    public JSONObject aggregateStatus(RuntimeState runtimeState, int port, String path) {
        JSONObject response = baseResponse(runtimeState, "test.status");
        JSONObject workers = new JSONObject();
        JSONObject status = new JSONObject();
        int successes = 0;

        for (String host : runtimeState.getRemoteHosts()) {
            DistributedCommandResponse workerResponse = this.workerClient.get(host, port, path);
            JSONObject workerObject = workerObject(workerResponse);
            workers.put(host, workerObject);
            if (workerResponse.isSuccess()) {
                successes++;
                status.put(host, workerResponse.getBody());
            }
        }

        finalizeResponse(response, workers, runtimeState.getRemoteHosts().size(), successes, "Distributed test status");
        response.put("statusByWorker", status);
        return response;
    }

    /**
     * Aggregates distributed test summary responses
     * @param runtimeState RuntimeState
     * @param port int
     * @param path String
     * @return JSONObject
     */
    public JSONObject aggregateSummary(RuntimeState runtimeState, int port, String path) {
        JSONObject response = baseResponse(runtimeState, "test.summary");
        JSONObject workers = new JSONObject();
        JSONObject summary = new JSONObject();
        int successes = 0;

        for (String host : runtimeState.getRemoteHosts()) {
            DistributedCommandResponse workerResponse = this.workerClient.get(host, port, path);
            JSONObject workerObject = workerObject(workerResponse);
            workers.put(host, workerObject);
            if (workerResponse.isSuccess()) {
                successes++;
                summary.put(host, workerResponse.getBody());
            }
        }

        finalizeResponse(response, workers, runtimeState.getRemoteHosts().size(), successes, "Distributed test summary");
        response.put("summaryByWorker", summary);
        return response;
    }

    /**
     * Aggregates distributed test error responses
     * @param runtimeState RuntimeState
     * @param port int
     * @param path String
     * @return JSONObject
     */
    public JSONObject aggregateErrors(RuntimeState runtimeState, int port, String path) {
        JSONObject response = baseResponse(runtimeState, "test.errors");
        JSONObject workers = new JSONObject();
        JSONObject errors = new JSONObject();
        int successes = 0;

        for (String host : runtimeState.getRemoteHosts()) {
            DistributedCommandResponse workerResponse = this.workerClient.get(host, port, path);
            JSONObject workerObject = workerObject(workerResponse);
            workers.put(host, workerObject);
            if (workerResponse.isSuccess()) {
                successes++;
                errors.put(host, workerResponse.getBody());
            }
        }

        finalizeResponse(response, workers, runtimeState.getRemoteHosts().size(), successes, "Distributed test errors");
        response.put("errorsByWorker", errors);
        return response;
    }

    /**
     * Creates the base response wrapper for distributed reads
     * @param runtimeState RuntimeState
     * @param endpoint String
     * @return JSONObject
     */
    private JSONObject baseResponse(RuntimeState runtimeState, String endpoint) {
        JSONObject response = new JSONObject();
        response.put("endpoint", endpoint);
        response.put("executionMode", ExecutionMode.DISTRIBUTED_CONTROLLER.name());
        return response;
    }

    /**
     * Finalizes top-level distributed read metadata
     * @param response JSONObject
     * @param workers JSONObject
     * @param workerCount int
     * @param successes int
     * @param descriptionPrefix String
     */
    private void finalizeResponse(JSONObject response, JSONObject workers, int workerCount, int successes, String descriptionPrefix) {
        response.put("workerCount", workerCount);
        response.put("successfulWorkers", successes);
        response.put("failedWorkers", workerCount - successes);
        response.put("workers", workers);
        if (workerCount == 0) {
            response.put("info", "error");
            response.put("description", descriptionPrefix + " unavailable because no workers are registered.");
        } else if (successes == workerCount) {
            response.put("info", "success");
            response.put("description", descriptionPrefix + " loaded from all workers.");
        } else if (successes > 0) {
            response.put("info", "partial_success");
            response.put("description", descriptionPrefix + " loaded with partial worker failures.");
        } else {
            response.put("info", "error");
            response.put("description", descriptionPrefix + " unavailable because all worker reads failed.");
        }
    }

    /**
     * Creates the worker result object included in aggregated read responses
     * @param workerResponse DistributedCommandResponse
     * @return JSONObject
     */
    private JSONObject workerObject(DistributedCommandResponse workerResponse) {
        JSONObject workerObject = new JSONObject();
        workerObject.put("info", workerResponse.getInfo());
        workerObject.put("statusCode", workerResponse.getStatusCode());
        if (workerResponse.getDescription() != null && !workerResponse.getDescription().isEmpty()) {
            workerObject.put("description", workerResponse.getDescription());
        }
        workerObject.put("response", workerResponse.getBody());
        return workerObject;
    }
}
