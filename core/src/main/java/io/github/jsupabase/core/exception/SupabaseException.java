package io.github.jsupabase.core.exception;

/**
 * Base exception for all errors that occur during
 * communication with Supabase APIs.
 *
 * @author neilhdezs
 * @version 0.0.3
 */
public class SupabaseException extends RuntimeException {

    /** - The HTTP status code (e.g., 404, 500) - **/
    private final int statusCode;

    /**
     * Constructor for errors with a status code.
     *
     * @param message The error message (usually the response body)
     * @param statusCode The HTTP status code
     */
    public SupabaseException(String message, int statusCode) {
        super(message);
        this.statusCode = statusCode;
    }

    /**
     * Constructor for network or deserialization errors
     * that do not have a status code.
     *
     * @param message The error message
     * @param cause The original exception (e.g., IOException, JsonProcessingException)
     */
    public SupabaseException(String message, Throwable cause) {
        super(message, cause);
        this.statusCode = -1; // Indicates no status code
    }

    public int getStatusCode() {
        return statusCode;
    }

    @Override
    public String getMessage() {
        if (statusCode != -1) {
            return "HTTP " + statusCode + ": " + super.getMessage();
        }
        return super.getMessage();
    }
}