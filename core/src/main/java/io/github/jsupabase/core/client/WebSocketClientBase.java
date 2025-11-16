package io.github.jsupabase.core.client;

import io.github.jsupabase.core.config.SupabaseConfig;
import io.github.jsupabase.core.enums.ConnectionState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Base abstract client for managing a persistent WebSocket connection.
 * It handles the connection lifecycle, auto-reconnection with exponential
 * backoff, and scheduled heartbeats.
 * <p>
 * Subclasses must implement the protocol-specific logic such as
 * message processing and heartbeat content.
 *
 * @author neilhdezs
 * @version 0.1.0
 * @since 0.1.0
 */
public abstract class WebSocketClientBase {

    /** - Logger for WebSocket operations - **/
    private static final Logger LOGGER = LoggerFactory.getLogger(WebSocketClientBase.class);

    /** - Immutable configuration for the client - **/
    protected final SupabaseConfig config;

    /** - The shared HttpClient used to build the WebSocket - **/
    private final HttpClient httpClient;

    /** - Thread-safe reference to the active WebSocket - **/
    private final AtomicReference<WebSocket> webSocket = new AtomicReference<>();

    /** - Single-thread scheduler for heartbeats and reconnections - **/
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(
            (r) -> new Thread(r, "jsupabase-websocket-scheduler") // Named thread for debugging
    );

    /** - Thread-safe reference to the current connection state - **/
    private final AtomicReference<ConnectionState> state = new AtomicReference<>(ConnectionState.CLOSED);

    /** - A reference to the scheduled heartbeat task - **/
    private ScheduledFuture<?> heartbeatTask;

    /** - Counter for reconnection attempts, used for backoff - **/
    private int reconnectAttempts = 0;

    /**
     * Constructs the WebSocket base client.
     *
     * @param config     The client's Supabase configuration.
     * @param httpClient The shared, global HttpClient instance.
     */
    protected WebSocketClientBase(SupabaseConfig config, HttpClient httpClient) {
        this.config = config;
        this.httpClient = httpClient;
    }

    /**
     * Initiates a connection to the WebSocket endpoint.
     * If already connected or connecting, this method does nothing.
     */
    public void connect() {
        if (state.get() != ConnectionState.CLOSED) {
            return;
        }

        LOGGER.debug("WebSocket: Connecting ...");
        setState(ConnectionState.CONNECTING);
        URI uri = buildConnectionUri();

        httpClient.newWebSocketBuilder()
                .buildAsync(uri, new WsListener())
                .thenAccept(ws -> {
                    this.webSocket.set(ws);
                    this.reconnectAttempts = 0; // Reset on successful connection
                    setState(ConnectionState.OPEN);
                    startHeartbeat();
                })
                .exceptionally(ex -> {
                    LOGGER.error("WebSocket: Connection failed: {}", ex.getMessage());
                    scheduleReconnect(); // Schedule reconnect on failure
                    return null;
                });
    }

    /**
     * Closes the WebSocket connection gracefully.
     * This will not trigger an automatic reconnect.
     */
    public void disconnect() {
        setState(ConnectionState.CLOSING);
        stopHeartbeat();
        WebSocket ws = webSocket.getAndSet(null);
        if (ws != null) {
            ws.sendClose(WebSocket.NORMAL_CLOSURE, "User disconnected").join();
        }
        setState(ConnectionState.CLOSED);
    }

    /**
     * Sends a text message (e.g., JSON) over the WebSocket.
     * The message will only be sent if the connection is OPEN.
     *
     * @param message The text message to send.
     */
    protected void sendText(String message) {
        WebSocket ws = webSocket.get();
        if (ws != null && state.get() == ConnectionState.OPEN) {
            ws.sendText(message, true);
        } else {
            LOGGER.error("WebSocket: Cannot send message, connection is not OPEN.");
        }
    }

    // --- Abstract Methods (For Protocol Implementation) ---

    /**
     * Handles an incoming text message from the WebSocket.
     *
     * @param message The full text payload.
     */
    protected abstract void onMessage(String message);

    /**
     * Gets the protocol-specific payload for the heartbeat (ping) message.
     *
     * @return A String (e.g., JSON) to be sent as a heartbeat.
     */
    protected abstract String getHeartbeatPayload();

    /**
     * Called when the connection state changes (e.g., OPEN, CLOSED).
     *
     * @param newState The new {@link ConnectionState}.
     */
    protected abstract void onStateChange(ConnectionState newState);

    /**
     * Builds the full WebSocket URI, including API key and parameters.
     *
     * @return The URI for the WebSocket connection.
     */
    protected abstract URI buildConnectionUri();

    // --- Internal Lifecycle Management ---

    /**
     * Atomically sets the state and notifies the subclass.
     */
    private void setState(ConnectionState newState) {
        this.state.set(newState);
        onStateChange(newState); // Notify subclass
    }

    /**
     * Starts the periodic heartbeat timer.
     * The interval is hardcoded to 30 seconds, per Phoenix standard.
     */
    private void startHeartbeat() {
        stopHeartbeat(); // Cancel previous task
        long interval = 30; // Phoenix standard heartbeat interval

        this.heartbeatTask = scheduler.scheduleAtFixedRate(() -> {
            try {
                if (state.get() == ConnectionState.OPEN) {
                    sendText(getHeartbeatPayload());
                }
            } catch (Exception e) {
                LOGGER.error("WebSocket: Failed to send heartbeat: " + e.getMessage());
            }
        }, interval, interval, TimeUnit.SECONDS);
    }

    /**
     * Stops the periodic heartbeat timer.
     */
    private void stopHeartbeat() {
        if (this.heartbeatTask != null && !this.heartbeatTask.isDone()) {
            this.heartbeatTask.cancel(true);
        }
    }

    /**
     * Schedules a reconnection attempt using exponential backoff.
     */
    private void scheduleReconnect() {
        if (state.get() == ConnectionState.CLOSING) {
            return; // Do not reconnect if disconnect() was called
        }

        setState(ConnectionState.CLOSED);
        stopHeartbeat();

        // 1s, 2s, 4s, 8s, 16s, then max 30s
        long delay = (long) Math.min(30, Math.pow(2, reconnectAttempts));
        reconnectAttempts++;
        LOGGER.debug("WebSocket: Reconnecting in " + delay + " seconds...");

        scheduler.schedule(this::connect, delay, TimeUnit.SECONDS);
    }

    // --- Private Inner WebSocket Listener ---

    /**
     * Private inner class that implements the WebSocket.Listener interface.
     * It forwards lifecycle events to the base class (onMessage, onClose, etc.).
     */
    private class WsListener implements WebSocket.Listener {

        /** - Buffer for accumulating text data from partial frames - **/
        private final StringBuilder textBuffer = new StringBuilder();

        @Override
        public void onOpen(WebSocket ws) {
            LOGGER.info("WebSocket: Connection OPEN");
            ws.request(1);
        }

        @Override
        public CompletionStage<?> onText(WebSocket ws, CharSequence data, boolean last) {
            textBuffer.append(data);

            if (last) {
                onMessage(textBuffer.toString());
                textBuffer.setLength(0);
            }

            ws.request(1);
            return null;
        }

        @Override
        public CompletionStage<?> onClose(WebSocket ws, int statusCode, String reason) {
            LOGGER.info("WebSocket: Connection CLOSED ({}: {})", statusCode, reason);
            scheduleReconnect();
            return null;
        }

        @Override
        public void onError(WebSocket ws, Throwable error) {
            LOGGER.error("WebSocket: ERROR: {}", error.getMessage());
        }
    }
}