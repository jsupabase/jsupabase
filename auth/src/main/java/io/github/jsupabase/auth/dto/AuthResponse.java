package io.github.jsupabase.auth.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * A wrapper DTO. For signIn, this will be identical to Session.
 * For signUp (with email confirm on), it may only contain the User.
 *
 * @author neilhdezs
 * @version 0.1.1
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class AuthResponse extends Session {

    /**
     * Helper method for AuthClient to check if this response
     * actually contains a valid session.
     */
    public Session getSession() {
        // Si no hay token, no es una sesión válida
        if (this.getAccessToken() == null) {
            return null;
        }
        return this;
    }


}