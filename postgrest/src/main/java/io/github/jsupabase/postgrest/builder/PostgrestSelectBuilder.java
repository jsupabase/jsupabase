package io.github.jsupabase.postgrest.builder;

import com.fasterxml.jackson.core.type.TypeReference;
import io.github.jsupabase.core.config.SupabaseConfig;
import io.github.jsupabase.core.util.JsonUtil;
import io.github.jsupabase.postgrest.builder.base.PostgrestFilterBuilder;
import io.github.jsupabase.postgrest.enums.CountType;
import io.github.jsupabase.postgrest.enums.NullsOrder;
import io.github.jsupabase.postgrest.enums.Order;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.http.HttpRequest;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Builds and executes a PostgREST SELECT query.
 * <p>
 * This class handles filtering (by extending {@link PostgrestFilterBuilder})
 * and modifiers specific to SELECT operations (e.g., .order, .limit, .count, .single).
 * <p>
 * (Refactored for the new HttpClientBase/Gateway architecture)
 *
 * @author neilhdezs
 * @version 0.1.0
 * @since 0.1.0
 */
public class PostgrestSelectBuilder extends PostgrestFilterBuilder<PostgrestSelectBuilder> {

    /** - SLF4J Logger - **/
    private static final Logger LOG = LoggerFactory.getLogger(PostgrestSelectBuilder.class);

    /** - Headers specific to this SELECT request (e.g., 'Prefer: count=' or 'Range: 0-9') - **/
    private final Map<String, String> headers = new HashMap<>();

    /**
     * Creates a new PostgrestSelectBuilder.
     * <p>
     * This constructor is called by {@link io.github.jsupabase.postgrest.clients.PostgrestTableClient}.
     * It configures the builder to target a specific table and set of columns.
     *
     * @param config  The shared SupabaseConfig, passed to the HttpClientBase constructor.
     * @param table   The database table to query.
     * @param columns The columns to select (e.g., "id, name, foreign_table(id, name)").
     */
    public PostgrestSelectBuilder(SupabaseConfig config, String table, String columns) {
        // Call the super constructor (FilterBuilder -> BaseBuilder)
        super(config, table);
        // Add the 'select' parameter to the inherited queryParams map
        this.queryParams.put("select", columns);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected PostgrestSelectBuilder self() {
        return this;
    }

    // --- SELECT Modifiers ---

    /**
     * Specifies the ordering for the query results.
     *
     * @param column     The column to order by.
     * @param order      The direction (ASC or DESC).
     * @param nullsOrder The handling of null values (FIRST or LAST).
     * @return this (for Builder chaining).
     */
    public PostgrestSelectBuilder order(String column, Order order, NullsOrder nullsOrder) {
        String existingOrder = this.queryParams.getOrDefault("order", "");
        String newOrder = column + "." + order.getValue() + "." + nullsOrder.getValue();

        String finalOrder = existingOrder.isEmpty() ? newOrder : existingOrder + "," + newOrder;
        this.queryParams.put("order", finalOrder);
        return this;
    }

    /**
     * (Convenience overload) Specifies the ordering (ASC/DESC), using the default nulls handling.
     */
    public PostgrestSelectBuilder order(String column, Order order) {
        String existingOrder = this.queryParams.getOrDefault("order", "");
        String newOrder = column + "." + order.getValue();

        String finalOrder = existingOrder.isEmpty() ? newOrder : existingOrder + "," + newOrder;
        this.queryParams.put("order", finalOrder);
        return this;
    }

    /**
     * (Convenience overload) Specifies the ordering (default ASC), with specific nulls handling.
     */
    public PostgrestSelectBuilder order(String column, NullsOrder nullsOrder) {
        return order(column, Order.ASC, nullsOrder);
    }

    /**
     * (Convenience overload) Specifies the ordering (default ASC and default nulls handling).
     */
    public PostgrestSelectBuilder order(String column) {
        return order(column, Order.ASC);
    }


    /**
     * Limits the number of rows returned by the query.
     * This is added as a query parameter (e.g., {@code ?limit=10}).
     *
     * @param count The maximum number of rows to return.
     * @return this (for Builder chaining).
     */
    public PostgrestSelectBuilder limit(long count) {
        this.queryParams.put("limit", String.valueOf(count));
        return this;
    }

    /**
     * Specifies the range of rows to return (for pagination).
     * This is added as a {@code Range} header (e.g., {@code Range: 0-9}).
     *
     * @param from The starting row index (inclusive).
     * @param to   The ending row index (inclusive).
     * @return this (for Builder chaining).
     */
    public PostgrestSelectBuilder range(long from, long to) {
        this.headers.put("Range", from + "-" + to);
        return this;
    }

    /**
     * Requests the total row count for the query.
     * This is added as a {@code Prefer} header (e.g., {@code Prefer: count=exact}).
     *
     * @param count The type of count to request (e.g., EXACT, PLANNED).
     * @return this (for Builder chaining).
     */
    public PostgrestSelectBuilder count(CountType count) {
        this.headers.put("Prefer", "count=" + count.getValue());
        return this;
    }

    /**
     * (Convenience overload) Requests an exact row count.
     */
    public PostgrestSelectBuilder count() {
        return count(CountType.EXACT);
    }

    /**
     * Requests a single row as a JSON object instead of a JSON array.
     * If the query returns multiple rows, PostgREST will return only the first.
     * This is added as an {@code Accept} header.
     *
     * @return this (for Builder chaining).
     */
    public PostgrestSelectBuilder single() {
        this.headers.put("Accept", "application/vnd.pgrst.object+json");
        return this;
    }

    // --- Execution Methods ---

    /**
     * Executes the built SELECT query asynchronously.
     *
     * @return A {@code CompletableFuture} that will contain the response body as a String (JSON).
     */
    public CompletableFuture<String> execute() {
        try {
            // Build the path with query params (inherited from PostgrestBaseBuilder)
            // e.g., "/rest/v1/my_table?select=*&id=eq.5&order=name.asc"
            String path = this.buildPath();

            // Create a request builder (inherited from HttpClientBase)
            // This already includes the base URL, 'apikey', and 'Authorization'
            HttpRequest.Builder requestBuilder = this.newRequest(path);

            // Add headers specific to this SELECT (Accept, Range, Prefer, etc.)
            for (Map.Entry<String, String> header : this.headers.entrySet()) {
                requestBuilder.header(header.getKey(), header.getValue());
            }

            // Build the final GET request
            HttpRequest request = requestBuilder.GET().build();

            LOG.debug("Executing SELECT: {}", request.uri());

            // Delegate the network call to the inherited sendAsyncString method
            return this.sendAsyncString(request);

        } catch (Exception e) {
            // Catches any build-time errors (e.g., URL encoding)
            LOG.error("Failed to build Postgrest SELECT request", e);
            return CompletableFuture.failedFuture(e);
        }
    }

    /**
     * Executes the SELECT query and deserializes the response to a specific POJO.
     * <p>
     * <strong>Note:</strong> If you expect a list (e.g., {@code List<MyPojo>}),
     * use {@link #execute(TypeReference)} instead.
     * This method is typically used with {@link #single()}.
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
     * Executes the SELECT query and deserializes the response to a generic type (e.g., List<POJO>).
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