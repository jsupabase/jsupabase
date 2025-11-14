package io.github.jsupabase.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO for the 'exchangeCodeForSession' request (PKCE flow).
 * This serializes to: {"grant_type": "authorization_code", "client_id": "...",
 * "auth_code": "...", "code_verifier": "..."}
 *
 * @author neilhdezs
 * @version 0.1.0
 */
public class ExchangeCodeCredentials {

    /** - The grant type, always "authorization_code" for PKCE - **/
    @JsonProperty("grant_type")
    private final String grantType = "authorization_code";

    /** - The Supabase Anon Key (required by GoTrue) - **/
    @JsonProperty("client_id")
    private final String clientId;

    /** - The authorization code from the URL redirect - **/
    @JsonProperty("auth_code")
    private final String authCode;

    /** - The PKCE code verifier string - **/
    @JsonProperty("code_verifier")
    private final String codeVerifier;

    /**
     * Creates a new credentials instance for PKCE code exchange.
     * @param clientId The Supabase Anon Key.
     * @param authCode The authorization code from the URL.
     * @param codeVerifier The PKCE code verifier.
     */
    public ExchangeCodeCredentials(String clientId, String authCode, String codeVerifier) {
        this.clientId = clientId;
        this.authCode = authCode;
        this.codeVerifier = codeVerifier;
    }

    // Getters (for serialization)
    public String getGrantType() { return grantType; }
    public String getClientId() { return clientId; }
    public String getAuthCode() { return authCode; }
    public String getCodeVerifier() { return codeVerifier; }
}