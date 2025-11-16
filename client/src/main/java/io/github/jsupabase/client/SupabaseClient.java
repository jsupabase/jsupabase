package io.github.jsupabase.client;

import io.github.jsupabase.auth.AuthClient;
import io.github.jsupabase.auth.dto.Session;
import io.github.jsupabase.auth.enums.AuthChangeEvent;
import io.github.jsupabase.core.client.HttpClientBase;
import io.github.jsupabase.core.config.SupabaseConfig;
import io.github.jsupabase.postgrest.PostgrestClient;
import io.github.jsupabase.realtime.RealtimeClient;
import io.github.jsupabase.storage.StorageClient;

import java.net.http.HttpClient;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Main entry point for the jsupabase SDK.
 * This class aggregates all specialized clients (Auth, Postgrest, Realtime, Storage).
 * It manages the authentication state and injects the JWT into other clients.
 *
 * @author neilhdezs
 * @version 0.4.0
 */
public class SupabaseClient {

    /** - The immutable *anonymous* configuration for this client - **/
    private final SupabaseConfig config;

    /** - Specialized client for Authentication (GoTrue) - **/
    private final AuthClient auth;

    /**
     * - Specialized client for Database (PostgREST).
     * - This is stored in an AtomicReference because it must be REPLACED
     * - whenever the authentication state changes (SIGNED_IN, SIGNED_OUT).
     */
    private final AtomicReference<PostgrestClient> postgrest;

    /** - Specialized client for Realtime (WebSockets) - **/
    private final RealtimeClient realtime;

    /**
     * - Specialized client for Storage (Files).
     * - This is also stored in an AtomicReference and replaced on auth change.
     */
    private final AtomicReference<StorageClient> storage;

    /**
     * Private constructor. Use the static create() method.
     *
     * @param config The global *anonymous* configuration object.
     */
    private SupabaseClient(SupabaseConfig config) {
        this.config = Objects.requireNonNull(config, "SupabaseConfig cannot be null");

        HttpClient sharedHttpClient = HttpClientBase.getSharedHttpClient();

        this.auth = new AuthClient(this.config);
        this.postgrest = new AtomicReference<>(new PostgrestClient(this.config));
        this.realtime = new RealtimeClient(this.config, sharedHttpClient);
        this.storage = new AtomicReference<>(new StorageClient(this.config));

        this.auth.onAuthStateChange(this::handleAuthStateChange);
    }

    /**
     * This is the core "glue" of the SDK.
     * It listens for auth events and creates new, authenticated
     * clients for Postgrest and Storage.
     */
    private void handleAuthStateChange(AuthChangeEvent event, Session session) {
        if (event == AuthChangeEvent.SIGNED_IN || event == AuthChangeEvent.TOKEN_REFRESHED) {
            String jwt = session.getAccessToken();

            SupabaseConfig authConfig = new SupabaseConfig.Builder(this.config.getSupabaseUrl().toString(), this.config.getSupabaseKey())
                    .addHeader("Authorization", "Bearer " + jwt)
                    .withSchema(this.config.getSchema())
                    .build();

            // 2. Replace the Postgrest client
            this.postgrest.set(new PostgrestClient(authConfig));

            // 3. Replace the Storage client ¡AÑADIDO!
            this.storage.set(new StorageClient(authConfig));

            // 4. Notify the Realtime client of the new token
            this.realtime.setAuth(jwt);

        } else if (event == AuthChangeEvent.SIGNED_OUT) {
            // 1. Revert Postgrest to the anonymous config
            this.postgrest.set(new PostgrestClient(this.config));

            // 2. Revert Storage to the anonymous config ¡AÑADIDO!
            this.storage.set(new StorageClient(this.config));

            // 3. Notify Realtime client (revert to anon key)
            this.realtime.setAuth(this.config.getSupabaseKey());
        }
    }

    /**
     * Creates a new SupabaseClient instance using the provided configuration.
     * This is the main factory method for the SDK.
     *
     * @param config The SupabaseConfig built using its Builder.
     * @return A new SupabaseClient instance.
     */
    public static SupabaseClient create(SupabaseConfig config) {
        return new SupabaseClient(config);
    }


    // --- Client Accessors ---

    /**
     * Accessor for the Authentication client.
     *
     * @return The AuthClient instance.
     */
    public AuthClient auth() {
        return this.auth;
    }


    /**
     * Accessor for the Postgrest client (para .rpc(), etc.).
     *
     * @return The *current* PostgrestClient instance (authenticated or not).
     */
    public PostgrestClient postgrest() {
        return this.postgrest.get();
    }

    /**
     * Accessor for the Realtime client.
     *
     * @return The RealtimeClient instance.
     */
    public RealtimeClient realtime() {
        return this.realtime;
    }

    /**
     * Accessor for the Storage client.
     *
     * @return The *current* StorageClient instance (authenticated or not).
     */
    public StorageClient storage() {
        return this.storage.get();
    }
}