package io.github.jsupabase.realtime.dto;

import java.util.Map;
import java.util.function.Consumer;

/**
 * - BROADCAST EVENT FILTER -
 * <p>
 * Filter configuration for subscribing to custom broadcast events on Supabase Realtime channels.
 * Broadcast events enable real-time peer-to-peer communication between clients without database
 * persistence, making them ideal for ephemeral data like cursor positions, typing indicators,
 * collaborative editing, or any transient application state.
 * <p>
 * Unlike database change events (postgres_changes), broadcast messages are not stored and only
 * delivered to currently connected subscribers. This makes them lightweight and fast, perfect
 * for high-frequency updates that don't need persistence.
 *
 * <h3>Use Cases:</h3>
 * <ul>
 * <li>Cursor tracking in collaborative applications</li>
 * <li>Typing indicators in chat applications</li>
 * <li>Real-time game state updates</li>
 * <li>Live presence indicators</li>
 * <li>Temporary notifications between clients</li>
 * </ul>
 *
 * <h3>Example Usage:</h3>
 * <pre>{@code
 * BroadcastEventFilter cursorFilter = new BroadcastEventFilter(
 *     "cursor_moved",
 *     payload -> {
 *         Map<String, Object> data = (Map<String, Object>) payload.get("payload");
 *         System.out.println("Cursor at: " + data.get("x") + ", " + data.get("y"));
 *     }
 * );
 *
 * channel.onBroadcast(cursorFilter).subscribe(status -> {
 *     System.out.println("Subscribed: " + status);
 * });
 * }</pre>
 *
 * @author neilhdezs
 * @version 0.1.0
 * @since 0.1.0
 */
public class BroadcastEventFilter {

    /** - Custom event name identifying the type of broadcast message to listen for - */
    private final String event;

    /** - Callback function invoked when matching broadcast events are received from the channel - */
    private final Consumer<Map<String, Object>> callback;

    /**
     * - BROADCAST FILTER CONSTRUCTOR -
     * <p>
     * Creates a new broadcast event filter that listens for specific custom events on a channel.
     *
     * @param event    Custom event name to listen for (e.g., "cursor_moved", "typing", "notification")
     * @param callback Function to execute when the event is received, receives the full message payload
     */
    public BroadcastEventFilter(String event, Consumer<Map<String, Object>> callback) {
        this.event = event;
        this.callback = callback;
    }

    /** - Returns the custom event name this filter is listening for - */
    public String getEvent() {
        return event;
    }

    /** - Returns the callback function to execute when events are received - */
    public Consumer<Map<String, Object>> getCallback() {
        return callback;
    }
}