package io.github.jsupabase.realtime.utils;

/**
 * Provides static methods for constructing API endpoint paths and event
 * strings for the Supabase Realtime (Phoenix) protocol.
 * <p>
 * This is a stateless utility class ({@code final} with a {@code private} constructor)
 * and is the single source of truth for all Realtime/Phoenix protocol strings.
 *
 * @author neilhdezs
 * @version 0.1.0
 * @since 0.1.0
 */
public final class RealtimePaths {

    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private RealtimePaths() {
        throw new IllegalStateException("This is a utility class and cannot be instantiated.");
    }

    /**
     * Gets the path for the WebSocket connection.
     *
     * @param servicePath The root path of the realtime service (e.g., "/realtime/v1").
     * @param apikey      The project's API key.
     * @param vsn         The Phoenix protocol version (e.g., "1.0.0").
     * @return The full path and query string (e.g., "/realtime/v1/websocket?apikey=...&vsn=...")
     */
    public static String getWebSocketPath(String servicePath, String apikey, String vsn) {
        return String.format("%s/websocket?apikey=%s&vsn=%s", servicePath, apikey, vsn);
    }

    // --- Phoenix Protocol Events ---

    /**
     * @return The event name for joining a topic ("phx_join").
     */
    public static String eventJoin() {
        return "phx_join";
    }

    /**
     * @return The event name for leaving a topic ("phx_leave").
     */
    public static String eventLeave() {
        return "phx_leave";
    }

    /**
     * @return The event name for a reply from the server ("phx_reply").
     */
    public static String eventReply() {
        return "phx_reply";
    }

    /**
     * @return The event name for the heartbeat keep-alive ("heartbeat").
     */
    public static String eventHeartbeat() {
        return "heartbeat";
    }

    /**
     * @return The topic name for the core Phoenix channel ("phoenix").
     */
    public static String topicPhoenix() {
        return "phoenix";
    }

    // --- Supabase Realtime Events ---

    /**
     * @return The event name for broadcast messages ("broadcast").
     */
    public static String eventBroadcast() {
        return "broadcast";
    }

    /**
     * @return The event name for presence state/diffs ("presence").
     */
    public static String eventPresence() {
        return "presence";
    }

    /**
     * @return The event name for database changes ("postgres_changes").
     */
    public static String eventPostgresChanges() {
        return "postgres_changes";
    }

    /**
     * @return The event name for system messages ("system").
     */
    public static String eventSystem() {
        return "system";
    }
}