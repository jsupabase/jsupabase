package io.github.jsupabase.auth;

import io.github.jsupabase.auth.dto.*;
import io.github.jsupabase.auth.enums.OAuthProvider;
import io.github.jsupabase.auth.events.AuthChangeEvent;
import io.github.jsupabase.auth.events.AuthEventManager;
import io.github.jsupabase.auth.events.AuthStateChangeListener;
import io.github.jsupabase.auth.events.AuthSubscription;
import io.github.jsupabase.core.client.HttpClientBase;
import io.github.jsupabase.core.config.SupabaseConfig;
import io.github.jsupabase.core.util.JsonUtil;

import java.net.http.HttpRequest;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Main client for Supabase Auth (GoTrue).
 * Inherits all network logic from HttpClientBase.
 * This client is stateful and manages the current user session.
 *
 * @author neilhdezs
 * @version 0.1.5 // Versión final
 */
public class AuthClient extends HttpClientBase {

    /** - Manages all event listeners - **/
    private final AuthEventManager eventManager;

    /** - Stores the current session state in a thread-safe way - **/
    private final AtomicReference<Session> currentSession = new AtomicReference<>();

    /**
     * Creates a new AuthClient.
     *
     * @param config The client configuration.
     */
    public AuthClient(SupabaseConfig config) {
        super(Objects.requireNonNull(config, "SupabaseConfig cannot be null"));
        this.eventManager = new AuthEventManager();
    }

    // --- Event & State Management ---

    /**
     * Subscribes to authentication state changes.
     *
     * @param listener The callback to execute when an event occurs.
     * @return An AuthSubscription object to manage the subscription.
     */
    public AuthSubscription onAuthStateChange(AuthStateChangeListener listener) {
        return eventManager.addListener(listener);
    }

    /**
     * Gets the current session from the client's state.
     *
     * @return The current Session, or null if not authenticated.
     */
    public Session getSession() {
        return currentSession.get();
    }

    /**
     * Manually sets the current session.
     * (Useful for restoring a session from storage).
     *
     * @param session The session to set as active.
     */
    public void setSession(Session session) {
        this.currentSession.set(session);
        eventManager.notify(AuthChangeEvent.INITIAL_SESSION, session);
    }

    // --- Stateful Auth Methods (Sign In / Sign Up) ---

    /**
     * Signs up a new user using email and password.
     */
    public CompletableFuture<AuthResponse> signUp(String email, String password) {
        return signUp(email, password, null);
    }

