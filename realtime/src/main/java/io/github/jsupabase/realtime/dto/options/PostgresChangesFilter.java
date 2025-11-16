package io.github.jsupabase.realtime.dto.options;

import io.github.jsupabase.realtime.enums.RealtimeEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * - POSTGRESQL CHANGES FILTER -
 * <p>
 * Filter configuration for subscribing to PostgreSQL database change events via Supabase Realtime.
 * This class enables fine-grained control over which database operations trigger callbacks,
 * allowing applications to react to INSERT, UPDATE, and DELETE operations on specific tables.
 * <p>
 * The filter uses a Builder pattern for intuitive configuration and supports advanced features
 * like schema specification, table filtering, and optional WHERE clause filtering for
 * row-level event targeting.
 *
 * <h3>Filter Capabilities:</h3>
 * <ul>
 * <li><b>Event Type</b> - Filter by operation (INSERT, UPDATE, DELETE, or ALL)</li>
 * <li><b>Schema</b> - Target specific database schema (default: "public")</li>
 * <li><b>Table</b> - Subscribe to changes on specific tables</li>
 * <li><b>Filter</b> - Optional WHERE clause for row-level filtering</li>
 * </ul>
 *
 * <h3>Example Usage:</h3>
 * <pre>{@code
 * // Listen to all operations on the "todos" table (a specific table, not all tables)
 * PostgresChangesFilter allChanges = new PostgresChangesFilter.Builder(RealtimeEvent.ALL)
 *     .schema("public")
 *     .table("todos")  // "todos" is the table name (like a TODO list app)
 *     .callback(payload -> {
 *         System.out.println("Change detected: " + payload);
 *     })
 *     .build();
 *
 * // Listen only to INSERTs on the "users" table
 * PostgresChangesFilter insertOnly = new PostgresChangesFilter.Builder(RealtimeEvent.INSERT)
 *     .table("users")  // "users" is another table name
 *     .callback(payload -> {
 *         Map<String, Object> record = (Map) ((Map) payload.get("data")).get("record");
 *         System.out.println("New user: " + record.get("email"));
 *     })
 *     .build();
 *
 * // Listen to UPDATEs on "products" table with a filter
 * PostgresChangesFilter productsFilter = new PostgresChangesFilter.Builder(RealtimeEvent.UPDATE)
 *     .table("products")
 *     .filter("price=gt.100")  // Only when price > 100
 *     .callback(payload -> {
 *         System.out.println("Expensive product updated");
 *     })
 *     .build();
 *
 * channel.onPostgresChanges(allChanges).subscribe(status -> {
 *     System.out.println("Subscribed: " + status);
 * });
 * }</pre>
 *
 * @author neilhdezs
 * @version 0.1.0
 * @since 0.1.0
 */
public class PostgresChangesFilter {

    /** - Database operation event type to listen for (INSERT, UPDATE, DELETE, or ALL for any operation) - */
    private final RealtimeEvent event;

    /** - Database schema name where the target table resides (commonly "public" in PostgreSQL) - */
    private final String schema;

    /** - Specific database table name to monitor for changes (e.g., "users", "products", "orders") - */
    private final String table;

    /** - Optional PostgREST-style filter for row-level filtering (e.g., "id=eq.5", "price=gt.100") - */
    private final String filter;

    /** - Callback function invoked when matching database change events occur on the subscribed table - */
    private final Consumer<Map<String, Object>> callback;

    /**
     * - PRIVATE CONSTRUCTOR -
     * <p>
     * Creates a PostgreSQL changes filter from a builder instance with validation.
     * Use the Builder pattern for public instantiation.
     *
     * @param builder Builder instance containing all filter configuration
     * @throws NullPointerException if required fields (event, schema, callback) are null
     */
    private PostgresChangesFilter(Builder builder) {
        this.event = Objects.requireNonNull(builder.event, "Event cannot be null.");
        this.schema = Objects.requireNonNull(builder.schema, "Schema cannot be null.");
        this.callback = Objects.requireNonNull(builder.callback, "Callback cannot be null.");
        this.table = builder.table;
        this.filter = builder.filter;
    }

    /** - Returns the Phoenix Channels event string for this database operation type - */
    public String getEvent() {
        return event.getPhxEvent();
    }

    /** - Returns the database schema name where the target table is located - */
    public String getSchema() {
        return schema;
    }

    /** - Returns the specific table name being monitored, or null if not specified - */
    public String getTable() {
        return table;
    }

    /** - Returns the optional PostgREST filter for row-level filtering, or null if not specified - */
    public String getFilter() {
        return filter;
    }

    /** - Returns the callback function executed when matching database changes occur - */
    public Consumer<Map<String, Object>> getCallback() {
        return callback;
    }

