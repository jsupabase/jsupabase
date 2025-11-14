package io.github.jsupabase.postgrest.builder;

import io.github.jsupabase.core.config.SupabaseConfig;
import io.github.jsupabase.core.util.JsonUtil;
import io.github.jsupabase.postgrest.PostgrestClient;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Abstract base class for PostgREST builders that support filtering.
 * Manages all common filter methods (.eq, .gt, .like, .contains, .textSearch, etc.)
 *
 * @param <T> The type of the concrete builder (e.g., PostgrestSelectBuilder)
 * @author neilhdezs
 * @version 0.0.1
 */
public abstract class PostgrestFilterBuilder<T extends PostgrestBaseBuilder<T>> extends PostgrestBaseBuilder<T> {

    /** - Stores all query parameters (filters, order, etc.) - **/
    protected final Map<String, String> queryParams = new HashMap<>();


    /**
     * Creates a new PostgrestFilterBuilder.
     *
     * @param client El cliente Postgrest activo.
     * @param table  The database table to query.
     */
    protected PostgrestFilterBuilder(PostgrestClient client, String table) {
        super(client, table);
    }

    /**
     * Abstract method that concrete builders must implement to return themselves.
     * This allows for type-safe fluent chaining without @SuppressWarnings.
     *
     * @return The concrete builder instance (e.g., PostgrestSelectBuilder).
     */
    protected abstract T self();

    // --- Basic Filter Methods ---

    /**
     * Adds an "equals" filter to the query. (e.g., "eq.value")
     *
     * @param column The column to filter on.
     * @param value  The value to filter by.
     * @return The concrete builder instance (for chaining).
     */
    public T eq(String column, Object value) {
        this.queryParams.put(column, "eq." + value.toString());
        return this.self();
    }

    /**
     * Adds a "not equals" filter to the query. (e.g., "neq.value")
     *
     * @param column The column to filter on.
     * @param value  The value to filter by.
     * @return The concrete builder instance (for chaining).
     */
    public T neq(String column, Object value) {
        this.queryParams.put(column, "neq." + value.toString());
        return this.self();
    }

    /**
     * Adds a "greater than" filter to the query. (e.g., "gt.value")
     *
     * @param column The column to filter on.
     * @param value  The value to filter by.
     * @return The concrete builder instance (for chaining).
     */
    public T gt(String column, Object value) {
        this.queryParams.put(column, "gt." + value.toString());
        return this.self();
    }

    /**
     * Adds a "greater than or equals" filter. (e.g., "gte.value")
     *
     * @param column The column to filter on.
     * @param value  The value to filter by.
     * @return The concrete builder instance (for chaining).
     */
    public T gte(String column, Object value) {
        this.queryParams.put(column, "gte." + value.toString());
        return this.self();
    }

    /**
     * Adds a "less than" filter to the query. (e.g., "lt.value")
     *
     * @param column The column to filter on.
     * @param value  The value to filter by.
     * @return The concrete builder instance (for chaining).
     */
    public T lt(String column, Object value) {
        this.queryParams.put(column, "lt." + value.toString());
        return this.self();
    }

    /**
     * Adds a "less than or equals" filter. (e.g., "lte.value")
     *
     * @param column The column to filter on.
     * @param value  The value to filter by.
     * @return The concrete builder instance (for chaining).
     */
    public T lte(String column, Object value) {
        this.queryParams.put(column, "lte." + value.toString());
        return this.self();
    }

    /**
     * Adds a "LIKE" filter (case-sensitive). (e.g., "like.*pattern*")
     *
     * @param column  The column to filter on.
     * @param pattern The pattern to match (e.g., "Supabase*").
     * @return The concrete builder instance (for chaining).
     */
    public T like(String column, String pattern) {
        this.queryParams.put(column, "like." + pattern);
        return this.self();
    }

    /**
     * Adds an "ILIKE" filter (case-insensitive). (e.g., "ilike.*pattern*")
     *
     * @param column  The column to filter on.
     * @param pattern The pattern to match (e.g., "supabase*").
     * @return The concrete builder instance (for chaining).
     */
    public T ilike(String column, String pattern) {
        this.queryParams.put(column, "ilike." + pattern);
        return this.self();
    }

    /**
     * Adds an "IS" filter. (e.g., "is.null", "is.true", "is.false")
     *
     * @param column The column to filter on.
     * @param value  The value (e.g., "null", "true", "false").
     * @return The concrete builder instance (for chaining).
     */
    public T is(String column, Object value) {
        this.queryParams.put(column, "is." + value.toString());
        return this.self();
    }

