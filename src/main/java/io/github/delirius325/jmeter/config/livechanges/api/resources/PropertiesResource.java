package io.github.delirius325.jmeter.config.livechanges.api.resources;

import io.github.delirius325.jmeter.config.livechanges.LiveChanges;
import io.github.delirius325.jmeter.config.livechanges.utils.JSONUtils;
import org.json.JSONObject;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/properties")
public class PropertiesResource {
    @GET
    @Path("/{param}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getVariables(@PathParam("param") String name) {
        JSONObject json = new JSONObject();
        LiveChanges.getjMeterProperties().entrySet().forEach(entry -> {
            json.put(entry.getKey().toString(), entry.getValue().toString());
        });
        return Response.ok(json.toString()).build();
    }

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
        JSONUtils.jsonSetInfo(json, "success", "Properties were changed.");
        return Response.ok(json.toString()).build();
    }
}
