package io.github.jsupabase.auth.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO for the main successful authentication response.
 * This object is the 'data' part of the response, containing
 * both the session and user details.
 *
 * @author neilhdezs
 * @version 0.1.0
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class AuthResponse {

    /** - The user's session object (can be null, e.g., on signup) - **/
    @JsonProperty("session")
    private Session session;

    /** - The user object - **/
    @JsonProperty("user")
    private User user;

    // Getters and Setters (needed for Jackson deserialization)

    public Session getSession() {
        return session;
    }

    public void setSession(Session session) {
        this.session = session;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}