package io.github.jsupabase.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO for 'signUp' and 'signIn' requests that use email and password.
 * This serializes to: {"email": "...", "password": "..."}
 *
 * @author neilhdezs
 * @version 0.1.0
 * @since 0.1.0
 */
public class EmailPasswordCredentials {

    /** - The user's email address - **/
    @JsonProperty("email")
    private final String email;

    /** - The user's plain-text password - **/
    @JsonProperty("password")
    private final String password;

    /**
     * Creates a new credentials instance.
     *
     * @param email    The user's email.
     * @param password The user's password.
     */
    public EmailPasswordCredentials(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }
}