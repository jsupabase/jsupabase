package io.github.jsupabase.postgrest.builder;

import com.fasterxml.jackson.core.type.TypeReference;
import io.github.jsupabase.core.config.SupabaseConfig;
import io.github.jsupabase.core.util.JsonUtil;
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
 * Builds and executes a PostgREST Remote Procedure Call (RPC).
 * <p>
 * This class inherits from {@link PostgrestFilterBuilder}, allowing
 * filters (.eq, .gt, etc.) to be applied to functions that return table sets
 * (when {@link #readOnly()} is used).
 * <p>
 * It is instantiated by {@code PostgrestClient.rpc().call()}.
 *
 * @author neilhdezs
 * @version 0.1.0
 * @since 0.1.0 (Refactored)
 */
public class PostgrestRpcBuilder extends PostgrestFilterBuilder<PostgrestRpcBuilder> {

    /** - SLF4J Logger - **/
    private static final Logger LOG = LoggerFactory.getLogger(PostgrestRpcBuilder.class);

    /** - The arguments (parameters) for the function (POJO or Map) - **/
    private final Object args;

    /** - Headers specific to this RPC request - **/
    private final Map<String, String> headers = new HashMap<>();

    /** - Set of 'Prefer' header values (e.g., "count=exact") - **/
    private final Set<String> prefer = new HashSet<>();

    /** - Flag to switch between POST (default) and GET - **/
    private boolean httpGet = false;

    /**
     * Creates a new PostgrestRpcBuilder.
     * <p>
     * This constructor is called by {@link io.github.jsupabase.postgrest.clients.PostgrestRpcClient}.
     * It configures the builder to target a specific function endpoint.
     *
     * @param config       The shared SupabaseConfig, passed to the HttpClientBase constructor.
     * @param functionName The name of the function to call (e.g., "my_function").
     * @param args         The arguments (parameters) for the function (a POJO or Map).
     */
    public PostgrestRpcBuilder(SupabaseConfig config, String functionName, Object args) {
        // Calls the super constructor, passing the full RPC path (e.g., "rpc/my_function")
        // as the "table" parameter for the base builder.
        super(config, "rpc/" + functionName);
        this.args = args;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected PostgrestRpcBuilder self() {
        return this;
    }

    /**
     * Switches the RPC call from the default <strong>POST</strong> to a <strong>GET</strong> request.
     * <p>
     * <strong>Note:</strong> When using GET, the {@code args} object provided
     * in the constructor is <strong>ignored</strong>. You must pass parameters
     * using the inherited filter methods (e.g., {@code .eq("name", "value")}).
     *
     * @return this (for Builder chaining).
     */
    public PostgrestRpcBuilder readOnly() {
        this.httpGet = true;
        return this;
    }

    /**
     * Specifies the count preference to retrieve the total number of rows.
     *
     * @param count The type of count to request (e.g., EXACT, PLANNED).
     * @return this (for Builder chaining).
     */
    public PostgrestRpcBuilder count(CountType count) {
        this.prefer.add("count".concat(count.getValue()));
        return this;
    }

    /**
     * (Convenience overload) Requests an exact count.
     */
    public PostgrestRpcBuilder count() {
        return count(CountType.EXACT);
    }

    /**
     * Requests that PostgREST return no body, only headers.
     * (Prefer: return=minimal)
     *
     * @return this (for Builder chaining)
     */
    public PostgrestRpcBuilder preferReturnMinimal() {
        this.prefer.add("return=minimal");
        return this;
    }

    // --- Execution Methods ---

    /**
     * Executes the built RPC query asynchronously.
     * <p>
     * This method builds the final {@link HttpRequest} based on whether
     * {@link #readOnly()} was called:
     * <ul>
     * <li><strong>POST (default):</strong> Sends parameters as a JSON body. Filters are ignored.</li>
     * <li><strong>GET:</strong> Sends parameters as filters (e.g., .eq()). The 'args' body is ignored.</li>
     * </ul>
     * It then delegates the network call to the inherited {@link #sendAsyncString(HttpRequest)} method.
     *
     * @return A {@code CompletableFuture} that will contain the response body as a String.
     */
    public CompletableFuture<String> execute() {
        try {
            HttpRequest.Builder requestBuilder;

            if (this.httpGet) {
                // --- GET Request ---
                String path = buildPath();
                requestBuilder = this.newRequest(path);

                String preferHeader = String.join(",", this.prefer);
                if (!preferHeader.isEmpty()) {
                    requestBuilder.header("Prefer", preferHeader);
                }

                for (Map.Entry<String, String> header : this.headers.entrySet()) {
                    requestBuilder.header(header.getKey(), header.getValue());
                }

                requestBuilder.GET();
                LOG.debug("Executing RPC (GET): {}", requestBuilder.build().uri());

            } else {
                // --- POST Request (default) ---
                String jsonBody = (args == null) ? "{}" : JsonUtil.toJson(args);

                // buildMutationRequestBuilder is inherited from PostgrestBaseBuilder
                requestBuilder = buildMutationRequestBuilder(this.prefer, this.headers);

                requestBuilder.POST(HttpRequest.BodyPublishers.ofString(jsonBody));
                LOG.debug("Executing RPC (POST): {}", requestBuilder.build().uri());
            }

            // Delegate the call to the inherited sendAsyncString
            return this.sendAsyncString(requestBuilder.build());

        } catch (Exception e) {
            // Catches any build-time errors
            LOG.error("Failed to build Postgrest RPC request", e);
            return CompletableFuture.failedFuture(e);
        }
    }

    /**
     * Executes the RPC query and deserializes the response to a specific POJO.
     *
     * @param <T>          The type of the POJO.
     * @param responseType The Class of the POJO.
     * @return A {@code CompletableFuture} with the deserialized POJO.
     */
    public <T> CompletableFuture<T> execute(Class<T> responseType) {
        return this.execute()
                .thenApply(json -> JsonUtil.fromJson(json, responseType));
    }

    /**
     * Executes the RPC query and deserializes the response to a generic type (e.g., List<POJO>).
     *
     * @param <T>     The generic type.
     * @param typeRef The TypeReference for deserialization (e.g., {@code new TypeReference<List<MyPojo>>(){}}).
     * @return A {@code CompletableFuture} with the deserialized generic type.
     */
    public <T> CompletableFuture<T> execute(TypeReference<T> typeRef) {
        return this.execute()
                .thenApply(json -> JsonUtil.fromJson(json, typeRef));
    }
}