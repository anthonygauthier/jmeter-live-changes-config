package io.github.delirius325.jmeter.config.livechanges.api.resources;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * Base endpoint for the ConnectivityResource class
 */
@Path("/test")
public class ConnectivityResource {
    /**
     * A simple endpoint testing the connectivity to the API
     * @return returns the string "connected" if successful
     */
    @GET
    @Path("/connectivity")
    @Produces(MediaType.TEXT_PLAIN)
    public String getConnected() {
        return "connected";
    }
}
