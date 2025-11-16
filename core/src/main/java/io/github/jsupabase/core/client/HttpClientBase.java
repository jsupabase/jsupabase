package io.github.jsupabase.core.client;

import com.fasterxml.jackson.core.type.TypeReference;
import io.github.jsupabase.core.config.SupabaseConfig;
import io.github.jsupabase.core.exceptions.SupabaseException;
import io.github.jsupabase.core.util.JsonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandler;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * The base, abstract network engine for all clients (Postgrest, Auth, etc.).
 * It manages the native Java 11 HttpClient, header injection,
 * and JSON serialization/deserialization.
 *
 * @author neilhdezs
 * @version 0.1.0
 * @since 0.1.0
 */
public abstract class HttpClientBase {

    /** - Logger for HTTP operations - **/
    private static final Logger LOGGER = LoggerFactory.getLogger(HttpClientBase.class);

    /**
     * A SINGLE HttpClient for the entire application.
     * It is immutable, thread-safe, and highly efficient (reuses connections).
     * It must not be recreated.
     */
    private static final HttpClient SHARED_HTTP_CLIENT = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_2)
            .connectTimeout(Duration.ofSeconds(10))
            .build();
    /** - Immutable configuration - **/
    protected final SupabaseConfig config;

    /**
     * Constructor for the base client.
     *
     * @param config The client configuration.
     */
    protected HttpClientBase(SupabaseConfig config) {
        this.config = config;
    }

    /**
     * Getter for the shared, static HttpClient instance.
     */
    public static HttpClient getSharedHttpClient() {
        return SHARED_HTTP_CLIENT;
    }

    /**
     * Starts a new HttpRequest.Builder pre-configured with
     * the base URL and common headers (API Key, Auth, etc.).
     *
     * @param path The relative path (e.g., "/rest/v1/my_table")
     * @return A HttpRequest.Builder ready to be modified (add .GET(), .POST(), etc.)
     */
    public HttpRequest.Builder newRequest(String path) {
        URI uri = config.resolveUrl(path);

        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(uri)
                .timeout(Duration.ofSeconds(15)); // Per-request timeout

        for (Map.Entry<String, String> header : config.getHeaders().entrySet()) {
            builder.header(header.getKey(), header.getValue());
        }

        return builder;
    }

    /**
     * Sends an asynchronous request and deserializes the JSON response to a DTO.
     * (Para DTOs simples como Bucket.class)
     *
     * @param request      The finalized HttpRequest.
     * @param responseType The DTO's class (e.g., MyPojo.class).
     * @param <T>          The DTO's generic type.
     * @return A CompletableFuture with the DTO instance.
     */
    public <T> CompletableFuture<T> sendAsync(HttpRequest request, Class<T> responseType) {
        return SHARED_HTTP_CLIENT.sendAsync(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8))
                .thenApply(httpResponse -> {
                    if (httpResponse.statusCode() >= 400) {
                        LOGGER.error("HTTP error {}: {} - Request: {}", httpResponse.statusCode(), httpResponse.body(), request.uri());
                        throw new SupabaseException(httpResponse.body(), httpResponse.statusCode());
                    }
                    String body = httpResponse.body();
                    if (responseType == null || responseType == Void.class || body == null || body.isEmpty()) {
                        return null;
                    }
                    try {
                        LOGGER.trace("Response body: {}", body);
                        return JsonUtil.fromJson(body, responseType);
                    } catch (Exception e) {
                        LOGGER.error("Failed to deserialize response: {}", e.getMessage());
                        throw new SupabaseException("Failed to deserialize JSON: " + e.getMessage(), e);
                    }
                })
                .exceptionally(ex -> {
                    if (ex.getCause() instanceof SupabaseException) {
                        throw (SupabaseException) ex.getCause();
                    }
                    LOGGER.error("Network request failed: {}", ex.getMessage());
                    throw new SupabaseException("Network request failed: " + ex.getMessage(), ex);
                });
    }

    /**
     * Sends an asynchronous request and deserializes a generic JSON response (e.g., List<T>).
     * (Para DTOs complejos como List<Bucket>)
     *
     * @param request The finalized HttpRequest.
     * @param typeRef The TypeReference for the DTO.
     * @param <T>     The DTO's generic type.
     * @return A CompletableFuture with the DTO instance.
     */
    public <T> CompletableFuture<T> sendAsync(HttpRequest request, TypeReference<T> typeRef) {
        return SHARED_HTTP_CLIENT.sendAsync(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8))
                .thenApply(httpResponse -> {
                    if (httpResponse.statusCode() >= 400) {
                        LOGGER.error("HTTP error {}: {} - Request: {}", httpResponse.statusCode(), httpResponse.body(), request.uri());
                        throw new SupabaseException(httpResponse.body(), httpResponse.statusCode());
                    }
                    String body = httpResponse.body();
                    if (body == null || body.isEmpty()) {
                        return null;
                    }
                    try {
                        LOGGER.trace("Response body: {}", body);
                        return JsonUtil.fromJson(body, typeRef);
                    } catch (Exception e) {
                        LOGGER.error("Failed to deserialize response: {}", e.getMessage());
                        throw new SupabaseException("Failed to deserialize JSON: " + e.getMessage(), e);
                    }
                })
                .exceptionally(ex -> {
                    if (ex.getCause() instanceof SupabaseException) {
                        throw (SupabaseException) ex.getCause();
                    }
                    LOGGER.error("Network request failed: {}", ex.getMessage());
                    throw new SupabaseException("Network request failed: " + ex.getMessage(), ex);
                });
    }

    /**
     * Sends an async request and returns the full HttpResponse.
     * This is used for custom body handling (e.g., file downloads).
     *
     * @param request     The finalized HttpRequest.
     * @param bodyHandler The BodyHandler to use (e.g., BodyHandlers.ofByteArray()).
     * @param <T>         The response body type (e.g., byte[]).
     * @return A CompletableFuture with the full HttpResponse<T>.
     */
    public <T> CompletableFuture<HttpResponse<T>> sendAsync(HttpRequest request, BodyHandler<T> bodyHandler) {
        return SHARED_HTTP_CLIENT.sendAsync(request, bodyHandler)
                .exceptionally(ex -> {
                    throw new SupabaseException("Network request failed: " + ex.getMessage(), ex);
                });
    }


    /**
     * Sends an asynchronous request that returns a String (e.g., for CSV).
     *
     * @param request The finalized HttpRequest.
     * @return A CompletableFuture with the body as a String.
     */
    public CompletableFuture<String> sendAsyncString(HttpRequest request) {
        return SHARED_HTTP_CLIENT.sendAsync(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8))
                .thenApply(httpResponse -> {
                    if (httpResponse.statusCode() >= 400) {
                        throw new SupabaseException(httpResponse.body(), httpResponse.statusCode());
                    }
                    return httpResponse.body();
                })
                .exceptionally(ex -> {
                    if (ex.getCause() instanceof SupabaseException) {
                        throw (SupabaseException) ex.getCause();
                    }
                    throw new SupabaseException("Network request failed: " + ex.getMessage(), ex);
                });
    }
}