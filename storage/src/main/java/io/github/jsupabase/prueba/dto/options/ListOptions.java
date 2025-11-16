package io.github.jsupabase.prueba.dto.options;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO for the complex 'list' options body.
 *
 * @author neilhdezs
 * @version 0.1.0
 * @since 0.1.0
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ListOptions {

    /** - Folder prefix to list (e.g., "avatars/") - **/
    @JsonProperty("prefix")
    private final String prefix;

    /** - Maximum number of items to return - **/
    @JsonProperty("limit")
    private final Integer limit;

    /** - Offset for pagination - **/
    @JsonProperty("offset")
    private final Integer offset;

    /** - Search term to filter results - **/
    @JsonProperty("search")
    private final String search;

    /** - Sort options (column + order) - **/
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

    /** - Returns the folder prefix to list - **/
    public String getPrefix() { return prefix; }
    /** - Returns the configured limit - **/
    public Integer getLimit() { return limit; }
    /** - Returns the configured offset - **/
    public Integer getOffset() { return offset; }
    /** - Returns the optional search term - **/
    public String getSearch() { return search; }
    /** - Returns the sort options - **/
    public SortBy getSortBy() { return sortBy; }

    // --- Builder Class ---
    public static class Builder {
        private String prefix;
        private Integer limit = 100; // Default limit
        private Integer offset = 0;
        private String search;
        private SortBy sortBy;

        /**
         * The folder path to list.
         *
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