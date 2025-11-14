package io.github.jsupabase.auth.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;

/**
 * DTO for the 'updateUser' request.
 * Contains all attributes that can be updated.
 * We use NON_NULL inclusion so only set fields are serialized.
 *
 * @author neilhdezs
 * @version 0.1.0
 */
@JsonInclude(JsonInclude.Include.NON_NULL) // ¡Importante!
public class UserAttributes {

    /** - A new email address for the user - **/
    @JsonProperty("email")
    private final String email;

    /** - A new password for the user - **/
    @JsonProperty("password")
    private final String password;

    /** - New user_metadata for the user - **/
    @JsonProperty("data")
    private final Map<String, Object> data;

    // --- Builder Pattern ---

    private UserAttributes(Builder builder) {
        this.email = builder.email;
        this.password = builder.password;
        this.data = builder.data;
    }

    public static class Builder {
        private String email;
        private String password;
        private Map<String, Object> data;

        public Builder email(String email) {
            this.email = email;
            return this;
        }

        public Builder password(String password) {
            this.password = password;
            return this;
        }

        public Builder data(Map<String, Object> data) {
            this.data = data;
            return this;
        }

        public UserAttributes build() {
            return new UserAttributes(this);
        }
    }

    /**
     * Creates a new builder for UserAttributes.
     * @return A new Builder instance.
     */
    public static Builder builder() {
        return new Builder();
    }

    // Getters (for serialization)

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public Map<String, Object> getData() {
        return data;
    }
}