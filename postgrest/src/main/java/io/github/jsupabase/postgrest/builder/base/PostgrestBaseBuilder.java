package io.github.jsupabase.postgrest.builder.base;

import io.github.jsupabase.core.client.HttpClientBase;
import io.github.jsupabase.core.config.SupabaseConfig;

import java.net.URLEncoder;
import java.net.http.HttpRequest;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * The abstract base class for all PostgREST builders.
 * (Refactored to extend HttpClientBase and use the Gateway architecture)
 *
 * @param <T> The type of the concrete builder (for fluent chaining)
 * @author neilhdezs
 * @version 0.1.0
 * @since 0.1.0 (Refactored)
 */
public abstract class PostgrestBaseBuilder<T extends PostgrestBaseBuilder<T>> extends HttpClientBase {

    /** - The root path for the service (e.g., "/rest/v1") - **/
    protected final String servicePath;

    /** - The database table or RPC function name - **/
    protected final String table;

    /** - Stores all query parameters (filters, order, select, etc.) - **/
    protected final Map<String, String> queryParams = new HashMap<>();

    /**
     * Creates a new PostgrestBaseBuilder.
     *
     * @param config The shared SupabaseConfig (para heredarla en HttpClientBase).
     * @param table  The database table or RPC function name.
     */
    public PostgrestBaseBuilder(SupabaseConfig config, String table) {
        // Llama al constructor de HttpClientBase
        super(config);
        this.servicePath = config.getPostgrestPath();
        this.table = Objects.requireNonNull(table, "Table or function name cannot be null");
    }

    /**
     * Returns the concrete instance (this) for fluent chaining.
     */
    protected abstract T self();

    /**
     * Builds the final path for the request, including the table and query params.
     *
     * @return The path string (e.g., "/rest/v1/my_table?select=*&id=eq.5")
     */
    protected String buildPath() {
        // El path base ahora usa el servicePath local
        String pathPrefix = this.servicePath + "/" + this.table;

        // Lógica de QueryParams
        String queryString = queryParams.entrySet().stream()
                .map(entry -> {
                    String key = URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8);
                    String value = URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8);
                    return key + "=" + value;
                })
                .collect(Collectors.joining("&"));

        // Lógica de Schema (ahora usa 'this.config' heredado de HttpClientBase)
        String schema = this.config.getSchema();
        if (!"public".equals(schema)) {
            String schemaParam = "schema=" + schema;
            if (queryString.isEmpty()) {
                queryString = schemaParam;
            } else {
                queryString += "&" + schemaParam;
            }
        }

        if (queryString.isEmpty()) {
            return pathPrefix;
        } else {
            return pathPrefix + "?" + queryString;
        }
    }

    /**
     * Protected helper for mutation builders (Insert, Update, Rpc)
     * to build the common parts of a request that sends a JSON body.
     *
     * @param prefer  The Set of 'Prefer' headers.
     * @param headers The Map of local headers.
     * @return A pre-configured HttpRequest.Builder.
     */
    protected HttpRequest.Builder buildMutationRequestBuilder(Set<String> prefer, Map<String, String> headers) {
        // 1. Build the path (Usa el buildPath() modificado)
        String path = buildPath();

        // 2. Build the 'Prefer' header
        String preferHeader = String.join(",", prefer);

        // 3. Create a request builder (Usa 'this.newRequest()' heredado)
        HttpRequest.Builder requestBuilder = this.newRequest(path)
                .header("Prefer", preferHeader)
                .header("Content-Type", "application/json");

        // 4. Add any other local headers
        for (Map.Entry<String, String> header : headers.entrySet()) {
            requestBuilder.header(header.getKey(), header.getValue());
        }

        return requestBuilder;
    }
}