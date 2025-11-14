package io.github.jsupabase.postgrest.builder;

import io.github.jsupabase.core.config.SupabaseConfig;
// 1. IMPORTA EL ENUM EXTERNO
import io.github.jsupabase.postgrest.PostgrestClient;
import io.github.jsupabase.postgrest.enums.CountType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.http.HttpRequest;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * Builds and executes a PostgREST SELECT query.
 * This class handles filtering (by extending PostgrestFilterBuilder)
 * and modifiers specific to SELECT (.order, .limit, .count, etc.).
 *
 * @author neilhdezs
 * @version 0.0.3
 */
public class PostgrestSelectBuilder extends PostgrestFilterBuilder<PostgrestSelectBuilder> {

    /** - LOGGER - **/
    private static final Logger LOG = LoggerFactory.getLogger(PostgrestSelectBuilder.class);

    /** - Headers specific to this SELECT request (like 'Prefer: count=' or 'Range: 0-9') - **/
    private final Map<String, String> headers = new HashMap<>();

    // 2. EL ENUM 'CountType' SE HA ELIMINADO DE AQUÍ Y AHORA SE IMPORTA

    /**
     * Creates a new PostgrestSelectBuilder.
     *
     * @param client  The client configuration.
     * @param table   The database table to query.
     * @param columns The columns to select (e.g., "*").
     */
    public PostgrestSelectBuilder(PostgrestClient client, String table, String columns) {
        super(client, table);
        Objects.requireNonNull(columns, "Columns cannot be null");

        // 'select' es un query param
        this.queryParams.put("select", columns);
    }

    /**
     * Implements the 'self()' method for the generic builder pattern.
     */
    @Override
    protected PostgrestSelectBuilder self() {
        return this;
    }


    // --- Modifiers (Count) ---

    /**
     * Requests the total count of rows for the query.
     * PostgREST will add the count to the 'Content-Range' header.
     *
     * @param type The algorithm for counting (EXACT, PLANNED, ESTIMATED).
     * @return this (for Builder chaining)
     */
    public PostgrestSelectBuilder count(CountType type) { // <-- 3. USA EL ENUM IMPORTADO
        this.headers.put("Prefer", "count=" + type.getValue());
        return this;
    }

    /**
     * Requests the exact total count of rows for the query.
     * (Convenience method for count(CountType.EXACT))
     *
     * @return this (for Builder chaining)
     */
    public PostgrestSelectBuilder count() {
        return count(CountType.EXACT);
    }

    // --- Modifiers (Order) ---

    /**
     * Adds an "order by" clause.
     *
     * @param column      The column to sort by.
     * @param ascending   True for ASC, false for DESC.
     * @param nullsFirst  True for NULLS FIRST, false for NULLS LAST.
     * @return this (for Builder chaining)
     */
    public PostgrestSelectBuilder order(String column, boolean ascending, boolean nullsFirst) {
        String dir = ascending ? "asc" : "desc";
        String nulls = nullsFirst ? "nullsfirst" : "nullslast";
        String currentOrder = this.queryParams.get("order");
        String newOrder = String.format("%s.%s.%s", column, dir, nulls);

        if (currentOrder != null) {
            newOrder = currentOrder + "," + newOrder;
        }

        this.queryParams.put("order", newOrder);
        return this;
    }

    /**
     * Adds an "order by" clause (defaulting to NULLS LAST).
     *
     * @param column    The column to sort by.
     * @param ascending True for ASC, false for DESC.
     * @return this (for Builder chaining)
     */
    public PostgrestSelectBuilder order(String column, boolean ascending) {
        return order(column, ascending, false);
    }

    /**
     * Adds an "order by" clause (defaulting to ASC, NULLS LAST).
     *
     * @param column The column to sort by.
     * @return this (for Builder chaining)
     */
    public PostgrestSelectBuilder order(String column) {
        return order(column, true, false);
    }

    // --- Modifiers (Limit & Paging) ---

    /**
     * Limits the number of rows returned.
     * (Uses 'limit' query parameter).
     *
     * @param count The maximum number of rows.
     * @return this (for Builder chaining)
     */
    public PostgrestSelectBuilder limit(int count) {
        this.queryParams.put("limit", String.valueOf(count));
        return this;
    }

    /**
     * Sets the starting row offset.
     * (Uses 'offset' query parameter).
     *
     * @param count The number of rows to skip.
     * @return this (for Builder chaining)
     */
    public PostgrestSelectBuilder offset(int count) {
        this.queryParams.put("offset", String.valueOf(count));
        return this;
    }

    /**
     * Sets the range of rows to return (for pagination).
     * (Uses 'Range' header).
     *
     * @param from The starting row index (inclusive).
     * @param to   The ending row index (inclusive).
     * @return this (for Builder chaining)
     */
    public PostgrestSelectBuilder range(int from, int to) {
        this.headers.put("Range", from + "-" + to);
        return this;
    }

    // --- Modifiers (Response Shape) ---

    /**
     * Requests the response as a single object instead of an array.
     * If 0 or >1 rows are found, PostgREST will return an error.
     *
     * @return this (for Builder chaining)
     */
    public PostgrestSelectBuilder single() {
        this.headers.put("Accept", "application/vnd.pgrst.object+json");
        return this;
    }

    /**
     * Requests the response as a single object or null.
     * If >1 rows are found, PostgREST will return an error.
     * If 0 rows are found, the *execution logic* should interpret this as 'null'.
     *
     * @return this (for Builder chaining)
     */
    public PostgrestSelectBuilder maybeSingle() {
        this.headers.put("Accept", "application/vnd.pgrst.object+json");
        return this;
    }

    /**
     * Requests the response in CSV format.
     * The execution method must be prepared to receive a String, not JSON.
     *
     * @return this (for Builder chaining)
     */
    public PostgrestSelectBuilder csv() {
        this.headers.put("Accept", "text/csv");
        return this;
    }

    // --- Modifiers (Debug) ---

    /**
     * Requests the EXPLAIN plan for the query.
     * PostgREST will return the plan instead of the data.
     *
     * @return this (for Builder chaining)
     */
    public PostgrestSelectBuilder explain() {
        this.headers.put("Accept", "application/vnd.pgrst.plan+json");
        return this;
    }

    // --- Execution Method ---

    /**
     * Executes the built SELECT query asynchronously.
     * (This is a skeleton and does not perform the HTTP call yet).
     *
     * @return A CompletableFuture that will eventually contain the JSON response as a String.
     */
    public CompletableFuture<String> execute() {
        try {
            // 1. Construye el path con los query params (heredado del FilterBuilder)
            // ej: "/my_table?select=*&id=eq.5&order=name.asc"
            String path = this.buildPath();

            // 2. Crea un request builder (heredado del HttpClientBase)
            //    Esto ya incluye la URL base, 'apikey' y 'Authorization'
            HttpRequest.Builder requestBuilder = client.newRequest(path);

            // 3. Añade los headers específicos de este SELECT (Accept, Range, Prefer, etc.)
            for (Map.Entry<String, String> header : this.headers.entrySet()) {
                requestBuilder.header(header.getKey(), header.getValue());
            }

            // 4. Construye la petición GET final
            HttpRequest request = requestBuilder.GET().build();

            LOG.debug("Executing SELECT: {}", request.uri());

            // 5. DELEGA la llamada al cliente, que la ejecutará
            return client.sendAsyncString(request);

        } catch (Exception e) {
            // Captura cualquier error de construcción (ej. URL mal formada)
            return CompletableFuture.failedFuture(e);
        }
    }
}