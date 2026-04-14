package io.github.delirius325.jmeter.config.livechanges.distributed;

import org.json.JSONObject;

/**
 * Represents one worker's response to a distributed command.
 */
public class DistributedCommandResponse {
    private final int statusCode;
    private final JSONObject body;
    private final String failureReason;

    /**
     * Constructor
     * @param statusCode int
     * @param body JSONObject
     * @param failureReason String
     */
    private DistributedCommandResponse(int statusCode, JSONObject body, String failureReason) {
        this.statusCode = statusCode;
        this.body = body;
        this.failureReason = failureReason;
    }

    /**
     * Factory method for successful worker responses
     * @param statusCode int
     * @param body JSONObject
     * @return DistributedCommandResponse
     */
    public static DistributedCommandResponse success(int statusCode, JSONObject body) {
        return new DistributedCommandResponse(statusCode, body, null);
    }

    /**
     * Factory method for failed worker responses
     * @param statusCode int
     * @param failureReason String
     * @return DistributedCommandResponse
     */
    public static DistributedCommandResponse failure(int statusCode, String failureReason) {
        return new DistributedCommandResponse(statusCode, new JSONObject(), failureReason);
    }

    /**
     * Getter for the HTTP status code
     * @return int
     */
    public int getStatusCode() {
        return this.statusCode;
    }

    /**
     * Getter for the worker response body
     * @return JSONObject
     */
    public JSONObject getBody() {
        return this.body;
    }

    /**
     * Getter for the failure reason
     * @return String
     */
    public String getFailureReason() {
        return this.failureReason;
    }

    /**
     * Indicates whether the worker response should be treated as successful
     * @return boolean
     */
    public boolean isSuccess() {
        return this.failureReason == null && this.statusCode >= 200 && this.statusCode < 300
                && !"error".equalsIgnoreCase(this.body.optString("info"));
    }

    /**
     * Returns the normalized info field for the response
     * @return String
     */
    public String getInfo() {
        if (this.body.has("info")) {
            return this.body.optString("info");
        }
        return this.isSuccess() ? "success" : "error";
    }

    /**
     * Returns either the failure reason or the body description
     * @return String
     */
    public String getDescription() {
        if (this.failureReason != null && !this.failureReason.isEmpty()) {
            return this.failureReason;
        }
        return this.body.optString("description");
    }
}
