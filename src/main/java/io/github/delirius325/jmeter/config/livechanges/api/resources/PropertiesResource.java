package io.github.delirius325.jmeter.config.livechanges.api.resources;

import io.github.delirius325.jmeter.config.livechanges.LiveChanges;
import io.github.delirius325.jmeter.config.livechanges.utils.JSONHelper;
import org.json.JSONObject;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Base Endpoint for the PropertiesResource class
 */
@Path("/properties")
public class PropertiesResource {
    /**
     * GET Endpoint that returns all the properties and their values
     * @return A stringified JSONObject as the Response
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getVariables() {
        JSONObject json = new JSONObject();
        LiveChanges.getjMeterProperties().entrySet().forEach(entry -> {
            json.put(entry.getKey().toString(), entry.getValue().toString());
        });
        return Response.ok(json.toString()).build();
    }

    /**
     * POST Endpoint that allows the user to change properties' values
     * @param request - The JSON request of the user
     * @return A stringified JSONObject as the Response
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response postVariables(String request) {
        JSONObject json = new JSONObject(request);
        java.util.Properties props = LiveChanges.getjMeterProperties();
        props.entrySet().forEach(entry -> {
            if(json.has(entry.getKey().toString()) && (json.get(entry.getKey().toString()) != entry.getValue().toString()))  {
                json.put(entry.getKey().toString(), json.get(entry.getKey().toString()));
                props.put(entry.getKey(), json.get(entry.getKey().toString()));
            }
        });
        JSONHelper.jsonSetInfo(json, "success", "Properties were changed.");
        return Response.ok(json.toString()).build();
    }
}
