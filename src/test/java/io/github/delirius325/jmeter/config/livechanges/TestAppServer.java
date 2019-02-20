package io.github.delirius325.jmeter.config.livechanges;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import io.github.delirius325.jmeter.config.livechanges.api.App;

import static junit.framework.TestCase.assertTrue;

public class TestAppServer {
    private static int PORT = 8080;
    private static String APP_ADDRESS = "http://localhost:" + PORT;

    private App app;

    @Before
    public void startTest() throws Exception {
        this.app = new App(PORT);
        this.app.start();
    }

    @Test
    public void testServerConnectivity() {
        try {
            HttpResponse<String> response = Unirest.get(APP_ADDRESS + "/api/test/connectivity").asString();
            assertTrue(response.getBody().contains("connected"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @After
    public void stopTest() throws Exception {
        this.app.stop();
    }
}
