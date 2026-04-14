package io.github.delirius325.jmeter.config.livechanges;

import io.github.delirius325.jmeter.config.livechanges.distributed.DistributedCommandResponse;
import io.github.delirius325.jmeter.config.livechanges.distributed.DistributedCommandRouter;
import io.github.delirius325.jmeter.config.livechanges.distributed.WorkerClient;
import org.json.JSONObject;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class DistributedCommandRouterTest {
    /**
     * Verifies that the distributed router reports success when every worker succeeds
     */
    @Test
    public void reportsSuccessWhenAllWorkersSucceed() {
        RuntimeState runtimeState = new RuntimeState();
        runtimeState.registerRemoteHost("worker-a");
        runtimeState.registerRemoteHost("worker-b");
        DistributedCommandRouter router = new DistributedCommandRouter(new StubWorkerClient()
                .withPost("worker-a", DistributedCommandResponse.success(200, successBody("ok-a")))
                .withPost("worker-b", DistributedCommandResponse.success(200, successBody("ok-b"))));

        JSONObject response = router.postJson(runtimeState, 7566, "/variables", "{\"example\":\"value\"}", "variables.update");

        assertEquals("success", response.getString("info"));
        assertEquals(2, response.getInt("successfulWorkers"));
        assertEquals(0, response.getInt("failedWorkers"));
    }

    /**
     * Verifies that partial worker failures are surfaced without dropping successes
     */
    @Test
    public void reportsPartialSuccessWhenSomeWorkersFail() {
        RuntimeState runtimeState = new RuntimeState();
        runtimeState.registerRemoteHost("worker-a");
        runtimeState.registerRemoteHost("worker-b");
        DistributedCommandRouter router = new DistributedCommandRouter(new StubWorkerClient()
                .withPost("worker-a", DistributedCommandResponse.success(200, successBody("ok-a")))
                .withPost("worker-b", DistributedCommandResponse.failure(503, "worker-b unavailable")));

        JSONObject response = router.postJson(runtimeState, 7566, "/properties", "{\"example.property\":\"value\"}", "properties.update");

        assertEquals("partial_success", response.getString("info"));
        assertEquals(1, response.getInt("successfulWorkers"));
        assertEquals(1, response.getInt("failedWorkers"));
        assertEquals("worker-b unavailable", response.getJSONObject("workers").getJSONObject("worker-b").getString("description"));
    }

    /**
     * Verifies that the distributed router reports error when every worker fails
     */
    @Test
    public void reportsErrorWhenAllWorkersFail() {
        RuntimeState runtimeState = new RuntimeState();
        runtimeState.registerRemoteHost("worker-a");
        runtimeState.registerRemoteHost("worker-b");
        DistributedCommandRouter router = new DistributedCommandRouter(new StubWorkerClient()
                .withGet("worker-a", DistributedCommandResponse.failure(503, "timeout"))
                .withGet("worker-b", DistributedCommandResponse.failure(500, "server error")));

        JSONObject response = router.get(runtimeState, 7566, "/test/end", "test.stop");

        assertEquals("error", response.getString("info"));
        assertEquals(0, response.getInt("successfulWorkers"));
        assertEquals(2, response.getInt("failedWorkers"));
    }

    /**
     * Utility method that creates a successful worker response body
     * @param description String
     * @return JSONObject
     */
    private static JSONObject successBody(String description) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("info", "success");
        jsonObject.put("description", description);
        return jsonObject;
    }

    /**
     * Stub worker client used to drive deterministic router test behavior
     */
    private static class StubWorkerClient implements WorkerClient {
        private final JSONObject postResponses = new JSONObject();
        private final JSONObject getResponses = new JSONObject();

        /**
         * Registers a POST response for a worker host
         * @param host String
         * @param response DistributedCommandResponse
         * @return StubWorkerClient
         */
        private StubWorkerClient withPost(String host, DistributedCommandResponse response) {
            this.postResponses.put(host, wrap(response));
            return this;
        }

        /**
         * Registers a GET response for a worker host
         * @param host String
         * @param response DistributedCommandResponse
         * @return StubWorkerClient
         */
        private StubWorkerClient withGet(String host, DistributedCommandResponse response) {
            this.getResponses.put(host, wrap(response));
            return this;
        }

        @Override
        public DistributedCommandResponse postJson(String host, int port, String path, String requestBody) {
            return unwrap(this.postResponses.getJSONObject(host));
        }

        @Override
        public DistributedCommandResponse get(String host, int port, String path) {
            return unwrap(this.getResponses.getJSONObject(host));
        }

        /**
         * Serializes a stub response into JSON storage for the test client
         * @param response DistributedCommandResponse
         * @return JSONObject
         */
        private JSONObject wrap(DistributedCommandResponse response) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("statusCode", response.getStatusCode());
            jsonObject.put("body", response.getBody());
            jsonObject.put("failureReason", response.getFailureReason());
            return jsonObject;
        }

        /**
         * Deserializes a stored stub response
         * @param jsonObject JSONObject
         * @return DistributedCommandResponse
         */
        private DistributedCommandResponse unwrap(JSONObject jsonObject) {
            String failureReason = jsonObject.optString("failureReason", null);
            if (failureReason != null && !failureReason.isEmpty() && !"null".equals(failureReason)) {
                return DistributedCommandResponse.failure(jsonObject.getInt("statusCode"), failureReason);
            }
            return DistributedCommandResponse.success(jsonObject.getInt("statusCode"), jsonObject.getJSONObject("body"));
        }
    }
}
