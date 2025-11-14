package io.github.jsupabase.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO for the 'Session' object returned by Supabase Auth.
 * This contains the user's tokens and the user object itself.
 *
 * @author neilhdezs
 * @version 0.1.0
 */
public class Session {

    /** - The JWT access token - **/
    @JsonProperty("access_token")
    private String accessToken;

    /** - The type of token (always "bearer") - **/
    @JsonProperty("token_type")
    private String tokenType;

    /** - The duration in seconds until the access token expires - **/
    @JsonProperty("expires_in")
    private int expiresIn;

    /** - The timestamp of when the access token expires - **/
    @JsonProperty("expires_at")
    private long expiresAt;

    /** - The token used to refresh the session - **/
    @JsonProperty("refresh_token")
    private String refreshToken;

    /** - The authenticated user object - **/
    @JsonProperty("user")
    private User user;

    // Getters and Setters (needed for Jackson deserialization)

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public int getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(int expiresIn) {
        this.expiresIn = expiresIn;
    }

    public long getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(long expiresAt) {
        this.expiresAt = expiresAt;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}