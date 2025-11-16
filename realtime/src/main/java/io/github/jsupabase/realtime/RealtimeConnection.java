package io.github.jsupabase.realtime;

import io.github.jsupabase.core.client.WebSocketClientBase;
import io.github.jsupabase.core.config.SupabaseConfig;
import io.github.jsupabase.core.enums.ConnectionState;
import io.github.jsupabase.core.exceptions.RealtimeConnectionException;
import io.github.jsupabase.core.util.JsonUtil;
import io.github.jsupabase.realtime.dto.PhoenixMessage;
import io.github.jsupabase.realtime.utils.RealtimeConstants;
import io.github.jsupabase.realtime.utils.RealtimePaths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * - REALTIME WEBSOCKET CONNECTION MANAGER -
 * <p>
 * Manages the underlying WebSocket connection and implements the Phoenix Channels protocol
 * for Supabase Realtime. This class serves as the central hub for all real-time communication,
 * handling message routing, channel lifecycle management, and authentication token updates.
 *
 * <h3>Architecture Role:</h3>
 * This class extends WebSocketClientBase to inherit low-level WebSocket functionality
 * (connection, reconnection, heartbeat) and adds Phoenix Channels protocol implementation
 * on top. It acts as a singleton connection manager shared across all channels.
 *
 * <h3>Phoenix Protocol Implementation:</h3>
 * The connection manages:
 * <ul>
 * <li><b>Message Routing</b> - Dispatches incoming messages to appropriate channels based on topic</li>
 * <li><b>Reference Generation</b> - Thread-safe atomic counter for message correlation (refs)</li>
 * <li><b>Channel Registry</b> - Thread-safe map tracking all active channel subscriptions</li>
 * <li><b>Authentication</b> - JWT token management and propagation to all channels</li>
 * </ul>
 *
 * <h3>Thread-Safety:</h3>
 * All operations are thread-safe using:
 * <ul>
 * <li><b>ConcurrentHashMap</b> - For channel registry (allows concurrent reads/writes)</li>
 * <li><b>AtomicLong</b> - For reference counter (lock-free atomic increment)</li>
 * <li><b>Volatile fields</b> - For JWT token visibility across threads</li>
 * </ul>
 *
 * <h3>Message Flow:</h3>
 * <pre>
 * Server → onMessage() → deserialize to PhoenixMessage
 *                      → extract topic
 *                      → find matching channel
 *                      → trigger channel event handlers
 * </pre>
 *
 * @author neilhdezs
 * @version 0.1.0
 * @since 0.1.0
 */
public class RealtimeConnection extends WebSocketClientBase {

    /** - Logger for WebSocket connection events and Phoenix protocol operations - */
    private static final Logger LOGGER = LoggerFactory.getLogger(RealtimeConnection.class);

    /** - Thread-safe registry of active channel subscriptions indexed by topic identifier - */
    private final Map<String, RealtimeChannelBuilder> channels = new ConcurrentHashMap<>();

    /** - Thread-safe atomic counter for generating unique message reference IDs for request-response correlation - */
    private final AtomicLong refCounter = new AtomicLong(0);

    /** - Current JWT authentication token, volatile for thread-safe visibility when updated via setAuth() - */
    private volatile String jwt;

    /**
     * - REALTIME CONNECTION CONSTRUCTOR -
     * <p>
     * Initializes the WebSocket connection manager and establishes connection to Supabase Realtime.
     * The constructor inherits WebSocket lifecycle management from WebSocketClientBase and adds
     * Phoenix Channels protocol implementation on top.
     * <p>
     * The JWT token is initialized with the anonymous API key from config and can be updated
     * later via setAuth() when user authentication state changes.
     *
     * @param config     Supabase configuration containing WebSocket URL and authentication settings
     * @param httpClient Shared HTTP client instance for connection management and heartbeat
     */
    public RealtimeConnection(SupabaseConfig config, HttpClient httpClient) {
        super(config, httpClient);
        this.jwt = config.getSupabaseKey();
        LOGGER.info("Initializing RealtimeConnection with WebSocket endpoint");
        connect();
        LOGGER.info("WebSocket connection initiated");
    }

    /**
     * - CHANNEL FACTORY METHOD -
     * <p>
     * Creates or retrieves a channel builder for the specified topic. This method uses
     * computeIfAbsent to ensure thread-safe lazy initialization - only one channel instance
     * is created per topic even with concurrent access.
     * <p>
     * Called by RealtimeClient.channel() to provide channel builders to application code.
     *
     * @param topic Channel topic identifier (e.g., "realtime:public:my_table")
     * @return RealtimeChannelBuilder instance for the specified topic (new or existing)
     */
    public RealtimeChannelBuilder channel(String topic) {
        return channels.computeIfAbsent(topic, t -> {
            LOGGER.debug(RealtimeConstants.LOG_CHANNEL_ADDED, topic);
            return new RealtimeChannelBuilder(t, this);
        });
    }

