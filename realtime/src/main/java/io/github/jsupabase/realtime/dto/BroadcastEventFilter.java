package io.github.jsupabase.realtime.dto;

import java.util.Map;
import java.util.function.Consumer;

/**
 * Configuration for a "broadcast" event listener.
 *
 * @author neilhdezs
 * @version 0.1.0
 */
public class BroadcastEventFilter {

    /** - The custom event name (e.g., "cursor-pos") - **/
    private final String event;

    /** - The user's callback - **/
    private final Consumer<Map<String, Object>> callback;

    public BroadcastEventFilter(String event, Consumer<Map<String, Object>> callback) {
        this.event = event;
        this.callback = callback;
    }

    public String getEvent() {
        return event;
    }

    public Consumer<Map<String, Object>> getCallback() {
        return callback;
    }
}