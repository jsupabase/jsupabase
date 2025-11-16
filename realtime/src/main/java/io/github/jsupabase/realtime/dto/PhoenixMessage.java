package io.github.jsupabase.realtime.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

/**
 * Data Transfer Object (DTO) for the Phoenix Realtime protocol.
 * This class represents the JSON structure sent and received
 * over the WebSocket.
 *
 * @author neilhdezs
 * @version 0.1.0
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PhoenixMessage {

    /** - The channel topic (e.g., "realtime:public:todos") - **/
    @JsonProperty("topic")
    private final String topic;

    /** - The event name (e.g., "phx_join", "INSERT", "heartbeat") - **/
    @JsonProperty("event")
    private final String event;

    /** - The JSON payload of the message - **/
    @JsonProperty("payload")
    private final Map<String, Object> payload;

    /** - The unique reference number for the message - **/
    @JsonProperty("ref")
    private final String ref;

    /**
     * Constructor for creating messages to send.
     */
    public PhoenixMessage(String topic, String event, Map<String, Object> payload, String ref) {
        this.topic = topic;
        this.event = event;
        this.payload = payload;
        this.ref = ref;
    }

    /**
     * Default constructor for Jackson deserialization (receiving messages).
     */
    private PhoenixMessage() {
        this.topic = null;
        this.event = null;
        this.payload = null;
        this.ref = null;
    }

    // --- Getters ---

    public String getTopic() {
        return topic;
    }

    public String getEvent() {
        return event;
    }

    public Map<String, Object> getPayload() {
        return payload;
    }

    public String getRef() {
        return ref;
    }
}