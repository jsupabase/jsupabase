package io.github.jsupabase.auth.enums;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Represents all supported OAuth providers.
 * The string value is what the GoTrue API expects.
 *
 * @author neilhdezs
 * @version 0.1.0
 * @since 0.1.0
 */
public enum OAuthProvider {

    // --- Lista de proveedores ---
    APPLE("apple"),
    AZURE("azure"),
    BITBUCKET("bitbucket"),
    DISCORD("discord"),
    FACEBOOK("facebook"),
    FIGMA("figma"),
    GITHUB("github"),
    GITLAB("gitlab"),
    GOOGLE("google"),
    KAKAO("kakao"),
    KEYCLOAK("keycloak"),
    LINKEDIN("linkedin"),
    NOTION("notion"),
    SLACK("slack"),
    SPOTIFY("spotify"),
    TWITCH("twitch"),
    TWITTER("twitter"),
    WORKOS("workos"),
    ZOOM("zoom"),
    FLY("fly"),
    LINKEDIN_OIDC("linkedin_oidc"),
    SLACK_OIDC("slack_oidc");

    /** - The raw string value for the API - **/
    private final String value;

    OAuthProvider(String value) {
        this.value = value;
    }

    /**
     * This annotation tells Jackson to serialize the enum
     * using this method's return value.
     *
     * @return The lowercase string value.
     */
    @JsonValue
    public String getValue() {
        return value;
    }
}