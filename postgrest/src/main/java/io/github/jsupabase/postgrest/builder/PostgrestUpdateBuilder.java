package io.github.jsupabase.postgrest.builder;

import io.github.jsupabase.core.util.JsonUtil;
import io.github.jsupabase.postgrest.PostgrestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.http.HttpRequest;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 * Builds and executes a PostgREST UPDATE query (HTTP PATCH).
 * This class inherits all filter methods (.eq, .gt, etc.) from
 * PostgrestFilterBuilder.
 *
 * @author neilhdezs
 * @version 0.0.3
 */
public class PostgrestUpdateBuilder extends PostgrestFilterBuilder<PostgrestUpdateBuilder> {

    /** - LOGGER - **/
    private static final Logger LOG = LoggerFactory.getLogger(PostgrestUpdateBuilder.class);

    /** - The data to be updated (POJO or Map) - **/
    private final Object data;

    /** - Headers specific to this UPDATE request - **/
    private final Map<String, String> headers = new HashMap<>();

    /** - Set of 'Prefer' header values - **/
    private final Set<String> prefer = new HashSet<>();

    /**
     * Creates a new PostgrestUpdateBuilder.
     *
     * @param client The active PostgrestClient.
     * @param table  The database table to query.
     * @param data   The data (POJO or Map) to update.
     */
    public PostgrestUpdateBuilder(PostgrestClient client, String table, Object data) {
        // Pasa el cliente y la tabla al constructor padre (PostgrestFilterBuilder)
        super(client, table);
        this.data = Objects.requireNonNull(data, "Data object cannot be null");

        // Por defecto, un UPDATE no devuelve nada
        this.prefer.add("return=minimal");
    }

    /**
     * Implements the 'self()' method for the generic builder pattern,
     * ensuring all inherited methods return the correct concrete type.
     */
    @Override
    protected PostgrestUpdateBuilder self() {
        return this;
    }

    /**
     * Requests that PostgREST return the updated row(s) in the response,
     * specifying which columns to return.
     *
     * @param columns The columns to retrieve (e.g., "id, name").
     * @return this (for Builder chaining)
     */
    public PostgrestUpdateBuilder select(String columns) {
        this.prefer.remove("return=minimal");
        this.prefer.add("return=representation");
        this.queryParams.put("select", columns); // 'queryParams' es del padre
        return this;
    }

    /**
     * Requests that PostgREST return all columns of the updated row(s).
     * (Convenience method for select("*"))
     *
     * @return this (for Builder chaining)
     */
    public PostgrestUpdateBuilder select() {
        return select("*");
    }


    // --- Execution Method ---

    /**
     * Executes the built UPDATE query asynchronously.
     *
     * @return A CompletableFuture that will contain the response body as a String.
     */
    public CompletableFuture<String> execute() {
        try {
            // 1. Serialize the body (the data to update)
            String jsonBody = JsonUtil.toJson(this.data);

            // 2. Call the new helper method from PostgrestBaseBuilder
            //    Esto construye la URI y añade headers comunes (Prefer, Content-Type)
            HttpRequest.Builder requestBuilder = buildMutationRequestBuilder(this.prefer, this.headers);

            // 3. Build the final PATCH request (UPDATE en PostgREST)
            HttpRequest request = requestBuilder
                    .method("PATCH", HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            LOG.debug("Executing UPDATE (PATCH): {}", request.uri());

            // 4. DELEGATE the call to the client
            return client.sendAsyncString(request);

        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }
}