    /**
     * Signs up a new user with additional options (like metadata).
     * Corresponds to: POST /auth/v1/signup
     */
    public CompletableFuture<AuthResponse> signUp(String email, String password, SignUpOptions options) {
        try {
            SignUpCredentials credentials = new SignUpCredentials(email, password, options);
            String jsonBody = JsonUtil.toJson(credentials);
            String path = "/auth/v1/signup";

            HttpRequest request = newRequest(path)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            return sendAsync(request, AuthResponse.class)
                    .thenApply(this::handleSignInResponse);

        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }

    /**
     * Signs in an existing user using email and password.
     * Notifies listeners with SIGNED_IN.
     */
    public CompletableFuture<AuthResponse> signInWithPassword(String email, String password) {
        try {
            EmailPasswordCredentials credentials = new EmailPasswordCredentials(email, password);
            String jsonBody = JsonUtil.toJson(credentials);
            String path = "/auth/v1/token?grant_type=password";

            HttpRequest request = newRequest(path)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            return sendAsync(request, AuthResponse.class)
                    .thenApply(this::handleSignInResponse);

        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }

    /**
     * Signs in a user using a native OIDC ID token (e.g., from Google or Apple).
     * Notifies listeners with SIGNED_IN.
     */
    public CompletableFuture<AuthResponse> signInWithIdToken(IdTokenCredentials credentials) {
        try {
            String jsonBody = JsonUtil.toJson(credentials);
            String path = "/auth/v1/sso/idtoken";

            HttpRequest request = newRequest(path)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            return sendAsync(request, AuthResponse.class)
                    .thenApply(this::handleSignInResponse);
        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }

    /**
     * Signs in a user anonymously.
     * Notifies listeners with SIGNED_IN.
     */
    public CompletableFuture<AuthResponse> signInAnonymously() {
        return signInAnonymously(null);
    }

    /**
     * Signs in a user anonymously with additional options (metadata, captcha).
     * Notifies listeners with SIGNED_IN.
     */
    public CompletableFuture<AuthResponse> signInAnonymously(AnonymousOptions options) {
        try {
            String jsonBody = (options == null) ? "{}" : JsonUtil.toJson(options);
            // The 'anon=true' query param tells /signup to create an anonymous user
            String path = "/auth/v1/signup?anon=true";

            HttpRequest request = newRequest(path)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            return sendAsync(request, AuthResponse.class)
                    .thenApply(this::handleSignInResponse);
        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }

    /**
     * Verifies a one-time password (OTP) challenge.
     * This method signs the user in and notifies listeners with SIGNED_IN.
     */
    public CompletableFuture<AuthResponse> verifyOtp(VerifyOtpParams params) {
        try {
            String jsonBody = JsonUtil.toJson(params);
            String path = "/auth/v1/verify";

            HttpRequest request = newRequest(path)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            return sendAsync(request, AuthResponse.class)
                    .thenApply(this::handleSignInResponse);

        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }

    /**
     * Exchanges an authorization code (from OAuth PKCE flow) for a session.
     * Notifies listeners with SIGNED_IN.
     */
    public CompletableFuture<AuthResponse> exchangeCodeForSession(String authCode, String codeVerifier) {
        try {
            // The anon key is required as 'client_id' in the body
            String clientId = config.getSupabaseKey();
            ExchangeCodeCredentials credentials = new ExchangeCodeCredentials(clientId, authCode, codeVerifier);
            String jsonBody = JsonUtil.toJson(credentials);
            String path = "/auth/v1/token"; // Note: No grant_type in query param

            HttpRequest request = newRequest(path)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            return sendAsync(request, AuthResponse.class)
                    .thenApply(this::handleSignInResponse);

        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }

    /**
     * Refreshes a session using a refresh token.
     * Notifies listeners with TOKEN_REFRESHED.
     */
    public CompletableFuture<AuthResponse> refreshSession(String refreshToken) {
        try {
            RefreshCredentials credentials = new RefreshCredentials(refreshToken);
            String jsonBody = JsonUtil.toJson(credentials);
            String path = "/auth/v1/token?grant_type=refresh_token";

            HttpRequest request = newRequest(path)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            return sendAsync(request, AuthResponse.class)
                    .thenApply(authResponse -> {
                        this.currentSession.set(authResponse.getSession());
                        eventManager.notify(AuthChangeEvent.TOKEN_REFRESHED, authResponse.getSession());
                        return authResponse;
                    });

        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }

    /**
     * Signs in a user using a one-time password (OTP) sent to their email.
     */
    public CompletableFuture<Void> signInWithOtpEmail(String email) {
        return signInWithOtp(OtpCredentials.forEmail(email, null));
    }
    public CompletableFuture<Void> signInWithOtpEmail(String email, OtpOptions options) {
        return signInWithOtp(OtpCredentials.forEmail(email, options));
    }
    public CompletableFuture<Void> signInWithOtpPhone(String phone) {
        return signInWithOtp(OtpCredentials.forPhone(phone, null));
    }
    public CompletableFuture<Void> signInWithOtpPhone(String phone, OtpOptions options) {
        return signInWithOtp(OtpCredentials.forPhone(phone, options));
    }

    /**
     * Private helper for 'signInWithOtp'.
     * Corresponds to: POST /auth/v1/otp
     */
    private CompletableFuture<Void> signInWithOtp(OtpCredentials credentials) {
        try {
            String jsonBody = JsonUtil.toJson(credentials);
            String path = "/auth/v1/otp";
            HttpRequest request = newRequest(path)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();
            return sendAsync(request, Void.class);
        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }

    /**
     * Signs in a user using a third-party OAuth provider.
     * This method returns a URL to which the user must be redirected.
     */
    public CompletableFuture<OAuthResponse> signInWithOAuth(OAuthProvider provider) {
        return signInWithOAuth(new OAuthCredentials(provider, null));
    }
    public CompletableFuture<OAuthResponse> signInWithOAuth(OAuthProvider provider, OAuthOptions options) {
        return signInWithOAuth(new OAuthCredentials(provider, options));
    }
    private CompletableFuture<OAuthResponse> signInWithOAuth(OAuthCredentials credentials) {
        try {
            String jsonBody = JsonUtil.toJson(credentials);
            String path = "/auth/v1/oauth";
            HttpRequest request = newRequest(path)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();
            return sendAsync(request, OAuthResponse.class);
        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }

    // --- Stateful Overloads (Methods using stored session) ---

    /**
     * Signs out the currently authenticated user.
     * Notifies listeners with SIGNED_OUT.
     */
    public CompletableFuture<Void> signOut() {
        try {
            Session session = currentSession.get();
            if (session == null || session.getAccessToken() == null) {
                return CompletableFuture.completedFuture(null);
            }
            return signOut(session.getAccessToken())
                    .thenApply(voidResult -> {
                        this.currentSession.set(null);
                        eventManager.notify(AuthChangeEvent.SIGNED_OUT, null);
                        return voidResult;
                    });
        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }

    /**
     * Fetches the profile of the currently authenticated user.
     * (Uses the stored session's JWT).
     */
    public CompletableFuture<User> getUser() {
        try {
            Session session = currentSession.get();
            if (session == null || session.getAccessToken() == null) {
                return CompletableFuture.failedFuture(new IllegalStateException("No active session"));
            }
            return getUser(session.getAccessToken());
        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }

    /**
     * Updates attributes for the currently authenticated user.
     * Notifies listeners with USER_UPDATED.
     */
    public CompletableFuture<User> updateUser(UserAttributes attributes) {
        try {
            Session session = currentSession.get();
            if (session == null || session.getAccessToken() == null) {
                return CompletableFuture.failedFuture(new IllegalStateException("No active session"));
            }
            return updateUser(session.getAccessToken(), attributes)
                    .thenApply(updatedUser -> {
                        session.setUser(updatedUser);
                        this.currentSession.set(session);
                        eventManager.notify(AuthChangeEvent.USER_UPDATED, session);
                        return updatedUser;
                    });
        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }

    // --- Stateless Methods (Do not affect client state) ---

    /**
     * Fetches a user profile using a specific JWT.
     * (Stateless method, does not affect the client's session).
     */
    public CompletableFuture<User> getUser(String jwt) {
        try {
            String path = "/auth/v1/user";
            HttpRequest request = newRequest(path)
                    .header("Authorization", "Bearer " + jwt)
                    .GET()
                    .build();
            return sendAsync(request, User.class);
        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }

    /**
     * Updates a user's attributes using a specific JWT.
     * (Stateless method, does not affect the client's session).
     */
    public CompletableFuture<User> updateUser(String jwt, UserAttributes attributes) {
        try {
            String jsonBody = JsonUtil.toJson(attributes);
            String path = "/auth/v1/user";
            HttpRequest request = newRequest(path)
                    .header("Authorization", "Bearer " + jwt)
                    .header("Content-Type", "application/json")
                    .PUT(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();
            return sendAsync(request, User.class);
        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }

    /**
     * Signs out a user using a specific JWT.
     * (Stateless method, does not affect the client's session).
     */
    public CompletableFuture<Void> signOut(String jwt) {
        try {
            String path = "/auth/v1/logout";
            HttpRequest request = newRequest(path)
                    .header("Authorization", "Bearer " + jwt)
                    .POST(HttpRequest.BodyPublishers.noBody())
                    .build();
            return sendAsync(request, Void.class);
        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }

    /**
     * Sends a password reset email to a user.
     * Corresponds to: POST /auth/v1/recover
     */
    public CompletableFuture<Void> resetPasswordForEmail(String email) {
        try {
            ResetPasswordCredentials credentials = new ResetPasswordCredentials(email);
            String jsonBody = JsonUtil.toJson(credentials);
            String path = "/auth/v1/recover";
            HttpRequest request = newRequest(path)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();
            return sendAsync(request, Void.class);
        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }

    // --- Private Helpers ---

    /**
     * Centralized handler for all successful sign-in responses.
     * Sets the current session and notifies SIGNED_IN listeners.
     */
    private AuthResponse handleSignInResponse(AuthResponse authResponse) {
        if (authResponse != null && authResponse.getSession() != null) {
            this.currentSession.set(authResponse.getSession());
            eventManager.notify(AuthChangeEvent.SIGNED_IN, authResponse.getSession());
        }
        return authResponse;
    }
}