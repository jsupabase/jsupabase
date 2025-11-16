package io.github.jsupabase.realtime;

import io.github.jsupabase.core.exceptions.RealtimeSubscriptionException;
import io.github.jsupabase.realtime.dto.BroadcastEventFilter;
import io.github.jsupabase.realtime.dto.PhoenixMessage;
import io.github.jsupabase.realtime.dto.PresenceEventFilter;
import io.github.jsupabase.realtime.dto.options.PostgresChangesFilter;
import io.github.jsupabase.realtime.enums.ChannelState;
import io.github.jsupabase.realtime.utils.RealtimeConstants;
import io.github.jsupabase.realtime.utils.RealtimePaths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * - REAL-TIME CHANNEL SUBSCRIPTION BUILDER -
 * <p>
 * Builder and manager for individual real-time channel subscriptions on specific topics.
 * This class provides a type-safe fluent API for configuring and managing subscriptions
 * to PostgreSQL database changes, custom broadcast events, and presence tracking events.
 * <p>
 * Channel builders are instantiated via RealtimeClient factory methods and maintain
 * their own subscription state including registered event listeners and channel lifecycle.
 * Each channel represents a dedicated subscription to a specific topic following the
 * Phoenix channels protocol over WebSocket connections.
 * <p>
 * The builder supports multiple event types: postgres_changes for database events,
 * broadcast for custom application events, and presence for user presence tracking.
 * All event listeners are thread-safe and can handle concurrent event processing.
 *
 * @author neilhdezs
 * @version 0.1.0
 * @since 0.1.0
 */
public class RealtimeChannelBuilder {

    /** - Logger for real-time channel operations and protocol debugging - */
    private static final Logger LOGGER = LoggerFactory.getLogger(RealtimeChannelBuilder.class);

