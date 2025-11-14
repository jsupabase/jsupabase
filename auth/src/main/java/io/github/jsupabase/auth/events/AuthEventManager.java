package io.github.jsupabase.auth.events;

import io.github.jsupabase.auth.dto.Session;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Manages all active auth state change listeners and notifies them.
 * This class is thread-safe.
 *
 * @author neilhdezs
 * @version 0.1.0
 */
public class AuthEventManager {

    /**
     * - A thread-safe set to store all active listeners.
     * - We use CopyOnWriteArraySet because reads (notifications)
     * - are far more frequent than writes (add/remove listener).
     */
    private final Set<AuthStateChangeListener> listeners = new CopyOnWriteArraySet<>();

    /**
     * Adds a new listener and returns its subscription.
     *
     * @param listener The listener to add.
     * @return An AuthSubscription object to manage the subscription.
     */
    public AuthSubscription addListener(AuthStateChangeListener listener) {
        listeners.add(listener);
        return new AuthSubscription(this, listener);
    }

    /**
     * Removes a listener.
     * (Called by AuthSubscription.unsubscribe())
     *
     * @param listener The listener to remove.
     */
    void removeListener(AuthStateChangeListener listener) {
        listeners.remove(listener);
    }

    /**
     * Notifies all registered listeners of an auth event.
     * (This will be called by AuthClient)
     *
     * @param event The event that occurred.
     * @param session The current session (or null).
     */
    public void notify(AuthChangeEvent event, Session session) {
        // This iteration is thread-safe due to CopyOnWriteArraySet
        for (AuthStateChangeListener listener : listeners) {
            try {
                listener.onAuthStateChange(event, session);
            } catch (Exception e) {
                // Log the error but do not stop notifying other listeners
                System.err.println("Error in AuthStateChangeListener: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
}