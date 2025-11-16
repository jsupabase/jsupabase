package io.github.jsupabase.realtime;

import io.github.jsupabase.core.config.SupabaseConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.http.HttpClient;
import java.util.Objects;

/**
 * - SUPABASE REAL-TIME API CLIENT -
 * <p>
 * Gateway client for Supabase Real-time operations providing WebSocket-based live updates
 * and database change subscriptions. This client implements a unique singleton pattern
 * for WebSocket connection management where a single persistent connection is shared
 * across the entire application lifecycle.
 *
 * <h3>Singleton Pattern Implementation:</h3>
 * Unlike other SDK modules that can have multiple instances, the RealtimeClient maintains
 * a static shared WebSocket connection. This design decision is crucial because:
 * <ul>
 * <li>WebSocket connections are expensive resources (TCP connection + handshake overhead)</li>
 * <li>One connection can handle unlimited channels efficiently</li>
 * <li>Reduces server load by avoiding multiple connections from same client</li>
 * <li>Simplifies reconnection logic and state management</li>
 * </ul>
 *
 * <h3>Thread-Safety with Double-Checked Locking:</h3>
 * The singleton initialization uses the double-checked locking pattern with volatile flag
 * to ensure thread-safe initialization without synchronization overhead on every access.
 * This pattern guarantees only one connection instance is ever created even with concurrent
 * access from multiple threads.
 *
 * <h3>Authentication Integration:</h3>
 * When user authentication state changes (login/logout/token refresh), the SupabaseClient
 * calls setAuth() to update the JWT token. The connection then automatically re-joins
 * all active channels with the new authentication context without closing the WebSocket.
 *
 * @author neilhdezs
 * @version 0.1.0
 * @since 0.1.0
 */
public class RealtimeClient {

    /** - Logger for real-time client operations and lifecycle events - */
    private static final Logger LOGGER = LoggerFactory.getLogger(RealtimeClient.class);
    /** - Synchronization lock object for double-checked locking pattern in singleton initialization - */
    private static final Object initLock = new Object();
    /** - Singleton WebSocket connection shared across all client instances for efficient resource usage - */
    private static RealtimeConnection connection;
    /** - Thread-safe initialization flag using volatile for proper memory visibility across threads - */
    private static volatile boolean isInitialized = false;

    /**
     * - REAL-TIME CLIENT CONSTRUCTOR -
     * <p>
     * Initializes the real-time gateway client and ensures singleton WebSocket connection
     * creation using the double-checked locking pattern. This pattern provides thread-safe
     * lazy initialization with minimal synchronization overhead.
     *
     * <h3>Double-Checked Locking Pattern:</h3>
     * The implementation uses two checks with a synchronized block in between:
     * <pre>{@code
     * if (!isInitialized) {              // First check (fast, no lock)
     *     synchronized (initLock) {      // Only one thread enters
     *         if (!isInitialized) {      // Second check (safe, locked)
     *             connection = new RealtimeConnection(...);
     *             isInitialized = true;  // Volatile write ensures visibility
     *         }
     *     }
     * }
     * }</pre>
     *
     * <h3>Why This Pattern:</h3>
     * <ul>
     * <li><b>First check</b> - Fast path for already-initialized case (99.9% of calls)</li>
     * <li><b>Synchronized block</b> - Only executed once during initialization</li>
     * <li><b>Second check</b> - Prevents race condition if multiple threads pass first check</li>
     * <li><b>Volatile flag</b> - Ensures memory visibility of initialization across threads</li>
     * </ul>
     * <p>
     * The connection is initialized only once per application lifecycle and shared
     * across all client instances for optimal resource utilization.
     *
     * @param config     Supabase configuration containing real-time service settings and WebSocket URL
     * @param httpClient Shared HTTP client instance for connection management and heartbeat
     */
    public RealtimeClient(SupabaseConfig config, HttpClient httpClient) {
        Objects.requireNonNull(config, "SupabaseConfig cannot be null");
        Objects.requireNonNull(httpClient, "HttpClient cannot be null");

        if (!isInitialized) {
            synchronized (initLock) {
                if (!isInitialized) {
                    LOGGER.info("Initializing singleton RealtimeConnection");
                    connection = new RealtimeConnection(config, httpClient);
                    isInitialized = true;
                    LOGGER.info("RealtimeConnection initialized successfully");
                }
            }
        }
    }

    /**
     * - CHANNEL BUILDER FACTORY -
     * <p>
     * Creates a new channel builder for subscribing to real-time events on a specific
     * topic. Channel builders provide a fluent API for configuring event listeners,
     * filters, and subscription options for PostgreSQL changes, presence tracking,
     * and broadcast messaging.
     *
     * @param topic Channel topic identifier following Supabase naming convention (e.g., "realtime:public:table_name")
     * @return New RealtimeChannelBuilder instance configured for the specified topic
     */
    public RealtimeChannelBuilder channel(String topic) {
        return connection.channel(topic);
    }

    /**
     * - CHANNEL REMOVAL -
     * <p>
     * Removes a specific channel subscription from the WebSocket connection and
     * cleans up associated resources. This method should be called when a channel
     * is no longer needed to prevent memory leaks and unnecessary event processing.
     *
     * @param channel Channel builder instance to remove from active subscriptions
     */
    public void removeChannel(RealtimeChannelBuilder channel) {
        connection.removeChannel(channel);
    }

    /**
     * - ALL CHANNELS REMOVAL -
     * <p>
     * Removes all currently active channel subscriptions from the WebSocket connection.
     * This method is useful for cleanup operations or when reinitializing the real-time
     * client with different subscription configurations.
     */
    public void removeAllChannels() {
        connection.removeAllChannels();
    }

    /**
     * - AUTHENTICATION UPDATE -
     * <p>
     * Updates the authentication token for the persistent WebSocket connection without
     * requiring connection re-establishment. This method is called automatically by
     * the SupabaseClient when authentication state changes, ensuring seamless
     * transition between anonymous and authenticated real-time operations.
     *
     * @param jwt New access token for authenticated real-time operations
     */
    public void setAuth(String jwt) {
        connection.setAuth(jwt);
    }
}