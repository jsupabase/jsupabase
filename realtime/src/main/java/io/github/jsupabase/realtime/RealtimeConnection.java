package io.github.jsupabase.realtime;

import io.github.jsupabase.core.client.WebSocketClientBase;
import io.github.jsupabase.core.config.SupabaseConfig;
import io.github.jsupabase.core.enums.ConnectionState;
import io.github.jsupabase.core.util.JsonUtil;
import io.github.jsupabase.realtime.dto.PhoenixMessage;

import java.net.URI;
import java.net.http.HttpClient;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Manages the underlying WebSocket connection and Phoenix protocol.
 * <p>
 * This class extends {@link WebSocketClientBase} to inherit all low-level
 * connection, reconnection, and heartbeat logic. It is responsible for
 * implementing the Phoenix protocol, dispatching messages to the correct
 * {@link RealtimeChannelBuilder}, and managing the authentication token (JWT).
 * <p>
 * This class is intended to be a **singleton** managed by the {@link RealtimeClient} gateway.
 *
 * @author neilhdezs
 * @version 1.0.0
 */
public class RealtimeConnection extends WebSocketClientBase {

    /** - A thread-safe map of active channels (topic -> Builder) - **/
    private final Map<String, RealtimeChannelBuilder> channels = new ConcurrentHashMap<>();

    /** - A thread-safe counter for message reference numbers (ref) - **/
    private final AtomicLong refCounter = new AtomicLong(0);

    /** - The current authentication token (JWT), managed by setAuth() - **/
    private String jwt = null;

    /**
     * Constructs the RealtimeConnection.
     *
     * @param config     The Supabase configuration.
     * @param httpClient The shared HttpClient (from HttpClientBase).
     */
    public RealtimeConnection(SupabaseConfig config, HttpClient httpClient) {
        super(config, httpClient);
        this.jwt = config.getSupabaseKey();
        connect();
    }

    /**
     * (Usado por RealtimeClient Gateway)
     * Creates or retrieves a channel builder for a given topic.
     *
     * @param topic The name of the topic (e.g., "realtime:public:my_table").
     * @return A {@link RealtimeChannelBuilder} instance.
     */
    public RealtimeChannelBuilder channel(String topic) {
        return channels.computeIfAbsent(topic, t -> new RealtimeChannelBuilder(t, this));
    }

    /**
     * (Usado por RealtimeClient Gateway)
     * Updates the authentication JWT for the connection.
     * <p>
     * This is called by {@code SupabaseClient} during {@code SIGNED_IN} or
     * {@code TOKEN_REFRESHED} events. It notifies all existing channels
     * to rejoin with the new credentials.
     *
     * @param jwt The new JWT.
     */
    public void setAuth(String jwt) {
        this.jwt = jwt;
        System.out.println("RealtimeConnection: Auth token updated. Re-joining channels...");
        for (RealtimeChannelBuilder realtimeChannelBuilder : channels.values()) {
            realtimeChannelBuilder.rejoinWithNewAuth();
        }
    }

    /**
     * (Usado por RealtimeClient Gateway)
     * Removes a specific channel from the connection.
     *
     * @param channel The channel builder instance to remove.
     */
    public void removeChannel(RealtimeChannelBuilder channel) {
        if (channel != null) {
            channels.remove(channel.getTopic());
            channel.unsubscribe();
        }
    }

    /**
     * (Usado por RealtimeClient Gateway)
     * Removes all channels from the connection.
     */
    public void removeAllChannels() {
        for (RealtimeChannelBuilder channel : channels.values()) {
            channel.unsubscribe();
        }
        channels.clear();
    }

    /**
     * (Package-Private) Gets the current JWT.
     * Used by {@link RealtimeChannelBuilder} to send an authenticated join message.
     *
     * @return The current access token (JWT).
     */
    String getJwt() {
        return this.jwt;
    }

    /**
     * (Package-Private) Gets the next sequential message reference number.
     *
     * @return A unique reference string.
     */
    String getNextRef() {
        return String.valueOf(refCounter.incrementAndGet());
    }

    /**
     * (Package-Private) Pushes a protocol message (DTO) onto the WebSocket.
     *
     * @param phoenixMessageDto The DTO to send (e.g., a "phx_join" message).
     */
    void push(Object phoenixMessageDto) {
        String json = JsonUtil.toJson(phoenixMessageDto);
        sendText(json); // sendText() is inherited from WebSocketClientBase
    }

    // --- Implementation of Abstract WebSocketClientBase Methods ---

    /**
     * {@inheritDoc}
     */
    @Override
    protected URI buildConnectionUri() {
        String servicePath = config.getRealtimePath();
        String apikey = config.getSupabaseKey();

        // Usa RealtimePaths para construir la URL
        String path = RealtimePaths.getWebSocketPath(servicePath, apikey, "1.0.0");

        // "https://id.supabase.co" -> "wss://id.supabase.co/realtime/v1/websocket?apikey=..."
        return URI.create(config.getSupabaseUrl().toString()
                .replaceFirst("http", "ws")
                + path);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onMessage(String message) {
        // System.out.println("Realtime RX: " + message); // Descomentar para depuración
        try {
            PhoenixMessage msg = JsonUtil.fromJson(message, PhoenixMessage.class);
            RealtimeChannelBuilder channel = channels.get(msg.getTopic());
            if (channel != null) {
                // Delega el mensaje al builder del canal correcto
                channel.trigger(msg.getEvent(), msg.getPayload(), msg.getRef());
            }
        } catch (Exception e) {
            System.err.println("Realtime: Failed to parse incoming message: " + e.getMessage());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getHeartbeatPayload() {
        // Usa RealtimePaths para los eventos
        PhoenixMessage heartbeatMsg = new PhoenixMessage(
                RealtimePaths.topicPhoenix(),
                RealtimePaths.eventHeartbeat(),
                Collections.emptyMap(),
                getNextRef()
        );
        return JsonUtil.toJson(heartbeatMsg);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onStateChange(ConnectionState newState) {
        System.out.println("RealtimeConnection: State changed to " + newState);
        if (newState == ConnectionState.OPEN) {
            // Si nos reconectamos, debemos re-suscribirnos a los canales
            System.out.println("RealtimeConnection: Re-subscribing to channels...");
            for (RealtimeChannelBuilder realtimeChannelBuilder : channels.values()) {
                realtimeChannelBuilder.resubscribe();
            }
        }
    }
}