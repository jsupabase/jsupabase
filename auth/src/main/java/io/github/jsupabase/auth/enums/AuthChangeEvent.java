package io.github.jsupabase.auth.enums;

/**
 * Defines the different types of authentication state changes.
 *
 * @author neilhdezs
 * @version 0.1.0
 */
public enum AuthChangeEvent {

    /**
     * Fired when the client is initialized and the initial session
     * is loaded from storage (or is null).
     */
    INITIAL_SESSION,

    /**
     * Fired when a user successfully signs in or a new session
     * is established.
     */
    SIGNED_IN,

    /**
     * Fired when a user signs out.
     */
    SIGNED_OUT,

    /**
     * Fired when the session's access token has been refreshed.
     */
    TOKEN_REFRESHED,

    /**
     * Fired when the user's details are updated.
     */
    USER_UPDATED,

    /**
     * Fired when the user is recovering their password.
     */
    PASSWORD_RECOVERY
}