    /**
     * - AUTHENTICATION TOKEN UPDATE -
     * <p>
     * Updates the JWT authentication token for this connection and triggers re-authentication
     * of all active channel subscriptions. This method is called by SupabaseClient when user
     * authentication state changes (sign in, sign out, token refresh).
     * <p>
     * Instead of closing and reopening the WebSocket connection, this method keeps the
     * connection alive and simply re-joins all channels with the new authentication context.
     * This provides seamless auth transitions without disconnecting active subscriptions.
     *
     * @param jwt New JWT access token for authenticated operations
     */
    public void setAuth(String jwt) {
        this.jwt = jwt;
        LOGGER.info("Auth token updated, re-joining {} active channels", channels.size());

        for (RealtimeChannelBuilder channelBuilder : channels.values()) {
            channelBuilder.rejoinWithNewAuth();
        }

        LOGGER.info("All channels rejoined with new authentication");
    }

    /**
     * - CHANNEL REMOVAL -
     * <p>
     * Removes a specific channel from the active registry and unsubscribes it from the server.
     * This method performs cleanup of channel resources and sends a phx_leave message to
     * the server to properly terminate the subscription.
     * <p>
     * Called by RealtimeClient.removeChannel() to clean up individual channels.
     *
     * @param channel Channel builder instance to remove and unsubscribe
     */
    public void removeChannel(RealtimeChannelBuilder channel) {
        if (channel != null) {
            String topic = channel.getTopic();
            channels.remove(topic);
            channel.unsubscribe();
            LOGGER.debug(RealtimeConstants.LOG_CHANNEL_REMOVED, topic);
        }
    }

    /**
     * - ALL CHANNELS REMOVAL -
     * <p>
     * Removes all active channels from the registry and unsubscribes them from the server.
     * This method is useful for cleanup operations during application shutdown or when
     * reinitializing the real-time connection with different configurations.
     * <p>
     * Called by RealtimeClient.removeAllChannels() for bulk cleanup.
     */
    public void removeAllChannels() {
        LOGGER.info("Removing all {} active channels", channels.size());

        for (RealtimeChannelBuilder channel : channels.values()) {
            channel.unsubscribe();
        }
        channels.clear();

        LOGGER.info("All channels removed and unsubscribed");
    }

    /**
     * - JWT TOKEN ACCESS -
     * <p>
     * Returns the current JWT authentication token for this connection. This method is
     * package-private and used by RealtimeChannelBuilder to include authentication tokens
     * in phx_join messages when subscribing to channels.
     * <p>
     * The token may be the anonymous API key or a user session token depending on
     * authentication state.
     *
     * @return Current JWT access token (API key or user token)
     */
    String getJwt() {
        return this.jwt;
    }

    /**
     * - REFERENCE COUNTER -
     * <p>
     * Generates the next unique message reference ID using a thread-safe atomic counter.
     * Each message sent to the server must have a unique ref for request-response correlation.
     * <p>
     * This method is package-private and used by RealtimeChannelBuilder when constructing
     * Phoenix protocol messages (phx_join, phx_leave, broadcast, etc.).
     * <p>
     * The AtomicLong ensures thread-safe increments without synchronization overhead,
     * allowing concurrent channel operations to generate unique refs safely.
     *
     * @return Unique message reference as string (e.g., "1", "2", "3"...)
     */
    String getNextRef() {
        return String.valueOf(refCounter.incrementAndGet());
    }

    /**
     * - MESSAGE SENDER -
     * <p>
     * Serializes a Phoenix protocol message DTO to JSON and sends it through the WebSocket
     * connection. This method is package-private and used by RealtimeChannelBuilder to send
     * protocol messages like phx_join, phx_leave, and broadcast events.
     * <p>
     * The message is serialized using JsonUtil and sent via sendText() inherited from
     * WebSocketClientBase. If serialization fails, an exception is thrown.
     *
     * @param phoenixMessageDto Phoenix protocol message DTO to serialize and send
     * @throws RealtimeConnectionException if message serialization or sending fails
     */
    void push(Object phoenixMessageDto) {
        try {
            String json = JsonUtil.toJson(phoenixMessageDto);
            LOGGER.trace(RealtimeConstants.LOG_MESSAGE_SENT, json);
            sendText(json);
        } catch (Exception e) {
            LOGGER.error(RealtimeConstants.ERROR_SERIALIZATION_FAILED, e);
            throw new RealtimeConnectionException(RealtimeConstants.ERROR_SEND_FAILED, e);
        }
    }

    // --- Implementation of Abstract WebSocketClientBase Methods ---

