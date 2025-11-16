package io.github.jsupabase.postgrest.clients;

import io.github.jsupabase.core.client.HttpClientBase;
import io.github.jsupabase.core.config.SupabaseConfig;
import io.github.jsupabase.postgrest.builder.PostgrestRpcBuilder;

/**
 * Client for executing PostgreSQL Remote Procedure Calls (RPC).
 * <p>
 * This client is instantiated once by {@link io.github.jsupabase.postgrest.PostgrestClient}
 * and accessed via `postgrest.rpc()`.
 *
 * @author neilhdezs
 * @version 0.1.0
 * @since 0.1.0 (Refactored to Gateway pattern)
 */
public class PostgrestRpcClient extends HttpClientBase {

    /**
     * Package-private constructor, called by PostgrestClient (Gateway).
     *
     * @param config The Supabase configuration.
     */
    public PostgrestRpcClient(SupabaseConfig config) {
        super(config);
    }

    /**
     * Calls a PostgreSQL function.
     *
     * @param functionName The name of the function to call.
     * @param args         The arguments (parameters) for the function (POJO or Map).
     * @return A PostgrestRpcBuilder.
     */
    public PostgrestRpcBuilder call(String functionName, Object args) {
        return new PostgrestRpcBuilder(this.config, functionName, args);
    }

    /**
     * (Convenience overload) Calls a PostgreSQL function without arguments.
     *
     * @param functionName The name of the function to call.
     * @return A PostgrestRpcBuilder.
     */
    public PostgrestRpcBuilder call(String functionName) {
        return call(functionName, null);
    }
}