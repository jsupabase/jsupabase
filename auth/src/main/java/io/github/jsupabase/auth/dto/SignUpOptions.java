package io.github.jsupabase.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

/**
 * DTO for the nested 'options' object used in 'signUp'.
 * We use JsonInclude.Include.NON_NULL so only set fields are serialized.
 *
 * @author neilhdezs
 * @version 0.1.0
 * @since 0.1.0
 */
public class SignUpOptions {

    /** - A URL to send the user to after they are confirmed - **/
    @JsonProperty("emailRedirectTo")
    private String emailRedirectTo;

    /** - A custom data object to store the user's metadata - **/
    @JsonProperty("data")
    private Map<String, Object> data;

    // --- Builder Pattern ---

    private SignUpOptions(Builder builder) {
        this.emailRedirectTo = builder.emailRedirectTo;
        this.data = builder.data;
    }

    /**
     * Creates a new builder for SignUpOptions.
     *
     * @return A new Builder instance.
     */
    public static Builder builder() {
        return new Builder();
    }

    // Getters (for serialization)
    public String getEmailRedirectTo() {
        return emailRedirectTo;
    }

    public Map<String, Object> getData() {
        return data;
    }

    public static class Builder {
        private String emailRedirectTo;
        private Map<String, Object> data;

        public Builder emailRedirectTo(String emailRedirectTo) {
            this.emailRedirectTo = emailRedirectTo;
            return this;
        }

        public Builder data(Map<String, Object> data) {
            this.data = data;
            return this;
        }

        public SignUpOptions build() {
            return new SignUpOptions(this);
        }
    }
}