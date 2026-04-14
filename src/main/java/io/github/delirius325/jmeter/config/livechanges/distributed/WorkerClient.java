package io.github.delirius325.jmeter.config.livechanges.distributed;

/**
 * Contract for forwarding controller-side commands to distributed workers.
 */
public interface WorkerClient {
    /**
     * Sends a JSON POST request to a worker host
     * @param host String
     * @param port int
     * @param path String
     * @param requestBody String
     * @return DistributedCommandResponse
     */
    DistributedCommandResponse postJson(String host, int port, String path, String requestBody);

    /**
     * Sends a GET request to a worker host
     * @param host String
     * @param port int
     * @param path String
     * @return DistributedCommandResponse
     */
    DistributedCommandResponse get(String host, int port, String path);
}
