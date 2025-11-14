package io.github.jsupabase.auth.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO for the 'signInWithIdToken' request (native OIDC sign-in).
 * We use NON_NULL inclusion for the optional fields.
 *
 * @author neilhdezs
 * @version 0.1.0
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class IdTokenCredentials {

    /** - The OIDC provider (e.g., "google", "apple") - **/
    @JsonProperty("provider")
    private final String provider;

    /** - The OIDC ID token issued by the provider - **/
    @JsonProperty("token")
    private final String token;

    /** - Optional access token (required by some providers) - **/
    @JsonProperty("access_token")
    private final String accessToken;

    /** - Optional nonce (if used during the OIDC flow) - **/
    @JsonProperty("nonce")
    private final String nonce;

    // --- Builder Pattern ---

    private IdTokenCredentials(Builder builder) {
        this.provider = builder.provider;
        this.token = builder.token;
        this.accessToken = builder.accessToken;
        this.nonce = builder.nonce;
    }

    public static class Builder {
        private String provider;
        private String token;
        private String accessToken;
        private String nonce;

        public Builder(String provider, String token) {
            this.provider = provider;
            this.token = token;
        }

        public Builder accessToken(String accessToken) {
            this.accessToken = accessToken;
            return this;
        }

        public Builder nonce(String nonce) {
            this.nonce = nonce;
            return this;
        }

        public IdTokenCredentials build() {
            return new IdTokenCredentials(this);
        }
    }

    /**
     * Creates a new builder for IdTokenCredentials.
     * @param provider The provider (e.g., "google").
     * @param token The OIDC ID token.
     * @return A new Builder instance.
     */
    public static Builder builder(String provider, String token) {
        return new Builder(provider, token);
    }

    // Getters (for serialization)
    public String getProvider() { return provider; }
    public String getToken() { return token; }
    public String getAccessToken() { return accessToken; }
    public String getNonce() { return nonce; }
}