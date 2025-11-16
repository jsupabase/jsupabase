package io.github.jsupabase.realtime;

import io.github.jsupabase.realtime.dto.BroadcastEventFilter;
import io.github.jsupabase.realtime.dto.PhoenixMessage;
import io.github.jsupabase.realtime.dto.PresenceEventFilter;
import io.github.jsupabase.realtime.dto.options.PostgresChangesFilter;
import io.github.jsupabase.realtime.enums.ChannelState;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Builds and manages a subscription to a single Realtime topic.
 * <p>
 * This class implements the type-safe API for subscribing to
 * postgres_changes, broadcast, and presence events. It is instantiated via
 * {@link RealtimeClient#channel(String)}.
 *
 * @author neilhdezs
 * @version 2.0.1 // Version updated
 */
public class RealtimeChannelBuilder {

    /** - The underlying WebSocket connection manager - **/
    private final RealtimeConnection client;

    /** - The topic name this channel subscribes to (e.g., "realtime:public:todos") - **/
    private final String topic;

    /** - The current state of the channel - **/
    private final AtomicReference<ChannelState> state = new AtomicReference<>(ChannelState.CLOSED);

    /** - User-defined callback for subscription status changes (SUBSCRIBED, ERROR) - **/
    private Consumer<String> statusCallback;

    /** - Internal one-time callback to handle the server's "phx_reply" to a join request - **/
    private Consumer<Map<String, Object>> joinReplyCallback;

    /** - Thread-safe list of all registered PostgresChanges listeners - **/
    private final CopyOnWriteArrayList<PostgresChangesFilter> postgresChangesFilters = new CopyOnWriteArrayList<>();
    /** - Thread-safe list of all registered Broadcast listeners - **/
    private final CopyOnWriteArrayList<BroadcastEventFilter> broadcastFilters = new CopyOnWriteArrayList<>();
    /** - Thread-safe list of all registered Presence listeners - **/
    private final CopyOnWriteArrayList<PresenceEventFilter> presenceFilters = new CopyOnWriteArrayList<>();

    /**
     * Package-private constructor. Channels are created via RealtimeClient.
     *
     * @param topic The name of the topic.
     * @param client The parent RealtimeConnection.
     */
    RealtimeChannelBuilder(String topic, RealtimeConnection client) {
        this.topic = topic;
        this.client = client;
    }

    /**
     * --- ¡MÉTODO AÑADIDO! ---
     * Gets the topic (name) of this channel.
     * <p>
     * (Used by {@link RealtimeConnection#removeChannel(RealtimeChannelBuilder)})
     *
     * @return The topic string.
     */
    public String getTopic() {
        return this.topic;
    }

    // --- Public Type-Safe API ---

    /**
     * Registers a listener for database changes (INSERT, UPDATE, DELETE, ALL).
     *
     * @param filterConfig The filter configuration built by {@link PostgresChangesFilter.Builder}.
     * @return this (for chaining).
     */
    public RealtimeChannelBuilder onPostgresChanges(PostgresChangesFilter filterConfig) {
        this.postgresChangesFilters.add(filterConfig);
        return this;
    }

    /**
     * Registers a listener for custom broadcast events.
     *
     * @param filterConfig The filter configuration (event name and callback).
     * @return this (for chaining).
     */
    public RealtimeChannelBuilder onBroadcast(BroadcastEventFilter filterConfig) {
        this.broadcastFilters.add(filterConfig);
        return this;
    }

    /**
     * Registers a listener for presence events (SYNC, JOIN, LEAVE).
     *
     * @param filterConfig The filter configuration (event type and callback).
     * @return this (for chaining).
     */
    public RealtimeChannelBuilder onPresence(PresenceEventFilter filterConfig) {
        this.presenceFilters.add(filterConfig);
        return this;
    }

    /**
     * Joins the channel topic on the server.
     * <p>
     * This method sends the "phx_join" event via the WebSocket. The
     * {@code statusCallback} will be invoked with "SUBSCRIBED" on success
     * or "ERROR" on failure.
     *
     * @param statusCallback A callback that receives the subscription status.
     * @return this (for chaining).
     */
    public RealtimeChannelBuilder subscribe(Consumer<String> statusCallback) {
        if (state.get() != ChannelState.CLOSED) {
            System.out.println("Channel " + topic + ": Already joining or joined.");
            return this;
        }

        this.statusCallback = statusCallback;

        // Create a one-time callback for the "phx_reply" to this join
        this.joinReplyCallback = (payload) -> {
            String status = (String) payload.get("status");
            if ("ok".equals(status)) {
                state.set(ChannelState.JOINED);
                if (this.statusCallback != null) this.statusCallback.accept("SUBSCRIBED");
            } else {
                state.set(ChannelState.ERRORED);
                if (this.statusCallback != null) this.statusCallback.accept("ERROR");
            }
            this.joinReplyCallback = null; // This callback is now processed
        };

        sendJoin();
        return this;
    }

    /**
     * Leaves the channel topic on the server (sends "phx_leave").
     */
    public void unsubscribe() {
        if (state.get() == ChannelState.CLOSED || state.get() == ChannelState.LEAVING) {
            return;
        }

        state.set(ChannelState.LEAVING);
        PhoenixMessage leaveMsg = new PhoenixMessage(
                this.topic, RealtimePaths.eventLeave(), new HashMap<>(), client.getNextRef()
        );
        client.push(leaveMsg);

        state.set(ChannelState.CLOSED);
        // Clear all registered filters
        postgresChangesFilters.clear();
        broadcastFilters.clear();
        presenceFilters.clear();
    }

    /**
     * Sends a "broadcast" message on this channel.
     *
     * @param event The broadcast event name (e.g., "cursor-pos").
     * @param payload The payload (Map) to send.
     */
    public void send(String event, Map<String, Object> payload) {
        if (state.get() != ChannelState.JOINED) {
            System.err.println("Cannot send message, channel " + topic + " is not joined.");
            return;
        }

        Map<String, Object> broadcastPayload = new HashMap<>();
        broadcastPayload.put("type", RealtimePaths.eventBroadcast());
        broadcastPayload.put("event", event); // The custom event
        broadcastPayload.put("payload", payload);

        PhoenixMessage broadcastMsg = new PhoenixMessage(
                this.topic, RealtimePaths.eventBroadcast(), broadcastPayload, client.getNextRef()
        );
        client.push(broadcastMsg);
    }

    // --- Internal & Package-Private Methods ---

    /**
     * (Called by RealtimeConnection)
     * Triggers the appropriate listeners when a message is received for this topic.
     *
     * @param event The event name (e.g., "INSERT", "phx_reply").
     * @param payload The message payload.
     * @param ref The message reference.
     */
    @SuppressWarnings("unchecked") // Para el cast de payload.get("data")
    void trigger(String event, Map<String, Object> payload, String ref) {
        // First, check if this is the join reply
        if (RealtimePaths.eventReply().equals(event) && joinReplyCallback != null) {
            joinReplyCallback.accept(payload);
            return;
        }

        // Check Postgres Changes
        if (RealtimePaths.eventPostgresChanges().equals(event)) {
            try {
                Map<String, Object> data = (Map<String, Object>) payload.get("data");
                if (data == null) return;
                String eventType = (String) data.get("type"); // INSERT, UPDATE, DELETE
                if (eventType == null) return;

                // Find listeners that match this specific type
                for (PostgresChangesFilter filter : postgresChangesFilters) {
                    String filterEvent = filter.getEvent();
                    if (filterEvent.equals(eventType) || "*".equals(filterEvent)) {
                        try {
                            filter.getCallback().accept(payload); // Send the full payload
                        } catch (Exception e) {
                            System.err.println("Realtime: Error in postgres_changes listener for " + eventType + ": " + e.getMessage());
                        }
                    }
                }
            } catch (ClassCastException e) {
                System.err.println("Realtime: Failed to cast postgres_changes data: " + e.getMessage());
            }
            return; // Handled
        }

        // Check Broadcast
        for (BroadcastEventFilter filter : broadcastFilters) {
            if (filter.getEvent().equals(event)) {
                try {
                    filter.getCallback().accept(payload);
                } catch (Exception e) {
                    System.err.println("Realtime: Error in broadcast listener for " + event + ": " + e.getMessage());
                }
            }
        }

        // Check Presence
        for (PresenceEventFilter filter : presenceFilters) {
            if (filter.getEvent().equals(event)) {
                try {
                    filter.getCallback().accept(payload);
                } catch (Exception e) {
                    System.err.println("Realtime: Error in presence listener for " + event + ": " + e.getMessage());
                }
            }
        }
    }

    /**
     * (Called by RealtimeConnection)
     * Re-joins the channel (e.g., after a WebSocket reconnection).
     */
    void resubscribe() {
        if (state.get() == ChannelState.JOINED || state.get() == ChannelState.JOINING) {
            sendJoin();
        }
    }

    /**
     * (Called by RealtimeConnection)
     * Re-joins with new auth credentials.
     */
    void rejoinWithNewAuth() {
        resubscribe();
    }


    /**
     * Helper method to build and send the "phx_join" message.
     */
    private void sendJoin() {
        state.set(ChannelState.JOINING);

        Map<String, Object> joinPayload = new HashMap<>();
        Map<String, Object> config = new HashMap<>();

        // 1. Add Postgres Changes config
        if (!postgresChangesFilters.isEmpty()) {
            List<Map<String, Object>> pgFilters = postgresChangesFilters.stream()
                    .map(PostgresChangesFilter::toPayloadMap)
                    .collect(Collectors.toList());
            config.put(RealtimePaths.eventPostgresChanges(), pgFilters);
        }

        // 2. Add Broadcast config
        if (!broadcastFilters.isEmpty()) {
            config.put(RealtimePaths.eventBroadcast(), Map.of("self", false));
        }

        // 3. Add Presence config
        if (!presenceFilters.isEmpty()) {
            config.put(RealtimePaths.eventPresence(), Map.of("key", ""));
        }

        joinPayload.put("config", config);
        joinPayload.put("access_token", client.getJwt());

        // --- Create and send the message ---
        PhoenixMessage joinMsg = new PhoenixMessage(
                this.topic, RealtimePaths.eventJoin(), joinPayload, client.getNextRef()
        );
        client.push(joinMsg);
    }
}