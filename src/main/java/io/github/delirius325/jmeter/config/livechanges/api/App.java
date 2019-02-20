package io.github.delirius325.jmeter.config.livechanges.api;

import io.github.delirius325.jmeter.config.livechanges.LiveChanges;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.glassfish.jersey.servlet.ServletContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class App {
    private static final Logger logger = LoggerFactory.getLogger(LiveChanges.class);

    private Server server;
    private int port;

    public App(int p) {
        this.port = p;
        this.server = new Server(this.port);
    }

    public void start() {
        ServletContextHandler servletContextHandler = new ServletContextHandler(ServletContextHandler.NO_SESSIONS);
        servletContextHandler.setContextPath("/");
        this.server.setHandler(servletContextHandler);
        ServletHolder servletHolder = servletContextHandler.addServlet(ServletContainer.class, "/api/*");
        servletHolder.setInitOrder(0);
        servletHolder.setInitParameter(
                "jersey.config.server.provider.packages",
                "io.github.delirius325.jmeter.config.livechanges.api.resources"
        );
        try {
            server.start();
        } catch (Exception e) {
            logger.error("Error occurred while starting embedded Jetty server", e);
            System.exit(1);
        }
    }

    public void stop() throws Exception {
        this.server.stop();
        this.server.destroy();
    }
}