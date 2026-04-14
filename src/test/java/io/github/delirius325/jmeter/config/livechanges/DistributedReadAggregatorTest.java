package io.github.delirius325.jmeter.config.livechanges;

import io.github.delirius325.jmeter.config.livechanges.distributed.DistributedCommandResponse;
import io.github.delirius325.jmeter.config.livechanges.distributed.DistributedReadAggregator;
import io.github.delirius325.jmeter.config.livechanges.distributed.WorkerClient;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class DistributedReadAggregatorTest {
    /**
     * Verifies that distributed thread reads aggregate worker data into one response
     */
    @Test
    public void aggregatesThreadVisibilityAcrossWorkers() {
        RuntimeState runtimeState = new RuntimeState();
        runtimeState.registerRemoteHost("worker-a");
        runtimeState.registerRemoteHost("worker-b");
        DistributedReadAggregator aggregator = new DistributedReadAggregator(new StubWorkerClient()
                .with("worker-a", successArray("checkout", 3))
                .with("worker-b", successArray("checkout", 5)));

        JSONObject response = aggregator.aggregateThreads(runtimeState, 7566, "/threads");

        assertEquals("success", response.getString("info"));
        assertEquals(2, response.getJSONArray("threads").length());
    }

    /**
     * Verifies that partial read failures keep successful worker data visible
     */
    @Test
    public void surfacesPartialReadFailuresWithoutDroppingGoodWorkers() {
        RuntimeState runtimeState = new RuntimeState();
        runtimeState.registerRemoteHost("worker-a");
        runtimeState.registerRemoteHost("worker-b");
        DistributedReadAggregator aggregator = new DistributedReadAggregator(new StubWorkerClient()
                .with("worker-a", DistributedCommandResponse.success(200, statusBody(10)))
                .with("worker-b", DistributedCommandResponse.failure(503, "worker-b unavailable")));

        JSONObject response = aggregator.aggregateStatus(runtimeState, 7566, "/test/status");

        assertEquals("partial_success", response.getString("info"));
        assertTrue(response.getJSONObject("statusByWorker").has("worker-a"));
        assertEquals("worker-b unavailable", response.getJSONObject("workers").getJSONObject("worker-b").getString("description"));
    }

    /**
     * Verifies that the distributed read aggregator reports error when every worker fails
     */
    @Test
    public void reportsErrorWhenEveryWorkerReadFails() {
        RuntimeState runtimeState = new RuntimeState();
        runtimeState.registerRemoteHost("worker-a");
        runtimeState.registerRemoteHost("worker-b");
        DistributedReadAggregator aggregator = new DistributedReadAggregator(new StubWorkerClient()
                .with("worker-a", DistributedCommandResponse.failure(500, "boom-a"))
                .with("worker-b", DistributedCommandResponse.failure(503, "boom-b")));

        JSONObject response = aggregator.aggregateErrors(runtimeState, 7566, "/test/errors");

        assertEquals("error", response.getString("info"));
        assertEquals(0, response.getInt("successfulWorkers"));
        assertEquals(2, response.getInt("failedWorkers"));
    }

    /**
     * Utility method that creates a successful thread read response body
     * @param threadGroupName String
     * @param activeThreads int
     * @return DistributedCommandResponse
     */
    private static DistributedCommandResponse successArray(String threadGroupName, int activeThreads) {
        JSONArray array = new JSONArray();
        JSONObject threadInfo = new JSONObject();
        threadInfo.put("active", activeThreads);
        JSONObject wrapped = new JSONObject();
        wrapped.put(threadGroupName, threadInfo);
        array.put(wrapped);
        return DistributedCommandResponse.success(200, new JSONObject().put("items", array));
    }

    /**
     * Utility method that creates a test status response body
     * @param totalThreads int
     * @return JSONObject
     */
    private static JSONObject statusBody(int totalThreads) {
        JSONObject body = new JSONObject();
        body.put("totalThreads", totalThreads);
        return body;
    }

    /**
     * Stub worker client used to drive deterministic read aggregation behavior
     */
    private static class StubWorkerClient implements WorkerClient {
        private final JSONObject responses = new JSONObject();

        /**
         * Registers a GET response for a worker host
         * @param host String
         * @param response DistributedCommandResponse
         * @return StubWorkerClient
         */
        private StubWorkerClient with(String host, DistributedCommandResponse response) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("statusCode", response.getStatusCode());
            jsonObject.put("body", response.getBody());
            jsonObject.put("failureReason", response.getFailureReason());
            this.responses.put(host, jsonObject);
            return this;
        }

        @Override
        public DistributedCommandResponse postJson(String host, int port, String path, String requestBody) {
            throw new UnsupportedOperationException();
        }

        @Override
        public DistributedCommandResponse get(String host, int port, String path) {
            JSONObject jsonObject = this.responses.getJSONObject(host);
            String failureReason = jsonObject.optString("failureReason", null);
            if (failureReason != null && !failureReason.isEmpty() && !"null".equals(failureReason)) {
                return DistributedCommandResponse.failure(jsonObject.getInt("statusCode"), failureReason);
            }
            return DistributedCommandResponse.success(jsonObject.getInt("statusCode"), jsonObject.getJSONObject("body"));
        }
    }
}
