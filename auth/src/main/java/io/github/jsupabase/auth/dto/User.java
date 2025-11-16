package io.github.jsupabase.auth.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

/**
 * - USER DATA TRANSFER OBJECT -
 * <p>
 * Data transfer object representing a Supabase user with comprehensive profile information
 * and authentication metadata. This immutable DTO contains all user-related data returned
 * by the Supabase Auth API including identity information, authentication timestamps,
 * metadata fields, and associated user identities from OAuth providers.
 * <p>
 * The User object is typically received after successful authentication operations and
 * contains both public profile information and authentication-specific metadata such as
 * email verification status and account creation timestamps.
 *
 * @author neilhdezs
 * @version 0.1.0
 * @since 0.1.0
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class User {

    /** - Unique user identifier assigned by Supabase Auth system - */
    @JsonProperty("id")
    private String id;

    /** - JWT audience claim identifying the intended recipient of the token - */
    @JsonProperty("aud")
    private String audience;

    /** - User role for authorization and access control within the application - */
    @JsonProperty("role")
    private String role;

    /** - Primary email address associated with the user account - */
    @JsonProperty("email")
    private String email;

    /** - Phone number associated with the user account for SMS authentication - */
    @JsonProperty("phone")
    private String phone;

    /** - ISO timestamp of when the user account was initially created - */
    @JsonProperty("created_at")
    private String createdAt;

    /** - The timestamp of when the user was last confirmed (generic) - **/
    @JsonProperty("confirmed_at")
    private String confirmedAt;

    /** - The timestamp of when the user's email was confirmed - **/
    @JsonProperty("email_confirmed_at")
    private String emailConfirmedAt;

    /** - The timestamp of when the user's phone was confirmed - **/
    @JsonProperty("phone_confirmed_at")
    private String phoneConfirmedAt;

    /** - The timestamp of the last sign-in - **/
    @JsonProperty("last_sign_in_at")
    private String lastSignInAt;

    /** - Application-specific metadata (read-only for clients) - **/
    @JsonProperty("app_metadata")
    private Map<String, Object> appMetadata;

    /** - User-specific metadata (readable and writable by the user) - **/
    @JsonProperty("user_metadata")
    private Map<String, Object> userMetadata;

    /** - The list of linked identities for this user (e.g., email, google) - **/
    @JsonProperty("identities")
    private List<UserIdentity> identities;

    /** - The timestamp of when the user was last updated - **/
    @JsonProperty("updated_at")
    private String updatedAt;

    // Getters and Setters (needed for Jackson deserialization)

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAudience() {
        return audience;
    }

    public void setAudience(String audience) {
        this.audience = audience;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getConfirmedAt() {
        return confirmedAt;
    }

    public void setConfirmedAt(String confirmedAt) {
        this.confirmedAt = confirmedAt;
    }

    public String getEmailConfirmedAt() {
        return emailConfirmedAt;
    }

    public void setEmailConfirmedAt(String emailConfirmedAt) {
        this.emailConfirmedAt = emailConfirmedAt;
    }

    public String getPhoneConfirmedAt() {
        return phoneConfirmedAt;
    }

    public void setPhoneConfirmedAt(String phoneConfirmedAt) {
        this.phoneConfirmedAt = phoneConfirmedAt;
    }

    public String getLastSignInAt() {
        return lastSignInAt;
    }

    public void setLastSignInAt(String lastSignInAt) {
        this.lastSignInAt = lastSignInAt;
    }

    public Map<String, Object> getAppMetadata() {
        return appMetadata;
    }

    public void setAppMetadata(Map<String, Object> appMetadata) {
        this.appMetadata = appMetadata;
    }

    public Map<String, Object> getUserMetadata() {
        return userMetadata;
    }

    public void setUserMetadata(Map<String, Object> userMetadata) {
        this.userMetadata = userMetadata;
    }

    public List<UserIdentity> getIdentities() {
        return identities;
    }

    public void setIdentities(List<UserIdentity> identities) {
        this.identities = identities;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }
}