    /**
     * - PAYLOAD MAP SERIALIZATION -
     * <p>
     * Converts this filter configuration into the Map format required by the Phoenix Channels
     * protocol for the phx_join payload. This serialized format is sent to the Supabase server
     * to configure which database changes should trigger callbacks.
     * <p>
     * The resulting map structure follows Supabase Realtime protocol specification:
     * <pre>{@code
     * {
     *   "event": "INSERT",
     *   "schema": "public",
     *   "table": "users",      // optional
     *   "filter": "id=eq.5"    // optional
     * }
     * }</pre>
     *
     * @return Map representation of this filter for Phoenix Channels protocol transmission
     */
    public Map<String, Object> toPayloadMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("event", this.getEvent());
        map.put("schema", this.getSchema());
        if (this.table != null) {
            map.put("table", this.getTable());
        }
        if (this.filter != null) {
            map.put("filter", this.getFilter());
        }
        return map;
    }

    /**
     * - POSTGRESQL CHANGES FILTER BUILDER -
     * <p>
     * Builder pattern implementation for creating PostgresChangesFilter instances with
     * fluent API and validation. Ensures required fields are set and provides sensible
     * defaults (e.g., schema defaults to "public").
     *
     * <h3>Required Fields:</h3>
     * <ul>
     * <li><b>event</b> - Set via constructor (INSERT, UPDATE, DELETE, or ALL)</li>
     * <li><b>callback</b> - Set via callback() method</li>
     * </ul>
     *
     * <h3>Optional Fields:</h3>
     * <ul>
     * <li><b>schema</b> - Defaults to "public" if not specified</li>
     * <li><b>table</b> - No default, can be null to listen to all tables in schema</li>
     * <li><b>filter</b> - No default, used for row-level filtering</li>
     * </ul>
     */
    public static class Builder {
        /** - Database operation event type (INSERT, UPDATE, DELETE, ALL) - */
        private final RealtimeEvent event;

        /** - Database schema name (defaults to "public") - */
        private String schema;

        /** - Optional specific table name to monitor - */
        private String table;

        /** - Optional PostgREST-style row filter - */
        private String filter;

        /** - Required callback function for matching events - */
        private Consumer<Map<String, Object>> callback;

        /**
         * - BUILDER CONSTRUCTOR -
         * <p>
         * Creates a new filter builder for the specified database operation event type.
         * Only database change events (INSERT, UPDATE, DELETE, ALL) are valid.
         * Schema defaults to "public".
         *
         * @param event Database operation event type to listen for
         * @throws IllegalArgumentException if event is not a valid database operation type
         */
        public Builder(RealtimeEvent event) {
            if (event != RealtimeEvent.ALL && event != RealtimeEvent.INSERT &&
                    event != RealtimeEvent.UPDATE && event != RealtimeEvent.DELETE) {
                throw new IllegalArgumentException("PostgresChangesFilter event must be ALL, INSERT, UPDATE, or DELETE.");
            }
            this.event = event;
            this.schema = "public";
        }

        /**
         * - SCHEMA CONFIGURATION -
         * <p>
         * Sets the database schema name where the target table resides.
         * Defaults to "public" if not called.
         *
         * @param schema Database schema name (e.g., "public", "auth", "storage")
         * @return This builder instance for method chaining
         */
        public Builder schema(String schema) {
            this.schema = schema;
            return this;
        }

        /**
         * - TABLE SPECIFICATION -
         * <p>
         * Sets the specific table name to monitor for changes. If not specified,
         * all tables in the schema may be monitored (depending on RLS policies).
         *
         * @param table Table name (e.g., "users", "products", "orders")
         * @return This builder instance for method chaining
         */
        public Builder table(String table) {
            this.table = table;
            return this;
        }

        /**
         * - ROW-LEVEL FILTER -
         * <p>
         * Sets an optional PostgREST-style filter for row-level event targeting.
         * Only rows matching this filter will trigger callbacks.
         * <p>
         * Filter syntax follows PostgREST conventions:
         * <ul>
         * <li>"id=eq.5" - id equals 5</li>
         * <li>"price=gt.100" - price greater than 100</li>
         * <li>"status=in.(active,pending)" - status in list</li>
         * </ul>
         *
         * @param filter PostgREST-style filter expression
         * @return This builder instance for method chaining
         */
        public Builder filter(String filter) {
            this.filter = filter;
            return this;
        }

        /**
         * - CALLBACK REGISTRATION -
         * <p>
         * Sets the callback function to execute when matching database change events occur.
         * This is a required field.
         *
         * @param callback Function receiving the event payload with change data
         * @return This builder instance for method chaining
         */
        public Builder callback(Consumer<Map<String, Object>> callback) {
            this.callback = callback;
            return this;
        }

        /**
         * - BUILD FILTER INSTANCE -
         * <p>
         * Creates the final immutable PostgresChangesFilter instance with all
         * configured parameters. Validates that required fields are set.
         *
         * @return Configured PostgresChangesFilter ready for subscription
         * @throws NullPointerException if required fields (event, schema, callback) are null
         */
        public PostgresChangesFilter build() {
            return new PostgresChangesFilter(this);
        }
    }
}