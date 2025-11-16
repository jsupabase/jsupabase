package io.github.jsupabase.realtime;

import io.github.jsupabase.core.client.HttpClientBase;
import io.github.jsupabase.core.config.SupabaseConfig;

import java.net.http.HttpClient;
import java.util.Objects;

/**
 * Main entry point for the Supabase Realtime API.
 * <p>
 * This class acts as a <strong>Gateway</strong> (or Factory) that provides access
 * to specialized clients (Channel Builders) for Realtime operations.
 * <p>
 * This client is **stateful** in a unique way: it manages a
 * connection ({@link RealtimeConnection}) that is <strong>static and shared</strong>
 * across the entire application. This is because an application only needs one
 * persistent WebSocket connection, regardless of authentication state.
 * <p>
 * via {@link #setAuth(String)} instead of being replaced.
 *
 * @author neilhdezs
 * @version 2.0.0
 */
public class RealtimeClient {

    /**
     * - The singleton WebSocket connection.
     * - It is 'static' because the application should only have ONE
     * - persistent WebSocket connection, regardless of how many
     * - SupabaseClient instances (anon or auth) exist.
     */
    private static RealtimeConnection connection;

    /** - Thread-safe flag to ensure single initialization - **/
    private static volatile boolean isInitialized = false;

    /** - Lock object for thread-safe initialization - **/
    private static final Object initLock = new Object();

    /**
     * Creates a new RealtimeClient (Gateway).
     *
     * @param config     The client configuration.
     * @param httpClient The shared HttpClient (from HttpClientBase.getSharedHttpClient())
     */
    public RealtimeClient(SupabaseConfig config, HttpClient httpClient) {
        Objects.requireNonNull(config, "SupabaseConfig cannot be null");
        Objects.requireNonNull(httpClient, "HttpClient cannot be null");

        // Initialize the singleton WebSocket connection (using Double-Checked Locking)
        // This ensures it only runs once, the first time a SupabaseClient is created.
        if (!isInitialized) {
            synchronized (initLock) {
                if (!isInitialized) {
                    connection = new RealtimeConnection(config, httpClient);
                    isInitialized = true;
                }
            }
        }
    }

    /**
     * Provides access to a <strong>Channel Builder</strong> for a specific topic.
     * <p>
     * This is the factory method for creating a new builder
     * bound to the specified {@code topic}.
     *
     * @param topic The name of the channel to join (e.g., "realtime:public:my_table").
     * @return A new {@link RealtimeChannelBuilder} instance.
     */
    public RealtimeChannelBuilder channel(String topic) {
        return connection.channel(topic);
    }

    /**
     * Removes a specific channel from the Realtime connection.
     *
     * @param channel The channel builder instance to remove.
     */
    public void removeChannel(RealtimeChannelBuilder channel) {
        connection.removeChannel(channel);
    }

    /**
     * Removes all currently subscribed channels from the Realtime connection.
     */
    public void removeAllChannels() {
        connection.removeAllChannels();
    }

    /**
     * (Used by SupabaseClient)
     * Passes the new authentication JWT to the persistent WebSocket connection.
     *
     * @param jwt The new access token.
     */
    public void setAuth(String jwt) {
        connection.setAuth(jwt);
    }
}