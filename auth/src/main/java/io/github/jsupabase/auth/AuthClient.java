package io.github.jsupabase.auth;

import io.github.jsupabase.auth.dto.*;
import io.github.jsupabase.auth.enums.AuthChangeEvent;
import io.github.jsupabase.auth.enums.OAuthProvider;
import io.github.jsupabase.auth.events.AuthEventManager;
import io.github.jsupabase.auth.events.AuthStateChangeListener;
import io.github.jsupabase.auth.events.AuthSubscription;
import io.github.jsupabase.auth.utils.AuthConstants;
import io.github.jsupabase.core.client.HttpClientBase;
import io.github.jsupabase.core.config.SupabaseConfig;
import io.github.jsupabase.core.util.JsonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.http.HttpRequest;
import java.time.Duration;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

/**
 * - SUPABASE AUTHENTICATION CLIENT -
 * <p>
 * Specialized client for Supabase Authentication operations via the GoTrue API.
 * This stateful client manages user sessions, authentication events, and provides
 * comprehensive user management functionality including sign-up, sign-in, password
 * recovery, and OAuth integration.
 * <p>
 * The client maintains internal session state with thread-safe operations and
 * implements an event-driven architecture that notifies listeners of authentication
 * state changes. This enables automatic reconfiguration of other SDK modules when
 * users authenticate or sign out.
 * <p>
 * All network operations inherit from HttpClientBase but with custom header management
 * to handle authentication-specific requirements where Authorization headers are
 * managed explicitly rather than through base configuration.
 *
 * @author neilhdezs
 * @version 0.1.0
 * @since 0.1.0
 */
public class AuthClient extends HttpClientBase {

    /** - Logger for authentication operations and state changes - **/
    private static final Logger LOGGER = LoggerFactory.getLogger(AuthClient.class);

    /** - Authentication service endpoint path for GoTrue API operations - **/
    private final String servicePath;

    /** - Event management system for authentication state change notifications - */
    private final AuthEventManager eventManager;

    /** - Thread-safe container for current user session state - */
    private final AtomicReference<Session> currentSession = new AtomicReference<>();

    /**
     * - AUTHENTICATION CLIENT CONSTRUCTOR -
     * <p>
     * Initializes a new authentication client with the provided configuration
     * and sets up the event management system for authentication state changes.
     * The client inherits HTTP functionality from HttpClientBase while implementing
     * authentication-specific header management.
     *
     * @param config Supabase configuration containing authentication service settings
     */
    public AuthClient(SupabaseConfig config) {
        super(Objects.requireNonNull(config, "SupabaseConfig cannot be null"));
        this.servicePath = config.getAuthPath();
        this.eventManager = new AuthEventManager();
    }

    /**
     * - AUTHENTICATION REQUEST BUILDER -
     * <p>
     * Creates a pre-configured HTTP request builder for authentication operations.
     * This method overrides the base implementation to exclude automatic Authorization
     * headers, allowing the AuthClient to manage authentication tokens explicitly
     * based on the specific operation being performed.
     * <p>
     * Some operations like sign-in require no authentication token, while others
     * like user profile retrieval need the current session token to be added manually.
     *
     * @param path Relative endpoint path for the authentication operation
     * @return HTTP request builder configured for authentication service calls
     */
    @Override
    public HttpRequest.Builder newRequest(String path) {
        URI uri = config.resolveUrl(path);

        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(uri)
                .timeout(Duration.ofSeconds(15));

        for (Map.Entry<String, String> header : config.getHeaders().entrySet()) {
            if (!header.getKey().equalsIgnoreCase(AuthConstants.HDR_AUTHORIZATION)) {
                builder.header(header.getKey(), header.getValue());
            }
        }

        return builder;
    }

    /**
     * - AUTHENTICATION STATE CHANGE SUBSCRIPTION -
     * <p>
     * Registers a listener for authentication state change events including sign-in,
     * sign-out, token refresh, and session initialization. This enables reactive
     * programming patterns where other components can respond to authentication
     * changes automatically.
     *
     * @param listener Callback function to execute when authentication events occur
     * @return AuthSubscription instance for managing the event subscription lifecycle
     */
    public AuthSubscription onAuthStateChange(AuthStateChangeListener listener) {
        return eventManager.addListener(listener);
    }

    /**
     * - CURRENT SESSION ACCESS -
     * <p>
     * Returns the currently active user session containing access tokens, user
     * information, and session metadata. This method provides thread-safe access
     * to the current authentication state.
     *
     * @return Active Session object if user is authenticated, null otherwise
     */
    public Session getSession() {
        return currentSession.get();
    }

