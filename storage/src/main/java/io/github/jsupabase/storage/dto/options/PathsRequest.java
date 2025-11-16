package io.github.jsupabase.storage.dto.options;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * DTO for remove() request body.
 *
 * @author neilhdezs
 * @version 0.1.0
 */
public class PathsRequest {

    @JsonProperty("prefixes")
    private final List<String> paths;

    public PathsRequest(List<String> paths) {
        this.paths = paths;
    }
}