package io.github.jsupabase.postgrest.builder;

import io.github.jsupabase.core.config.SupabaseConfig;
import io.github.jsupabase.core.util.JsonUtil;
import io.github.jsupabase.postgrest.builder.base.PostgrestFilterBuilder;
import io.github.jsupabase.postgrest.enums.CountType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.http.HttpRequest;
import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * Builds and executes a PostgREST INSERT query.
 *
 * @author neilhdezs
 * @version 0.1.0
 * @since 0.1.0
 */
public class PostgrestInsertBuilder extends PostgrestFilterBuilder<PostgrestInsertBuilder> {

    /** - SLF4J Logger - **/
    private static final Logger LOG = LoggerFactory.getLogger(PostgrestInsertBuilder.class);

    /** - The data object(s) to insert - **/
    private final Object data;

    /** - Headers specific to this INSERT request - **/
    private final Map<String, String> headers = new HashMap<>();

    /** - Set of 'Prefer' header values (e.g., return=minimal) - **/
    private final Set<String> prefer = new HashSet<>();

    /**
     * Creates a new PostgrestInsertBuilder.
     *
     * @param config The shared SupabaseConfig.
     * @param table  The database table to query.
     * @param data   The data (POJO, Map, or List) to insert.
     */
    public PostgrestInsertBuilder(SupabaseConfig config, String table, Object data) {
        super(config, table);
        this.data = Objects.requireNonNull(data, "Data cannot be null for INSERT.");

        // Por defecto, PostgREST devuelve el objeto insertado (return=representation)
        this.prefer.add("return=representation");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected PostgrestInsertBuilder self() {
        return this;
    }

    /**
     * Specifies the total count of rows inserted.
     *
     * @param type The counting algorithm (EXACT, PLANNED, ESTIMATED).
     * @return this (for Builder chaining)
     */
    public PostgrestInsertBuilder count(CountType type) {
        this.prefer.add("count=" + type.getValue());
        return this;
    }

    /**
     * Specifies the columns to return in the response body.
     * This implicitly sets 'return=representation' if 'return=minimal' was not set.
     *
     * @param columns The columns to retrieve (e.g., "id, name").
     * @return this (for Builder chaining)
     */
    public PostgrestInsertBuilder select(String columns) {
        this.queryParams.put("select", columns);
        return this;
    }

    /**
     * Requests that PostgREST return no body, only headers.
     * (Prefer: return=minimal)
     *
     * @return this (for Builder chaining)
     */
    public PostgrestInsertBuilder preferReturnMinimal() {
        this.prefer.remove("return=representation");
        this.prefer.add("return=minimal");
        return this;
    }

    /**
     * Configures the behavior when a conflict occurs (i.e., when performing an {@code INSERT}
     * where the primary key or unique constraint is already present).
     *
     * @param columns The constraint columns to check for conflict.
     * @return this (for Builder chaining)
     */
    public PostgrestInsertBuilder onConflict(String columns) {
        this.headers.put("Prefer", "resolution=ignore-duplicates, constraint=" + columns);
        return this;
    }

    /**
     * Performs an UPSERT operation (insert or update on conflict).
     *
     * @param columns The constraint columns to check for conflict.
     * @return this (for Builder chaining)
     */
    public PostgrestInsertBuilder upsert(String columns) {
        this.prefer.add("resolution=merge-duplicates, constraint=" + columns);
        return this;
    }

    /**
     * Executes the built INSERT query asynchronously using a POST request.
     *
     * @return A CompletableFuture that will contain the response body as a String.
     */
    public CompletableFuture<String> execute() {
        try {
            String jsonBody = JsonUtil.toJson(this.data);

            // Mutation helper builds path, headers, and adds Content-Type
            HttpRequest.Builder requestBuilder = buildMutationRequestBuilder(this.prefer, this.headers);

            HttpRequest request = requestBuilder.POST(HttpRequest.BodyPublishers.ofString(jsonBody)).build();

            LOG.debug("Executing INSERT: {}", request.uri());

            return this.sendAsyncString(request);

        } catch (Exception e) {
            LOG.error("Failed to build Postgrest INSERT request", e);
            return CompletableFuture.failedFuture(e);
        }
    }
}