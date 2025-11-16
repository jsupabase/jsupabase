package io.github.jsupabase.auth;

/**
 * Provides static methods for constructing API endpoint paths for Supabase Auth (GoTrue).
 * <p>
 * This is a stateless utility class and is the single source of truth for
 * all GoTrue API paths.
 *
 * @author neilhdezs
 * @version 0.1.0
 */
public final class AuthPaths {

    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private AuthPaths() {
        throw new IllegalStateException("This is a utility class and cannot be instantiated.");
    }

    /**
     * Corresponds to: POST /signup
     * (Also used for anonymous sign-in with ?anon=true)
     */
    public static String getSignupPath(String servicePath) {
        return servicePath + "/signup";
    }

    /**
     * Corresponds to: POST /token
     * (Used for password, refresh_token, and pkce grants)
     */
    public static String getTokenPath(String servicePath) {
        return servicePath + "/token";
    }

    /**
     * Corresponds to: POST /sso/idtoken
     */
    public static String getSsoIdTokenPath(String servicePath) {
        return servicePath + "/sso/idtoken";
    }

    /**
     * Corresponds to: POST /verify
     */
    public static String getVerifyPath(String servicePath) {
        return servicePath + "/verify";
    }

    /**
     * Corresponds to: POST /otp
     */
    public static String getOtpPath(String servicePath) {
        return servicePath + "/otp";
    }

    /**
     * Corresponds to: POST /oauth
     * (Note: The doc also mentions /authorize, but /oauth is for the PKCE/API flow)
     */
    public static String getOAuthPath(String servicePath) {
        return servicePath + "/oauth";
    }

    /**
     * Corresponds to: GET /user and PUT /user
     */
    public static String getUserPath(String servicePath) {
        return servicePath + "/user";
    }

    /**
     * Corresponds to: POST /logout
     */
    public static String getLogoutPath(String servicePath) {
        return servicePath + "/logout";
    }

    /**
     * Corresponds to: POST /recover
     */
    public static String getRecoverPath(String servicePath) {
        return servicePath + "/recover";
    }
}