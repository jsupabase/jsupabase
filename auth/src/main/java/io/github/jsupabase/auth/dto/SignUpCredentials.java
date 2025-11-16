package io.github.jsupabase.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO for the complete 'signUp' request, including optional data.
 *
 * @author neilhdezs
 * @version 0.1.0
 * @since 0.1.0
 */

public class SignUpCredentials {

    @JsonProperty("email")
    private final String email;

    @JsonProperty("password")
    private final String password;

    @JsonProperty("options")
    private final SignUpOptions options;

    public SignUpCredentials(String email, String password, SignUpOptions options) {
        this.email = email;
        this.password = password;
        this.options = options;
    }

    // Getters
    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public SignUpOptions getOptions() {
        return options;
    }
}