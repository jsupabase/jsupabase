package io.github.jsupabase.core.exceptions;

/**
 * - POSTGREST EXCEPTION -
 * <p>
 * Specialized runtime exception for Supabase PostgREST operations. Used to
 * represent query building errors, filter validation failures, and HTTP/protocol
 * errors that occur while interacting with the PostgREST API.
 *
 * @author neilhdezs
 * @version 0.1.0
 * @since 0.1.0
 */
public class PostgrestException extends SupabaseException {

    /** - Creates a PostgrestException with a message - **/
    public PostgrestException(String message) {
        super(message, -1);
    }

    /** - Creates a PostgrestException with a message and HTTP status code - **/
    public PostgrestException(String message, int statusCode) {
        super(message, statusCode);
    }

    /** - Creates a PostgrestException with a message and cause - **/
    public PostgrestException(String message, Throwable cause) {
        super(message, cause);
    }
}

