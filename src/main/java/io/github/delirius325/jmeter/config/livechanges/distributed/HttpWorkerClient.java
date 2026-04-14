package io.github.delirius325.jmeter.config.livechanges.distributed;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import org.json.JSONObject;

/**
 * Default worker client that forwards requests over HTTP to worker-local APIs.
 */
public class HttpWorkerClient implements WorkerClient {
    @Override
    public DistributedCommandResponse postJson(String host, int port, String path, String requestBody) {
        try {
            HttpResponse<String> response = Unirest.post(buildUrl(host, port, path))
                    .header("Content-Type", "application/json")
                    .body(requestBody)
                    .asString();
            return toDistributedResponse(response);
        } catch (Exception e) {
            return DistributedCommandResponse.failure(503, e.getMessage());
        }
    }

    @Override
    public DistributedCommandResponse get(String host, int port, String path) {
        try {
            HttpResponse<String> response = Unirest.get(buildUrl(host, port, path)).asString();
            return toDistributedResponse(response);
        } catch (Exception e) {
            return DistributedCommandResponse.failure(503, e.getMessage());
        }
    }

    private DistributedCommandResponse toDistributedResponse(HttpResponse<String> response) {
        try {
            return DistributedCommandResponse.success(response.getStatus(), new JSONObject(response.getBody()));
        } catch (Exception ignored) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("rawBody", response.getBody());
            return DistributedCommandResponse.success(response.getStatus(), jsonObject);
        }
    }

    private String buildUrl(String host, int port, String path) {
        return String.format("http://%s:%d/v1%s", host, port, path);
    }
}
