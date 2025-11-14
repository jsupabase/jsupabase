package io.github.jsupabase.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO for the 'signInWithOAuth' response.
 * This object contains the URL to redirect the user to.
 *
 * @author neilhdezs
 * @version 0.1.0
 */
public class OAuthResponse {

    /** - The provider name (e.g., "google", "github") - **/
    @JsonProperty("provider")
    private String provider;

    /** - The URL to which the user should be redirected - **/
    @JsonProperty("url")
    private String url;

    // Getters and Setters (needed for Jackson deserialization)

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}