    /** - Shared scheduler for handling subscription timeouts across all channels - */
    private static final ScheduledExecutorService TIMEOUT_SCHEDULER =
            Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r, "realtime-timeout-scheduler");
                t.setDaemon(true);
                return t;
            });

    /** - WebSocket connection manager for sending and receiving real-time messages - */
    private final RealtimeConnection client;

    /** - Topic identifier for this channel subscription (e.g., "realtime:public:todos") - */
    private final String topic;
    /** - Thread-safe atomic reference to the current channel subscription state - */
    private final AtomicReference<ChannelState> state = new AtomicReference<>(ChannelState.CLOSED);
    /** - Thread-safe list of registered PostgreSQL database change event listeners - */
    private final CopyOnWriteArrayList<PostgresChangesFilter> postgresChangesFilters = new CopyOnWriteArrayList<>();
    /** - Thread-safe list of registered broadcast event listeners for custom messaging - */
    private final CopyOnWriteArrayList<BroadcastEventFilter> broadcastFilters = new CopyOnWriteArrayList<>();
    /** - Thread-safe list of registered presence event listeners for user tracking - */
    private final CopyOnWriteArrayList<PresenceEventFilter> presenceFilters = new CopyOnWriteArrayList<>();
    /** - Reference ID of the last join request for reply correlation in Phoenix protocol - */
    private String joinRef;
    /** - Future for the timeout task, allowing cancellation on successful join - */
    private ScheduledFuture<?> timeoutTask;
    /** - User-defined callback for subscription status notifications (SUBSCRIBED, ERROR) - */
    private Consumer<String> statusCallback;
    /** - Internal callback for handling server join response messages - */
    private Consumer<Map<String, Object>> joinReplyCallback;

    /**
     * - PROTECTED CONSTRUCTOR -
     * <p>
     * Creates a new channel builder for the specified topic. Channels are instantiated
     * exclusively through the RealtimeClient factory methods to ensure proper connection
     * management and lifecycle coordination. Protected access allows both same-package
     * classes and potential subclasses to access this constructor.
     *
     * @param topic  Channel topic identifier following Supabase naming convention
     * @param client Parent RealtimeConnection instance for WebSocket communication
     */
    protected RealtimeChannelBuilder(String topic, RealtimeConnection client) {
        this.topic = topic;
        this.client = client;
    }

    /**
     * - CHANNEL TOPIC ACCESS -
     * <p>
     * Returns the topic identifier for this channel subscription.
     * Used internally by the connection manager for channel lifecycle operations.
     *
     * @return Channel topic string identifier
     */
    public String getTopic() {
        return this.topic;
    }

    /**
     * - POSTGRES CHANGES SUBSCRIPTION -
     * <p>
     * Registers a listener for PostgreSQL database change events including INSERT, UPDATE,
     * DELETE, and wildcard operations. Multiple listeners can be registered on the same
     * channel for different tables or event types. Event filtering is applied server-side
     * based on the configured filter parameters.
     *
     * @param filterConfig PostgreSQL changes filter configuration with event type and callback
     * @return This builder instance for method chaining
     */
    public RealtimeChannelBuilder onPostgresChanges(PostgresChangesFilter filterConfig) {
        this.postgresChangesFilters.add(filterConfig);
        return this;
    }

    /**
     * - BROADCAST EVENT SUBSCRIPTION -
     * <p>
     * Registers a listener for custom broadcast events on this channel. Broadcast events
     * enable real-time communication between clients without database persistence. Multiple
     * listeners can be registered for different broadcast event types on the same channel.
     *
     * @param filterConfig Broadcast filter configuration with event name and callback
     * @return This builder instance for method chaining
     */
    public RealtimeChannelBuilder onBroadcast(BroadcastEventFilter filterConfig) {
        this.broadcastFilters.add(filterConfig);
        return this;
    }

    /**
     * - PRESENCE EVENT SUBSCRIPTION -
     * <p>
     * Registers a listener for presence tracking events including SYNC, JOIN, and LEAVE.
     * Presence tracking enables monitoring of user online status and activity across
     * distributed clients. Multiple listeners can be registered for different presence
     * event types on the same channel.
     *
     * @param filterConfig Presence filter configuration with event type and callback
     * @return This builder instance for method chaining
     */
    public RealtimeChannelBuilder onPresence(PresenceEventFilter filterConfig) {
        this.presenceFilters.add(filterConfig);
        return this;
    }

    /**
     * - CHANNEL SUBSCRIPTION ACTIVATION -
     * <p>
     * Initiates the channel subscription by sending a join request to the WebSocket server.
     * The status callback receives notifications about subscription lifecycle events including
     * successful subscription confirmation and error conditions. Only one subscription attempt
     * is allowed per channel builder instance.
     *
     * @param statusCallback Callback function receiving subscription status updates ("SUBSCRIBED" or "ERROR")
     * @return This builder instance for method chaining
     */
    public RealtimeChannelBuilder subscribe(Consumer<String> statusCallback) {
        return subscribe(statusCallback, null);
    }

    /**
     * - CHANNEL SUBSCRIPTION ACTIVATION WITH ERROR HANDLING -
     * <p>
     * Initiates the channel subscription with comprehensive error handling including timeout
     * management and server rejection detection. This method provides robust subscription
     * lifecycle management with automatic timeout after 10 seconds if the server doesn't respond.
     *
     * <h3>Error Scenarios Handled:</h3>
     * <ul>
     * <li><b>Timeout</b> - Server doesn't respond within 10 seconds → RealtimeSubscriptionException with ERROR_JOIN_TIMEOUT</li>
     * <li><b>Server Rejection</b> - Server responds with error status → RealtimeSubscriptionException with ERROR_SUBSCRIPTION_FAILED</li>
     * <li><b>Already Subscribed</b> - Channel already joining/joined → Warning logged, no error thrown</li>
     * </ul>
     *
     * @param statusCallback Callback function receiving subscription status updates ("SUBSCRIBED" or "ERROR")
     * @param onError        Callback invoked when subscription errors occur (timeout, rejection), can be null
     * @return This builder instance for method chaining
     */
    public RealtimeChannelBuilder subscribe(Consumer<String> statusCallback, Consumer<Throwable> onError) {
        if (state.get() != ChannelState.CLOSED) {
            LOGGER.warn(RealtimeConstants.LOG_CHANNEL_ALREADY_JOINING, topic);
            return this;
        }

        this.statusCallback = statusCallback;

        // Set up timeout for the join request (10 seconds)
        if (onError != null) {
            timeoutTask = TIMEOUT_SCHEDULER.schedule(() -> {
                if (state.get() == ChannelState.JOINING) {
                    state.set(ChannelState.ERRORED);
                    LOGGER.error(RealtimeConstants.ERROR_JOIN_TIMEOUT, topic);
                    onError.accept(new RealtimeSubscriptionException(
                            "Channel join timed out for topic: " + topic,
                            null
                    ));
                }
            }, 10, TimeUnit.SECONDS);
        }

        this.joinReplyCallback = (payload) -> {
            // Cancel timeout task as we've received a reply
            if (timeoutTask != null && !timeoutTask.isDone()) {
                timeoutTask.cancel(false);
            }

            String status = (String) payload.get(RealtimeConstants.PAYLOAD_KEY_STATUS);
            if (RealtimeConstants.REPLY_STATUS_OK.equals(status)) {
                state.set(ChannelState.JOINED);
                if (this.statusCallback != null) {
                    this.statusCallback.accept(RealtimeConstants.STATUS_SUBSCRIBED);
                }
            } else {
                this.state.set(ChannelState.ERRORED);
                if (this.statusCallback != null) {
                    this.statusCallback.accept(RealtimeConstants.STATUS_ERROR);
                }
                // Invoke error callback if provided
                if (onError != null) {
                    LOGGER.error(RealtimeConstants.ERROR_SUBSCRIPTION_FAILED, topic);
                    onError.accept(new RealtimeSubscriptionException(
                            "Failed to subscribe to channel: " + topic,
                            null
                    ));
                }
            }
            this.joinReplyCallback = null;
        };

        sendJoin();
        return this;
    }

    /**
     * - CHANNEL UNSUBSCRIPTION -
     * <p>
     * Terminates the channel subscription by sending a leave request to the server and
     * cleaning up all registered event listeners. This method is idempotent and can be
     * called multiple times safely. After unsubscription, the channel builder cannot
     * be reused and a new instance must be created for resubscription.
     */
    public void unsubscribe() {
        if (this.state.get() == ChannelState.CLOSED || this.state.get() == ChannelState.LEAVING) {
            return;
        }

        this.state.set(ChannelState.LEAVING);
        PhoenixMessage leaveMsg = new PhoenixMessage(
                this.topic, RealtimePaths.eventLeave(), new HashMap<>(), client.getNextRef()
        );
        client.push(leaveMsg);

        this.state.set(ChannelState.CLOSED);
        postgresChangesFilters.clear();
        broadcastFilters.clear();
        presenceFilters.clear();
    }

    /**
     * - BROADCAST MESSAGE SENDING -
     * <p>
     * Sends a custom broadcast message to all subscribers of this channel. Broadcast
     * messages enable real-time communication between clients without database persistence.
     * The channel must be in JOINED this.state for message sending to succeed.
     * <p>
     * According to Supabase Realtime protocol, broadcast messages are sent with the
     * following structure:
     * <pre>{@code
     * {
     *   "event": "custom_event_name",
     *   "payload": { ...user data... }
     * }
     * }</pre>
     *
     * @param event   Broadcast event name identifying the message type
     * @param payload Message payload containing arbitrary data as key-value pairs
     */
    public void send(String event, Map<String, Object> payload) {
        if (this.state.get() != ChannelState.JOINED) {
            LOGGER.warn(RealtimeConstants.LOG_CANNOT_SEND_NOT_JOINED, topic);
            return;
        }

        // Build broadcast payload according to Supabase Realtime protocol
        Map<String, Object> broadcastPayload = new HashMap<>();
        broadcastPayload.put(RealtimeConstants.PAYLOAD_KEY_EVENT, event);
        broadcastPayload.put(RealtimeConstants.PAYLOAD_KEY_PAYLOAD, payload);

        PhoenixMessage broadcastMsg = new PhoenixMessage(this.topic, RealtimePaths.eventBroadcast(), broadcastPayload, client.getNextRef());
        client.push(broadcastMsg);
    }

    /**
     * - MESSAGE TRIGGER HANDLER -
     * <p>
     * Processes incoming real-time messages by dispatching them to appropriate registered
     * event listeners. This method is invoked by the RealtimeConnection when messages
     * arrive for this channel's topic.
     *
     * <h3>Phoenix Channels Protocol Flow:</h3>
     * This method implements the client-side message routing for the Phoenix Channels protocol
     * used by Supabase Realtime. It handles different event types according to the protocol spec:
     *
     * <ul>
     * <li><b>phx_reply</b> - Server responses to client requests (JOIN, LEAVE)</li>
     * <li><b>postgres_changes</b> - Database change events (INSERT, UPDATE, DELETE)</li>
     * <li><b>broadcast</b> - Custom application events between clients</li>
     * <li><b>presence</b> - User presence tracking events (SYNC, JOIN, LEAVE)</li>
     * </ul>
     *
     * <h3>Message Reference (ref) Correlation:</h3>
     * The ref parameter is critical for request-response correlation in the Phoenix protocol.
     * When a client sends a message (e.g., JOIN), it includes a unique ref. The server's reply
     * includes the same ref, allowing the client to match responses to requests. This is essential
     * because:
     *
     * <ul>
     * <li>Multiple channels can be active simultaneously</li>
     * <li>Reconnections may trigger multiple JOIN attempts</li>
     * <li>Authentication changes require channel rejoining</li>
     * <li>Network delays can cause responses to arrive out of order</li>
     * </ul>
     * <p>
     * Example:
     * <pre>{@code
     * // Client sends JOIN with ref="1"
     * channel.sendJoin();  // stores "1" in this.joinRef
     *
     * // Server responds with ref="1"
     * trigger("phx_reply", payload, "1");  // Matches, processes response
     *
     * // Later, stale response arrives with ref="0" (from previous attempt)
     * trigger("phx_reply", payload, "0");  // Doesn't match, logs warning
     * }</pre>
     *
     * <h3>Type Safety Without Warning Suppression:</h3>
     * PostgreSQL change events contain nested Map structures that require careful type checking.
     * This implementation uses explicit instanceof validation before casting, eliminating the need
     * for {@code @SuppressWarnings("unchecked")}. This approach provides:
     *
     * <ul>
     * <li>Compile-time type safety without suppressing warnings</li>
     * <li>Runtime validation that prevents ClassCastException</li>
     * <li>Clear logging when protocol violations occur</li>
     * <li>Better debugging information for protocol issues</li>
     * </ul>
     *
     * <h3>Listener Isolation and Error Handling:</h3>
     * Each listener callback is wrapped in a try-catch block to ensure that exceptions in
     * one listener don't affect others. All errors are logged using java.util.logging with
     * appropriate severity levels:
     *
     * <ul>
     * <li><b>WARNING</b> - Protocol mismatches (e.g., ref mismatch)</li>
     * <li><b>SEVERE</b> - Data validation errors or listener exceptions</li>
     * </ul>
     * <p>
     * This provides:
     * <ul>
     * <li>Resilience: One failing listener doesn't break the entire channel</li>
     * <li>Observability: All errors are properly logged with context and stack traces</li>
     * <li>Stability: The channel remains operational even with buggy user code</li>
     * </ul>
     *
     * @param event   Event type identifier from the Phoenix protocol (e.g., "phx_reply", "postgres_changes")
     * @param payload Message payload containing event data as a Map with protocol-specific structure
     * @param ref     Message reference for correlation with client requests - must match this.joinRef for replies
     */
    void trigger(String event, Map<String, Object> payload, String ref) {
        // Handle JOIN reply correlation
        if (RealtimePaths.eventReply().equals(event) && joinReplyCallback != null) {
            if (ref != null && ref.equals(this.joinRef)) {
                joinReplyCallback.accept(payload);
            } else {
                LOGGER.warn(RealtimeConstants.LOG_REF_MISMATCH, this.joinRef, ref);
            }
            return;
        }

        // Handle PostgreSQL database change events
        if (RealtimePaths.eventPostgresChanges().equals(event)) {
            Object dataObj = payload.get(RealtimeConstants.PAYLOAD_KEY_DATA);

            // Type-safe casting: validate before cast
            if (!(dataObj instanceof Map)) {
                LOGGER.error(RealtimeConstants.LOG_INVALID_POSTGRES_DATA,
                        dataObj != null ? dataObj.getClass().getSimpleName() : "null");
                return;
            }

            // Safe cast - we validated with instanceof above
            Map<?, ?> rawData = (Map<?, ?>) dataObj;

            Object typeObj = rawData.get(RealtimeConstants.PAYLOAD_KEY_TYPE);
            if (!(typeObj instanceof String)) {
                LOGGER.error(RealtimeConstants.LOG_MISSING_TYPE_FIELD);
                return;
            }

            String eventType = (String) typeObj;

            for (PostgresChangesFilter filter : postgresChangesFilters) {
                String filterEvent = filter.getEvent();
                if (filterEvent.equals(eventType) || "*".equals(filterEvent)) {
                    try {
                        filter.getCallback().accept(payload);
                    } catch (Exception e) {
                        LOGGER.error(RealtimeConstants.LOG_LISTENER_ERROR, "postgres_changes", eventType, e);
                    }
                }
            }
            return;
        }

        // Handle custom broadcast events
        for (BroadcastEventFilter filter : broadcastFilters) {
            if (filter.getEvent().equals(event)) {
                try {
                    filter.getCallback().accept(payload);
                } catch (Exception e) {
                    LOGGER.error(RealtimeConstants.LOG_LISTENER_ERROR, "broadcast", event, e);
                }
            }
        }

        // Handle presence tracking events
        for (PresenceEventFilter filter : presenceFilters) {
            if (filter.getEvent().equals(event)) {
                try {
                    filter.getCallback().accept(payload);
                } catch (Exception e) {
                    LOGGER.error(RealtimeConstants.LOG_LISTENER_ERROR, "presence", event, e);
                }
            }
        }
    }

    /**
     * - CHANNEL RESUBSCRIPTION -
     * <p>
     * Re-joins the channel after WebSocket reconnection or connection recovery.
     * This method is invoked by the RealtimeConnection to restore active subscriptions
     * after connection interruptions.
     */
    void resubscribe() {
        if (this.state.get() == ChannelState.JOINED || this.state.get() == ChannelState.JOINING) {
            sendJoin();
        }
    }

    /**
     * - AUTHENTICATION-TRIGGERED REJOIN -
     * <p>
     * Re-joins the channel with updated authentication credentials. This method is
     * invoked when user authentication this.state changes to ensure the channel subscription
     * operates with the correct access permissions.
     */
    void rejoinWithNewAuth() {
        resubscribe();
    }

    /**
     * - JOIN MESSAGE CONSTRUCTION -
     * <p>
     * Constructs and sends the Phoenix channel join message with all configured event
     * listeners and authentication tokens. This is the core method that implements the
     * Phoenix Channels protocol handshake for Supabase Realtime.
     *
     * <h3>Phoenix Channels Protocol:</h3>
     * When a client wants to subscribe to a channel, it must send a "phx_join" event.
     * The server validates the request and responds with "phx_reply". This handshake
     * establishes the subscription and allows events to flow.
     *
     * <h3>Join Payload Structure:</h3>
     * The join payload contains two critical sections:
     *
     * <pre>{@code
     * {
     *   "config": {
     *     "postgres_changes": [ {...filter configs...} ],  // DB change subscriptions
     *     "broadcast": { "self": false },                   // Broadcast config
     *     "presence": { "key": "" }                         // Presence config
     *   },
     *   "access_token": "eyJhbGc..."                        // JWT for auth
     * }
     * }</pre>
     *
     * <h3>Filter Configuration:</h3>
     * Each event type (postgres_changes, broadcast, presence) has its own configuration:
     *
     * <ul>
     * <li><b>postgres_changes</b>: Array of filters specifying which DB events to receive
     *     (table name, event type, schema)</li>
     * <li><b>broadcast</b>: Configuration for receiving custom events. "self": false means
     *     the client won't receive its own broadcast messages</li>
     * <li><b>presence</b>: Configuration for presence tracking. "key" identifies the user</li>
     * </ul>
     *
     * <h3>Reference ID (ref) Management:</h3>
     * This method generates and stores a unique ref for this JOIN request:
     *
     * <pre>{@code
     * this.joinRef = client.getNextRef();  // e.g., "1", "2", "3"...
     * }</pre>
     * <p>
     * This ref is critical for:
     * <ul>
     * <li>Correlating the server's "phx_reply" with this specific JOIN request</li>
     * <li>Ignoring stale replies from previous JOIN attempts (reconnections)</li>
     * <li>Thread-safe request-response matching in concurrent scenarios</li>
     * </ul>
     *
     * <h3>Channel State Transition:</h3>
     * State changes: CLOSED → JOINING → (server reply) → JOINED or ERRORED
     *
     * <h3>Why Rejoin?</h3>
     * This method is called in multiple scenarios:
     * <ul>
     * <li>Initial subscription (user calls subscribe())</li>
     * <li>WebSocket reconnection (network recovery)</li>
     * <li>Authentication change (new JWT token after login/refresh)</li>
     * </ul>
     * <p>
     * Each rejoin gets a new ref to ensure we process the correct reply.
     */
    private void sendJoin() {
        // Mark channel as attempting to join
        this.state.set(ChannelState.JOINING);

        // Build the join payload according to Phoenix Channels protocol
        Map<String, Object> joinPayload = new HashMap<>();
        Map<String, Object> config = new HashMap<>();

        // Configure PostgreSQL change subscriptions if any listeners registered
        // Each filter specifies: table, schema, event type (INSERT/UPDATE/DELETE)
        if (!postgresChangesFilters.isEmpty()) {
            List<Map<String, Object>> pgFilters = postgresChangesFilters.stream()
                    .map(PostgresChangesFilter::toPayloadMap)
                    .collect(Collectors.toList());
            config.put(RealtimePaths.eventPostgresChanges(), pgFilters);
        }

        // Configure broadcast subscriptions if any listeners registered
        if (!broadcastFilters.isEmpty()) {
            config.put(RealtimePaths.eventBroadcast(),
                    Map.of(RealtimeConstants.PAYLOAD_KEY_SELF, RealtimeConstants.DEFAULT_BROADCAST_SELF));
        }

        // Configure presence tracking if any listeners registered
        if (!presenceFilters.isEmpty()) {
            config.put(RealtimePaths.eventPresence(),
                    Map.of(RealtimeConstants.PAYLOAD_KEY_KEY, RealtimeConstants.DEFAULT_PRESENCE_KEY));
        }

        // Attach configuration and authentication token
        joinPayload.put(RealtimeConstants.PAYLOAD_KEY_CONFIG, config);
        joinPayload.put(RealtimeConstants.PAYLOAD_KEY_ACCESS_TOKEN, client.getJwt());

        // Generate and store unique ref for this JOIN request
        // This is CRITICAL for request-response correlation (see trigger() method)
        this.joinRef = client.getNextRef();

        // Construct Phoenix protocol message
        PhoenixMessage joinMsg = new PhoenixMessage(
                this.topic,                        // e.g., "realtime:public:todos"
                RealtimePaths.eventJoin(),         // "phx_join"
                joinPayload,                       // config + auth
                this.joinRef                       // unique ref for correlation
        );

        // Send to WebSocket server
        client.push(joinMsg);
    }
}