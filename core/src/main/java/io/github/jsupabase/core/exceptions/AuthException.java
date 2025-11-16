package io.github.jsupabase.core.exceptions;

/**
 * - AUTHENTICATION EXCEPTION -
 * <p>
 * Specialized runtime exception for Supabase Authentication operations. Used to
 * represent authentication failures, session errors, OAuth flow issues, and credential
 * validation problems that occur while interacting with the GoTrue API.
 *
 * @author neilhdezs
 * @version 0.1.0
 * @since 0.1.0
 */
public class AuthException extends SupabaseException {

    /** - Creates an AuthException with a message - **/
    public AuthException(String message) {
        super(message, -1);
    }

    /** - Creates an AuthException with a message and HTTP status code - **/
    public AuthException(String message, int statusCode) {
        super(message, statusCode);
    }

    /** - Creates an AuthException with a message and cause - **/
    public AuthException(String message, Throwable cause) {
        super(message, cause);
    }
}

