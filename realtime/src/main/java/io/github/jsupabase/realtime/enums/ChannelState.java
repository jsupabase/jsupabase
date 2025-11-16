package io.github.jsupabase.realtime.enums;

/**
 * Represents the distinct states of a Realtime Channel.
 *
 * @author neilhdezs
 * @version 0.1.0
 */
public enum ChannelState {
    /** - The channel is closed and not attempting to join. - **/
    CLOSED,

    /** - The channel is attempting to join the topic (sending phx_join). - **/
    JOINING,

    /** - The channel has successfully joined the topic and is active. - **/
    JOINED,

    /** - The channel is leaving the topic (sending phx_leave). - **/
    LEAVING,

    /** - An error occurred, and the channel is in a failed state. - **/
    ERRORED
}