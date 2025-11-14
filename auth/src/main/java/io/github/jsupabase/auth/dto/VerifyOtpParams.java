package io.github.jsupabase.auth.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.jsupabase.auth.enums.OtpType; // Importamos el Enum

/**
 * DTO for the 'verifyOtp' request.
 * This object can contain *either* an email or a phone number.
 *
 * @author neilhdezs
 * @version 0.1.0
 */
@JsonInclude(JsonInclude.Include.NON_NULL) // Solo serializa campos no nulos
public class VerifyOtpParams {

    /** - The user's email address (if verifying with email) - **/
    @JsonProperty("email")
    private final String email;

    /** - The user's phone number (if verifying with phone) - **/
    @JsonProperty("phone")
    private final String phone;

    /** - The OTP token received by the user - **/
    @JsonProperty("token")
    private final String token;

    /** - The type of OTP challenge being verified - **/
    @JsonProperty("type")
    private final OtpType type;

    /**
     * Private constructor. Use static factory methods.
     */
    private VerifyOtpParams(String email, String phone, String token, OtpType type) {
        this.email = email;
        this.phone = phone;
        this.token = token;
        this.type = type;
    }

    /**
     * Creates parameters for verifying an email-based OTP.
     *
     * @param email The user's email.
     * @param token The token received via email.
     * @param type The type of challenge (e.g., OtpType.SIGNUP).
     * @return A new VerifyOtpParams instance.
     */
    public static VerifyOtpParams forEmailToken(String email, String token, OtpType type) {
        return new VerifyOtpParams(email, null, token, type);
    }

    /**
     * Creates parameters for verifying a phone-based OTP.
     *
     * @param phone The user's phone number.
     * @param token The token received via SMS/WhatsApp.
     * @param type The type of challenge (e.g., OtpType.SIGNUP).
     * @return A new VerifyOtpParams instance.
     */
    public static VerifyOtpParams forPhoneToken(String phone, String token, OtpType type) {
        return new VerifyOtpParams(null, phone, token, type);
    }

    // Getters (for serialization)

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public String getToken() {
        return token;
    }

    public OtpType getType() {
        return type;
    }
}