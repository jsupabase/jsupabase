package io.github.jsupabase.realtime.enums;

/**
 * - REAL-TIME EVENT TYPES -
 * <p>
 * Defines the types of events that can be subscribed to on Supabase Realtime channels.
 * This enum encapsulates both database change events (INSERT, UPDATE, DELETE) and
 * presence tracking events (SYNC, JOIN, LEAVE), mapping them to their corresponding
 * Phoenix Channels protocol event strings.
 *
 * <h3>Database Change Events:</h3>
 * Used with PostgresChangesFilter for listening to database table modifications.
 *
 * <h3>Presence Events:</h3>
 * Used with PresenceEventFilter for tracking user online status and channel participation.
 *
 * @author neilhdezs
 * @version 0.1.0
 * @since 0.1.0
 */
public enum RealtimeEvent {
    /** - Wildcard event matching all database operations (INSERT, UPDATE, DELETE) - */
    ALL("*"),

    /** - Database INSERT operation event - Fired when new rows are inserted into subscribed tables - */
    INSERT("INSERT"),

    /** - Database UPDATE operation event - Fired when existing rows are modified in subscribed tables - */
    UPDATE("UPDATE"),

    /** - Database DELETE operation event - Fired when rows are removed from subscribed tables - */
    DELETE("DELETE"),

    /** - Presence state synchronization event - Sends complete presence state when joining or periodically - */
    SYNC("sync"),

    /** - Presence user join event - Fired when a new user joins the channel - */
    JOIN("join"),

    /** - Presence user leave event - Fired when a user disconnects or explicitly leaves the channel - */
    LEAVE("leave");

    /** - Phoenix Channels protocol event string representation - */
    private final String phxEvent;

    /**
     * - REALTIME EVENT CONSTRUCTOR -
     *
     * @param phxEvent Phoenix Channels protocol event string
     */
    RealtimeEvent(String phxEvent) {
        this.phxEvent = phxEvent;
    }

    /** - Returns the Phoenix Channels protocol event string for this event type - */
    public String getPhxEvent() {
        return phxEvent;
    }
}