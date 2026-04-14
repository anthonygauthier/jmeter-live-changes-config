package io.github.delirius325.jmeter.config.livechanges;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import io.github.delirius325.jmeter.config.livechanges.api.App;
import io.github.delirius325.jmeter.config.livechanges.distributed.DistributedCommandResponse;
import io.github.delirius325.jmeter.config.livechanges.distributed.DistributedCommandRouter;
import io.github.delirius325.jmeter.config.livechanges.distributed.WorkerClient;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class DistributedControllerApiTest {
    private static final int PORT = 8890;
    private static final String BASE_URL = "http://localhost:" + PORT + "/v1";

    private App app;

    /**
     * Starts the embedded API in distributed-controller mode for API-level verification
     * @throws Exception if setup fails
     */
    @Before
    public void setUp() throws Exception {
        RuntimeState runtimeState = new RuntimeState();
        runtimeState.registerRemoteHost("worker-a");
        runtimeState.registerRemoteHost("worker-b");
        LiveChanges.setRuntimeState(runtimeState);
        LiveChanges.setDistributedCommandRouter(new DistributedCommandRouter(new TestWorkerClient()));
        new LiveChanges().setHttpServerPort(PORT);

        this.app = new App(PORT);
        this.app.start();
    }

    /**
     * Stops the embedded API and resets runtime state
     * @throws Exception if teardown fails
     */
    @After
    public void tearDown() throws Exception {
        if (this.app != null) {
            this.app.stop();
        }
        LiveChanges.setRuntimeState(new RuntimeState());
    }

    /**
     * Verifies that controller-facing distributed write endpoints return worker-aware responses
     * @throws Exception if the HTTP requests fail
     */
    @Test
    public void returnsWorkerAwareDistributedResponses() throws Exception {
        HttpResponse<String> threadsResponse = Unirest.post(BASE_URL + "/threads/checkout")
                .header("Content-Type", "application/json")
                .body("{\"threadNum\":3}")
                .asString();
        HttpResponse<String> variablesResponse = Unirest.post(BASE_URL + "/variables")
                .header("Content-Type", "application/json")
                .body("{\"exampleVar\":\"new-value\"}")
                .asString();
        HttpResponse<String> propertiesResponse = Unirest.post(BASE_URL + "/properties")
                .header("Content-Type", "application/json")
                .body("{\"example.property\":\"new-value\"}")
                .asString();
        HttpResponse<String> stopResponse = Unirest.get(BASE_URL + "/test/end").asString();

        System.out.println("THREADS_RESPONSE=" + threadsResponse.getBody());
        System.out.println("VARIABLES_RESPONSE=" + variablesResponse.getBody());
        System.out.println("PROPERTIES_RESPONSE=" + propertiesResponse.getBody());
        System.out.println("STOP_RESPONSE=" + stopResponse.getBody());

        assertTrue(threadsResponse.getBody().contains("\"command\":\"threads.update\""));
        assertTrue(variablesResponse.getBody().contains("\"command\":\"variables.update\""));
        assertTrue(propertiesResponse.getBody().contains("\"command\":\"properties.update\""));
        assertTrue(stopResponse.getBody().contains("\"command\":\"test.stop\""));
    }

    /**
     * Stub worker client used by the API-level distributed controller test
     */
    private static class TestWorkerClient implements WorkerClient {
        @Override
        public DistributedCommandResponse postJson(String host, int port, String path, String requestBody) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("info", "success");
            jsonObject.put("host", host);
            jsonObject.put("path", path);
            jsonObject.put("requestBody", new JSONObject(requestBody));
            return DistributedCommandResponse.success(200, jsonObject);
        }

        @Override
        public DistributedCommandResponse get(String host, int port, String path) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("info", "success");
            jsonObject.put("host", host);
            jsonObject.put("path", path);
            return DistributedCommandResponse.success(200, jsonObject);
        }
    }
}
