package io.github.jsupabase.postgrest.builder.base;

import io.github.jsupabase.core.config.SupabaseConfig;
import io.github.jsupabase.core.util.JsonUtil;
import io.github.jsupabase.postgrest.builder.PostgrestDeleteBuilder;
import io.github.jsupabase.postgrest.builder.PostgrestSelectBuilder;
import io.github.jsupabase.postgrest.builder.PostgrestUpdateBuilder;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * An abstract base for PostgREST builders that support filtering operations.
 * <p>
 * This class provides the core methods for building a PostgREST filter query
 * (e.g., {@code .eq()}, {@code .gt()}, {@code .like()}, {@code .contains()}, {@code .fts()}, etc.).
 * It is extended by {@link PostgrestSelectBuilder}, {@link PostgrestUpdateBuilder},
 * {@link PostgrestDeleteBuilder}, and {@link PostgrestRpcBuilder}.
 * <p>
 * (Refactored to align with the new Gateway architecture and extend PostgrestBaseBuilder).
 *
 * @param <T> The concrete builder type (e.g., PostgrestSelectBuilder) for fluent chaining.
 * @author neilhdezs
 * @version 0.1.0
 * @since 0.1.0
 */
public abstract class PostgrestFilterBuilder<T extends PostgrestBaseBuilder<T>> extends PostgrestBaseBuilder<T> {

    /**
     * Creates a new PostgrestFilterBuilder.
     * <p>
     * This constructor is called by concrete builders (Select, Update, etc.).
     * It passes the core configuration up to the {@link PostgrestBaseBuilder}.
     *
     * @param config The shared SupabaseConfig, which holds the auth state and base URL.
     * @param table  The database table or RPC function name this builder targets.
     */
    public PostgrestFilterBuilder(SupabaseConfig config, String table) {
        // Pass the required parameters up to the PostgrestBaseBuilder (which extends HttpClientBase)
        super(config, table);
    }

    /**
     * Finds all rows where the value in the {@code column} equals the
     * specified {@code value}. (e.g., {@code id=eq.5}).
     *
     * @param column The column to filter on.
     * @param value  The value to filter with.
     * @return The concrete builder instance (for chaining).
     */
    public T eq(String column, Object value) {
        return filter(column, "eq", value.toString());
    }

    /**
     * Finds all rows where the value in the {@code column} does not equal
     * the specified {@code value}. (e.g., {@code id=neq.5}).
     *
     * @param column The column to filter on.
     * @param value  The value to filter with.
     * @return The concrete builder instance (for chaining).
     */
    public T neq(String column, Object value) {
        return filter(column, "neq", value.toString());
    }

    /**
     * Finds all rows where the value in the {@code column} is greater than
     * the specified {@code value}. (e.g., {@code age=gt.18}).
     *
     * @param column The column to filter on.
     * @param value  The value to filter with.
     * @return The concrete builder instance (for chaining).
     */
    public T gt(String column, Object value) {
        return filter(column, "gt", value.toString());
    }

    /**
     * Finds all rows where the value in the {@code column} is greater than
     * or equal to the specified {@code value}. (e.g., {@code age=gte.18}).
     *
     * @param column The column to filter on.
     * @param value  The value to filter with.
     * @return The concrete builder instance (for chaining).
     */
    public T gte(String column, Object value) {
        return filter(column, "gte", value.toString());
    }

    /**
     * Finds all rows where the value in the {@code column} is less than
     * the specified {@code value}. (e.g., {@code age=lt.65}).
     *
     * @param column The column to filter on.
     * @param value  The value to filter with.
     * @return The concrete builder instance (for chaining).
     */
    public T lt(String column, Object value) {
        return filter(column, "lt", value.toString());
    }

    /**
     * Finds all rows where the value in the {@code column} is less than
     * or equal to the specified {@code value}. (e.g., {@code age=lte.65}).
     *
     * @param column The column to filter on.
     * @param value  The value to filter with.
     * @return The concrete builder instance (for chaining).
     */
    public T lte(String column, Object value) {
        return filter(column, "lte", value.toString());
    }

    /**
     * Finds all rows where the value in the {@code column} matches the
     * {@code pattern} (case-sensitive). (e.g., {@code name=like.*John*}).
     *
     * @param column  The column to filter on.
     * @param pattern The pattern (e.g., "El*"). Use {@code *} as a wildcard.
     * @return The concrete builder instance (for chaining).
     */
    public T like(String column, String pattern) {
        return filter(column, "like", pattern.replace("%", "*"));
    }

    /**
     * Finds all rows where the value in the {@code column} matches the
     * {@code pattern} (case-insensitive). (e.g., {@code name=ilike.*john*}).
     *
     * @param column  The column to filter on.
     * @param pattern The pattern (e.g., "el*"). Use {@code *} as a wildcard.
     * @return The concrete builder instance (for chaining).
     */
    public T ilike(String column, String pattern) {
        return filter(column, "ilike", pattern.replace("%", "*"));
    }

    /**
     * Finds all rows where the value in the {@code column} is {@code true},
     * {@code false}, {@code null}, or {@code notnull}.
     *
     * @param column The column to filter on.
     * @param value  The boolean value.
     * @return The concrete builder instance (for chaining).
     */
    public T is(String column, boolean value) {
        return filter(column, "is", String.valueOf(value));
    }

