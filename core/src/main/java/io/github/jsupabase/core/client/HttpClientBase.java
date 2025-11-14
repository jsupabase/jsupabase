package io.github.jsupabase.core.client;

import io.github.jsupabase.core.config.SupabaseConfig;
import io.github.jsupabase.core.exception.SupabaseException;
import io.github.jsupabase.core.util.JsonUtil; // Make sure this class exists

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
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
 * @version 0.0.3
 */
public abstract class HttpClientBase {

    /** - Immutable configuration - **/
    protected final SupabaseConfig config;

    /**
     * A SINGLE HttpClient for the entire application.
     * It is immutable, thread-safe, and highly efficient (reuses connections).
     * It must not be recreated.
     */
    private static final HttpClient SHARED_HTTP_CLIENT = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_2)
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    /**
     * Constructor for the base client.
     *
     * @param config The client configuration.
     */
    protected HttpClientBase(SupabaseConfig config) {
        this.config = config;
    }

    /**
     * Starts a new HttpRequest.Builder pre-configured with
     * the base URL and common headers (API Key, Auth, etc.).
     *
     * @param path The relative path (e.g., "/rest/v1/my_table")
     * @return A HttpRequest.Builder ready to be modified (add .GET(), .POST(), etc.)
     */
    public HttpRequest.Builder newRequest(String path) {
        // Resolve the full URL using the config's base
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
     *
     * @param request      The finalized HttpRequest.
     * @param responseType The DTO's class (e.g., MyPojo.class or List.class).
     * @param <T>          The DTO's generic type.
     * @return A CompletableFuture with the DTO instance.
     */
    public <T> CompletableFuture<T> sendAsync(HttpRequest request, Class<T> responseType) {
        return SHARED_HTTP_CLIENT.sendAsync(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8))
                .thenApply(httpResponse -> {
                    // First, handle HTTP errors
                    if (httpResponse.statusCode() >= 400) {
                        throw new SupabaseException(httpResponse.body(), httpResponse.statusCode());
                    }

                    String body = httpResponse.body();

                    // If no response is expected (Void) or the body is empty
                    if (responseType == null || responseType == Void.class || body == null || body.isEmpty()) {
                        return null;
                    }

                    // Deserialize the JSON body to the expected class
                    try {
                        return JsonUtil.fromJson(body, responseType);
                    } catch (Exception e) {
                        throw new SupabaseException("Failed to deserialize JSON: " + e.getMessage(), e);
                    }
                })
                .exceptionally(ex -> {
                    // Catch the SupabaseException or any other network error
                    if (ex.getCause() instanceof SupabaseException) {
                        throw (SupabaseException) ex.getCause();
                    }
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

    /**
     * Getter for the configuration, useful for subclasses.
     *
     * @return The SupabaseConfig
     */
    public SupabaseConfig getConfig() {
        return config;
    }
}