package io.github.jsupabase.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO for the 'resetPasswordForEmail' request.
 * This serializes to: {"email": "..."}
 *
 * @author neilhdezs
 * @version 0.1.0
 * @since 0.1.0
 */
public class ResetPasswordCredentials {

    /** - The email address of the user - **/
    @JsonProperty("email")
    private final String email;

    /**
     * Creates a new credentials instance for password reset.
     *
     * @param email The user's email.
     */
    public ResetPasswordCredentials(String email) {
        this.email = email;
    }

    // Getter (required for Jackson serialization)

    public String getEmail() {
        return email;
    }
}