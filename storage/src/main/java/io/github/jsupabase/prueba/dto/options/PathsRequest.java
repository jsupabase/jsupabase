package io.github.jsupabase.prueba.dto.options;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * DTO for remove() request body.
 *
 * @author neilhdezs
 * @version 0.1.0
 * @since 0.1.0
 */
public class PathsRequest {

    @JsonProperty("prefixes")
    private final List<String> paths;

    public PathsRequest(List<String> paths) {
        this.paths = paths;
    }
}