package io.github.delirius325.jmeter.config.livechanges.api.resources;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/test")
public class ConnectivityResource {
    @GET
    @Path("/connectivity")
    @Produces(MediaType.TEXT_PLAIN)
    public String getVariables(@PathParam("param") String name) {
        return "connected";
    }
}
