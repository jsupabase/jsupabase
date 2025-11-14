package io.github.jsupabase.auth.events;

import io.github.jsupabase.auth.dto.Session;

/**
 * A functional interface (callback) for listening to auth state changes.
 *
 * @author neilhdezs
 * @version 0.1.0
 */
@FunctionalInterface
public interface AuthStateChangeListener {

    /**
     * This method is called when an authentication event occurs.
     *
     * @param event The type of event that occurred (e.g., SIGNED_IN).
     * @param session The current session (can be null if signed out).
     */
    void onAuthStateChange(AuthChangeEvent event, Session session);
}