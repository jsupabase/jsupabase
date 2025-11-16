package io.github.jsupabase.realtime.utils;

/**
 * - REALTIME MODULE CONSTANTS -
 * <p>
 * Centralized constant definitions for the Supabase Realtime module including
 * WebSocket configuration, protocol parameters, error messages, and log messages.
 * This class follows the utility class pattern and cannot be instantiated.
 * <p>
 * Centralizing constants improves maintainability, reduces magic strings in code,
 * and provides a single source of truth for configuration values across the module.
 *
 * @author neilhdezs
 * @version 0.1.0
 * @since 0.1.0
 */
public final class RealtimeConstants {

    /** - Default Phoenix protocol version for WebSocket connections - */
    public static final String PHOENIX_PROTOCOL_VERSION = "1.0.0";

    // ==================== WEBSOCKET CONFIGURATION ====================
    /** - WebSocket endpoint path suffix - */
    public static final String WEBSOCKET_PATH = "/websocket";
    /** - Query parameter name for API key - */
    public static final String APIKEY_PARAM = "apikey";
    /** - Query parameter name for protocol version - */
    public static final String VSN_PARAM = "vsn";
    /** - Default subscription status when channel successfully joins - */
    public static final String STATUS_SUBSCRIBED = "SUBSCRIBED";

    // ==================== CHANNEL SUBSCRIPTION ====================
    /** - Subscription status when channel join fails - */
    public static final String STATUS_ERROR = "ERROR";
    /** - Phoenix Channels reply status for successful operations - */
    public static final String REPLY_STATUS_OK = "ok";
    /** - Phoenix Channels reply status for failed operations - */
    public static final String REPLY_STATUS_ERROR = "error";
    /** - Log message when channel is already joining or joined - */
    public static final String LOG_CHANNEL_ALREADY_JOINING = "Channel {}: Already joining or joined";

    // ==================== LOG MESSAGES ====================
    /** - Log message when attempting to send on non-joined channel - */
    public static final String LOG_CANNOT_SEND_NOT_JOINED = "Cannot send message, channel {} is not joined";
    /** - Log message when ref mismatch is detected - */
    public static final String LOG_REF_MISMATCH = "Received reply with mismatched ref. Expected: {}, Got: {}";
    /** - Log message when postgres_changes data is invalid - */
    public static final String LOG_INVALID_POSTGRES_DATA = "Expected Map for postgres_changes data, got: {}";
    /** - Log message when postgres_changes type field is missing - */
    public static final String LOG_MISSING_TYPE_FIELD = "postgres_changes event missing or invalid type field";
    /** - Log message when listener throws exception - */
    public static final String LOG_LISTENER_ERROR = "Error in {} listener for {}";
    /** - Log message when WebSocket connection is established - */
    public static final String LOG_WEBSOCKET_OPENED = "WebSocket connection opened to: {}";
    /** - Log message when WebSocket connection closes - */
    public static final String LOG_WEBSOCKET_CLOSED = "WebSocket connection closed. Code: {}, Reason: {}";
    /** - Log message when WebSocket error occurs - */
    public static final String LOG_WEBSOCKET_ERROR = "WebSocket error occurred";
    /** - Log message when message is received - */
    public static final String LOG_MESSAGE_RECEIVED = "Received message: {}";
    /** - Log message when message is sent - */
    public static final String LOG_MESSAGE_SENT = "Sent message: {}";
    /** - Log message when channel is added - */
    public static final String LOG_CHANNEL_ADDED = "Channel added: {}";
    /** - Log message when channel is removed - */
    public static final String LOG_CHANNEL_REMOVED = "Channel removed: {}";
    /** - Log message when no channel found for topic - */
    public static final String LOG_NO_CHANNEL_FOR_TOPIC = "No channel found for topic: {}";
    /** - Error message when WebSocket connection fails - */
    public static final String ERROR_CONNECTION_FAILED = "Failed to establish WebSocket connection to Supabase Realtime";

    // ==================== ERROR MESSAGES ====================
    /** - Error message when message deserialization fails - */
    public static final String ERROR_DESERIALIZATION_FAILED = "Failed to deserialize Phoenix message";
    /** - Error message when message serialization fails - */
    public static final String ERROR_SERIALIZATION_FAILED = "Failed to serialize Phoenix message";
    /** - Error message when sending message fails - */
    public static final String ERROR_SEND_FAILED = "Failed to send message through WebSocket";
    /** - Error message when subscription fails - */
    public static final String ERROR_SUBSCRIPTION_FAILED = "Failed to subscribe to channel: {}";
    /** - Error message when channel join times out - */
    public static final String ERROR_JOIN_TIMEOUT = "Channel join timed out for topic: {}";
    /** - Payload key for status field in Phoenix replies - */
    public static final String PAYLOAD_KEY_STATUS = "status";

    // ==================== PAYLOAD KEYS ====================
    /** - Payload key for data field in postgres_changes events - */
    public static final String PAYLOAD_KEY_DATA = "data";
    /** - Payload key for type field in postgres_changes data - */
    public static final String PAYLOAD_KEY_TYPE = "type";
    /** - Payload key for config field in join messages - */
    public static final String PAYLOAD_KEY_CONFIG = "config";
    /** - Payload key for access_token field in join messages - */
    public static final String PAYLOAD_KEY_ACCESS_TOKEN = "access_token";
    /** - Payload key for event field in broadcast messages - */
    public static final String PAYLOAD_KEY_EVENT = "event";
    /** - Payload key for payload field in messages - */
    public static final String PAYLOAD_KEY_PAYLOAD = "payload";
    /** - Payload key for self field in broadcast config - */
    public static final String PAYLOAD_KEY_SELF = "self";
    /** - Payload key for key field in presence config - */
    public static final String PAYLOAD_KEY_KEY = "key";
    /** - Default value for broadcast self field (don't receive own messages) - */
    public static final boolean DEFAULT_BROADCAST_SELF = false;

    // ==================== CONFIGURATION DEFAULTS ====================
    /** - Default value for presence key field - */
    public static final String DEFAULT_PRESENCE_KEY = "";

    /**
     * - PRIVATE CONSTRUCTOR -
     * <p>
     * Prevents instantiation of this utility class.
     */
    private RealtimeConstants() {
        throw new IllegalStateException("RealtimeConstants is a utility class and cannot be instantiated");
    }
}

