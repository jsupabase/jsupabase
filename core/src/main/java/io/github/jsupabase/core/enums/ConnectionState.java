package io.github.jsupabase.core.enums;

/**
 * Represents the distinct lifecycle states of a WebSocket connection.
 *
 * @author neilhdezs
 * @version 0.1.0
 */
public enum ConnectionState {
    /** - The client is actively trying to establish a connection. - **/
    CONNECTING,

    /** - The connection is established and active. - **/
    OPEN,

    /** - The client is actively closing the connection (e.g., user requested). - **/
    CLOSING,

    /** - The connection is not active, (e.g., initial state or after a disconnect). - **/
    CLOSED
}