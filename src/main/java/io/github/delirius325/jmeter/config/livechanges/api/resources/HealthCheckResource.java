package io.github.delirius325.jmeter.config.livechanges.api.resources;

import io.github.delirius325.jmeter.config.livechanges.LiveChanges;
import io.github.delirius325.jmeter.config.livechanges.RuntimeState;
import org.json.JSONObject;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Base endpoint for the ConnectivityResource class
 */
@Path("/healthcheck")
public class HealthCheckResource {
    /**
     * A simple endpoint testing the connectivity to the API
     * @return returns the string "connected" if successful
     */
    @GET
    @Produces({MediaType.TEXT_PLAIN, MediaType.APPLICATION_JSON})
    public Response getConnected() {
        RuntimeState runtimeState = LiveChanges.getRuntimeState();
        if (runtimeState.hasStartupFailure()) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("info", "error");
            jsonObject.put("description", runtimeState.getStartupFailureMessage());
            jsonObject.put("executionMode", runtimeState.getExecutionMode().name());
            jsonObject.put("remoteHosts", runtimeState.getRemoteHosts());
            return Response.status(Response.Status.SERVICE_UNAVAILABLE)
                    .type(MediaType.APPLICATION_JSON)
                    .entity(jsonObject.toString())
                    .build();
        }

        return Response.ok("connected", MediaType.TEXT_PLAIN).build();
    }
}
