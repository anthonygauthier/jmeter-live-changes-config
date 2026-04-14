package io.github.delirius325.jmeter.config.livechanges;

import io.github.delirius325.jmeter.config.livechanges.api.resources.TestResource;
import io.github.delirius325.jmeter.config.livechanges.api.resources.ThreadsResource;
import io.github.delirius325.jmeter.config.livechanges.distributed.DistributedCommandResponse;
import io.github.delirius325.jmeter.config.livechanges.distributed.DistributedReadAggregator;
import io.github.delirius325.jmeter.config.livechanges.distributed.WorkerClient;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.ThreadGroup;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.core.Response;
import java.util.HashSet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class DistributedReadResourcesTest {
    private RuntimeState runtimeState;

    @Before
    public void setUp() {
        this.runtimeState = new RuntimeState();
        LiveChanges.setRuntimeState(this.runtimeState);
        LiveChanges.setDistributedReadAggregator(new DistributedReadAggregator(new TestWorkerClient()));
        new LiveChanges().setHttpServerPort(7566);
        LiveChanges.setTestThreadGroups(new HashSet<ThreadGroup>());
        LiveChanges.setStaticCalcRate(0);
    }

    @After
    public void tearDown() {
        LiveChanges.setRuntimeState(new RuntimeState());
        LiveChanges.setDistributedReadAggregator(new DistributedReadAggregator(new TestWorkerClient()));
        LiveChanges.setTestThreadGroups(new HashSet<ThreadGroup>());
    }

    @Test
    public void routesThreadsAndTestReadsThroughDistributedAggregator() {
        this.runtimeState.registerRemoteHost("worker-a");

        JSONObject threads = new JSONObject(new ThreadsResource().getThreads().getEntity().toString());
        JSONObject status = new JSONObject(new TestResource().getTestStatus().getEntity().toString());
        JSONObject summary = new JSONObject(new TestResource().getTestSummary().getEntity().toString());
        JSONObject errors = new JSONObject(new TestResource().getTestErrors().getEntity().toString());

        assertEquals("success", threads.getString("info"));
        assertEquals("success", status.getString("info"));
        assertEquals("success", summary.getString("info"));
        assertEquals("success", errors.getString("info"));
    }

    @Test
    public void preservesSingleNodeThreadReadShape() {
        ThreadGroup threadGroup = new ThreadGroup();
        threadGroup.setName("checkout");
        HashSet<ThreadGroup> threadGroups = new HashSet<>();
        threadGroups.add(threadGroup);
        LiveChanges.setTestThreadGroups(threadGroups);

        Response response = new ThreadsResource().getThreads();
        JSONArray jsonArray = new JSONArray(response.getEntity().toString());

        assertEquals(1, jsonArray.length());
        assertFalse(response.getEntity().toString().contains("\"workers\""));
    }

    @Test
    public void preservesSingleNodeStatusShape() {
        Response response = new TestResource().getTestStatus();
        JSONObject jsonObject = new JSONObject(response.getEntity().toString());

        assertTrue(jsonObject.has("startTime"));
        assertTrue(jsonObject.has("runningTime"));
        assertFalse(jsonObject.has("workers"));
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
                JSONObject child = new JSONObject();
                child.put("active", 3);
                JSONObject wrapped = new JSONObject();
                wrapped.put("checkout", child);
                array.put(wrapped);
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
