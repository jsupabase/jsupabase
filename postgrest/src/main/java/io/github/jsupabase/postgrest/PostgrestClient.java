package io.github.jsupabase.postgrest;

import io.github.jsupabase.core.client.HttpClientBase;
import io.github.jsupabase.core.config.SupabaseConfig;
import io.github.jsupabase.postgrest.clients.PostgrestRpcClient;
import io.github.jsupabase.postgrest.clients.PostgrestTableClient;

import java.util.Objects;

/**
 * Main entry point for the Supabase PostgREST API.
 * <p>
 * This class acts as a <strong>Gateway</strong> (or Factory) that provides access
 * to specialized clients for Table and RPC operations.
 * <p>
 * It <strong>extends HttpClientBase</strong> to inherit and manage the authentication
 * state (JWT) from the main SupabaseClient, passing it down to all
 * specialized sub-clients.
 *
 * @author neilhdezs
 * @version 1.0.0 (Refactored to Gateway pattern)
 */
public class PostgrestClient extends HttpClientBase {

    /** - Client for RPC operations (GET/POST /rpc/{function}) - **/
    private final PostgrestRpcClient rpcClient;

    /**
     * Creates a new PostgrestClient (Gateway).
     *
     * @param config The client configuration.
     */
    public PostgrestClient(SupabaseConfig config) {
        super(config);

        this.rpcClient = new PostgrestRpcClient(config);
    }

    /**
     * Provides access to the client for <strong>Table</strong> operations.
     * <p>
     * This is a factory method that creates a new client instance
     * bound to the specified {@code table}.
     *
     * @param table The name of the database table to interact with (e.g., "profiles").
     * @return A new {@link PostgrestTableClient} instance bound to that table.
     */
    public PostgrestTableClient table(String table) {
        return new PostgrestTableClient(this.config, table);
    }

    /**
     * Provides access to the client for <strong>RPC (Remote Procedure Call)</strong> operations.
     * <p>
     * Corresponds to the {@code /rpc} endpoint group.
     *
     * @return The singleton PostgrestRpcClient instance.
     */
    public PostgrestRpcClient rpc() {
        return this.rpcClient;
    }
}