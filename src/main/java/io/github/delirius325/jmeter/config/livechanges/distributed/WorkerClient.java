package io.github.delirius325.jmeter.config.livechanges.distributed;

/**
 * Contract for forwarding controller-side commands to distributed workers.
 */
public interface WorkerClient {
    DistributedCommandResponse postJson(String host, int port, String path, String requestBody);

    DistributedCommandResponse get(String host, int port, String path);
}
