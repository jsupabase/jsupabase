package io.github.jsupabase.auth.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.jsupabase.auth.enums.OAuthProvider; // Importamos el Enum

/**
 * DTO for the 'signInWithOAuth' request body.
 *
 * @author neilhdezs
 * @version 0.1.0
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OAuthCredentials {

    /** - The OAuth provider (e.g., GOOGLE, GITHUB) - **/
    @JsonProperty("provider")
    private final OAuthProvider provider;

    /** - Additional options for the OAuth request - **/
    @JsonProperty("options")
    private final OAuthOptions options;

    /**
     * Creates a new OAuthCredentials instance.
     * @param provider The OAuth provider.
     * @param options The (optional) options.
     */
    public OAuthCredentials(OAuthProvider provider, OAuthOptions options) {
        this.provider = provider;
        this.options = options;
    }

    /**
     * Creates a new OAuthCredentials instance (no options).
     * @param provider The OAuth provider.
     */
    public OAuthCredentials(OAuthProvider provider) {
        this.provider = provider;
        this.options = null;
    }

    // Getters (for serialization)

    public OAuthProvider getProvider() {
        return provider;
    }

    public OAuthOptions getOptions() {
        return options;
    }
}