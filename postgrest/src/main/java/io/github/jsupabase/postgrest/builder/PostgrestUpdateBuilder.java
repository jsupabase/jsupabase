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
 * Builds and executes a PostgREST UPDATE query.
 *
 * @author neilhdezs
 * @version 0.1.0
 * @since 0.1.0
 */
public class PostgrestUpdateBuilder extends PostgrestFilterBuilder<PostgrestUpdateBuilder> {

    /** - SLF4J Logger - **/
    private static final Logger LOG = LoggerFactory.getLogger(PostgrestUpdateBuilder.class);

    /** - The data object to update - **/
    private final Object data;

    /** - Headers specific to this UPDATE request - **/
    private final Map<String, String> headers = new HashMap<>();

    /** - Set of 'Prefer' header values (e.g., return=minimal) - **/
    private final Set<String> prefer = new HashSet<>();

    /**
     * Creates a new PostgrestUpdateBuilder.
     *
     * @param config The shared SupabaseConfig.
     * @param table  The database table to query.
     * @param data   The data (POJO or Map) to update.
     */
    public PostgrestUpdateBuilder(SupabaseConfig config, String table, Object data) {
        super(config, table);
        this.data = Objects.requireNonNull(data, "Data cannot be null for UPDATE.");

        // Por defecto, PostgREST devuelve el objeto actualizado
        this.prefer.add("return=representation");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected PostgrestUpdateBuilder self() {
        return this;
    }

    /**
     * Specifies the total count of rows updated.
     *
     * @param type The algorithm for counting (EXACT, PLANNED, ESTIMATED).
     * @return this (for Builder chaining)
     */
    public PostgrestUpdateBuilder count(CountType type) {
        this.prefer.add("count=" + type.getValue());
        return this;
    }

    /**
     * Specifies the columns to return in the response body.
     *
     * @param columns The columns to retrieve (e.g., "id, name").
     * @return this (for Builder chaining)
     */
    public PostgrestUpdateBuilder select(String columns) {
        this.queryParams.put("select", columns);
        return this;
    }

    /**
     * Requests that PostgREST return no body, only headers.
     *
     * @return this (for Builder chaining)
     */
    public PostgrestUpdateBuilder preferReturnMinimal() {
        this.prefer.remove("return=representation");
        this.prefer.add("return=minimal");
        return this;
    }

    // --- Execution Method ---

    /**
     * Executes the built UPDATE query asynchronously using a PATCH request.
     *
     * @return A CompletableFuture that will contain the response body as a String.
     */
    public CompletableFuture<String> execute() {
        try {
            String jsonBody = JsonUtil.toJson(this.data);

            // Mutation helper builds path, headers, and adds Content-Type
            HttpRequest.Builder requestBuilder = buildMutationRequestBuilder(this.prefer, this.headers);

            // Build the final PATCH request
            HttpRequest request = requestBuilder
                    .method("PATCH", HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            LOG.debug("Executing UPDATE (PATCH): {}", request.uri());

            // DELEGATE the call to the inherited sendAsyncString
            return this.sendAsyncString(request);

        } catch (Exception e) {
            LOG.error("Failed to build Postgrest UPDATE request", e);
            return CompletableFuture.failedFuture(e);
        }
    }
}