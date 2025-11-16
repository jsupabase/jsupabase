package io.github.jsupabase.prueba.dto.options;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO for the 'sortBy' parameter in list() options.
 *
 * @author neilhdezs
 * @version 0.1.0
 * @since 0.1.0
 */
public class SortBy {

    @JsonProperty("column")
    private final String column;

    @JsonProperty("order")
    private final String order;

    public SortBy(String column, String order) {
        this.column = column;
        this.order = order;
    }
}