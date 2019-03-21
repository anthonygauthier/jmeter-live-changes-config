package io.github.delirius325.jmeter.config.livechanges.api.resources;

import io.github.delirius325.jmeter.config.livechanges.LiveChanges;
import io.github.delirius325.jmeter.config.livechanges.ResultHolder;
import io.github.delirius325.jmeter.config.livechanges.SamplerMap;
import io.github.delirius325.jmeter.config.livechanges.helpers.JSONHelper;
import org.apache.jmeter.threads.JMeterContextService;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Map;

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

    @GET
    @Path("/summary")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getTestSummary() {
        JSONArray jsonArray = new JSONArray();
        SamplerMap samplerMap = LiveChanges.getSamplerMap();
        for(Map.Entry<String, ResultHolder> entry : samplerMap.getMap().entrySet()) {
            try {
                JSONObject parentObject = new JSONObject();
                JSONObject childObject = new JSONObject();
                ResultHolder resultHolder = entry.getValue();
                String sampleLabel = entry.getKey();

                // construct JSON objects and add to array
                childObject.put("averageBytes", resultHolder.getAvgBytes());
                childObject.put("averageLatency", resultHolder.getAvgLatency());
                childObject.put("averageResponseTime", resultHolder.getAvgResponseTime());
                childObject.put("errorPercentage", resultHolder.getErrorPercentage());
                childObject.put("hitsPerSecond", resultHolder.getHitsPerSecond());
                childObject.put("minResponseTime", resultHolder.getMinResponseTime());
                childObject.put("maxResponseTime", resultHolder.getMaxResponseTime());
                childObject.put("90thPercentile", resultHolder.getNinetyPercentile());
                childObject.put("standardDeviation", resultHolder.getStdDeviation());
                childObject.put("sentBytesPerSecond", resultHolder.getSentBytesPerSec());
                childObject.put("receivedBytesPerSecond", resultHolder.getReceivedBytesPerSec());
                childObject.put("totalSamples", resultHolder.getTotalSamples());
                childObject.put("totalErrors", resultHolder.getTotalErrors());
                childObject.put("totalBytes", resultHolder.getTotalBytes());
                childObject.put("totalSentBytes", resultHolder.getTotalSentBytes());
                childObject.put("timeRunning", (System.currentTimeMillis() - JMeterContextService.getTestStartTime()));
                parentObject.put(sampleLabel, childObject);
                jsonArray.put(parentObject);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return Response.ok(jsonArray.toString()).build();
    }
}