    /**
     * Finds all rows where the value in the {@code column} is {@code null}.
     *
     * @param column The column to filter on.
     * @return The concrete builder instance (for chaining).
     */
    public T isNull(String column) {
        return filter(column, "is", "null");
    }

    /**
     * Finds all rows where the value in the {@code column} is not {@code null}.
     *
     * @param column The column to filter on.
     * @return The concrete builder instance (for chaining).
     */
    public T isNotNull(String column) {
        return filter(column, "isnot", "null");
    }

    /**
     * Finds all rows where the value in the {@code column} is present in the
     * {@code values} list. (e.g., {@code id=in.(1,2,3)}).
     *
     * @param column The column to filter on.
     * @param values A collection of values.
     * @return The concrete builder instance (for chaining).
     */
    public T in(String column, Collection<?> values) {
        String formattedValue = values.stream()
                .map(Object::toString)
                .map(s -> {
                    // Quote strings, not numbers or other literals
                    if (s.matches("^-?\\d+(\\.\\d+)?$") || s.equals("null") || s.equals("true") || s.equals("false")) {
                        return s;
                    }
                    // Escape quotes and wrap in quotes
                    return "\"" + s.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
                })
                .collect(Collectors.joining(","));
        return filter(column, "in", "(" + formattedValue + ")");
    }

    /**
     * Convenience overload for {@link #in(String, Collection)} using varargs.
     */
    public T in(String column, Object... values) {
        return in(column, Arrays.asList(values));
    }

    /**
     * Finds all rows where the {@code column} (JSONB) contains the
     * specified {@code value}. (e.g., {@code metadata=cs.{"key":"value"}}).
     *
     * @param column The JSONB column to filter on.
     * @param value  The Map or POJO to check for containment.
     * @return The concrete builder instance (for chaining).
     */
    public T contains(String column, Object value) {
        return filter(column, "cs", formatFilterValue(value));
    }

    /**
     * Finds all rows where the {@code column} (JSONB) is contained within
     * the specified {@code value}. (e.g., {@code "tags": ["a", "b"]=cd.["a", "b", "c"]}).
     *
     * @param column The JSONB column to filter on.
     * @param value  The Map or POJO to check for containment.
     * @return The concrete builder instance (for chaining).
     */
    public T containedBy(String column, Object value) {
        return filter(column, "cd", formatFilterValue(value));
    }

    // --- Full-Text Search ---

    /**
     * Executes a full-text search using {@code to_tsquery}.
     *
     * @param column The {@code tsvector} column to search on.
     * @param query  The search query.
     * @return The concrete builder instance (for chaining).
     */
    public T fts(String column, String query) {
        return filter(column, "fts", query);
    }

    /**
     * Executes a full-text search using {@code plainto_tsquery}.
     *
     * @param column The {@code tsvector} column to search on.
     * @param query  The search query.
     * @return The concrete builder instance (for chaining).
     */
    public T plfts(String column, String query) {
        return filter(column, "plfts", query);
    }

    /**
     * Executes a full-text search using {@code phraseto_tsquery}.
     *
     * @param column The {@code tsvector} column to search on.
     * @param query  The search query.
     * @return The concrete builder instance (for chaining).
     */
    public T phfts(String column, String query) {
        return filter(column, "phfts", query);
    }

    /**
     * Executes a full-text search using {@code websearch_to_tsquery}.
     *
     * @param column The {@code tsvector} column to search on.
     * @param query  The search query.
     * @return The concrete builder instance (for chaining).
     */
    public T wfts(String column, String query) {
        return filter(column, "wfts", query);
    }

    // --- Logical Operators ---

    /**
     * Combines filters using AND.
     * (e.g., {@code and=(id.eq.5,name.like.*John*)}).
     *
     * @param filters Filters in PostgREST format (e.g., "id.eq.5", "name.like.*John*").
     * @return The concrete builder instance (for chaining).
     */
    public T and(String... filters) {
        return logicalFilter("and", filters);
    }

    /**
     * Combines filters using OR.
     * (e.g., {@code or=(id.eq.5,name.like.*John*)}).
     *
     * @param filters Filters in PostgREST format (e.g., "id.eq.5", "name.like.*John*").
     * @return The concrete builder instance (for chaining).
     */
    public T or(String... filters) {
        return logicalFilter("or", filters);
    }

    /**
     * Negates a filter.
     * (e.g., {@code id=not.eq.5}).
     *
     * @param filter Filter in PostgREST format (e.g., "id.eq.5").
     * @return The concrete builder instance (for chaining).
     */
    public T not(String filter) {
        String[] parts = filter.split("\\.", 2);
        if (parts.length == 2) {
            this.queryParams.put(parts[0], "not." + parts[1]);
        }
        return this.self();
    }

    // --- Core Filter Methods ---

    /**
     * The central filter method that adds a key/value pair to the query parameter map.
     *
     * @param column   The column to filter on.
     * @param operator The PostgREST operator (e.g., "eq", "cs", "fts").
     * @param value    The raw string value for the filter.
     * @return The concrete builder instance (for chaining).
     */
    public T filter(String column, String operator, String value) {
        this.queryParams.put(column, operator + "." + value);
        return this.self();
    }

    /**
     * Central method for logical filters (AND/OR).
     */
    private T logicalFilter(String operator, String[] filters) {
        String formattedFilters = String.join(",", filters);
        this.queryParams.put(operator, "(" + formattedFilters + ")");
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