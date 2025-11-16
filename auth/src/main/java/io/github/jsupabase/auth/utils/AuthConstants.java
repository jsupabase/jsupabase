package io.github.jsupabase.auth.utils;

/**
 * - AUTHENTICATION CONSTANTS -
 * <p>
 * Centralized constants for headers, payload keys, query parameters, and log messages
 * used across the Authentication module.
 *
 * @author neilhdezs
 * @version 0.1.0
 * @since 0.1.0
 */
public final class AuthConstants {

    private AuthConstants() {}

    /** - HTTP header Content-Type - **/
    public static final String HDR_CONTENT_TYPE = "Content-Type";
    /** - HTTP header Authorization - **/
    public static final String HDR_AUTHORIZATION = "Authorization";

    /** - MIME type for JSON - **/
    public static final String MIME_JSON = "application/json";

    /** - Query parameter grant_type - **/
    public static final String PARAM_GRANT_TYPE = "grant_type";
    /** - Query parameter anon - **/
    public static final String PARAM_ANON = "anon";

    /** - Grant type password - **/
    public static final String GRANT_PASSWORD = "password";
    /** - Grant type refresh_token - **/
    public static final String GRANT_REFRESH_TOKEN = "refresh_token";

    /** - Query string for password grant - **/
    public static final String QUERY_GRANT_PASSWORD = "?grant_type=password";
    /** - Query string for refresh token grant - **/
    public static final String QUERY_GRANT_REFRESH = "?grant_type=refresh_token";
    /** - Query string for anonymous signup - **/
    public static final String QUERY_ANON_TRUE = "?anon=true";

    /** - Log: User sign up initiated - **/
    public static final String LOG_SIGNUP_INITIATED = "User sign up initiated for email: {}";
    /** - Log: User sign in successful - **/
    public static final String LOG_SIGNIN_SUCCESS = "User signed in successfully: {}";
    /** - Log: User sign out - **/
    public static final String LOG_SIGNOUT = "User signed out";
    /** - Log: Session refreshed - **/
    public static final String LOG_SESSION_REFRESHED = "Session refreshed for user: {}";
    /** - Log: OTP sent - **/
    public static final String LOG_OTP_SENT = "OTP sent to: {}";
    /** - Log: OTP verified - **/
    public static final String LOG_OTP_VERIFIED = "OTP verified successfully";
    /** - Log: OAuth flow initiated - **/
    public static final String LOG_OAUTH_INITIATED = "OAuth flow initiated for provider: {}";
    /** - Log: Password reset requested - **/
    public static final String LOG_PASSWORD_RESET = "Password reset requested for: {}";
    /** - Log: User updated - **/
    public static final String LOG_USER_UPDATED = "User attributes updated";
    /** - Log: Session set - **/
    public static final String LOG_SESSION_SET = "Session set manually";

    /** - Error: Sign up failed - **/
    public static final String ERROR_SIGNUP_FAILED = "Sign up failed: {}";
    /** - Error: Sign in failed - **/
    public static final String ERROR_SIGNIN_FAILED = "Sign in failed: {}";
    /** - Error: Sign out failed - **/
    public static final String ERROR_SIGNOUT_FAILED = "Sign out failed: {}";
    /** - Error: Session refresh failed - **/
    public static final String ERROR_REFRESH_FAILED = "Session refresh failed: {}";
    /** - Error: OTP send failed - **/
    public static final String ERROR_OTP_SEND_FAILED = "OTP send failed: {}";
    /** - Error: User update failed - **/
    public static final String ERROR_USER_UPDATE_FAILED = "User update failed: {}";
}

