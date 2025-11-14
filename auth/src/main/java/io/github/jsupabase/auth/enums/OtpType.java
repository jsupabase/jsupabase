package io.github.jsupabase.auth.enums;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * DTO for the different types of One-Time Passwords (OTP)
 * used for verification.
 *
 * @author neilhdezs
 * @version 0.1.0
 */
public enum OtpType {

    /** - For verifying a new user's email or phone after signup - **/
    SIGNUP("signup"),

    /** - For verifying a user who is recovering their password - **/
    RECOVERY("recovery"),

    /** - For verifying a user who was invited - **/
    INVITE("invite"),

    /** - For verifying a user who is changing their email address - **/
    EMAIL_CHANGE("email_change"),

    /** - For verifying a user who is changing their phone number - **/
    PHONE_CHANGE("phone_change");

    /** - The raw string value for the API - **/
    private final String value;

    OtpType(String value) {
        this.value = value;
    }

    /**
     * This annotation tells Jackson to serialize the enum
     * using this method's return value.
     * @return The lowercase string value.
     */
    @JsonValue
    public String getValue() {
        return value;
    }
}