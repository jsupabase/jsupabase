package io.github.jsupabase.auth.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.jsupabase.auth.enums.OtpType;

/**
 * DTO for the verifyOtp() request body.
 * (Based on GoTrue API spec)
 *
 * @author neilhdezs
 * @version 0.1.0
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class VerifyOtpParams {

    @JsonProperty("email")
    private final String email;

    @JsonProperty("phone")
    private final String phone;

    @JsonProperty("token")
    private final String token;

    @JsonProperty("type")
    private final OtpType type;

    /**
     * Creates parameters for OTP verification.
     *
     * @param email The email (if type is email-based).
     * @param phone The phone (if type is phone-based).
     * @param token The OTP code the user provided.
     * @param type The type of OTP (e.g., SIGNUP, RECOVERY).
     */
    public VerifyOtpParams(String email, String phone, String token, OtpType type) {
        this.email = email;
        this.phone = phone;
        this.token = token;
        this.type = type;
    }

    // --- Conveniences ---

    public static VerifyOtpParams forEmail(String email, String token, OtpType type) {
        return new VerifyOtpParams(email, null, token, type);
    }

    public static VerifyOtpParams forPhone(String phone, String token, OtpType type) {
        return new VerifyOtpParams(null, phone, token, type);
    }
}