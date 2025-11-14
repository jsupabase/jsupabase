package io.github.jsupabase.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO for the 'refreshSession' request.
 * This serializes to: {"refresh_token": "..."}
 *
 * @author neilhdezs
 * @version 0.1.0
 */
public class RefreshCredentials {

    /** - The refresh token received from a previous session - **/
    @JsonProperty("refresh_token")
    private final String refreshToken;

    /**
     * Creates a new refresh credentials instance.
     * @param refreshToken The user's refresh token.
     */
    public RefreshCredentials(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    // Getter (required for Jackson serialization)

    public String getRefreshToken() {
        return refreshToken;
    }
}