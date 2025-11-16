package io.github.jsupabase.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

/**
 * DTO for the nested 'options' object used in 'signInWithOAuth'.
 * We use JsonInclude.Include.NON_NULL so only set fields are serialized.
 *
 * @author neilhdezs
 * @version 0.1.0
 */
 // ¡Importante!
public class OAuthOptions {

    /** - A URL to send the user to after they are confirmed - **/
    @JsonProperty("redirectTo")
    private String redirectTo;

    /** - A space-separated list of scopes granted to the OAuth application - **/
    @JsonProperty("scopes")
    private String scopes;

    /** - An object of query params - **/
    @JsonProperty("queryParams")
    private Map<String, String> queryParams;

    // --- Builder Pattern ---

    private OAuthOptions(Builder builder) {
        this.redirectTo = builder.redirectTo;
        this.scopes = builder.scopes;
        this.queryParams = builder.queryParams;
    }

    public static class Builder {
        private String redirectTo;
        private String scopes;
        private Map<String, String> queryParams;

        public Builder redirectTo(String redirectTo) {
            this.redirectTo = redirectTo;
            return this;
        }

        public Builder scopes(String scopes) {
            this.scopes = scopes;
            return this;
        }

        public Builder queryParams(Map<String, String> queryParams) {
            this.queryParams = queryParams;
            return this;
        }

        public OAuthOptions build() {
            return new OAuthOptions(this);
        }
    }

    /**
     * Creates a new builder for OAuthOptions.
     * @return A new Builder instance.
     */
    public static Builder builder() {
        return new Builder();
    }

    // Getters (for serialization)
    public String getRedirectTo() { return redirectTo; }
    public String getScopes() { return scopes; }
    public Map<String, String> getQueryParams() { return queryParams; }
}