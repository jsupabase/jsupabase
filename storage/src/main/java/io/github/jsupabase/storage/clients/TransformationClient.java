package io.github.jsupabase.storage.clients;

import io.github.jsupabase.core.client.HttpClientBase;
import io.github.jsupabase.core.config.SupabaseConfig;
import io.github.jsupabase.core.exception.SupabaseException;
import io.github.jsupabase.core.util.JsonUtil;
import io.github.jsupabase.storage.StoragePaths;
import io.github.jsupabase.storage.dto.SignedUrlResponse;
import io.github.jsupabase.storage.dto.options.TransformOptions;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * Client for Image Transformation operations within a specific Supabase Storage Bucket.
 * <p>
 * This class implements all methods corresponding to the {@code /render/image}
 * endpoint group in the Supabase Storage API. It handles generating public URLs
 * for transformations, downloading transformed images, and creating signed URLs
 * for transformed images.
 * <p>
 * This client is instantiated by {@link io.github.jsupabase.storage.StorageClient#transformation(String)}
 * and is bound to a specific {@code bucketId} for all its operations.
 *
 * @author neilhdezs
 * @version 1.0.0
 */
public class TransformationClient extends HttpClientBase {

    /** - The path prefix for the Storage service (e.g., "/storage/v1") - **/
    private final String servicePath;

    /** - The ID/Name of the bucket this client is bound to (e.g., "avatars") - **/
    private final String bucketId;

    /**
     * Package-private constructor for the TransformationClient.
     *
     * @param config      The SupabaseConfig, used to get the service path and base URL.
     * @param bucketId    The ID (name) of the bucket this client will operate on.
     */
    public TransformationClient(SupabaseConfig config, String bucketId) {
        // Use the copy constructor of HttpClientBase to inherit auth state
        super(config);
        this.servicePath = config.getStoragePath();
        this.bucketId = Objects.requireNonNull(bucketId, "bucketId cannot be null");
    }

    /**
     * Retrieves the public URL for a transformed image.
     * <p>
     * This method <strong>constructs the URL string locally</strong> and does not make an HTTP request.
     * The URL includes the transformation options as query parameters.
     * <p>
     * Corresponds to: {@code GET /render/image/public/{bucketId}/{wildcard}}
     *
     * @param path    The path to the image (e.g., "folder/image.png").
     * @param options The transformation options (width, height, resize, etc.).
     * @return The full, absolute public URL as a String for the transformed image.
     */
    public String getPublicRenderUrl(String path, TransformOptions options) {
        URI baseUrl = this.config.getSupabaseUrl();
        return StoragePaths.getPublicRenderUrl(baseUrl, this.servicePath, this.bucketId, path, options);
    }

    /**
     * Downloads a transformed image from the bucket using authentication (JWT).
     * <p>
     * This method uses the authenticated render endpoint and is suitable for private images
     * that require RLS policies.
     * <p>
     * Corresponds to: {@code GET /render/image/authenticated/{bucketId}/{path}}
     *
     * @param path    The path to the image (e.g., "folder/image.png").
     * @param options The transformation options (width, height, resize, etc.).
     * @return A {@code CompletableFuture} with the transformed image's raw bytes.
     */
    public CompletableFuture<byte[]> getAuthenticatedRenderUrl(String path, TransformOptions options) {
        String fullPath = StoragePaths.getAuthenticatedRenderPath(this.servicePath, this.bucketId, path);
        String queryString = (options != null) ? options.toQueryParameters() : "";

        // El constructor de HttpRequest se encarga de unir la URI y la query
        HttpRequest request = newRequest(fullPath + queryString).GET().build();

        return sendAsync(request, HttpResponse.BodyHandlers.ofByteArray())
                .thenApply(response -> {
                    if (response.statusCode() >= 400) {
                        throw new SupabaseException("Failed to download transformed file: " + response.statusCode(), response.statusCode());
                    }
                    return response.body();
                });
    }

    /**
     * Creates a signed URL for a transformed image.
     * <p>
     * Note: The Supabase API specification is ambiguous here. This implementation
     * follows the {@code supabase-js} library by sending transformation options
     * in the body of a <strong>POST</strong> request to the
     * {@code /object/sign/{bucketId}/{path}} endpoint, which is technically
     * part of the 'object' group but is used here for this 'transformation' functionality.
     *
     * @param path      The path to the image (e.g., "folder/image.png").
     * @param expiresIn The number of seconds until the URL expires.
     * @param options   The transformation options (width, height, resize, etc.).
     * @return A {@code CompletableFuture} with the {@link SignedUrlResponse}.
     */
    public CompletableFuture<SignedUrlResponse> createSignedRenderUrl(String path, int expiresIn, TransformOptions options) {
        // Usamos el endpoint de /object/sign/{...} ya que no existe un /render/image/sign POST
        String fullPath = StoragePaths.getSignedPath(this.servicePath, this.bucketId, path);

        // Combina expiresIn y las opciones de transformación en el body del JSON
        Map<String, Object> bodyMap = JsonUtil.toMap(options);
        bodyMap.put("expiresIn", expiresIn);
        String jsonBody = JsonUtil.toJson(bodyMap);

        HttpRequest request = newRequest(fullPath)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        return sendAsync(request, SignedUrlResponse.class);
    }
}