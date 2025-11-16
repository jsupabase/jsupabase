package io.github.jsupabase.storage.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO for one item in a createSignedUrls() response list.
 *
 * @author neilhdezs
 * @version 0.1.0
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class SignedUrlListItem {

    @JsonProperty("error")
    private String error;

    @JsonProperty("path")
    private String path;

    @JsonProperty("signedURL")
    private String signedUrl;

    // Getters
    public String getError() { return error; }
    public String getPath() { return path; }
    public String getSignedUrl() { return signedUrl; }
}