    /**
     * Adds an "IN" filter to the query. (e.g., "in.(val1,val2,"quoted val")")
     *
     * @param column The column to filter on.
     * @param values A collection of values (e.g., a List or Set).
     * @return The concrete builder instance (for chaining).
     */
    public T in(String column, Collection<?> values) {
        String formattedValues = values.stream()
                .map(v -> {
                    if (v instanceof String) {
                        // Escape quotes and backslashes
                        String s = ((String) v).replace("\\", "\\\\").replace("\"", "\\\"");
                        return "\"" + s + "\""; // Quote strings
                    }
                    return v.toString();
                })
                .collect(Collectors.joining(","));

        this.queryParams.put(column, "in.(" + formattedValues + ")");
        return this.self();
    }

    /**
     * Adds an "IN" filter to the query using varargs.
     *
     * @param column The column to filter on.
     * @param values The array of values to match.
     * @return The concrete builder instance (for chaining).
     */
    public T in(String column, Object... values) {
        return this.in(column, Arrays.asList(values));
    }

    // --- Array/JSONB/Range Filters ---

    /**
     * Adds a "contains" filter. (e.g., "cs.{val1,val2}" or "cs.{\"key\":\"val\"}")
     * Used for array, jsonb, and range columns.
     *
     * @param column The column to filter on.
     * @param value  The value(s) to check for containment (e.g., a List or a Map).
     * @return The concrete builder instance (for chaining).
     */
    public T contains(String column, Object value) {
        this.queryParams.put(column, "cs." + formatFilterValue(value));
        return this.self();
    }

    /**
     * Adds a "contained by" filter. (e.g., "cd.{val1,val2}" or "cd.{\"key\":\"val\"}")
     * Used for array, jsonb, and range columns.
     *
     * @param column The column to filter on.
     * @param value  The value(s) to check for containment (e.g., a List or a Map).
     * @return The concrete builder instance (for chaining).
     */
    public T containedBy(String column, Object value) {
        this.queryParams.put(column, "cd." + formatFilterValue(value));
        return this.self();
    }

    /**
     * Adds an "overlaps" filter. (e.g., "ov.{val1,val2}" or "ov.[from,to]")
     * Used for array and range columns.
     *
     * @param column The column to filter on.
     * @param value  The value(s) to check for overlap (e.g., a List or a range String).
     * @return The concrete builder instance (for chaining).
     */
    public T overlaps(String column, Object value) {
        this.queryParams.put(column, "ov." + formatFilterValue(value));
        return this.self();
    }

    // --- Range Filters ---

    /**
     * Adds a "strictly left of" (range) filter. (e.g., "sl.[from,to)")
     *
     * @param column The column to filter on.
     * @param range  The range string (e.g., "[20,30)").
     * @return The concrete builder instance (for chaining).
     */
    public T rangeLt(String column, String range) {
        this.queryParams.put(column, "sl." + range);
        return this.self();
    }

    /**
     * Adds a "strictly right of" (range) filter. (e.g., "sr.[from,to)")
     *
     * @param column The column to filter on.
     * @param range  The range string (e.g., "[20,30)").
     * @return The concrete builder instance (for chaining).
     */
    public T rangeGt(String column, String range) {
        this.queryParams.put(column, "sr." + range);
        return this.self();
    }

    /**
     * Adds a "not extend to the right" (range) filter. (e.g., "nxr.[from,to)")
     *
     * @param column The column to filter on.
     * @param range  The range string (e.g., "[20,30)").
     * @return The concrete builder instance (for chaining).
     */
    public T rangeGte(String column, String range) {
        this.queryParams.put(column, "nxr." + range);
        return this.self();
    }

    /**
     * Adds a "not extend to the left" (range) filter. (e.g., "nxl.[from,to)")
     *
     * @param column The column to filter on.
     * @param range  The range string (e.g., "[20,30)").
     * @return The concrete builder instance (for chaining).
     */
    public T rangeLte(String column, String range) {
        this.queryParams.put(column, "nxl." + range);
        return this.self();
    }

    /**
     * Adds an "adjacent to" (range) filter. (e.g., "adj.[from,to)")
     *
     * @param column The column to filter on.
     * @param range  The range string (e.g., "[20,30)").
     * @return The concrete builder instance (for chaining).
     */
    public T rangeAdjacent(String column, String range) {
        this.queryParams.put(column, "adj." + range);
        return this.self();
    }

    // --- Full-Text Search ---

