package io.github.jsupabase.core.exceptions;

/**
 * - PLATFORM EXCEPTION -
 * <p>
 * Specialized runtime exception for Supabase Platform Management API operations.
 * Used to represent configuration errors, organization/project management failures,
 * and access control issues that occur while interacting with the Platform API.
 *
 * @author neilhdezs
 * @version 0.1.0
 * @since 0.1.0
 */
public class PlatformException extends SupabaseException {

    /** - Creates a PlatformException with a message - **/
    public PlatformException(String message) {
        super(message, -1);
    }

    /** - Creates a PlatformException with a message and HTTP status code - **/
    public PlatformException(String message, int statusCode) {
        super(message, statusCode);
    }

    /** - Creates a PlatformException with a message and cause - **/
    public PlatformException(String message, Throwable cause) {
        super(message, cause);
    }
}

