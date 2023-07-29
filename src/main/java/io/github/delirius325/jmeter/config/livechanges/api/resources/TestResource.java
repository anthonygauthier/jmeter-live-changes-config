package io.github.delirius325.jmeter.config.livechanges.api.resources;

import io.github.delirius325.jmeter.config.livechanges.LiveChanges;
import io.github.delirius325.jmeter.config.livechanges.ResultHolder;
import io.github.delirius325.jmeter.config.livechanges.SamplerMap;
import io.github.delirius325.jmeter.config.livechanges.helpers.GenericHelper;
import io.github.delirius325.jmeter.config.livechanges.helpers.JSONHelper;
import org.apache.jmeter.threads.JMeterContextService;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.text.DecimalFormat;
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
        jsonObject.put("runningTime", GenericHelper.getTestTimeElapsedSec());
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
                DecimalFormat df = new DecimalFormat("#.##");
                JSONObject parentObject = new JSONObject();
                JSONObject childObject = new JSONObject();
                ResultHolder resultHolder = entry.getValue();
                String sampleLabel = resultHolder.getCalculator().getLabel();

                // construct JSON objects and add to array
                childObject.put("averageBytes", df.format(resultHolder.getCalculator().getAvgPageBytes()));
                childObject.put("averageLatency", df.format(resultHolder.getAvgLatency()));
                childObject.put("averageResponseTime", df.format(resultHolder.getCalculator().getMeanAsNumber()));
                childObject.put("errorPercentage", df.format(resultHolder.getCalculator().getErrorPercentage()));
                childObject.put("hitsPerSecond", df.format(resultHolder.getCalculator().getRate()));
                childObject.put("minResponseTime", df.format(resultHolder.getCalculator().getMin()));
                childObject.put("maxResponseTime", df.format(resultHolder.getCalculator().getMax()));
                childObject.put("90thPercentile", df.format(resultHolder.getNinetiethPercentile()));
                childObject.put("standardDeviation", df.format(resultHolder.getCalculator().getStandardDeviation()));
                childObject.put("sentBytesPerSecond", df.format(resultHolder.getCalculator().getSentBytesPerSecond()));
                childObject.put("receivedBytesPerSecond", df.format(resultHolder.getCalculator().getBytesPerSecond()));
                childObject.put("totalSamples", df.format(resultHolder.getCalculator().getCount()));
                childObject.put("totalErrors", df.format(resultHolder.getTotalErrors()));
                childObject.put("totalBytes", df.format(resultHolder.getCalculator().getTotalBytes()));
                parentObject.put(sampleLabel, childObject);
                jsonArray.put(parentObject);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return Response.ok(jsonArray.toString()).build();
    }
}