package io.github.jsupabase.auth.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO for the 'signInWithOtp' request.
 * This object can contain *either* an email or a phone number.
 * We use NON_NULL inclusion to serialize only the provided field.
 *
 * @author neilhdezs
 * @version 0.1.0
 */
@JsonInclude(JsonInclude.Include.NON_NULL) // ¡Muy importante!
public class OtpCredentials {

    /** - The user's email address (if signing in with email) - **/
    @JsonProperty("email")
    private final String email;

    /** - The user's phone number (if signing in with phone) - **/
    @JsonProperty("phone")
    private final String phone;

    /** - Additional options for the OTP request - **/
    @JsonProperty("options")
    private final OtpOptions options;

    /**
     * Private constructor. Use static factory methods.
     */
    private OtpCredentials(String email, String phone, OtpOptions options) {
        this.email = email;
        this.phone = phone;
        this.options = options;
    }

    /**
     * Creates an OtpCredentials instance for an email sign-in.
     *
     * @param email The user's email.
     * @param options The (optional) OTP options.
     * @return A new OtpCredentials instance.
     */
    public static OtpCredentials forEmail(String email, OtpOptions options) {
        return new OtpCredentials(email, null, options);
    }

    /**
     * Creates an OtpCredentials instance for an email sign-in (no options).
     *
     * @param email The user's email.
     * @return A new OtpCredentials instance.
     */
    public static OtpCredentials forEmail(String email) {
        return new OtpCredentials(email, null, null);
    }

    /**
     * Creates an OtpCredentials instance for a phone sign-in.
     *
     * @param phone The user's phone number.
     * @param options The (optional) OTP options.
     * @return A new OtpCredentials instance.
     */
    public static OtpCredentials forPhone(String phone, OtpOptions options) {
        return new OtpCredentials(null, phone, options);
    }

    /**
     * Creates an OtpCredentials instance for a phone sign-in (no options).
     *
     * @param phone The user's phone number.
     * @return A new OtpCredentials instance.
     */
    public static OtpCredentials forPhone(String phone) {
        return new OtpCredentials(null, phone, null);
    }

    // Getters (for serialization)

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public OtpOptions getOptions() {
        return options;
    }
}