package io.github.jsupabase.core.exceptions;

/**
 * - REALTIME CONNECTION EXCEPTION -
 * <p>
 * Exception thrown when WebSocket connection errors occur in the Realtime module.
 * Includes connection failures, unexpected disconnects, and protocol violations.
 *
 * @author neilhdezs
 * @version 0.1.0
 * @since 0.1.0
 */
public class RealtimeConnectionException extends SupabaseException {

    /** - Constructs a RealtimeConnectionException with message and cause - **/
    public RealtimeConnectionException(String message, Throwable cause) {
        super(message, cause);
    }
}

