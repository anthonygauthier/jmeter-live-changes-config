package io.github.delirius325.jmeter.config.livechanges;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import io.github.delirius325.jmeter.config.livechanges.api.App;
import io.github.delirius325.jmeter.config.livechanges.distributed.DistributedCommandResponse;
import io.github.delirius325.jmeter.config.livechanges.distributed.DistributedReadAggregator;
import io.github.delirius325.jmeter.config.livechanges.distributed.WorkerClient;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class DistributedReadApiTest {
    private static final int PORT = 8891;
    private static final String BASE_URL = "http://localhost:" + PORT + "/v1";

    private App app;

    @Before
    public void setUp() throws Exception {
        RuntimeState runtimeState = new RuntimeState();
        runtimeState.registerRemoteHost("worker-a");
        runtimeState.registerRemoteHost("worker-b");
        LiveChanges.setRuntimeState(runtimeState);
        LiveChanges.setDistributedReadAggregator(new DistributedReadAggregator(new TestWorkerClient()));
        new LiveChanges().setHttpServerPort(PORT);

        this.app = new App(PORT);
        this.app.start();
    }

    @After
    public void tearDown() throws Exception {
        if (this.app != null) {
            this.app.stop();
        }
        LiveChanges.setRuntimeState(new RuntimeState());
    }

    @Test
    public void returnsWorkerAwareReadResponses() throws Exception {
        HttpResponse<String> threads = Unirest.get(BASE_URL + "/threads").asString();
        HttpResponse<String> status = Unirest.get(BASE_URL + "/test/status").asString();
        HttpResponse<String> summary = Unirest.get(BASE_URL + "/test/summary").asString();
        HttpResponse<String> errors = Unirest.get(BASE_URL + "/test/errors").asString();

        System.out.println("THREADS_READ_RESPONSE=" + threads.getBody());
        System.out.println("STATUS_READ_RESPONSE=" + status.getBody());
        System.out.println("SUMMARY_READ_RESPONSE=" + summary.getBody());
        System.out.println("ERRORS_READ_RESPONSE=" + errors.getBody());

        assertTrue(threads.getBody().contains("\"endpoint\":\"threads.read\""));
        assertTrue(status.getBody().contains("\"endpoint\":\"test.status\""));
        assertTrue(summary.getBody().contains("\"endpoint\":\"test.summary\""));
        assertTrue(errors.getBody().contains("\"endpoint\":\"test.errors\""));
    }

    private static class TestWorkerClient implements WorkerClient {
        @Override
        public DistributedCommandResponse postJson(String host, int port, String path, String requestBody) {
            throw new UnsupportedOperationException();
        }

        @Override
        public DistributedCommandResponse get(String host, int port, String path) {
            if ("/threads".equals(path)) {
                JSONArray array = new JSONArray();
                array.put(new JSONObject().put("checkout", new JSONObject().put("active", 3)));
                return DistributedCommandResponse.success(200, new JSONObject().put("items", array));
            }
            if ("/test/status".equals(path)) {
                return DistributedCommandResponse.success(200, new JSONObject().put("totalThreads", 4).put("totalActiveThreads", 4));
            }
            if ("/test/summary".equals(path)) {
                return DistributedCommandResponse.success(200, new JSONObject().put("samples", new JSONArray().put(new JSONObject().put("checkout", new JSONObject().put("totalSamples", 10)))));
            }
            if ("/test/errors".equals(path)) {
                return DistributedCommandResponse.success(200, new JSONObject().put("errors", new JSONArray()));
            }
            return DistributedCommandResponse.failure(404, "not found");
        }
    }
}