    /**
     * - WEBSOCKET URI BUILDER -
     * <p>
     * Constructs the complete WebSocket URI for connecting to Supabase Realtime service.
     * This method implements the abstract method from WebSocketClientBase to provide
     * the Realtime-specific connection URL.
     *
     * <h3>URL Construction:</h3>
     * Transforms HTTP(S) URL to WS(S) URL and appends WebSocket endpoint with query parameters:
     * <pre>
     * <a href="https://id.supabase.co">https://id.supabase.co</a> → wss://id.supabase.co/realtime/v1/websocket?apikey=xxx&vsn=2.0.0
     * </pre>
     * <p>
     * Uses RealtimePaths utility to construct the path following Phoenix Channels conventions.
     *
     * @return Complete WebSocket URI for Supabase Realtime connection
     */
    @Override
    protected URI buildConnectionUri() {
        String servicePath = config.getRealtimePath();
        String apikey = config.getSupabaseKey();

        String path = RealtimePaths.getWebSocketPath(
                servicePath,
                apikey,
                RealtimeConstants.PHOENIX_PROTOCOL_VERSION
        );

        URI wsUri = URI.create(config.getSupabaseUrl().toString()
                .replaceFirst("http", "ws")
                + path);

        LOGGER.debug(RealtimeConstants.LOG_WEBSOCKET_OPENED, wsUri);
        return wsUri;
    }

    /**
     * - MESSAGE HANDLER -
     * <p>
     * Processes incoming WebSocket messages by deserializing them as Phoenix protocol messages
     * and routing them to the appropriate channel based on topic. This method implements the
     * abstract method from WebSocketClientBase.
     *
     * <h3>Message Flow:</h3>
     * <pre>
     * 1. Deserialize JSON string → PhoenixMessage DTO
     * 2. Extract topic from message
     * 3. Find matching channel in registry
     * 4. Delegate to channel.trigger() for event handling
     * </pre>
     * <p>
     * If deserialization fails or no channel exists for the topic, the message is logged
     * and discarded to prevent breaking other channel operations.
     *
     * @param message Raw JSON message string received from WebSocket
     */
    @Override
    protected void onMessage(String message) {
        LOGGER.trace(RealtimeConstants.LOG_MESSAGE_RECEIVED, message);

        try {
            PhoenixMessage msg = JsonUtil.fromJson(message, PhoenixMessage.class);
            RealtimeChannelBuilder channel = channels.get(msg.getTopic());

            if (channel != null) {
                channel.trigger(msg.getEvent(), msg.getPayload(), msg.getRef());
            } else {
                LOGGER.debug(RealtimeConstants.LOG_NO_CHANNEL_FOR_TOPIC, msg.getTopic());
            }
        } catch (Exception e) {
            LOGGER.error(RealtimeConstants.ERROR_DESERIALIZATION_FAILED, e);
        }
    }

    /**
     * - HEARTBEAT PAYLOAD GENERATOR -
     * <p>
     * Generates the Phoenix Channels heartbeat message to keep the WebSocket connection alive.
     * This method implements the abstract method from WebSocketClientBase and is called
     * periodically by the inherited heartbeat mechanism.
     *
     * <h3>Phoenix Heartbeat Protocol:</h3>
     * The heartbeat message follows Phoenix Channels specification:
     * <pre>{@code
     * {
     *   "topic": "phoenix",
     *   "event": "heartbeat",
     *   "payload": {},
     *   "ref": "unique_ref_id"
     * }
     * }</pre>
     * <p>
     * The server responds to heartbeats to confirm the connection is still alive.
     * If heartbeats fail, WebSocketClientBase triggers reconnection logic.
     *
     * @return JSON string containing the Phoenix heartbeat message
     */
    @Override
    protected String getHeartbeatPayload() {
        PhoenixMessage heartbeatMsg = new PhoenixMessage(
                RealtimePaths.topicPhoenix(),
                RealtimePaths.eventHeartbeat(),
                Map.of(),
                getNextRef()
        );
        return JsonUtil.toJson(heartbeatMsg);
    }

    /**
     * - CONNECTION STATE CHANGE HANDLER -
     * <p>
     * Handles WebSocket connection state transitions and triggers appropriate recovery actions.
     * This method implements the abstract method from WebSocketClientBase and is called
     * whenever the connection state changes (CONNECTING, OPEN, CLOSED, etc.).
     *
     * <h3>Automatic Reconnection:</h3>
     * When the connection transitions to OPEN state after a disconnection, this method
     * automatically re-subscribes all active channels to restore subscriptions. This
     * provides transparent reconnection without requiring application code to handle it.
     *
     * <h3>State Transitions:</h3>
     * <ul>
     * <li><b>CONNECTING</b> - Initial connection attempt or reconnection in progress</li>
     * <li><b>OPEN</b> - Connection established → trigger channel resubscription</li>
     * <li><b>CLOSED</b> - Connection closed, will attempt reconnection if configured</li>
     * <li><b>ERROR</b> - Connection error occurred, reconnection pending</li>
     * </ul>
     *
     * @param newState New connection state after transition
     */
    @Override
    protected void onStateChange(ConnectionState newState) {
        LOGGER.info("RealtimeConnection state changed to: {}", newState);

        if (newState == ConnectionState.OPEN) {
            LOGGER.info("Connection opened, re-subscribing {} channels", channels.size());

            for (RealtimeChannelBuilder channelBuilder : channels.values()) {
                channelBuilder.resubscribe();
            }

            LOGGER.info("All channels re-subscribed after reconnection");
        }
    }
}