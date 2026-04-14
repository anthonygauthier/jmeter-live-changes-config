package io.github.delirius325.jmeter.config.livechanges.distributed;

import io.github.delirius325.jmeter.config.livechanges.ExecutionMode;
import io.github.delirius325.jmeter.config.livechanges.RuntimeState;
import org.json.JSONObject;

import java.util.Set;

/**
 * Fanout helper for controller-side distributed write commands.
 */
public class DistributedCommandRouter {
    private final WorkerClient workerClient;

    public DistributedCommandRouter(WorkerClient workerClient) {
        this.workerClient = workerClient;
    }

    public JSONObject postJson(RuntimeState runtimeState, int port, String path, String requestBody, String commandName) {
        return this.route(runtimeState, port, path, requestBody, commandName, true);
    }

    public JSONObject get(RuntimeState runtimeState, int port, String path, String commandName) {
        return this.route(runtimeState, port, path, null, commandName, false);
    }

    private JSONObject route(RuntimeState runtimeState, int port, String path, String requestBody, String commandName, boolean isPost) {
        JSONObject parentObject = new JSONObject();
        JSONObject workersObject = new JSONObject();
        Set<String> remoteHosts = runtimeState.getRemoteHosts();
        int successfulWorkers = 0;

        for (String remoteHost : remoteHosts) {
            DistributedCommandResponse workerResponse = isPost
                    ? this.workerClient.postJson(remoteHost, port, path, requestBody)
                    : this.workerClient.get(remoteHost, port, path);
            JSONObject workerObject = new JSONObject();
            workerObject.put("info", workerResponse.getInfo());
            workerObject.put("statusCode", workerResponse.getStatusCode());
            if (workerResponse.getDescription() != null && !workerResponse.getDescription().isEmpty()) {
                workerObject.put("description", workerResponse.getDescription());
            }
            workerObject.put("response", workerResponse.getBody());
            workersObject.put(remoteHost, workerObject);

            if (workerResponse.isSuccess()) {
                successfulWorkers++;
            }
        }

        parentObject.put("command", commandName);
        parentObject.put("executionMode", ExecutionMode.DISTRIBUTED_CONTROLLER.name());
        parentObject.put("workerCount", remoteHosts.size());
        parentObject.put("successfulWorkers", successfulWorkers);
        parentObject.put("failedWorkers", remoteHosts.size() - successfulWorkers);
        parentObject.put("workers", workersObject);

        if (remoteHosts.isEmpty()) {
            parentObject.put("info", "error");
            parentObject.put("description", "No distributed workers are registered for this controller run.");
        } else if (successfulWorkers == remoteHosts.size()) {
            parentObject.put("info", "success");
            parentObject.put("description", String.format("Distributed command succeeded on all %d workers.", successfulWorkers));
        } else if (successfulWorkers > 0) {
            parentObject.put("info", "partial_success");
            parentObject.put("description", String.format("Distributed command succeeded on %d of %d workers.", successfulWorkers, remoteHosts.size()));
        } else {
            parentObject.put("info", "error");
            parentObject.put("description", "Distributed command failed on all workers.");
        }

        return parentObject;
    }
}