    /**
     * - SESSION STATE RESTORATION -
     * <p>
     * Manually sets an active user session for session persistence scenarios.
     * This method is essential for applications that need to restore authentication
     * state from external storage such as local storage, secure cookies, or cache
     * systems when the application restarts.
     * <p>
     * Upon setting the session, the client updates its internal state and triggers
     * an INITIAL_SESSION event, enabling all other SDK modules to automatically
     * reconfigure themselves with the restored authentication context.
     *
     * @param session Complete session object containing user data and authentication tokens
     */
    public void setSession(Session session) {
        this.currentSession.set(session);
        eventManager.notify(AuthChangeEvent.INITIAL_SESSION, session);
        LOGGER.info(AuthConstants.LOG_SESSION_SET);
    }

    // --- Stateful Auth Methods (Sign In / Sign Up) ---

    /**
     * Provides a convenience method to sign up a new user using only email and password.
     * <p>
     * Internally, this method calls the more detailed overload, passing {@code null}
     * for any additional sign-up options ({@code SignUpOptions}), centralizing the
     * HTTP request logic to the GoTrue API.
     *
     * @param email    The new user's email address.
     * @param password The new user's password.
     * @return A {@code CompletableFuture} containing the authentication response.
     */
    public CompletableFuture<AuthResponse> signUp(String email, String password) {
        return signUp(email, password, null);
    }

    /**
     * Signs up a new user on the Supabase platform via a POST request to the GoTrue API,
     * allowing the inclusion of additional options like user metadata.
     * <p>
     * This method serves as the core implementation of the sign-up logic. Its function is to:
     * Serialize the user credentials (email, password, and {@code SignUpOptions})
     * into a **JSON body**.
     * Construct and execute a **POST** request to the sign-up endpoint ({@code /signup})
     * within the base authentication path configured in the client ({@code servicePath}).
     * Upon a successful response, it uses {@code handleSignInResponse} to **process the
     * session** returned, **set it** as the client's internal active session, and
     * **emit the** {@code SIGNED_IN} **event** to SDK subscribers.
     * * **NOTE:** If the sign-up is successful and GoTrue does not require email verification,
     * the user is automatically **logged in** with the returned session.
     *
     * @param email    The new user's email address.
     * @param password The new user's password.
     * @param options  Additional sign-up options (e.g., user data, redirect URL).
     * @return A {@code CompletableFuture} containing the authentication response (AuthResponse) or an error.
     */
    public CompletableFuture<AuthResponse> signUp(String email, String password, SignUpOptions options) {
        try {
            LOGGER.debug(AuthConstants.LOG_SIGNUP_INITIATED, email);
            SignUpCredentials credentials = new SignUpCredentials(email, password, options);
            String jsonBody = JsonUtil.toJson(credentials);
            String path = AuthPaths.getSignupPath(this.servicePath);

            HttpRequest request = newRequest(path)
                    .header(AuthConstants.HDR_CONTENT_TYPE, AuthConstants.MIME_JSON)
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            return sendAsync(request, AuthResponse.class)
                    .thenApply(this::handleSignInResponse);

        } catch (Exception e) {
            LOGGER.error(AuthConstants.ERROR_SIGNUP_FAILED, e.getMessage());
            return CompletableFuture.failedFuture(e);
        }
    }

    /**
     * Signs in an existing user using their email and password credentials.
     * <p>
     * This method is the central entry point for password-based authentication. Its function is to:
     * Serialize the credentials (email and password) into a **JSON body**.
     * Construct and execute a **POST** request to the token endpoint ({@code /token?grant_type=password})
     * within the client's configured base authentication path ({@code servicePath}),
     * utilizing the password grant type.
     * Upon a successful response, it uses {@code handleSignInResponse} to **process the
     * returned session**, **set it** as the client's internal active session, and
     * **emit the** {@code SIGNED_IN} **event** to subscribed SDK clients.
     *
     * @param email    The user's email address.
     * @param password The user's password.
     * @return A {@code CompletableFuture} containing the authentication response (AuthResponse) or an error.
     */
    public CompletableFuture<AuthResponse> signInWithPassword(String email, String password) {
        try {
            LOGGER.debug("Attempting sign in with password for email: {}", email);
            EmailPasswordCredentials credentials = new EmailPasswordCredentials(email, password);
            String jsonBody = JsonUtil.toJson(credentials);
            String path = AuthPaths.getTokenPath(this.servicePath) + AuthConstants.QUERY_GRANT_PASSWORD;

            HttpRequest request = newRequest(path)
                    .header(AuthConstants.HDR_CONTENT_TYPE, AuthConstants.MIME_JSON)
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            return sendAsync(request, AuthResponse.class)
                    .thenApply(response -> {
                        LOGGER.info(AuthConstants.LOG_SIGNIN_SUCCESS, email);
                        return handleSignInResponse(response);
                    });

        } catch (Exception e) {
            LOGGER.error(AuthConstants.ERROR_SIGNIN_FAILED, e.getMessage());
            return CompletableFuture.failedFuture(e);
        }
    }

