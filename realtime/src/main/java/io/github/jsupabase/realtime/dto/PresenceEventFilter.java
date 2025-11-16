package io.github.jsupabase.realtime.dto;

import io.github.jsupabase.realtime.enums.RealtimeEvent;

import java.util.Map;
import java.util.function.Consumer;

/**
 * Configuration for a "presence" event listener (SYNC, JOIN, LEAVE).
 *
 * @author neilhdezs
 * @version 0.1.0
 */
public class PresenceEventFilter {

    /** - The presence event type (SYNC, JOIN, or LEAVE) - **/
    private final RealtimeEvent event;

    /** - The user's callback - **/
    private final Consumer<Map<String, Object>> callback;

    public PresenceEventFilter(RealtimeEvent event, Consumer<Map<String, Object>> callback) {
        if (event != RealtimeEvent.SYNC && event != RealtimeEvent.JOIN && event != RealtimeEvent.LEAVE) {
            throw new IllegalArgumentException("PresenceEventFilter event must be SYNC, JOIN, or LEAVE.");
        }
        this.event = event;
        this.callback = callback;
    }

    public String getEvent() {
        return event.getPhxEvent();
    }

    public Consumer<Map<String, Object>> getCallback() {
        return callback;
    }
}