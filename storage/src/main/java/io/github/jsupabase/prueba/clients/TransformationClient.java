package io.github.jsupabase.prueba.clients;

import io.github.jsupabase.core.client.HttpClientBase;
import io.github.jsupabase.core.config.SupabaseConfig;
import io.github.jsupabase.core.exceptions.StorageException;
import io.github.jsupabase.core.util.JsonUtil;
import io.github.jsupabase.prueba.StoragePaths;
import io.github.jsupabase.prueba.dto.SignedUrlResponse;
import io.github.jsupabase.prueba.dto.options.TransformOptions;
import io.github.jsupabase.prueba.utils.StorageConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * - TRANSFORMATION CLIENT -
 * <p>
 * Provides real-time image transformation capabilities for Supabase Storage objects.
 * This client generates URLs and downloads transformed images with on-the-fly resizing,
 * format conversion, quality adjustment, and other optimizations. Transformations are
 * performed server-side and can be cached by CDNs for optimal delivery performance.
 *
 * <h3>Architecture Role:</h3>
 * TransformationClient instances are created on-demand per bucket by
 * {@link io.github.jsupabase.prueba.StorageClient#transformation(String)}. Each instance
 * is lightweight and bound to a single bucket, similar to ObjectClient. Transformations
 * leverage Supabase's image processing pipeline built on modern optimization libraries.
 *
 * <h3>Transformation Capabilities:</h3>
 * <ul>
 * <li><b>Resizing</b>: Width/height constraints with multiple fit modes (cover, contain, fill)</li>
 * <li><b>Format conversion</b>: WebP, JPEG, PNG, AVIF for optimal compatibility</li>
 * <li><b>Quality adjustment</b>: Compression levels for size/quality tradeoff</li>
 * <li><b>Cropping</b>: Focal point and smart crop for responsive layouts</li>
 * </ul>
 *
 * <h3>Performance Benefits:</h3>
 * <ul>
 * <li>CDN-friendly URLs enable edge caching of transformed images</li>
 * <li>Responsive image delivery without client-side processing</li>
 * <li>Bandwidth optimization through format conversion (e.g., WebP)</li>
 * <li>Lazy transformation: images processed only when requested</li>
 * </ul>
 *
 * <h3>Use Cases:</h3>
 * <ul>
 * <li><b>Responsive images</b>: Generate multiple sizes for different viewports</li>
 * <li><b>Thumbnails</b>: Create preview versions of uploaded images</li>
 * <li><b>Format optimization</b>: Serve WebP to modern browsers, JPEG as fallback</li>
 * <li><b>Profile pictures</b>: Consistent dimensions and format for avatars</li>
 * </ul>
 *
 * @author neilhdezs
 * @version 0.1.0
 * @since 0.1.0
 */
public class TransformationClient extends HttpClientBase {

    /** - Logger for transformation operations - **/
    private static final Logger LOGGER = LoggerFactory.getLogger(TransformationClient.class);

    /** - The path prefix for the Storage service (e.g., "/storage/v1") - **/
    private final String servicePath;

    /** - The ID/Name of the bucket this client is bound to (e.g., "avatars") - **/
    private final String bucketId;

    /**
     * - TRANSFORMATION CLIENT CONSTRUCTOR -
     * <p>
     * Initializes a transformation client bound to a specific bucket. This constructor
     * inherits authentication state from {@code HttpClientBase} ensuring transformed
     * image requests include appropriate credentials for private buckets.
     * <p>
     * The client is lightweight and can be instantiated multiple times without
     * performance concerns, though typically one instance per bucket is sufficient
     * for application lifecycle.
     *
     * @param config Supabase configuration with storage path and authentication
     * @param bucketId Unique bucket identifier containing images to transform
     */
    public TransformationClient(SupabaseConfig config, String bucketId) {
        // Use the copy constructor of HttpClientBase to inherit auth state
        super(config);
        this.servicePath = config.getStoragePath();
        this.bucketId = Objects.requireNonNull(bucketId, "bucketId cannot be null");
    }

    /**
     * - GET PUBLIC TRANSFORMATION URL -
     * <p>
     * Constructs a public URL for accessing a transformed version of an image. This is
     * a local operation (no HTTP request) that builds the URL with transformation
     * parameters encoded as query strings. The resulting URL can be embedded directly
     * in HTML img tags or used in HTTP requests.
     * <p>
     * The transformation is performed server-side when the URL is first accessed, and
     * the result is typically cached by CDN infrastructure for subsequent requests.
     * This enables efficient delivery of responsive images without preprocessing.
     * <p>
     * Query parameters control transformation:
     * <ul>
     * <li><b>width/height</b>: Target dimensions in pixels</li>
     * <li><b>resize</b>: Fit mode (cover, contain, fill)</li>
     * <li><b>format</b>: Output format (webp, jpeg, png)</li>
     * <li><b>quality</b>: Compression level (1-100)</li>
     * </ul>
     * <p>
     * Use for: Responsive images, thumbnails, format optimization, CDN-cached assets.
     * <p>
     * REST endpoint template: {@code GET /storage/v1/render/image/public/{bucketId}/{path}?width=...}
     *
     * @param path Relative path to the source image in the bucket
     * @param options Transformation parameters (dimensions, format, quality, etc.)
     * @return Complete public URL string ready for browser/client consumption
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

        HttpRequest request = newRequest(fullPath + queryString).GET().build();

        return sendAsync(request, HttpResponse.BodyHandlers.ofByteArray())
                .thenApply(response -> {
                    if (response.statusCode() >= 400) {
                        throw new StorageException("Failed to download transformed file: " + response.statusCode(), response.statusCode());
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
    public CompletableFuture<SignedUrlResponse> createSignedUrl(String path, int expiresIn, TransformOptions options) {
        String fullPath = StoragePaths.getSignedRenderPath(this.servicePath, this.bucketId, path);

        Map<String, Object> bodyMap = JsonUtil.toMap(options);
        bodyMap.put(StorageConstants.JSON_EXPIRES_IN, expiresIn);
        String jsonBody = JsonUtil.toJson(bodyMap);

        LOGGER.debug("Creating signed transformation URL for path: {}", path);
        HttpRequest request = newRequest(fullPath)
                .header(StorageConstants.HDR_CONTENT_TYPE, StorageConstants.MIME_JSON)
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        return sendAsync(request, SignedUrlResponse.class);
    }
}