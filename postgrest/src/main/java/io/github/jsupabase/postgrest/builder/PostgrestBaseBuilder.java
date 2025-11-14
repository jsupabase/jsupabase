package io.github.jsupabase.postgrest.builder;

import io.github.jsupabase.postgrest.PostgrestClient;

import java.io.UnsupportedEncodingException;
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
 * It holds the shared components: client, table, query parameters,
 * and the URL-building logic.
 *
 * @param <T> The type of the concrete builder (for fluent chaining)
 * @author neilhdezs
 * @version 0.0.4
 */
public abstract class PostgrestBaseBuilder<T extends PostgrestBaseBuilder<T>> {

    /** - The network client for this module - **/
    protected final PostgrestClient client;

    /** - The database table this query targets - **/
    protected final String table;

    /** - Stores all query parameters (filters, order, select, etc.) - **/
    protected final Map<String, String> queryParams = new HashMap<>();

    /**
     * Creates a new PostgrestBaseBuilder.
     *
     * @param client The active PostgrestClient.
     * @param table  The database table or RPC function name.
     */
    public PostgrestBaseBuilder(PostgrestClient client, String table) {
        this.client = Objects.requireNonNull(client, "PostgrestClient cannot be null");
        this.table = Objects.requireNonNull(table, "Table name cannot be null");
    }

    /**
     * Abstract method that concrete builders must implement to return themselves.
     * This allows for type-safe fluent chaining.
     *
     * @return The concrete builder instance (e.g., PostgrestSelectBuilder).
     */
    protected abstract T self();

    /**
     * Builds the final URL path and query string from the table and queryParams.
     * This is the single source of truth for URL building.
     *
     * @return A URL-encoded path string (e.g., "/my_table?id=eq.5&select=*")
     */
    protected String buildPath() {
        String schema = client.getConfig().getSchema();
        String pathPrefix = "/rest/v1/" + table;

        // Build the query string (e.g., "id=eq.5&select=*")
        String queryString = queryParams.entrySet().stream()
                .map(entry -> {
                    // URL-encode keys and values
                    String key = URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8);
                    String value = URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8);
                    return key + "=" + value;
                })
                .collect(Collectors.joining("&"));

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
     * Protected helper for mutation builders (Insert, Update)
     * to build the common parts of a request that sends a JSON body.
     *
     * @param prefer  The Set of 'Prefer' headers.
     * @param headers The Map of local headers.
     * @return A pre-configured HttpRequest.Builder.
     */
    protected HttpRequest.Builder buildMutationRequestBuilder(Set<String> prefer, Map<String, String> headers) {
        // 1. Build the path
        String path = buildPath();

        // 2. Build the 'Prefer' header
        String preferHeader = String.join(",", prefer);

        // 3. Create a request builder
        HttpRequest.Builder requestBuilder = client.newRequest(path)
                .header("Prefer", preferHeader)
                .header("Content-Type", "application/json");

        // 4. Add any other local headers
        for (Map.Entry<String, String> header : headers.entrySet()) {
            requestBuilder.header(header.getKey(), header.getValue());
        }

        return requestBuilder;
    }
}