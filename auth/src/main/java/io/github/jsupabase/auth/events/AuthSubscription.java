package io.github.jsupabase.auth.events;

/**
 * Represents an active subscription to auth events.
 *
 * @author neilhdezs
 * @version 0.1.0
 * @since 0.1.0
 */
public class AuthSubscription {

    /** - The manager that holds this subscription - **/
    private final AuthEventManager eventManager;

    /** - The listener that this subscription wraps - **/
    private final AuthStateChangeListener listener;

    /**
     * Creates a new subscription object.
     * (Package-private, only the AuthEventManager should create this)
     *
     * @param eventManager The manager responsible for this subscription.
     * @param listener     The listener to be removed upon unsubscribing.
     */
    AuthSubscription(AuthEventManager eventManager, AuthStateChangeListener listener) {
        this.eventManager = eventManager;
        this.listener = listener;
    }

    /**
     * Unsubscribes this listener from all future auth events.
     */
    public void unsubscribe() {
        eventManager.removeListener(listener);
    }
}