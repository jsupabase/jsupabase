package io.github.jsupabase.realtime.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

/**
 * - PHOENIX CHANNELS PROTOCOL MESSAGE -
 * <p>
 * Data Transfer Object representing messages in the Phoenix Channels protocol used by
 * Supabase Realtime. This class encapsulates the structure of WebSocket messages that
 * are sent to and received from the Supabase Realtime server.
 *
 * <h3>Phoenix Channels Protocol Structure:</h3>
 * According to the Phoenix Channels specification, messages follow this JSON object format:
 * <pre>{@code
 * {
 *   "topic": "realtime:public:todos",
 *   "event": "phx_join",
 *   "payload": { "config": {...}, "access_token": "..." },
 *   "ref": "1"
 * }
 * }</pre>
 *
 * <h3>Message Components:</h3>
 * <ul>
 * <li><b>topic</b> - Channel identifier (e.g., "realtime:public:table_name")</li>
 * <li><b>event</b> - Event type ("phx_join", "phx_leave", "postgres_changes", "broadcast", etc.)</li>
 * <li><b>payload</b> - Event-specific data and configuration</li>
 * <li><b>ref</b> - Unique message reference for request-response correlation</li>
 * </ul>
 *
 * <h3>Serialization:</h3>
 * This class uses Jackson annotations for JSON serialization/deserialization. The message
 * is serialized as a JSON object (not array) which is then sent over the WebSocket connection.
 *
 * @author neilhdezs
 * @version 0.1.0
 * @since 0.1.0
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PhoenixMessage {

    /** - Channel topic identifier following Phoenix Channels naming convention (e.g., "realtime:public:todos") - */
    @JsonProperty("topic")
    private final String topic;

    /** - Event type identifying the message purpose (e.g., "phx_join", "postgres_changes", "broadcast") - */
    @JsonProperty("event")
    private final String event;

    /** - Message payload containing event-specific data, configuration, or authentication tokens - */
    @JsonProperty("payload")
    private final Map<String, Object> payload;

    /** - Unique message reference for correlating server responses with client requests - */
    @JsonProperty("ref")
    private final String ref;

    /**
     * - PHOENIX MESSAGE CONSTRUCTOR -
     * <p>
     * Creates a new Phoenix Channels protocol message for sending to the server.
     *
     * @param topic   Channel topic identifier
     * @param event   Event type string
     * @param payload Event data and configuration
     * @param ref     Unique message reference for response correlation
     */
    public PhoenixMessage(String topic, String event, Map<String, Object> payload, String ref) {
        this.topic = topic;
        this.event = event;
        this.payload = payload;
        this.ref = ref;
    }

    /**
     * - DEFAULT CONSTRUCTOR FOR DESERIALIZATION -
     * <p>
     * Private constructor used by Jackson for deserializing incoming messages from the server.
     */
    private PhoenixMessage() {
        this.topic = null;
        this.event = null;
        this.payload = null;
        this.ref = null;
    }

    /** - Returns the channel topic this message is associated with - */
    public String getTopic() {
        return topic;
    }

    /** - Returns the event type of this message - */
    public String getEvent() {
        return event;
    }

    /** - Returns the message payload containing event-specific data - */
    public Map<String, Object> getPayload() {
        return payload;
    }

    /** - Returns the unique message reference for response correlation - */
    public String getRef() {
        return ref;
    }
}