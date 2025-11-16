package io.github.jsupabase.storage.dto.options;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * DTO for createSignedUrls() request body.
 *
 * @author neilhdezs
 * @version 0.1.0
 */
public class SignedUrlRequest {

    @JsonProperty("paths")
    private final List<String> paths;

    @JsonProperty("expiresIn")
    private final int expiresIn;

    public SignedUrlRequest(List<String> paths, int expiresIn) {
        this.paths = paths;
        this.expiresIn = expiresIn;
    }
}