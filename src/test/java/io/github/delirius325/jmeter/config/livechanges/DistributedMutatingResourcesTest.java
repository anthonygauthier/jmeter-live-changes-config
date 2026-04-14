package io.github.delirius325.jmeter.config.livechanges;

import io.github.delirius325.jmeter.config.livechanges.api.resources.PropertiesResource;
import io.github.delirius325.jmeter.config.livechanges.api.resources.TestResource;
import io.github.delirius325.jmeter.config.livechanges.api.resources.ThreadsResource;
import io.github.delirius325.jmeter.config.livechanges.api.resources.VariablesResource;
import io.github.delirius325.jmeter.config.livechanges.distributed.DistributedCommandResponse;
import io.github.delirius325.jmeter.config.livechanges.distributed.DistributedCommandRouter;
import io.github.delirius325.jmeter.config.livechanges.distributed.WorkerClient;
import org.apache.jmeter.threads.JMeterVariables;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.core.Response;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class DistributedMutatingResourcesTest {
    private RuntimeState runtimeState;

    @Before
    public void setUp() {
        this.runtimeState = new RuntimeState();
        LiveChanges.setRuntimeState(this.runtimeState);
        LiveChanges.setDistributedCommandRouter(new DistributedCommandRouter(new CapturingWorkerClient()));
        new LiveChanges().setHttpServerPort(7566);
        LiveChanges.setjMeterVariables(new JMeterVariables());
        LiveChanges.setjMeterProperties(new Properties());
        LiveChanges.setStopTest(false);
    }

    @After
    public void tearDown() {
        LiveChanges.setRuntimeState(new RuntimeState());
        LiveChanges.setDistributedCommandRouter(new DistributedCommandRouter(new CapturingWorkerClient()));
        LiveChanges.setjMeterVariables(new JMeterVariables());
        LiveChanges.setjMeterProperties(new Properties());
        LiveChanges.setStopTest(false);
    }

    @Test
    public void routesThreadUpdatesThroughDistributedRouter() throws Exception {
        this.runtimeState.registerRemoteHost("worker-a");

        Response response = new ThreadsResource().modifySpecificThread("{\"threadNum\":3}", "checkout");
        JSONObject jsonObject = new JSONObject(response.getEntity().toString());

        assertEquals("threads.update", jsonObject.getString("command"));
        assertEquals("success", jsonObject.getString("info"));
        assertTrue(jsonObject.getJSONObject("workers").has("worker-a"));
    }

    @Test
    public void routesVariableUpdatesThroughDistributedRouter() {
        this.runtimeState.registerRemoteHost("worker-a");

        Response response = new VariablesResource().postVariables("{\"exampleVar\":\"new-value\"}");
        JSONObject jsonObject = new JSONObject(response.getEntity().toString());

        assertEquals("variables.update", jsonObject.getString("command"));
        assertEquals("success", jsonObject.getString("info"));
    }

    @Test
    public void routesPropertyUpdatesThroughDistributedRouter() {
        this.runtimeState.registerRemoteHost("worker-a");

        Response response = new PropertiesResource().postVariables("{\"example.property\":\"new-value\"}");
        JSONObject jsonObject = new JSONObject(response.getEntity().toString());

        assertEquals("properties.update", jsonObject.getString("command"));
        assertEquals("success", jsonObject.getString("info"));
    }

    @Test
    public void routesStopCommandThroughDistributedRouter() {
        this.runtimeState.registerRemoteHost("worker-a");

        Response response = new TestResource().endTestRun();
        JSONObject jsonObject = new JSONObject(response.getEntity().toString());

        assertEquals("test.stop", jsonObject.getString("command"));
        assertEquals("success", jsonObject.getString("info"));
    }

    @Test
    public void preservesSingleNodeVariableUpdates() {
        JMeterVariables vars = new JMeterVariables();
        vars.put("exampleVar", "old-value");
        LiveChanges.setjMeterVariables(vars);

        Response response = new VariablesResource().postVariables("{\"exampleVar\":\"new-value\"}");
        JSONObject jsonObject = new JSONObject(response.getEntity().toString());

        assertEquals("success", jsonObject.getString("info"));
        assertEquals("new-value", LiveChanges.getjMeterVariables().get("exampleVar"));
    }

    @Test
    public void preservesSingleNodePropertyUpdates() {
        Properties properties = new Properties();
        properties.put("example.property", "old-value");
        LiveChanges.setjMeterProperties(properties);

        Response response = new PropertiesResource().postVariables("{\"example.property\":\"new-value\"}");
        JSONObject jsonObject = new JSONObject(response.getEntity().toString());

        assertEquals("success", jsonObject.getString("info"));
        assertEquals("new-value", LiveChanges.getjMeterProperties().get("example.property"));
    }

    @Test
    public void preservesSingleNodeStopBehavior() {
        Response response = new TestResource().endTestRun();
        JSONObject jsonObject = new JSONObject(response.getEntity().toString());

        assertEquals("success", jsonObject.getString("info"));
        assertTrue(LiveChanges.getStopTest());
        assertFalse(jsonObject.has("workers"));
    }

    private static class CapturingWorkerClient implements WorkerClient {
        @Override
        public DistributedCommandResponse postJson(String host, int port, String path, String requestBody) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("info", "success");
            jsonObject.put("path", path);
            jsonObject.put("requestBody", requestBody);
            return DistributedCommandResponse.success(200, jsonObject);
        }

        @Override
        public DistributedCommandResponse get(String host, int port, String path) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("info", "success");
            jsonObject.put("path", path);
            return DistributedCommandResponse.success(200, jsonObject);
        }
    }
}
