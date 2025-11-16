package io.github.jsupabase.auth.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

/**
 * DTO for the 'identity' object nested within a User.
 * Represents a single linked identity (e.g., email, google, etc.).
 *
 * @author neilhdezs
 * @version 0.1.0
 * @since 0.1.0
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserIdentity {

    /** - The provider-specific ID for this identity - **/
    @JsonProperty("id")
    private String id;

    /** - The user ID, which should match the parent User's ID - **/
    @JsonProperty("user_id")
    private String userId;

    /** - The provider name (e.g., "email", "google", "github") - **/
    @JsonProperty("provider")
    private String provider;

    /** - The timestamp of when this identity was created - **/
    @JsonProperty("created_at")
    private String createdAt;

    /** - The timestamp of when this identity was last updated - **/
    @JsonProperty("last_sign_in_at")
    private String lastSignInAt;

    /** - Provider-specific metadata for this identity - **/
    @JsonProperty("identity_data")
    private Map<String, Object> identityData;

    // Getters and Setters (needed for Jackson deserialization)

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getLastSignInAt() {
        return lastSignInAt;
    }

    public void setLastSignInAt(String lastSignInAt) {
        this.lastSignInAt = lastSignInAt;
    }

    public Map<String, Object> getIdentityData() {
        return identityData;
    }

    public void setIdentityData(Map<String, Object> identityData) {
        this.identityData = identityData;
    }
}