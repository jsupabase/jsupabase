package io.github.jsupabase.postgrest;

import com.fasterxml.jackson.core.type.TypeReference; // For Map conversion
import io.github.jsupabase.core.util.JsonUtil;
import io.github.jsupabase.postgrest.builder.PostgrestFilterBuilder; // <-- 1. Extends this
import io.github.jsupabase.postgrest.enums.CountType; // <-- Import refactored Enum
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
 * Builds and executes a PostgREST Remote Procedure Call (RPC).
 * This class now inherits from PostgrestFilterBuilder, allowing
 * filters (.eq, .gt, etc.) on functions that return table sets.
 *
 * @author neilhdezs
 * @version 0.0.4
 */
public class PostgrestRpcBuilder extends PostgrestFilterBuilder<PostgrestRpcBuilder> {

    /** - LOGGER - **/
    private static final Logger LOG = LoggerFactory.getLogger(PostgrestRpcBuilder.class);

    /** - The arguments (parameters) for the function (POJO or Map) - **/
    private final Object args;

    /** - Headers specific to this RPC request - **/
    private final Map<String, String> headers = new HashMap<>();

    /** - Set of 'Prefer' header values (e.g., count=exact) - **/
    private final Set<String> prefer = new HashSet<>();

    /** - Flag for read-only (GET) request (doc: "get: true") - **/
    private boolean isReadOnly = false;

    /**
     * Creates a new PostgrestRpcBuilder.
     *
     * @param client       The active PostgrestClient.
     * @param functionName The name of the Postgres function to call.
     * @param args         The arguments (parameters) for the function.
     */
    // 3. CONSTRUCTOR UPDATED
    public PostgrestRpcBuilder(PostgrestClient client, String functionName, Object args) {
        // PostgREST treats 'rpc/' as the "table"
        super(client, "rpc/" + Objects.requireNonNull(functionName, "Function name cannot be null"));
        this.args = args; // args can be null
    }

    /**
     * Implements the 'self()' method for the generic builder pattern.
     */
    @Override
    protected PostgrestRpcBuilder self() {
        return this;
    }


    // --- MODIFIERS ---

    /**
     * Requests that the RPC response includes columns via a SELECT query string.
     *
     * @param columns The columns to retrieve (e.g., "*, my_column").
     * @return this (for chaining)
     */
    public PostgrestRpcBuilder select(String columns) {
        this.queryParams.put("select", columns); // 'queryParams' is inherited
        return this;
    }

    /**
     * Requests the total count of rows for the query (if the function returns a table).
     *
     * @param type The counting algorithm (EXACT, PLANNED, ESTIMATED).
     * @return this (for chaining)
     */
    public PostgrestRpcBuilder count(CountType type) {
        this.prefer.add("count=" + type.getValue());
        return this;
    }

    /**
     * Sets the request to read-only mode (doc: "get: true").
     * The execution logic will use a GET request instead of POST.
     *
     * @return this (for chaining)
     */
    public PostgrestRpcBuilder readOnly() {
        this.isReadOnly = true;
        return this;
    }

    /**
     * Requests only the headers and count, without the body (doc: "head: true").
     *
     * @return this (for chaining)
     */
    public PostgrestRpcBuilder head() {
        this.prefer.add("head");
        return this;
    }

    // --- EXECUTION METHOD ---

    /**
     * 5. REAL .EXECUTE() METHOD
     * Executes the built RPC call asynchronously.
     *
     * @return A CompletableFuture that will contain the response body as a String.
     */
    public CompletableFuture<String> execute() {
        try {
            HttpRequest.Builder requestBuilder;
            String path;

            if (isReadOnly) {
                // --- GET Request (read-only) ---
                // Filters (from parent) AND args must all be query params.

                // 1. Merge args (if they exist) into queryParams
                if (args != null) {
                    // Convert POJO/Map into a Map<String, String> for query params
                    Map<String, String> argsMap = JsonUtil.convertValue(args, new TypeReference<Map<String, String>>() {});
                    this.queryParams.putAll(argsMap);
                }

                // 2. Build path with all params (filters + args)
                path = buildPath(); // buildPath() is inherited

                requestBuilder = client.newRequest(path); // client is inherited
                requestBuilder.GET();

                // 3. Add GET-specific headers (Prefer, local headers)
                String preferHeader = String.join(",", this.prefer);
                if (!preferHeader.isEmpty()) {
                    requestBuilder.header("Prefer", preferHeader);
                }
                for (Map.Entry<String, String> header : this.headers.entrySet()) {
                    requestBuilder.header(header.getKey(), header.getValue());
                }
                LOG.debug("Executing RPC (GET): {}", requestBuilder.build().uri());

            } else {
                // --- POST Request (default) ---
                // Filters (from parent) are query params. Args is JSON body.

                // 1. Serialize args to body
                String jsonBody = (args == null) ? "{}" : JsonUtil.toJson(args);

                // 2. Call the mutation helper (from PostgrestBaseBuilder)
                //    This builds the path and adds 'Prefer', 'Content-Type', and local 'headers'
                requestBuilder = buildMutationRequestBuilder(this.prefer, this.headers);

                // 3. Set the body for the POST request
                requestBuilder.POST(HttpRequest.BodyPublishers.ofString(jsonBody));
                LOG.debug("Executing RPC (POST): {}", requestBuilder.build().uri());
            }

            // 4. DELEGATE the call to the client
            return client.sendAsyncString(requestBuilder.build());

        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }
}