package io.github.jsupabase.postgrest.builder;

import io.github.jsupabase.core.config.SupabaseConfig;
import io.github.jsupabase.postgrest.builder.base.PostgrestFilterBuilder;
import io.github.jsupabase.postgrest.enums.CountType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.http.HttpRequest;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 * Builds and executes a PostgREST DELETE query.
 * <p>
 * (Refactored to align with the new Gateway architecture and inherit network capabilities.)
 *
 * @author neilhdezs
 * @version 2.0.0 // Version updated
 */ // TODO IMPLEMENTAR UNA FORMA DE QUE EL DELETE TIENE QUE LLEVAR UN WHERE OBLIGATORIO
public class PostgrestDeleteBuilder extends PostgrestFilterBuilder<PostgrestDeleteBuilder> {

    /** - SLF4J Logger - **/
    private static final Logger LOG = LoggerFactory.getLogger(PostgrestDeleteBuilder.class);

    /** - Headers specific to this DELETE request - **/
    private final Map<String, String> headers = new HashMap<>();

    /** - Set of 'Prefer' header values - **/
    private final Set<String> prefer = new HashSet<>();

    /**
     * Creates a new PostgrestDeleteBuilder.
     * <p>
     * This constructor is called by {@code PostgrestTableClient}.
     *
     * @param config The shared SupabaseConfig, passed to the base constructor.
     * @param table  The database table to query.
     */
    public PostgrestDeleteBuilder(SupabaseConfig config, String table) {
        super(config, table);

        this.prefer.add("return=minimal");
    }

    /**
     * Implements the 'self()' method for the generic builder pattern,
     * ensuring all inherited methods return the correct concrete type.
     */
    @Override
    protected PostgrestDeleteBuilder self() {
        return this;
    }

    /**
     * Requests that PostgREST return the deleted row(s) in the response,
     * specifying which columns to return.
     *
     * @param columns The columns to retrieve (e.g., "id, name").
     * @return this (for Builder chaining)
     */
    public PostgrestDeleteBuilder select(String columns) {
        this.prefer.remove("return=minimal");
        this.prefer.add("return=representation");
        this.queryParams.put("select", columns); // 'queryParams' es del padre
        return this;
    }

    public PostgrestDeleteBuilder select() {
        return select("*");
    }

    /**
     * Requests the total count of rows deleted.
     *
     * @param type The algorithm for counting (EXACT, PLANNED, ESTIMATED).
     * @return this (for Builder chaining)
     */
    public PostgrestDeleteBuilder count(CountType type) {
        this.prefer.add("count=" + type.getValue());
        return this;
    }

    public PostgrestDeleteBuilder count() {
        return count(CountType.EXACT);
    }

    // --- Execution Method ---

    /**
     * Executes the built DELETE query asynchronously.
     *
     * @return A CompletableFuture that will contain the response body as a String.
     */
    public CompletableFuture<String> execute() {
        try {
            // 1. Build the path with filters (inherited)
            String path = buildPath();

            // 2. Build the 'Prefer' header
            String preferHeader = String.join(",", this.prefer);

            // 3. Create a request builder (Usa this.newRequest() heredado)
            HttpRequest.Builder requestBuilder = this.newRequest(path)
                    .header("Prefer", preferHeader);

            // 4. Add any other local headers
            for (Map.Entry<String, String> header : this.headers.entrySet()) {
                requestBuilder.header(header.getKey(), header.getValue());
            }

            // 5. Build the final DELETE request
            HttpRequest request = requestBuilder.DELETE().build();

            LOG.debug("Executing DELETE: {}", request.uri());

            // 6. DELEGATE the call to the inherited sendAsyncString
            return this.sendAsyncString(request);

        } catch (Exception e) {
            LOG.error("Failed to build Postgrest DELETE request", e);
            return CompletableFuture.failedFuture(e);
        }
    }
}