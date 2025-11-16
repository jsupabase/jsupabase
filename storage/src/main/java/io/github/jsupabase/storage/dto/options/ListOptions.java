package io.github.jsupabase.storage.dto.options;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO for the complex 'list' options body.
 *
 * @author neilhdezs
 * @version 0.1.0
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ListOptions {

    @JsonProperty("prefix")
    private final String prefix;

    @JsonProperty("limit")
    private final Integer limit;

    @JsonProperty("offset")
    private final Integer offset;

    @JsonProperty("search")
    private final String search;

    @JsonProperty("sortBy")
    private final SortBy sortBy;

    // Constructor privado para el Builder
    private ListOptions(Builder builder) {
        this.prefix = builder.prefix;
        this.limit = builder.limit;
        this.offset = builder.offset;
        this.search = builder.search;
        this.sortBy = builder.sortBy;
    }

    // --- Builder Class ---
    public static class Builder {
        private String prefix;
        private Integer limit = 100; // Default limit
        private Integer offset = 0;
        private String search;
        private SortBy sortBy;

        /**
         * The folder path to list.
         * @param prefix (e.g., "avatars/")
         */
        public Builder(String prefix) {
            this.prefix = prefix;
        }

        public Builder limit(int limit) {
            this.limit = limit;
            return this;
        }

        public Builder offset(int offset) {
            this.offset = offset;
            return this;
        }

        public Builder search(String search) {
            this.search = search;
            return this;
        }

        public Builder sortBy(String column, String order) {
            this.sortBy = new SortBy(column, order);
            return this;
        }

        public ListOptions build() {
            return new ListOptions(this);
        }
    }
}