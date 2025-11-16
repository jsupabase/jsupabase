package io.github.jsupabase.realtime.enums;

/**
 * - CHANNEL LIFECYCLE STATES -
 * <p>
 * Defines the possible states of a real-time channel subscription throughout its lifecycle.
 * These states follow the Phoenix Channels protocol state machine and ensure proper
 * sequencing of subscription operations.
 *
 * <h3>State Transition Diagram:</h3>
 * <pre>
 * CLOSED ──subscribe()──> JOINING ──server reply──> JOINED
 *   ↑                                                  │
 *   │                                                  │
 *   └────────────unsubscribe()────LEAVING <───────────┘
 *
 * ERRORED <── (can occur from JOINING if server rejects)
 * </pre>
 *
 * <h3>Valid Transitions:</h3>
 * <ul>
 * <li>CLOSED → JOINING (via subscribe())</li>
 * <li>JOINING → JOINED (server accepts join)</li>
 * <li>JOINING → ERRORED (server rejects join)</li>
 * <li>JOINED → LEAVING (via unsubscribe())</li>
 * <li>LEAVING → CLOSED (leave complete)</li>
 * <li>JOINED → JOINING (reconnection after network failure)</li>
 * </ul>
 *
 * @author neilhdezs
 * @version 0.1.0
 * @since 0.1.0
 */
public enum ChannelState {
    /** - Channel is not subscribed and not attempting to subscribe - Initial state and final state after unsubscribe - */
    CLOSED,

    /** - Channel is attempting to join by sending phx_join and awaiting server phx_reply - Transient state during subscription - */
    JOINING,

    /** - Channel successfully joined and is actively receiving events - Normal operational state where messages flow - */
    JOINED,

    /** - Channel is leaving by sending phx_leave - Transient state during unsubscription and cleanup - */
    LEAVING,

    /** - Channel join was rejected by server or encountered a protocol error - Terminal error state requiring resubscription - */
    ERRORED
}