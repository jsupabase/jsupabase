package io.github.jsupabase.auth.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents the successful authentication response from GoTrue.
 * This object contains the user and their access tokens.
 *
 * @author neilhdezs
 * @version 0.1.0
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Session {

    @JsonProperty("access_token")
    private String accessToken;

    @JsonProperty("refresh_token")
    private String refreshToken;

    @JsonProperty("expires_in")
    private int expiresIn;

    @JsonProperty("token_type")
    private String tokenType;

    @JsonProperty("user")
    private User user;

    // Getters
    public String getAccessToken() {
        return accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public int getExpiresIn() {
        return expiresIn;
    }

    public String getTokenType() {
        return tokenType;
    }

    public User getUser() {
        return user;
    }

    // Setter (usado por AuthClient para actualizar el usuario)
    public void setUser(User user) {
        this.user = user;
    }

    @Override
    public String toString() {
        return "Session{" +
                "accessToken='" + accessToken + '\'' +
                ", refreshToken='" + refreshToken + '\'' +
                ", expiresIn=" + expiresIn +
                ", tokenType='" + tokenType + '\'' +
                ", user=" + user +
                '}';
    }
}