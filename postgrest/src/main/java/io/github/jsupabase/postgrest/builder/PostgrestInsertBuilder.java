package io.github.jsupabase.postgrest.builder;

import io.github.jsupabase.core.util.JsonUtil;
import io.github.jsupabase.postgrest.PostgrestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.http.HttpRequest;
import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * Builds and executes a PostgREST INSERT query.
 *
 * @author neilhdezs
 * @version 0.0.3
 */
public class PostgrestInsertBuilder extends PostgrestBaseBuilder<PostgrestInsertBuilder> {

    /** - LOGGER - **/
    private static final Logger LOG = LoggerFactory.getLogger(PostgrestInsertBuilder.class);

    /** - The data to be inserted (POJO, Map, or List<...>) - **/
    private final Object data;

    /** - Headers specific to this INSERT request - **/
    private final Map<String, String> headers = new HashMap<>();

    /** - Set of 'Prefer' header values (e.g., return=representation, resolution=merge-duplicates) - **/
    private final Set<String> prefer = new HashSet<>();

    /**
     * Creates a new PostgrestInsertBuilder.
     *
     * @param client The active PostgrestClient.
     * @param table  The database table to query.
     * @param data   The data (POJO, Map, or List) to insert.
     */
    public PostgrestInsertBuilder(PostgrestClient client, String table, Object data) {
        // Llama al constructor de PostgrestBaseBuilder
        super(client, table);
        this.data = Objects.requireNonNull(data, "Data object cannot be null");

        // By default, an INSERT returns nothing
        this.prefer.add("return=minimal");
    }

    /**
     * Implements the 'self()' method for the generic builder pattern.
     */
    @Override
    protected PostgrestInsertBuilder self() {
        return this;
    }

    /**
     * Replaces the old `.returningRepresentation()`.
     * Requests that PostgREST return the inserted row(s) in the response,
     * specifying which columns to return.
     *
     * @param columns The columns to retrieve (e.g., "id, name").
     * @return this (for Builder chaining)
     */
    public PostgrestInsertBuilder select(String columns) {
        this.prefer.remove("return=minimal");
        this.prefer.add("return=representation");
        this.queryParams.put("select", columns); // 'queryParams' es del padre
        return this;
    }

    /**
     * Replaces the old `.returningRepresentation()`.
     * Requests that PostgREST return all columns of the inserted row(s).
     * (Convenience method for select("*"))
     *
     * @return this (for Builder chaining)
     */
    public PostgrestInsertBuilder select() {
        return select("*");
    }

    /**
     * Specifies the column(s) to check for a conflict.
     *
     * @param onConflict The column name(s) (e.g., "id", "col1,col2").
     * @return this (for Builder chaining)
     */
    public PostgrestInsertBuilder onConflict(String onConflict) {
        this.queryParams.put("on_conflict", onConflict);
        return this;
    }

    /**
     * Performs an "UPSERT" operation.
     * Requires .onConflict() to be called first.
     * (Prefer: resolution=merge-duplicates)
     *
     * @return this (for Builder chaining)
     */
    public PostgrestInsertBuilder upsert() {
        // PostgREST requiere el header 'Prefer' para el 'upsert'
        this.prefer.add("resolution=merge-duplicates");
        return this;
    }

    /**
     * Executes the built INSERT query asynchronously.
     *
     * @return A CompletableFuture that will contain the response body as a String.
     */
    public CompletableFuture<String> execute() {
        try {
            // 1. Serialize the body (the data to insert)
            String jsonBody = JsonUtil.toJson(this.data);

            // 2. Call the new helper method from PostgrestBaseBuilder
            //    Esto construye la URI y añade headers comunes (Prefer, Content-Type)
            HttpRequest.Builder requestBuilder = buildMutationRequestBuilder(this.prefer, this.headers);

            // 3. Build the final POST request
            HttpRequest request = requestBuilder
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            LOG.debug("Executing INSERT (POST): {}", request.uri());

            // 4. DELEGATE the call to the client
            return client.sendAsyncString(request);

        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }
}