    /**
     * Signs in a user to Supabase using an ID Token issued by an external OIDC (OpenID Connect)
     * compliant provider, such as Google or Apple.
     * <p>
     * This method is essential for native Single Sign-On (SSO) flows. Its function is to:
     * Serialize the credentials containing the ID Token into a **JSON body**.
     * Construct and execute a **POST** request to the GoTrue endpoint {@code /sso/idtoken},
     * which validates the token and exchanges it for a Supabase session.
     * Upon a successful response, it uses {@code handleSignInResponse} to **process the
     * returned session**, **set it** as the client's internal active session, and
     * **emit the** {@code SIGNED_IN} **event** to subscribed SDK clients.
     *
     * @param credentials The credentials containing the OIDC ID token.
     * @return A {@code CompletableFuture} containing the authentication response (AuthResponse) or an error.
     */
    public CompletableFuture<AuthResponse> signInWithIdToken(IdTokenCredentials credentials) {
        try {
            String jsonBody = JsonUtil.toJson(credentials);
            String path = AuthPaths.getSsoIdTokenPath(this.servicePath);

            HttpRequest request = newRequest(path)
                    .header(AuthConstants.HDR_CONTENT_TYPE, AuthConstants.MIME_JSON)
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            return sendAsync(request, AuthResponse.class)
                    .thenApply(this::handleSignInResponse);
        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }

    /**
     * Provides a convenience method to sign in a user **anonymously**, requiring no credentials.
     * <p>
     * This method internally calls the more detailed overload, passing {@code null}
     * for the optional {@code AnonymousOptions} parameter. Its primary utility is to allow
     * developers to easily obtain a temporary session token from GoTrue so the user can
     * interact with public or anonymously configured resources (e.g., Postgrest or Storage).
     * <p>
     * * **RISKS AND CONFIGURATION:**
     * Anonymous authentication must be **enabled** in the Supabase GoTrue configuration.
     * It creates a **real, permanent user** in the Auth database, whose JWT includes the {@code is_anonymous: true} claim.
     * This allows the application of specific **RLS (Row Level Security) policies** for anonymous users.
     * Due to the risk of abuse (bots creating many users), it is highly recommended to configure and use **CAPTCHA** via the {@code AnonymousOptions} in the detailed overload.
     *
     * @return A {@code CompletableFuture} containing the authentication response (AuthResponse) or an error.
     */
    public CompletableFuture<AuthResponse> signInAnonymously() {
        return signInAnonymously(null);
    }

    /**
     * Signs in a user **anonymously**, allowing additional options to be passed,
     * such as user metadata or a CAPTCHA token to mitigate abuse.
     * <p>
     * This method is the core implementation of anonymous authentication. Its function is to:
     * Serialize the {@code AnonymousOptions} (or an empty body if null) into a **JSON body**.
     * Construct and execute a **POST** request to the GoTrue sign-up endpoint ({@code /signup?anon=true}).
     * The addition of {@code ?anon=true} is crucial to signal that an anonymous user should
     * be created and the corresponding JWT should be issued.
     * Upon a successful response, it uses {@code handleSignInResponse} to **set the
     * active session** of the new anonymous user and **emit the** {@code SIGNED_IN}
     * **event** to SDK subscribers.
     * <p>
     * * **CAPTCHA USAGE:** It is highly recommended to use the {@code AnonymousOptions}
     * to pass a CAPTCHA token to protect this endpoint against mass user creation by bots.
     *
     * @param options Additional options for anonymous sign-in (e.g., metadata, captcha token).
     * @return A {@code CompletableFuture} containing the authentication response (AuthResponse) or an error.
     */
    public CompletableFuture<AuthResponse> signInAnonymously(AnonymousOptions options) {
        try {
            LOGGER.debug("Initiating anonymous sign-in");
            String jsonBody = (options == null) ? "{}" : JsonUtil.toJson(options);
            String path = AuthPaths.getSignupPath(this.servicePath) + AuthConstants.QUERY_ANON_TRUE;

            HttpRequest request = newRequest(path)
                    .header(AuthConstants.HDR_CONTENT_TYPE, AuthConstants.MIME_JSON)
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            return sendAsync(request, AuthResponse.class)
                    .thenApply(this::handleSignInResponse);
        } catch (Exception e) {
            LOGGER.error(AuthConstants.ERROR_SIGNIN_FAILED, e.getMessage());
            return CompletableFuture.failedFuture(e);
        }
    }

    /**
     * Verifies a previously sent One-Time Password (OTP) challenge (via email or phone).
     * <p>
     * This method is essential for completing passwordless sign-in flows or credential verification. Its function is to:
     * Serialize the verification parameters (which include the email/phone, OTP token, and event type)
     * into a **JSON body**.
     * Construct and execute a **POST** request to the GoTrue {@code /verify} endpoint.
     * If verification is successful, GoTrue returns a valid session, resulting in:
     * The invocation of {@code handleSignInResponse}, **setting the user's session**,
     * and **emitting the** {@code SIGNED_IN} **event** to SDK subscribers.
     *
     * @param params The OTP verification parameters (email/phone, token, and event type).
     * @return A {@code CompletableFuture} containing the authentication response (AuthResponse) or an error.
     */
    public CompletableFuture<AuthResponse> verifyOtp(VerifyOtpParams params) {
        try {
            LOGGER.debug("Verifying OTP");
            String jsonBody = JsonUtil.toJson(params);
            String path = AuthPaths.getVerifyPath(this.servicePath);

            HttpRequest request = newRequest(path)
                    .header(AuthConstants.HDR_CONTENT_TYPE, AuthConstants.MIME_JSON)
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            return sendAsync(request, AuthResponse.class)
                    .thenApply(response -> {
                        LOGGER.info(AuthConstants.LOG_OTP_VERIFIED);
                        return handleSignInResponse(response);
                    });

        } catch (Exception e) {
            LOGGER.error("OTP verification failed: {}", e.getMessage());
            return CompletableFuture.failedFuture(e);
        }
    }

    /**
     * Completes the OAuth 2.0 authentication process with PKCE (Proof Key for Code Exchange)
     * by exchanging the received authorization code for an active user session.
     * <p>
     * This method is essential in the final step of authenticating with third-party providers
     * (like Google, GitHub) that use the PKCE flow. Its function is to:
     * Serialize the authorization code ({@code authCode}) and the code verifier
     * ({@code codeVerifier}) into a **JSON body**, along with the Supabase public key
     * (used as the {@code clientId}).
     * Construct and execute a **POST** request to the GoTrue token endpoint ({@code /token}).
     * Upon a successful response, GoTrue returns the JWT session. This session is processed via
     * {@code handleSignInResponse}, which **sets the session** as active and **emits the
     * ** {@code SIGNED_IN} **event** to SDK subscribers.
     *
     * @param authCode     The authorization code received after redirection from the OAuth provider.
     * @param codeVerifier The locally generated secret key used for interception attack prevention.
     * @return A {@code CompletableFuture} containing the authentication response (AuthResponse) or an error.
     */
    public CompletableFuture<AuthResponse> exchangeCodeForSession(String authCode, String codeVerifier) {
        try {
            String clientId = config.getSupabaseKey();
            ExchangeCodeCredentials credentials = new ExchangeCodeCredentials(clientId, authCode, codeVerifier);
            String jsonBody = JsonUtil.toJson(credentials);
            String path = AuthPaths.getTokenPath(this.servicePath);

            HttpRequest request = newRequest(path)
                    .header(AuthConstants.HDR_CONTENT_TYPE, AuthConstants.MIME_JSON)
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            return sendAsync(request, AuthResponse.class)
                    .thenApply(this::handleSignInResponse);

        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }

    /**
     * Refreshes an active user session using its refresh token.
     * <p>
     * This method is critical for keeping the user's session alive and secure without requiring
     * them to log in again. Its function is to:
     * Serialize the {@code refreshToken} into a **JSON body**.
     * Construct and execute a **POST** request to the GoTrue token endpoint ({@code /token?grant_type=refresh_token}),
     * specifying the refresh token grant type.
     * Upon receiving the new session (which includes a new JWT and a new Refresh Token), the handling logic
     * **updates** the internal session ({@code currentSession}) and **emits the**
     * {@code TOKEN_REFRESHED} **event** to subscribers. This event notifies the {@code SupabaseClient}
     * that it must reconfigure its Postgrest and Storage clients with the new JWT.
     *
     * @param refreshToken The refresh token of the current session.
     * @return A {@code CompletableFuture} containing the new authentication response (AuthResponse) or an error.
     */
    public CompletableFuture<AuthResponse> refreshSession(String refreshToken) {
        try {
            LOGGER.debug("Refreshing session with refresh token");
            RefreshCredentials credentials = new RefreshCredentials(refreshToken);
            String jsonBody = JsonUtil.toJson(credentials);
            String path = AuthPaths.getTokenPath(this.servicePath) + AuthConstants.QUERY_GRANT_REFRESH;

            HttpRequest request = newRequest(path)
                    .header(AuthConstants.HDR_CONTENT_TYPE, AuthConstants.MIME_JSON)
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            return sendAsync(request, AuthResponse.class)
                    .thenApply(authResponse -> {
                        this.currentSession.set(authResponse.getSession());
                        eventManager.notify(AuthChangeEvent.TOKEN_REFRESHED, authResponse.getSession());
                        LOGGER.info(AuthConstants.LOG_SESSION_REFRESHED, authResponse.getSession().getUser().getEmail());
                        return authResponse;
                    });

        } catch (Exception e) {
            LOGGER.error(AuthConstants.ERROR_REFRESH_FAILED, e.getMessage());
            return CompletableFuture.failedFuture(e);
        }
    }

    /**
     * Provides a convenience method to request a One-Time Password (OTP) to be sent to the specified email address.
     * <p>
     * This method is part of the passwordless authentication flow. It internally calls
     * the private {@code signInWithOtp} method, constructing the necessary credentials
     * with null options ({@code null}). The main utility is to start the process
     * for the user to receive the token, which must then be verified using the
     * {@code verifyOtp} method. This method **does not** sign the user in immediately.
     *
     * @param email The user's email address to send the OTP to.
     * @return An empty {@code CompletableFuture} indicating the success of the OTP send request.
     */
    public CompletableFuture<Void> signInWithOtpEmail(String email) {
        return signInWithOtp(OtpCredentials.forEmail(email, null));
    }

    /**
     * Requests a One-Time Password (OTP) to be sent to the specified email address,
     * allowing the configuration of additional options (e.g., user data, redirect URL).
     * <p>
     * This method is part of the passwordless authentication flow. Its function is to:
     * Build the necessary OTP credentials for the given email and options.
     * Internally call the private {@code signInWithOtp} method to perform the **POST**
     * request to the GoTrue API ({@code /otp}).
     * The primary utility is to initiate the process for the user to receive the token.
     * This method **does not** sign the user in immediately; the token must be verified
     * later using the {@code verifyOtp} method.
     *
     * @param email   The user's email address to send the OTP to.
     * @param options Additional OTP options (e.g., redirect URL, user data).
     * @return An empty {@code CompletableFuture} indicating the success of the OTP send request.
     */
    public CompletableFuture<Void> signInWithOtpEmail(String email, OtpOptions options) {
        return signInWithOtp(OtpCredentials.forEmail(email, options));
    }

    /**
     * Provides a convenience method to request a One-Time Password (OTP) to be sent to the specified phone number.
     * <p>
     * This method is part of the passwordless authentication flow. Its function is to:
     * Build the necessary OTP credentials for the phone number and null options ({@code null}).
     * Internally call the private {@code signInWithOtp} method to initiate the **POST**
     * request to the GoTrue API ({@code /otp}).
     * The primary utility is to start the process for the user to receive the token.
     * This method **does not** sign the user in immediately; the token must be verified
     * later using the {@code verifyOtp} method.
     *
     * @param phone The user's phone number to send the OTP to.
     * @return An empty {@code CompletableFuture} indicating the success of the OTP send request.
     */
    public CompletableFuture<Void> signInWithOtpPhone(String phone) {
        return signInWithOtp(OtpCredentials.forPhone(phone, null));
    }

    /**
     * Requests a One-Time Password (OTP) to be sent to the specified phone number,
     * allowing the configuration of additional options.
     * <p>
     * This method is part of the passwordless authentication flow. Its function is to:
     * Build the necessary OTP credentials for the phone number and the provided options.
     * Internally call the private {@code signInWithOtp} method to initiate the **POST**
     * request to the GoTrue API ({@code /otp}).
     * The primary utility is to start the process for the user to receive the token via
     * a text message.
     * This method **does not** sign the user in immediately; the token must be **verified later**
     * using the {@code verifyOtp} method to complete the authentication process.
     *
     * @param phone   The user's phone number to send the OTP to.
     * @param options Additional OTP options (e.g., redirect URL, user data).
     * @return An empty {@code CompletableFuture} indicating the success of the OTP send request.
     */
    public CompletableFuture<Void> signInWithOtpPhone(String phone, OtpOptions options) {
        return signInWithOtp(OtpCredentials.forPhone(phone, options));
    }

    /**
     * Sends a One-Time Password (OTP) request to the GoTrue API to initiate the
     * passwordless authentication flow.
     * <p>
     * This private method is the **core logic** called by the public overloads
     * (such as {@code signInWithOtpEmail} or {@code signInWithOtpPhone}). Its function is to:
     * Serialize the OTP credentials (containing the email or phone number and options)
     * into a **JSON body**.
     * Construct and execute a **POST** request to the GoTrue {@code /otp} endpoint.
     * The expected response is empty ({@code Void}) as it only confirms that the server
     * has initiated the code sending process; the actual login occurs only after a
     * successful verification using {@code verifyOtp}.
     *
     * @param credentials The credentials specifying the method and details for the OTP send request.
     * @return A {@code CompletableFuture<Void>} indicating the send request was successfully processed.
     */
    private CompletableFuture<Void> signInWithOtp(OtpCredentials credentials) {
        try {
            String jsonBody = JsonUtil.toJson(credentials);
            String path = AuthPaths.getOtpPath(this.servicePath);

            HttpRequest request = newRequest(path)
                    .header(AuthConstants.HDR_CONTENT_TYPE, AuthConstants.MIME_JSON)
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            return sendAsync(request, Void.class);
        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }

    /**
     * Provides a convenience method to start the authentication flow with a third-party
     * OAuth provider without any additional options.
     * <p>
     * This method is part of the social authentication flow. Its function is to internally
     * call the detailed {@code signInWithOAuth} overload, passing {@code null} for the
     * options. The return value is an {@code OAuthResponse} containing the **redirect URL**
     * to which the application must direct the user to begin the authorization process on
     * the external provider's website (e.g., Google, GitHub).
     *
     * @param provider The third-party OAuth provider (e.g., Google, Facebook).
     * @return A {@code CompletableFuture} containing the response with the redirect URL.
     */
    public CompletableFuture<OAuthResponse> signInWithOAuth(OAuthProvider provider) {
        return signInWithOAuth(new OAuthCredentials(provider, null));
    }

    /**
     * Initiates the authentication flow with a third-party OAuth provider, allowing
     * the configuration of additional options such as scope or redirect URL.
     * <p>
     * This method is part of the social authentication flow. Its function is to build
     * the necessary OAuth credentials with the provided options and call the private
     * {@code signInWithOAuth} method. The return value is an {@code OAuthResponse}
     * containing the **redirect URL** to which the application must direct the user
     * to begin the authorization process.
     *
     * @param provider The third-party OAuth provider (e.g., Google, Facebook).
     * @param options  Additional OAuth configuration options (e.g., specific redirect URL, scopes).
     * @return A {@code CompletableFuture} containing the response with the redirect URL.
     */
    public CompletableFuture<OAuthResponse> signInWithOAuth(OAuthProvider provider, OAuthOptions options) {
        return signInWithOAuth(new OAuthCredentials(provider, options));
    }

    /**
     * Sends the request to the GoTrue API to initiate the third-party OAuth authentication flow.
     * <p>
     * This private method is the **core logic** called by the public overloads
     * (such as {@code signInWithOAuth}). Its function is to:
     * Serialize the OAuth credentials (containing the provider and options) into a **JSON body**.
     * Construct and execute a **POST** request to the GoTrue {@code /oauth} endpoint.
     * The GoTrue API **does not** sign the user in immediately; instead, it responds with the
     * {@code OAuthResponse} containing the necessary **redirect URL**. The client application
     * must take this URL and redirect the user to complete the sign-in process on the
     * external provider's website.
     *
     * @param credentials The credentials specifying the OAuth provider and flow options.
     * @return A {@code CompletableFuture} containing the {@code OAuthResponse} with the redirect URL.
     */
    private CompletableFuture<OAuthResponse> signInWithOAuth(OAuthCredentials credentials) {
        try {
            String jsonBody = JsonUtil.toJson(credentials);
            String path = AuthPaths.getOAuthPath(this.servicePath);

            HttpRequest request = newRequest(path)
                    .header(AuthConstants.HDR_CONTENT_TYPE, AuthConstants.MIME_JSON)
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            return sendAsync(request, OAuthResponse.class);
        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }

    // --- Stateful Overloads (Methods using stored session) ---

    /**
     * Signs out the currently authenticated user both on the client and the GoTrue server.
     * <p>
     * This method is crucial for security and session state management. Its function is to:
     * Check if an active session ({@code currentSession}) with an access token exists. If not, it returns a completed future.
     * If the session is valid, it calls the delegated method {@code signOut(String jwt)} to send a **POST**
     * request to the GoTrue {@code /logout} endpoint using the user's JWT, effectively **revoking** the refresh token on the server.
     * Following successful server sign-out, it **invalidates the client's internal session**
     * (sets {@code currentSession} to {@code null}) and **emits the** {@code SIGNED_OUT}
     * **event** to all subscribers. This event is vital for other SDK clients
     * (Postgrest, Storage) to remove their authorization tokens.
     *
     * @return A {@code CompletableFuture<Void>} indicating that the sign-out process is complete.
     */
    public CompletableFuture<Void> signOut() {
        try {
            Session session = currentSession.get();
            if (session == null || session.getAccessToken() == null) {
                LOGGER.debug("No active session to sign out");
                return CompletableFuture.completedFuture(null);
            }
            return signOut(session.getAccessToken())
                    .thenApply(voidResult -> {
                        this.currentSession.set(null);
                        eventManager.notify(AuthChangeEvent.SIGNED_OUT, null);
                        LOGGER.info(AuthConstants.LOG_SIGNOUT);
                        return voidResult;
                    });
        } catch (Exception e) {
            LOGGER.error(AuthConstants.ERROR_SIGNOUT_FAILED, e.getMessage());
            return CompletableFuture.failedFuture(e);
        }
    }

    /**
     * Fetches the profile of the currently authenticated user using the access token
     * from the session internally stored in the client ({@code currentSession}).
     * <p>
     * This method is a **stateful overload**. Its function is to:
     * Check if an active session and a valid access token exist within the client.
     * If the session is valid, it calls the delegated method {@code getUser(String jwt)} to
     * perform a **GET** request to the GoTrue {@code /user} endpoint, including the JWT
     * in the authorization header.
     * If no active session exists, it fails with an {@code IllegalStateException}.
     * This method is read-only and does not affect the session state, only queries it.
     *
     * @return A {@code CompletableFuture} containing the {@code User} object if the session is valid.
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
     * Updates the attributes (e.g., email, password, metadata) for the currently
     * authenticated user on the GoTrue server.
     * <p>
     * This method is a **stateful overload** that relies on the internal active session.
     * Its function is to:
     * Check if an active session ({@code currentSession}) with an access token exists. If not, it throws an exception.
     * If valid, it calls the delegated method {@code updateUser(String jwt, UserAttributes)} to
     * perform a **PUT** request to the GoTrue {@code /user} endpoint, sending the new attributes and the JWT.
     * Upon successful update, it **updates the client's internal session** ({@code currentSession})
     * with the modified user details and **emits the** {@code USER_UPDATED} **event** to subscribers.
     * This event is crucial for the application to react to user state changes (e.g., email change).
     *
     * @param attributes The user attributes to be updated (e.g., {@code email}, {@code password}, {@code user_metadata}).
     * @return A {@code CompletableFuture} containing the updated {@code User} object.
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
     * Fetches a user profile by querying the GoTrue API using a specific JWT.
     * <p>
     * This is a **stateless method** and is the core implementation of the user retrieval
     * logic. Its function is to:
     * Execute a **GET** request to the GoTrue {@code /user} endpoint.
     * Include the provided JWT in the {@code Authorization: Bearer ...} header.
     * GoTrue uses the JWT to identify and return the corresponding {@code User} object.
     * This method neither requires nor affects the client's internally stored session ({@code currentSession}),
     * thus it can be used to query users with any valid JWT.
     *
     * @param jwt The JSON Web Token (JWT) of the user to fetch.
     * @return A {@code CompletableFuture} containing the {@code User} object returned by the server.
     */
    public CompletableFuture<User> getUser(String jwt) {
        try {
            String path = AuthPaths.getUserPath(this.servicePath);

            HttpRequest request = newRequest(path)
                    .header(AuthConstants.HDR_AUTHORIZATION, "Bearer " + jwt)
                    .GET()
                    .build();

            return sendAsync(request, User.class);
        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }

    /**
     * Updates an existing user's attributes (e.g., email, password, metadata) by querying
     * the GoTrue API using a specific JWT.
     * <p>
     * This is a **stateless method** and is the core implementation of the user update
     * logic. Its function is to:
     * Serialize the user attributes ({@code UserAttributes}) into a **JSON body**.
     * Execute a **PUT** request to the GoTrue {@code /user} endpoint.
     * Include the provided JWT in the {@code Authorization: Bearer ...} header to
     * identify the user being modified.
     * GoTrue applies the modifications and returns the updated {@code User} object.
     * This method neither requires nor affects the client's internally stored session
     * ({@code currentSession}), making it suitable for user management by administrators
     * or services with appropriate access tokens.
     *
     * @param jwt        The JSON Web Token (JWT) of the user to be modified.
     * @param attributes The user attributes to update (e.g., {@code email}, {@code password}, {@code user_metadata}).
     * @return A {@code CompletableFuture} containing the updated {@code User} object.
     */
    public CompletableFuture<User> updateUser(String jwt, UserAttributes attributes) {
        try {
            String jsonBody = JsonUtil.toJson(attributes);
            String path = AuthPaths.getUserPath(this.servicePath);

            HttpRequest request = newRequest(path)
                    .header(AuthConstants.HDR_AUTHORIZATION, "Bearer " + jwt)
                    .header(AuthConstants.HDR_CONTENT_TYPE, AuthConstants.MIME_JSON)
                    .PUT(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            return sendAsync(request, User.class);
        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }

    /**
     * Signs out a user remotely on the GoTrue server by using a specific JWT to revoke the session.
     * <p>
     * This is a **stateless method** and the core implementation of the sign-out logic.
     * Its function is to:
     * Execute a **POST** request to the GoTrue {@code /logout} endpoint.
     * Include the provided JWT in the {@code Authorization: Bearer ...} header
     * to identify the session to terminate.
     * The GoTrue server **revokes the associated Refresh Token**, effectively invalidating
     * the session for future refreshes.
     * This method neither requires nor affects the client's internally stored session ({@code currentSession});
     * it is the responsibility of the calling method ({@code signOut()}) to manage the local state.
     *
     * @param jwt The JSON Web Token (JWT) of the session to sign out.
     * @return A {@code CompletableFuture<Void>} indicating that the session revocation on the server was successful.
     */
    public CompletableFuture<Void> signOut(String jwt) {
        try {
            String path = AuthPaths.getLogoutPath(this.servicePath);

            HttpRequest request = newRequest(path)
                    .header(AuthConstants.HDR_AUTHORIZATION, "Bearer " + jwt)
                    .POST(HttpRequest.BodyPublishers.noBody())
                    .build();
            return sendAsync(request, Void.class);
        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }

    /**
     * Sends a password reset email to the user at the specified email address.
     * <p>
     * This method initiates the password reset flow. Its function is to:
     * Serialize the user's email address into a **JSON body**.
     * Construct and execute a **POST** request to the GoTrue {@code /recover} endpoint.
     * If the user exists and the email is valid, GoTrue sends a link or token to the user's email.
     * The expected response is empty ({@code Void}), as it only confirms that the reset email
     * request was processed by the server. The user must follow the link in the email to
     * complete the password reset process.
     *
     * @param email The email address of the user who needs to reset their password.
     * @return A {@code CompletableFuture<Void>} indicating that the email sending request was successfully processed.
     */
    public CompletableFuture<Void> resetPasswordForEmail(String email) {
        try {
            ResetPasswordCredentials credentials = new ResetPasswordCredentials(email);
            String jsonBody = JsonUtil.toJson(credentials);
            String path = AuthPaths.getRecoverPath(this.servicePath);

            HttpRequest request = newRequest(path)
                    .header(AuthConstants.HDR_CONTENT_TYPE, AuthConstants.MIME_JSON)
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            return sendAsync(request, Void.class);
        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }

    // --- Private Helpers ---

    /**
     * Centralized handler for all successful sign-in responses from GoTrue.
     * <p>
     * This private method is vital because it centralizes the client's **state management**
     * after any operation that results in a new session (e.g., {@code signInWithPassword},
     * {@code signUp}, {@code verifyOtp}). Its function is to:
     * 1. **Set the Session:** Stores the new session (JWT, Refresh Token, user data)
     * received from GoTrue in the client's internal {@code currentSession} variable.
     * 2. **Notify Listeners:** Emits the {@code SIGNED_IN} event via the {@code eventManager}.
     * This notification is crucial so that other SDK components (like the Postgrest and
     * Storage clients) can reconfigure themselves with the new JWT and begin making
     * authenticated requests.
     *
     * @param authResponse The successful authentication response containing the {@code Session} object.
     * @return The same authentication response.
     */
    private AuthResponse handleSignInResponse(AuthResponse authResponse) {
        Session session = (authResponse != null) ? authResponse.getSession() : null;

        if (session != null) {
            this.currentSession.set(session);
            eventManager.notify(AuthChangeEvent.SIGNED_IN, session);
        }
        return authResponse;
    }
}
