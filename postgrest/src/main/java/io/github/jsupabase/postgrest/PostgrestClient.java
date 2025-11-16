package io.github.jsupabase.postgrest;

import io.github.jsupabase.core.client.HttpClientBase;
import io.github.jsupabase.core.config.SupabaseConfig;
import io.github.jsupabase.postgrest.clients.PostgrestRpcClient;
import io.github.jsupabase.postgrest.clients.PostgrestTableClient;

/**
 * - POSTGRESQL REST API CLIENT -
 * <p>
 * Gateway client for accessing Supabase PostgreSQL database operations via the PostgREST API.
 * This client provides a unified interface for both table-based CRUD operations and stored
 * procedure execution through specialized sub-clients.
 * <p>
 * The Gateway pattern implementation enables seamless access to different database operation
 * types while maintaining consistent authentication and configuration management. All
 * sub-clients automatically inherit the authentication state from the parent client,
 * ensuring proper JWT token propagation for authenticated database operations.
 * <p>
 * Table operations support comprehensive querying with filters, ordering, pagination,
 * and real-time subscriptions, while RPC operations enable execution of custom database
 * functions and stored procedures with type-safe parameter passing.
 *
 * @author neilhdezs
 * @version 0.1.0
 * @since 0.1.0
 */
public class PostgrestClient extends HttpClientBase {

    /** - Specialized client for database stored procedure and function execution - */
    private final PostgrestRpcClient rpcClient;

    /**
     * - POSTGREST CLIENT CONSTRUCTOR -
     * <p>
     * Initializes the PostgreSQL REST API client with the provided configuration
     * and creates specialized sub-clients for different operation types. The client
     * inherits HTTP functionality and authentication management from HttpClientBase.
     *
     * @param config Supabase configuration containing PostgreSQL REST API settings
     */
    public PostgrestClient(SupabaseConfig config) {
        super(config);
        this.rpcClient = new PostgrestRpcClient(config);
    }

    /**
     * - TABLE OPERATIONS CLIENT FACTORY -
     * <p>
     * Creates a new specialized client for database table operations including
     * SELECT, INSERT, UPDATE, DELETE with comprehensive filtering, ordering,
     * and pagination capabilities. Each table client is bound to a specific
     * database table and inherits the authentication context from the parent client.
     *
     * @param table Database table name for CRUD operations
     * @return New PostgrestTableClient instance configured for the specified table
     */
    public PostgrestTableClient table(String table) {
        return new PostgrestTableClient(this.config, table);
    }

    /**
     * - RPC OPERATIONS CLIENT ACCESS -
     * <p>
     * Provides access to the specialized client for executing database stored
     * procedures and functions via the PostgREST RPC endpoint. Enables type-safe
     * parameter passing and result handling for custom database logic execution.
     *
     * @return PostgrestRpcClient instance for stored procedure execution
     */
    public PostgrestRpcClient rpc() {
        return this.rpcClient;
    }
}