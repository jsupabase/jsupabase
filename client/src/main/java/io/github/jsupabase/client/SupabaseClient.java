package io.github.jsupabase.client;

import io.github.jsupabase.auth.AuthClient;
import io.github.jsupabase.auth.dto.Session;
import io.github.jsupabase.auth.enums.AuthChangeEvent;
import io.github.jsupabase.core.client.HttpClientBase;
import io.github.jsupabase.core.config.SupabaseConfig;
import io.github.jsupabase.postgrest.PostgrestClient;
import io.github.jsupabase.realtime.RealtimeClient;
import io.github.jsupabase.prueba.StorageClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.http.HttpClient;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

/**
 * - UNIFIED SUPABASE SDK CLIENT -
 * <p>
 * Main entry point and orchestrator for the jsupabase SDK that implements the Gateway pattern.
 * This class aggregates all specialized clients (Auth, Postgrest, Realtime, Storage) and provides
 * a unified interface for accessing Supabase services. The client automatically manages authentication
 * state changes and dynamically reconfigures underlying service clients with JWT tokens when users
 * sign in or out.
 * <p>
 * The Gateway pattern implementation ensures that all service clients are automatically updated
 * with the appropriate authentication headers when the user's session state changes, eliminating
 * the need for manual token management across different service modules.
 *
 * @author neilhdezs
 * @version 0.1.0
 * @since 0.1.0
 */
public class SupabaseClient {

    /** - Logger for Gateway pattern operations and client lifecycle - **/
    private static final Logger LOGGER = LoggerFactory.getLogger(SupabaseClient.class);

    /** - Immutable base configuration used for anonymous operations and client initialization - **/
    private final SupabaseConfig config;

    /** - Authentication client for user management and session handling via GoTrue API - */
    private final AuthClient auth;

    /** - PostgreSQL REST client stored in AtomicReference for thread-safe authentication state updates - */
    private final AtomicReference<PostgrestClient> postgrest;

    /** - Real-time WebSocket client for database change subscriptions and live updates - */
    private final RealtimeClient realtime;

    /** - Storage client for file operations stored in AtomicReference for authentication state updates - */
    private final AtomicReference<StorageClient> storage;

    /**
     * - PRIVATE CONSTRUCTOR -
     * <p>
     * Initializes all service clients with the provided anonymous configuration and sets up
     * the authentication state change listener for automatic client reconfiguration.
     * Use the static create() factory method for public instantiation.
     *
     * @param config Base Supabase configuration containing URL, anonymous key, and default headers
     */
    private SupabaseClient(SupabaseConfig config) {
        this.config = Objects.requireNonNull(config, "SupabaseConfig cannot be null");

        HttpClient sharedHttpClient = HttpClientBase.getSharedHttpClient();

        LOGGER.debug("Initializing SupabaseClient with URL: {}", config.getSupabaseUrl());

        this.auth = new AuthClient(this.config);
        this.postgrest = new AtomicReference<>(new PostgrestClient(this.config));
        this.realtime = new RealtimeClient(this.config, sharedHttpClient);
        this.storage = new AtomicReference<>(new StorageClient(this.config));

        this.auth.onAuthStateChange(this::handleAuthStateChange);

        LOGGER.info("SupabaseClient initialized successfully");
    }

    /**
     * - FACTORY METHOD -
     * <p>
     * Creates a new SupabaseClient instance with the provided configuration and initializes
     * all service clients. This is the main entry point for SDK instantiation and automatically
     * sets up authentication state change listeners for Gateway pattern functionality.
     *
     * @param config Supabase configuration built using SupabaseConfig.Builder
     * @return Fully initialized SupabaseClient instance ready for service operations
     */
    public static SupabaseClient create(SupabaseConfig config) {
        LOGGER.debug("Creating new SupabaseClient instance");
        return new SupabaseClient(config);
    }

    /**
     * - AUTHENTICATION STATE CHANGE HANDLER -
     * <p>
     * Core Gateway pattern implementation that automatically reconfigures all service clients
     * when authentication state changes. Creates new authenticated client instances with JWT
     * tokens for authorized operations or reverts to anonymous configuration when signed out.
     * This ensures seamless integration between authentication and all other services without
     * requiring manual token management.
     *
     * @param event   The authentication event type (SIGNED_IN, SIGNED_OUT, TOKEN_REFRESHED)
     * @param session Current user session containing access token and user information
     */
    private void handleAuthStateChange(AuthChangeEvent event, Session session) {
        LOGGER.debug("Handling auth state change: {}", event);

        if (event == AuthChangeEvent.SIGNED_IN || event == AuthChangeEvent.TOKEN_REFRESHED) {
            String jwt = session.getAccessToken();

            LOGGER.info("Reconfiguring service clients with authenticated context (event: {})", event);

            SupabaseConfig authConfig = new SupabaseConfig.Builder(this.config.getSupabaseUrl().toString(), this.config.getSupabaseKey())
                    .addHeader("Authorization", "Bearer " + jwt)
                    .withSchema(this.config.getSchema())
                    .build();

            this.postgrest.set(new PostgrestClient(authConfig));
            this.storage.set(new StorageClient(authConfig));
            this.realtime.setAuth(jwt);

            LOGGER.debug("Service clients reconfigured with JWT token");

        } else if (event == AuthChangeEvent.SIGNED_OUT) {
            LOGGER.info("Reconfiguring service clients with anonymous context");

            this.postgrest.set(new PostgrestClient(this.config));
            this.storage.set(new StorageClient(this.config));
            this.realtime.setAuth(this.config.getSupabaseKey());

            LOGGER.debug("Service clients reverted to anonymous configuration");
        }
    }

    /**
     * - AUTHENTICATION SERVICE ACCESS -
     * <p>
     * Provides access to the authentication client for user management operations including
     * sign up, sign in, password reset, and session management via Supabase GoTrue API.
     *
     * @return AuthClient instance for authentication operations
     */
    public AuthClient auth() {
        return this.auth;
    }

    /**
     * - DATABASE SERVICE ACCESS -
     * <p>
     * Provides access to the PostgreSQL REST client for database operations including
     * table queries, RPC calls, and CRUD operations. Returns the current client instance
     * which may be authenticated or anonymous depending on user session state.
     *
     * @return Current PostgrestClient instance with appropriate authentication context
     */
    public PostgrestClient postgrest() {
        return this.postgrest.get();
    }

    /**
     * - REAL-TIME SERVICE ACCESS -
     * <p>
     * Provides access to the real-time client for WebSocket-based database change subscriptions
     * and live updates. Supports listening to INSERT, UPDATE, DELETE events on database tables.
     *
     * @return RealtimeClient instance for real-time database subscriptions
     */
    public RealtimeClient realtime() {
        return this.realtime;
    }

    /**
     * - STORAGE SERVICE ACCESS -
     * <p>
     * Provides access to the storage client for file management operations including
     * bucket creation, file upload/download, and object manipulation. Returns the current
     * client instance with appropriate authentication context.
     *
     * @return Current StorageClient instance with appropriate authentication context
     */
    public StorageClient storage() {
        return this.storage.get();
    }
}