    /**
     * Enum for PostgREST Full-Text Search types.
     */
    public enum TextSearchType {
        FTS("fts"),
        PLFTS("plfts"),
        PHFTS("phfts"),
        WFTS("wfts");

        private final String operator;

        TextSearchType(String operator) {
            this.operator = operator;
        }

        public String getOperator() {
            return operator;
        }
    }

    /**
     * The base method for "Full-Text Search" filters.
     *
     * @param column The column to search on.
     * @param query  The search query.
     * @param type   The type of search (FTS, PLFTS, PHFTS, WFTS).
     * @param config The text search configuration (e.g., "english"). Can be null.
     * @return The concrete builder instance (for chaining).
     */
    public T textSearch(String column, String query, TextSearchType type, String config) {
        String configPart = (config == null || config.isEmpty()) ? "" : "(" + config + ")";
        // Format: operator(config).query
        this.queryParams.put(column, type.getOperator() + configPart + "." + query);
        return this.self();
    }

    /**
     * Adds a "Full-Text Search" (fts) filter using the default "fts" type.
     *
     * @param column The column to search on.
     * @param query  The search query.
     * @return The concrete builder instance (for chaining).
     */
    public T textSearch(String column, String query) {
        return textSearch(column, query, TextSearchType.FTS, null);
    }

    /**
     * Adds a "Plain Full-Text Search" (plfts) filter.
     *
     * @param column The column to search on.
     * @param query  The search query.
     * @param config The text search configuration (e.g., "english"). Can be null.
     * @return The concrete builder instance (for chaining).
     */
    public T plainTextSearch(String column, String query, String config) {
        return textSearch(column, query, TextSearchType.PLFTS, config);
    }

    /**
     * Adds a "Phrase Full-Text Search" (phfts) filter.
     *
     * @param column The column to search on.
     * @param query  The search query.
     * @param config The text search configuration (e.g., "english"). Can be null.
     * @return The concrete builder instance (for chaining).
     */
    public T phraseTextSearch(String column, String query, String config) {
        return textSearch(column, query, TextSearchType.PHFTS, config);
    }

    /**
     * Adds a "Web Search Full-Text Search" (wfts) filter.
     *
     * @param column The column to search on.
     * @param query  The search query.
     * @param config The text search configuration (e.g., "english"). Can be null.
     * @return The concrete builder instance (for chaining).
     */
    public T webTextSearch(String column, String query, String config) {
        return textSearch(column, query, TextSearchType.WFTS, config);
    }


    // --- Meta Filters ---

    /**
     * Adds a "NOT" operator to a filter.
     *
     * @param column   The column to filter on.
     * @param operator The operator (e.g., "eq", "in", "like").
     * @param value    The raw string value for the filter (e.g., "value", "(1,2,3)").
     * @return The concrete builder instance (for chaining).
     */
    public T not(String column, String operator, String value) {
        this.queryParams.put(column, "not." + operator + "." + value);
        return this.self();
    }

    /**
     * Adds an "OR" filter, joining multiple filters.
     *
     * @param filters The raw filter string (e.g., "id.eq.1,name.like.*John*").
     * @return The concrete builder instance (for chaining).
     */
    public T or(String filters) {
        this.queryParams.put("or", "(" + filters + ")");
        return this.self();
    }

    /**
     * Adds a raw filter. Use this for operators not covered by other methods.
     *
     * @param column   The column to filter on.
     * @param operator The operator (e.g., "eq", "cs", "fts").
     * @param value    The raw string value for the filter.
     * @return The concrete builder instance (for chaining).
     */
    public T filter(String column, String operator, String value) {
        this.queryParams.put(column, operator + "." + value);
        return this.self();
    }

    // --- Helper Methods ---

    /**
     * Formats a value for array/jsonb/range filters.
     *
     * @param value The value to format.
     * @return A string formatted for PostgREST.
     */
    private String formatFilterValue(Object value) {
        if (value instanceof Collection) {
            // Format as Postgres array literal: {"item1","item2"}
            String content = ((Collection<?>) value).stream()
                    .map(v -> {
                        String s = v.toString();
                        // Escape quotes and backslashes
                        s = s.replace("\\", "\\\\").replace("\"", "\\\"");
                        return "\"" + s + "\""; // Quote all elements
                    })
                    .collect(Collectors.joining(","));
            return "{" + content + "}";
        }
        if (value instanceof Map) {
            // Format as JSON literal
            return JsonUtil.toJson(value);
        }
        // Assume it's a simple string (like a range "[1,5)")
        return value.toString();
    }
}