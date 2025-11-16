package io.github.jsupabase.postgrest.clients;

import io.github.jsupabase.core.client.HttpClientBase;
import io.github.jsupabase.core.config.SupabaseConfig;
import io.github.jsupabase.postgrest.builder.PostgrestDeleteBuilder;
import io.github.jsupabase.postgrest.builder.PostgrestInsertBuilder;
import io.github.jsupabase.postgrest.builder.PostgrestSelectBuilder;
import io.github.jsupabase.postgrest.builder.PostgrestUpdateBuilder;

import java.util.Objects;

/**
 * Client for operations on a specific database table.
 * <p>
 * This class replaces the old PostgrestQueryBuilder and acts as a
 * factory for the specialized query builders (Select, Insert, etc.).
 * <p>
 * Instantiated via `postgrest.table("my_table")`.
 *
 * @author neilhdezs
 * @version 0.1.0
 * @since 0.1.0 (Refactored to Gateway pattern)
 */
public class PostgrestTableClient extends HttpClientBase {

    /** - The path prefix for this service (e.g., "/rest/v1") - **/
    private final String servicePath;

    /** - The database table this client is bound to - **/
    private final String table;

    /**
     * Package-private constructor, called by PostgrestClient (Gateway).
     *
     * @param config The Supabase configuration.
     * @param table  The table name this client is bound to.
     */
    public PostgrestTableClient(SupabaseConfig config, String table) {
        super(config);
        this.servicePath = config.getPostgrestPath();
        this.table = Objects.requireNonNull(table, "Table name cannot be null");
    }

    /**
     * Prepares a SELECT query.
     * <p>
     * <strong>This is the entry point for joins.</strong>
     * Example: {@code .select("id, name, foreign_table(id, name)")}
     *
     * @param columns The columns to select (e.g., "id, name").
     * @return A PostgrestSelectBuilder.
     */
    public PostgrestSelectBuilder select(String columns) {
        return new PostgrestSelectBuilder(this.config, this.table, columns);
    }

    /**
     * (Convenience overload) Prepara una consulta SELECT para todas las columnas ("*").
     */
    public PostgrestSelectBuilder select() {
        return select("*");
    }

    /**
     * Prepares an INSERT operation.
     *
     * @param data The data to insert (POJO, Map, or List<...>).
     * @return A PostgrestInsertBuilder.
     */
    public PostgrestInsertBuilder insert(Object data) {
        return new PostgrestInsertBuilder(this.config, this.table, data);
    }

    /**
     * Prepares an UPDATE operation.
     *
     * @param data The data (POJO or Map) to update.
     * @return A PostgrestUpdateBuilder.
     */
    public PostgrestUpdateBuilder update(Object data) {
        return new PostgrestUpdateBuilder(this.config, this.table, data);
    }

    /**
     * Prepares a DELETE operation.
     *
     * @return A PostgrestDeleteBuilder.
     */
    public PostgrestDeleteBuilder delete() {
        return new PostgrestDeleteBuilder(this.config, this.table);
    }
}