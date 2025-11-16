package io.github.jsupabase.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

/**
 * DTO for the nested 'options' object used in 'signInWithOtp'.
 * We use JsonInclude.Include.NON_NULL so that only the fields
 * that are set (not null) will be included in the JSON.
 *
 * @author neilhdezs
 * @version 0.1.0
 */

public class OtpOptions {

    /** - The redirect URL embedded in the email link - **/
    @JsonProperty("emailRedirectTo")
    private String emailRedirectTo;

    /** - If set to false, this method will not create a new user - **/
    @JsonProperty("shouldCreateUser")
    private Boolean shouldCreateUser;

    /** - Custom user metadata - **/
    @JsonProperty("data")
    private Map<String, Object> data;

    /** - Verification token for CAPTCHA - **/
    @JsonProperty("captchaToken")
    private String captchaToken;

    /** - Messaging channel to use (e.g., "sms" or "whatsapp") - **/ // TODO: VER LOS CANALES Y METERLOS EN ENUMERADOS
    @JsonProperty("channel")
    private String channel;

    // --- Builder Pattern ---
    // Usamos un Builder para hacer la creación de estas opciones más limpia.

    private OtpOptions(Builder builder) {
        this.emailRedirectTo = builder.emailRedirectTo;
        this.shouldCreateUser = builder.shouldCreateUser;
        this.data = builder.data;
        this.captchaToken = builder.captchaToken;
        this.channel = builder.channel;
    }

    public static class Builder {
        private String emailRedirectTo;
        private Boolean shouldCreateUser;
        private Map<String, Object> data;
        private String captchaToken;
        private String channel;

        public Builder emailRedirectTo(String emailRedirectTo) {
            this.emailRedirectTo = emailRedirectTo;
            return this;
        }

        public Builder shouldCreateUser(boolean shouldCreateUser) {
            this.shouldCreateUser = shouldCreateUser;
            return this;
        }

        public Builder data(Map<String, Object> data) {
            this.data = data;
            return this;
        }

        public Builder captchaToken(String captchaToken) {
            this.captchaToken = captchaToken;
            return this;
        }

        public Builder channel(String channel) {
            this.channel = channel;
            return this;
        }

        public OtpOptions build() {
            return new OtpOptions(this);
        }
    }

    // --- Métodos de Fábrica (Helpers) ---
    /**
     * Crea un constructor de OtpOptions.
     * @return un nuevo Builder.
     */
    public static Builder builder() {
        return new Builder();
    }


    // Getters (para serialización)
    public String getEmailRedirectTo() { return emailRedirectTo; }
    public Boolean getShouldCreateUser() { return shouldCreateUser; }
    public Map<String, Object> getData() { return data; }
    public String getCaptchaToken() { return captchaToken; }
    public String getChannel() { return channel; }
}