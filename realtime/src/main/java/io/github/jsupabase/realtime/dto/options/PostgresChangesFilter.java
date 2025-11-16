package io.github.jsupabase.realtime.dto.options;

import io.github.jsupabase.realtime.enums.RealtimeEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Configuration for a "postgres_changes" event listener.
 * This class uses a Builder pattern to construct the filter.
 *
 * @author neilhdezs
 * @version 0.1.0
 */
public class PostgresChangesFilter {

    /** - The database event (INSERT, UPDATE, DELETE, ALL) - **/
    private final RealtimeEvent event;

    /** - The database schema (e.g., "public") - **/
    private final String schema;

    /** - The database table (e.g., "todos") - **/
    private final String table;

    /** - The RLS filter (e.g., "id=eq.5") - **/
    private final String filter;

    /** - The user's callback - **/
    private final Consumer<Map<String, Object>> callback;

    // Private constructor, force use of Builder
    private PostgresChangesFilter(Builder builder) {
        this.event = Objects.requireNonNull(builder.event, "Event cannot be null.");
        this.schema = Objects.requireNonNull(builder.schema, "Schema cannot be null.");
        this.callback = Objects.requireNonNull(builder.callback, "Callback cannot be null.");
        this.table = builder.table;
        this.filter = builder.filter;
    }

    // Getters
    public String getEvent() { return event.getPhxEvent(); }
    public String getSchema() { return schema; }
    public String getTable() { return table; }
    public String getFilter() { return filter; }
    public Consumer<Map<String, Object>> getCallback() { return callback; }

    /**
     * Serializes this filter into the Map format expected
     * by the Phoenix "phx_join" payload.
     *
     * @return A Map representing this filter configuration.
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

    // --- Builder Class ---

    /**
     * Builder for creating a PostgresChangesFilter.
     */
    public static class Builder {
        private RealtimeEvent event;
        private String schema;
        private String table;
        private String filter;
        private Consumer<Map<String, Object>> callback;

        /**
         * Creates a new Builder for a postgres_changes filter.
         *
         * @param event The event type (INSERT, UPDATE, DELETE, or ALL).
         */
        public Builder(RealtimeEvent event) {
            if (event != RealtimeEvent.ALL && event != RealtimeEvent.INSERT &&
                    event != RealtimeEvent.UPDATE && event != RealtimeEvent.DELETE) {
                throw new IllegalArgumentException("PostgresChangesFilter event must be ALL, INSERT, UPDATE, or DELETE.");
            }
            this.event = event;
            this.schema = "public"; // Default schema
        }

        public Builder schema(String schema) {
            this.schema = schema;
            return this;
        }

        public Builder table(String table) {
            this.table = table;
            return this;
        }

        public Builder filter(String filter) {
            this.filter = filter;
            return this;
        }

        public Builder callback(Consumer<Map<String, Object>> callback) {
            this.callback = callback;
            return this;
        }

        public PostgresChangesFilter build() {
            return new PostgresChangesFilter(this);
        }
    }
}