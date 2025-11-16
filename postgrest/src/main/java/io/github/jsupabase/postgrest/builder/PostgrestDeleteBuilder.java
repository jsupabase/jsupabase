package io.github.jsupabase.postgrest.builder;

import io.github.jsupabase.core.config.SupabaseConfig;
import io.github.jsupabase.postgrest.builder.base.PostgrestFilterBuilder;
import io.github.jsupabase.postgrest.enums.CountType;
import io.github.jsupabase.postgrest.utils.PostgrestConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.http.HttpRequest;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 * - POSTGREST DELETE BUILDER -
 * <p>
 * Constructs and executes DELETE queries against PostgREST endpoints. This builder
 * inherits comprehensive filtering capabilities from {@link PostgrestFilterBuilder},
 * enabling precise row targeting through WHERE clause equivalents (eq, gt, like, etc.).
 *
 * <h3>Architecture Role:</h3>
 * Created by {@link io.github.jsupabase.postgrest.PostgrestClient} when {@code .delete()}
 * is invoked on a table. The builder accumulates filters and configuration before
 * executing the DELETE via inherited HTTP capabilities from {@code HttpClientBase}.
 *
 * <h3>Safety Considerations:</h3>
 * <ul>
 * <li><b>Unfiltered DELETE</b>: Without filters, deletes ALL rows in the table (destructive)</li>
 * <li><b>Best practice</b>: Always add at least one filter (.eq, .gt, etc.) before execute()</li>
 * <li><b>RLS protection</b>: Row Level Security policies provide server-side safety net</li>
 * <li><b>Transaction support</b>: PostgREST wraps operations in transactions (rollback on error)</li>
 * </ul>
 *
 * <h3>Response Options:</h3>
 * <ul>
 * <li><b>Minimal</b> (default): Only HTTP status, no body (efficient for bulk deletes)</li>
 * <li><b>Representation</b>: Returns deleted rows via select() (useful for confirmation/logging)</li>
 * <li><b>Count</b>: Returns number of rows deleted for metrics/validation</li>
 * </ul>
 *
 * <h3>Use Cases:</h3>
 * <ul>
 * <li>Soft delete simulation: .eq("id", recordId).execute()</li>
 * <li>Cleanup operations: .lt("created_at", oldDate).execute()</li>
 * <li>Cascade alternative: Delete with .select() to process related records</li>
 * <li>Audit trail: Combine .select() with logging before deletion</li>
 * </ul>
 *
 * @author neilhdezs
 * @version 0.1.0
 * @since 0.1.0
 */
public class PostgrestDeleteBuilder extends PostgrestFilterBuilder<PostgrestDeleteBuilder> {

    /** - Logger for DELETE query operations and execution tracking - **/
    private static final Logger LOG = LoggerFactory.getLogger(PostgrestDeleteBuilder.class);

    /** - HTTP headers specific to this DELETE request (Content-Type, custom headers, etc.) - **/
    private final Map<String, String> headers = new HashMap<>();

    /** - Prefer header values controlling response format (return=minimal/representation, count=exact) - **/
    private final Set<String> prefer = new HashSet<>();

    /**
     * - DELETE BUILDER CONSTRUCTOR -
     * <p>
     * Initializes a DELETE builder bound to a specific table with default minimal response.
     * The builder inherits authentication state and filter capabilities from parent classes,
     * starting with PostgrestConstants.PREFER_RETURN_MINIMAL preference for efficient bulk operations.
     * <p>
     * Called exclusively by {@link io.github.jsupabase.postgrest.PostgrestClient} when
     * {@code table(name).delete()} is invoked. Each delete operation creates a new
     * isolated builder instance.
     *
     * @param config Shared Supabase configuration with auth and service paths
     * @param table Database table name for DELETE operation (must exist in schema)
     */
    public PostgrestDeleteBuilder(SupabaseConfig config, String table) {
        super(config, table);

        this.prefer.add(PostgrestConstants.PREFER_RETURN_MINIMAL);
    }

    /**
     * - FLUENT CHAINING SELF-REFERENCE -
     * <p>
     * Returns this builder instance typed as {@code PostgrestDeleteBuilder} for
     * proper method chaining. Required by the generic builder pattern to ensure
     * inherited filter methods (eq, gt, like, etc.) return the correct concrete type.
     *
     * @return This DELETE builder instance for fluent API chaining
     */
    @Override
    protected PostgrestDeleteBuilder self() {
        return this;
    }

