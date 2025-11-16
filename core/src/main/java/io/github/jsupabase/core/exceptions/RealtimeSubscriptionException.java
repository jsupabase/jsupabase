package io.github.jsupabase.core.exceptions;

/**
 * - REALTIME SUBSCRIPTION EXCEPTION -
 * <p>
 * Exception thrown when channel subscription errors occur in Realtime.
 * Covers JOIN handshake failures, timeouts, and server rejections.
 *
 * @author neilhdezs
 * @version 0.1.0
 * @since 0.1.0
 */
public class RealtimeSubscriptionException extends SupabaseException {

    /** - Constructs a RealtimeSubscriptionException with message and cause - **/
    public RealtimeSubscriptionException(String message, Throwable cause) {
        super(message, cause);
    }
}

