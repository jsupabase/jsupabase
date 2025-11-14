package io.github.jsupabase.client;

import io.github.jsupabase.auth.AuthClient;
import io.github.jsupabase.auth.dto.Session;
import io.github.jsupabase.auth.events.AuthChangeEvent;
import io.github.jsupabase.core.config.SupabaseConfig;
import io.github.jsupabase.postgrest.PostgrestClient;
import io.github.jsupabase.postgrest.PostgrestQueryBuilder;
import io.github.jsupabase.postgrest.builder.PostgrestSelectBuilder; // Importa el builder

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference; // Para un estado thread-safe

/**
 * Main entry point for the jsupabase SDK.
 * This class aggregates all specialized clients (Auth, Postgrest, Realtime, Storage).
 * It manages the authentication state and injects the JWT into other clients.
 *
 * @author neilhdezs
 * @version 0.1.0 // Versión inicial del cliente principal
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
    // private final RealtimeClient realtime;

    /** - Specialized client for Storage (Files) - **/
    // private final StorageClient storage;

    /**
     * Private constructor. Use the static create() method.
     *
     * @param config The global *anonymous* configuration object.
     */
    private SupabaseClient(SupabaseConfig config) {
        this.config = Objects.requireNonNull(config, "SupabaseConfig cannot be null");

        // --- Initialize specialized clients ---

        // 1. AuthClient se crea con la config anónima
        this.auth = new AuthClient(this.config);

        // 2. PostgrestClient se inicializa anónimo, pero en un AtomicReference
        this.postgrest = new AtomicReference<>(new PostgrestClient(this.config));

        // 3. ¡LA MAGIA! Nos suscribimos a los eventos de Auth.
        this.auth.onAuthStateChange(this::handleAuthStateChange);

        // this.realtime = new RealtimeClient(this.config);
        // this.storage = new StorageClient(this.config);
    }

    /**
     * This is the core "glue" of the SDK.
     * It listens for auth events and creates a new, authenticated
     * PostgrestClient when the user signs in.
     */
    private void handleAuthStateChange(AuthChangeEvent event, Session session) {
        if (event == AuthChangeEvent.SIGNED_IN || event == AuthChangeEvent.TOKEN_REFRESHED) {
            // Usuario ha iniciado sesión.
            // 1. Obtener el nuevo token JWT.
            String jwt = session.getAccessToken();

            // 2. Crear una nueva config *autenticada*
            SupabaseConfig authConfig = new SupabaseConfig.Builder(
                    this.config.getSupabaseUrl().toString(),
                    this.config.getSupabaseKey()
            )
                    .addHeader("Authorization", "Bearer " + jwt) // ¡Añade el token!
                    .withSchema(this.config.getSchema()) // Mantén el schema
                    .build();

            // 3. Reemplazar el cliente Postgrest por uno nuevo y autenticado.
            this.postgrest.set(new PostgrestClient(authConfig));

        } else if (event == AuthChangeEvent.SIGNED_OUT) {
            // Usuario ha cerrado sesión.
            // 1. Volver a la configuración anónima original.
            this.postgrest.set(new PostgrestClient(this.config));
        }
        // (Otros eventos como USER_UPDATED no requieren cambiar el cliente,
        // ya que el JWT sigue siendo el mismo).
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

    /**
     * Getter for the configuration.
     *
     * @return The SupabaseConfig instance.
     */
    public SupabaseConfig getConfig() {
        return this.config;
    }

    // --- Client Accessors ---

    /**
     * Accessor for the Authentication client.
     * @return The AuthClient instance.
     */
    public AuthClient auth() {
        return this.auth;
    }

    /**
     * Creates a new Postgrest query builder for a specific table.
     * 'from' is the traditional Supabase entry point for Postgrest.
     *
     * @param table The table to query.
     * @return A PostgrestSelectBuilder instance.
     */
    public PostgrestQueryBuilder from(String table) {
        // Obtenemos el cliente Postgrest *actual* (que puede ser anónimo o auth)
        return this.postgrest.get().from(table);
    }

    /**
     * Accessor for the Postgrest client (para .rpc(), etc.).
     * @return The *current* PostgrestClient instance (authenticated or not).
     */
    public PostgrestClient postgrest() {
        return this.postgrest.get();
    }

    /*
    public RealtimeClient realtime() {
        return this.realtime;
    }
    */

    /*
    public StorageClient storage() {
        return this.storage;
    }
    */
}