    /**
     * - REQUEST DELETED ROW REPRESENTATION -
     * <p>
     * Configures the DELETE to return the deleted row(s) in the response body with
     * specified columns. This switches from default PostgrestConstants.PREFER_RETURN_MINIMAL to PostgrestConstants.PREFER_RETURN_REPRESENTATION,
     * enabling confirmation of deletion, audit logging, or cascade processing.
     * <p>
     * Use cases:
     * <ul>
     * <li><b>Audit trail</b>: Log deleted row data for compliance</li>
     * <li><b>Confirmation</b>: Show user what was deleted</li>
     * <li><b>Cascade simulation</b>: Process related records client-side</li>
     * <li><b>Undo functionality</b>: Store deleted data for potential restoration</li>
     * </ul>
     * <p>
     * Performance note: Returning data adds response size and processing time.
     * Use minimal (no select) for bulk deletions where confirmation isn't needed.
     *
     * @param columns Comma-separated column list to return (e.g., "id,name,email")
     * @return This builder for method chaining
     */
    public PostgrestDeleteBuilder select(String columns) {
        this.prefer.remove(PostgrestConstants.PREFER_RETURN_MINIMAL);
        this.prefer.add(PostgrestConstants.PREFER_RETURN_REPRESENTATION);
        this.queryParams.put(PostgrestConstants.PARAM_SELECT, columns);
        return this;
    }

    /**
     * - REQUEST ALL COLUMNS OF DELETED ROWS -
     * <p>
     * Convenience overload that returns all columns (*) of deleted row(s).
     * Equivalent to {@code select("*")}.
     *
     * @return This builder for method chaining
     */
    public PostgrestDeleteBuilder select() {
        return select("*");
    }

    /**
     * - REQUEST DELETION COUNT -
     * <p>
     * Adds a "count" header to the response indicating how many rows were deleted.
     * This is useful for metrics, validation, and user feedback without requiring
     * full row representation (more efficient than select()).
     * <p>
     * Count algorithms:
     * <ul>
     * <li><b>EXACT</b>: Precise count via full scan (slow on large datasets)</li>
     * <li><b>PLANNED</b>: PostgreSQL planner estimate (fast, approximate)</li>
     * <li><b>ESTIMATED</b>: Table statistics estimate (fastest, least accurate)</li>
     * </ul>
     * <p>
     * The count is returned in the "Content-Range" response header, not the body.
     *
     * @param type Count algorithm determining accuracy vs. performance tradeoff
     * @return This builder for method chaining
     */
    public PostgrestDeleteBuilder count(CountType type) {
        this.prefer.add("count=" + type.getValue());
        return this;
    }

    /**
     * - REQUEST EXACT DELETION COUNT -
     * <p>
     * Convenience overload that requests exact count (full scan).
     * Equivalent to {@code count(CountType.EXACT)}.
     *
     * @return This builder for method chaining
     */
    public PostgrestDeleteBuilder count() {
        return count(CountType.EXACT);
    }

    /**
     * - EXECUTE DELETE QUERY -
     * <p>
     * Builds and executes the DELETE query asynchronously, returning a CompletableFuture
     * that resolves to the response body (empty for minimal, JSON for representation).
     * This method constructs the final HTTP DELETE request with accumulated filters,
     * Prefer headers, and authentication credentials inherited from HttpClientBase.
     * <p>
     * Execution flow:
     * <ol>
     * <li>Build path with table name and query parameters (filters, select)</li>
     * <li>Construct Prefer header from accumulated preferences (return, count)</li>
     * <li>Add custom headers if any</li>
     * <li>Create HTTP DELETE request with authentication</li>
     * <li>Delegate to inherited sendAsyncString for async execution</li>
     * </ol>
     * <p>
     * Response handling:
     * <ul>
     * <li><b>2xx</b>: Success, body contains deleted data (representation) or empty (minimal)</li>
     * <li><b>404</b>: Table not found or no rows matched filters</li>
     * <li><b>401/403</b>: Authentication or RLS policy violation</li>
     * <li><b>4xx/5xx</b>: Client/server errors wrapped in PostgrestException</li>
     * </ul>
     * <p>
     * Safety note: Unfiltered DELETE (no .eq, .gt, etc.) will delete ALL rows in table.
     * Always verify filters before calling execute() on production data.
     *
     * @return CompletableFuture with response body string (JSON or empty) or error
     */
    public CompletableFuture<String> execute() {
        try {
            String path = buildPath();
            String preferHeader = String.join(",", this.prefer);

            HttpRequest.Builder requestBuilder = this.newRequest(path)
                    .header(PostgrestConstants.HDR_PREFER, preferHeader);

            for (Map.Entry<String, String> header : this.headers.entrySet()) {
                requestBuilder.header(header.getKey(), header.getValue());
            }

            HttpRequest request = requestBuilder.DELETE().build();
            LOG.debug("Executing DELETE: {}", request.uri());

            return this.sendAsyncString(request);

        } catch (Exception e) {
            LOG.error("Failed to build PostgREST DELETE request", e);
            return CompletableFuture.failedFuture(e);
        }
    }
}
