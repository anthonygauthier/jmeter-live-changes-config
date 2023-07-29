package io.github.delirius325.jmeter.config.livechanges.api.resources;

import io.github.delirius325.jmeter.config.livechanges.LiveChanges;
import io.github.delirius325.jmeter.config.livechanges.utils.JSONHelper;
import org.apache.jmeter.threads.JMeterVariables;
import org.json.JSONObject;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;


/**
 * Base endpoint for the VariablesResource Class
 */
@Path("/variables")
public class VariablesResource {
    /**
     * Endpoint that retrieves all the user defined variables
     * @return A stringified JSONObject as the Response
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getVariable() {
        JSONObject json = new JSONObject();
        LiveChanges.getjMeterVariables().entrySet().forEach(entry -> {
            json.put(entry.getKey(), entry.getValue().toString());
        });
        return Response.ok(json.toString()).build();
    }

    /**
     * Endpoint that retrieves a specific user defined variable
     * @param key The name of the user defined variable
     * @return A stringified JSONObject as the Response
     */
    @GET
    @Path("/{param}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getVariables(@PathParam("param") String key) {
        JSONObject json = new JSONObject();
        LiveChanges.getjMeterVariables().entrySet().forEach(entry -> {
            if(entry.getKey() == key) {
                json.put(entry.getKey(), entry.getValue().toString());
            }
        });
        return Response.ok(json.toString()).build();
    }

    /**
     * Endpoint that allows the user to modify user defined variables
     * @param request The body of the request as JSON
     * @return A stringified JSONObject as the Response
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response postVariables(String request) {
        JSONObject json = new JSONObject(request);
        JMeterVariables vars = LiveChanges.getjMeterVariables();
        vars.entrySet().forEach(entry -> {
            if(json.has(entry.getKey()) && (json.get(entry.getKey()) != entry.getValue().toString())) {
                json.put(entry.getKey(), json.get(entry.getKey()));
                vars.put(entry.getKey(), json.get(entry.getKey()).toString());
            }
        });
        LiveChanges.setjMeterVariables(vars);
        JSONHelper.jsonSetInfo(json, "success", "VariablesResource were changed.");
        return Response.ok(json.toString()).build();
    }
}


