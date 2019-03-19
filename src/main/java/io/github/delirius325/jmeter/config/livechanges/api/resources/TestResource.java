package io.github.delirius325.jmeter.config.livechanges.api.resources;

import io.github.delirius325.jmeter.config.livechanges.LiveChanges;
import io.github.delirius325.jmeter.config.livechanges.utils.JSONHelper;
import org.apache.jmeter.threads.JMeterContextService;
import org.json.JSONObject;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Base Endpoint for the TestResource class
 */
@Path("/test")
public class TestResource {
    /**
     * GET Endpoint that retrieves the current status of the running test (startTime, runningTime, etc.)
     * @return A stringified JSONObject as the Response
     */
    @GET
    @Path("/status")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getTestStatus() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("startTime", JMeterContextService.getTestStartTime());
        jsonObject.put("runningTime", System.currentTimeMillis() - JMeterContextService.getTestStartTime());
        jsonObject.put("totalActiveThreads", JMeterContextService.getNumberOfThreads());
        jsonObject.put("totalThreads", JMeterContextService.getTotalThreads());
        return Response.ok(jsonObject.toString()).build();
    }

    /**
     * GET Endpoint that allows the user to stop the test
     * @return A stringified JSONObject as the Response
     */
    @GET
    @Path("/end")
    @Produces(MediaType.APPLICATION_JSON)
    public Response endTestRun() {
        JSONObject jsonObject = new JSONObject();
        LiveChanges.setStopTest(true);
        JSONHelper.jsonSetInfo(jsonObject, "success", "Threads will gracefully stop.");
        return Response.ok(jsonObject.toString()).build();
    }
}