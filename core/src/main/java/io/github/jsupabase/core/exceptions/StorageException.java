package io.github.jsupabase.core.exceptions;

/**
 * - STORAGE EXCEPTION -
 * <p>
 * Specialized runtime exception for Supabase Storage operations. Used to
 * represent HTTP/protocol or client-side errors that occur while interacting
 * with the Storage REST API.
 *
 * @author neilhdezs
 * @version 0.1.0
 * @since 0.1.0
 */
public class StorageException extends SupabaseException {

    /** - Creates a StorageException with a message - **/
    public StorageException(String message) {
        super(message, -1);
    }

    /** - Creates a StorageException with a message and HTTP status code - **/
    public StorageException(String message, int statusCode) {
        super(message, statusCode);
    }

    /** - Creates a StorageException with a message and cause - **/
    public StorageException(String message, Throwable cause) {
        super(message, cause);
    }
}

