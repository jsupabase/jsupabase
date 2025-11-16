package io.github.jsupabase.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

/**
 * DTO for the 'signInAnonymously' options.
 *
 * @author neilhdezs
 * @version 0.1.0
 */

public class AnonymousOptions {

    /** - A custom data object to store the user's metadata - **/
    @JsonProperty("data")
    private final Map<String, Object> data;

    /** - Verification token for CAPTCHA - **/
    @JsonProperty("captchaToken")
    private final String captchaToken;

    // --- Builder Pattern ---

    private AnonymousOptions(Builder builder) {
        this.data = builder.data;
        this.captchaToken = builder.captchaToken;
    }

    public static class Builder {
        private Map<String, Object> data;
        private String captchaToken;

        public Builder data(Map<String, Object> data) {
            this.data = data;
            return this;
        }

        public Builder captchaToken(String captchaToken) {
            this.captchaToken = captchaToken;
            return this;
        }

        public AnonymousOptions build() {
            return new AnonymousOptions(this);
        }
    }

    /**
     * Creates a new builder for AnonymousOptions.
     * @return A new Builder instance.
     */
    public static Builder builder() {
        return new Builder();
    }

    // Getters (for serialization)
    public Map<String, Object> getData() { return data; }
    public String getCaptchaToken() { return captchaToken; }
}