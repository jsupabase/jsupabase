package io.github.jsupabase.storage.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO for a successful createSignedUrl() response.
 *
 * @author neilhdezs
 * @version 0.1.0
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class SignedUrlResponse {

    @JsonProperty("signedURL")
    private String signedUrl;

    public String getSignedUrl() {
        return signedUrl;
    }
}