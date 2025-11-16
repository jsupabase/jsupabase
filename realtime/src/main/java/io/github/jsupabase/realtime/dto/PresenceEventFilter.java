package io.github.jsupabase.realtime.dto;

import io.github.jsupabase.realtime.enums.RealtimeEvent;

import java.util.Map;
import java.util.function.Consumer;

/**
 * - PRESENCE EVENT FILTER -
 * <p>
 * Filter configuration for subscribing to presence tracking events on Supabase Realtime channels.
 * Presence tracking enables monitoring of user online status, detecting when users join or leave
 * a channel, and synchronizing presence state across all connected clients. This is essential
 * for building collaborative applications that need to show "who's online" or track user activity.
 * <p>
 * Presence uses a Conflict-free Replicated Data Type (CRDT) to ensure eventually consistent
 * state across all clients, even in the face of network partitions or temporary disconnections.
 *
 * <h3>Event Types:</h3>
 * <ul>
 * <li><b>SYNC</b> - Complete presence state synchronization, sent when joining or periodically</li>
 * <li><b>JOIN</b> - Fired when a new user joins the channel</li>
 * <li><b>LEAVE</b> - Fired when a user leaves the channel (disconnect or explicit leave)</li>
 * </ul>
 *
 * <h3>Use Cases:</h3>
 * <ul>
 * <li>Showing "currently online" user lists</li>
 * <li>Collaborative editing awareness (who's viewing/editing)</li>
 * <li>Multiplayer game lobbies</li>
 * <li>Chat room participant tracking</li>
 * <li>Real-time collaboration indicators</li>
 * </ul>
 *
 * <h3>Example Usage:</h3>
 * <pre>{@code
 * PresenceEventFilter syncFilter = new PresenceEventFilter(
 *     RealtimeEvent.SYNC,
 *     payload -> {
 *         Map<String, Object> presence = (Map<String, Object>) payload.get("presence");
 *         System.out.println("Online users: " + presence.keySet());
 *     }
 * );
 *
 * PresenceEventFilter joinFilter = new PresenceEventFilter(
 *     RealtimeEvent.JOIN,
 *     payload -> {
 *         System.out.println("User joined: " + payload.get("user"));
 *     }
 * );
 *
 * channel.onPresence(syncFilter)
 *        .onPresence(joinFilter)
 *        .subscribe(status -> System.out.println("Status: " + status));
 * }</pre>
 *
 * @author neilhdezs
 * @version 0.1.0
 * @since 0.1.0
 */
public class PresenceEventFilter {

    /** - Presence event type defining which presence state changes to listen for - */
    private final RealtimeEvent event;

    /** - Callback function invoked when matching presence events occur on the channel - */
    private final Consumer<Map<String, Object>> callback;

    /**
     * - PRESENCE FILTER CONSTRUCTOR -
     * <p>
     * Creates a new presence event filter for tracking user online status and channel participation.
     * Only SYNC, JOIN, and LEAVE events are valid for presence tracking.
     *
     * @param event    Presence event type (must be RealtimeEvent.SYNC, JOIN, or LEAVE)
     * @param callback Function to execute when presence events occur, receives presence state data
     * @throws IllegalArgumentException if event is not SYNC, JOIN, or LEAVE
     */
    public PresenceEventFilter(RealtimeEvent event, Consumer<Map<String, Object>> callback) {
        if (event != RealtimeEvent.SYNC && event != RealtimeEvent.JOIN && event != RealtimeEvent.LEAVE) {
            throw new IllegalArgumentException("PresenceEventFilter event must be SYNC, JOIN, or LEAVE.");
        }
        this.event = event;
        this.callback = callback;
    }

    /** - Returns the Phoenix Channels event string for this presence event type - */
    public String getEvent() {
        return event.getPhxEvent();
    }

    /** - Returns the callback function to execute when presence events occur - */
    public Consumer<Map<String, Object>> getCallback() {
        return callback;
    }
}