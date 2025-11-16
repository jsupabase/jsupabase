package io.github.jsupabase.prueba.clients;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.github.jsupabase.core.exceptions.StorageException;
import io.github.jsupabase.prueba.utils.StorageConstants;
import com.fasterxml.jackson.core.type.TypeReference;
import io.github.jsupabase.core.client.HttpClientBase;
import io.github.jsupabase.core.config.SupabaseConfig;
import io.github.jsupabase.core.util.JsonUtil;
import io.github.jsupabase.core.util.MimeTypeUtil;
import io.github.jsupabase.prueba.StoragePaths;
import io.github.jsupabase.prueba.dto.*;
import io.github.jsupabase.prueba.dto.options.ListOptions;
import io.github.jsupabase.prueba.dto.options.MoveCopyRequest;
import io.github.jsupabase.prueba.dto.options.PathsRequest;
import io.github.jsupabase.prueba.dto.options.SignedUrlRequest;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * - OBJECT CLIENT -
 * <p>
 * Manages file-level operations within a specific Supabase Storage bucket. This client
 * provides the complete CRUD lifecycle for storage objects including upload, download,
 * metadata retrieval, move/copy operations, and URL generation (public and signed).
 * Each instance is bound to a single bucket for all its operations.
 *
 * <h3>Architecture Role:</h3>
 * ObjectClient instances are created on-demand by {@link io.github.jsupabase.prueba.StorageClient#object(String)}
 * and are lightweight - they hold only bucket context and service configuration. All
 * authentication and HTTP transport is inherited from {@code HttpClientBase}, ensuring
 * consistent auth propagation across operations.
 *
 * <h3>Object Lifecycle:</h3>
 * <pre>
 * 1. Upload object (POST/PUT with file data and metadata)
 * 2. Download object (authenticated or public URL access)
 * 3. List/Search objects (with pagination and filtering)
 * 4. Move/Copy objects (atomic server-side operations)
 * 5. Generate URLs (public for assets, signed for secure temporary access)
 * 6. Delete object(s) (single or batch operations)
 * </pre>
 *
 * <h3>Path Structure:</h3>
 * Objects use hierarchical paths within buckets (e.g., "users/123/avatar.jpg").
 * While Supabase Storage doesn't have true folders, paths with slashes enable
 * logical organization and prefix-based listing/filtering.
 *
 * <h3>Use Cases:</h3>
 * <ul>
 * <li><b>User content</b>: Profile pictures, documents, media uploads</li>
 * <li><b>Application assets</b>: Images, stylesheets, static resources</li>
 * <li><b>File management</b>: Document storage with version control via paths</li>
 * <li><b>CDN integration</b>: Public URLs for cached asset delivery</li>
 * <li><b>Secure sharing</b>: Signed URLs with expiration for private content</li>
 * </ul>
 *
 * @author neilhdezs
 * @version 0.1.0
 * @since 0.1.0
 */
public class ObjectClient extends HttpClientBase {

    /** - Logger for object operations - **/
    private static final Logger LOGGER = LoggerFactory.getLogger(ObjectClient.class);

    /** - The path prefix for the Storage service (e.g., "/storage/v1") - **/
    private final String servicePath;

    /** - The ID/Name of the bucket this client is bound to (e.g., "avatars") - **/
    private final String bucketId;

    /**
     * - OBJECT CLIENT CONSTRUCTOR -
     * <p>
     * Initializes an object client bound to a specific bucket. This constructor is
     * package-private and called by {@link io.github.jsupabase.prueba.StorageClient#object(String)}
     * each time a bucket-scoped client is requested. The lightweight design allows
     * creating multiple instances without performance penalty.
     * <p>
     * All operations performed through this client will target the configured bucket,
     * eliminating the need to pass bucket ID to individual methods and reducing
     * the risk of bucket mismatch errors.
     *
     * @param config Supabase configuration with storage path and authentication context
     * @param bucketId Unique bucket identifier that scopes all operations (must not be null)
     */
    public ObjectClient(SupabaseConfig config, String bucketId) {
        super(config);
        this.servicePath = config.getStoragePath();
        this.bucketId = Objects.requireNonNull(bucketId, "bucketId cannot be null");
    }

    // --- Core Object Operations ---

    /**
     * - UPLOAD FILE TO BUCKET -
     * <p>
     * Uploads a file to the specified path within the bucket. This operation automatically
     * detects the MIME type using {@link io.github.jsupabase.core.util.MimeTypeUtil} based
     * on file extension and content probing. If detection fails, defaults to
     * "application/octet-stream" for safe binary handling.
     * <p>
     * Upsert behavior:
     * <ul>
     * <li><b>upsert=true</b>: Overwrites existing file at path (update operation)</li>
     * <li><b>upsert=false</b>: Fails with 409 Conflict if file exists (create-only)</li>
     * </ul>
     * <p>
     * Best practices:
     * <ul>
     * <li>Use hierarchical paths for organization: "users/{id}/documents/{filename}"</li>
     * <li>Set upsert=false for initial uploads to prevent accidental overwrites</li>
     * <li>Verify bucket permissions (public/private) before upload</li>
     * <li>Check bucket size limits to avoid 413 Payload Too Large errors</li>
     * </ul>
     * <p>
     * REST endpoint: {@code POST /storage/v1/object/{bucketId}/{path}}
     *
     * @param path Relative path in bucket where file will be stored (e.g., "avatars/user123.jpg")
     * @param file Local file to upload as {@link java.nio.file.Path}
     * @param upsert If true, overwrites existing file; if false, fails on conflict
     * @return CompletableFuture with upload metadata (id, key, fullPath) or error
     */
    public CompletableFuture<UploadResponse> upload(String path, Path file, boolean upsert) {
        try {
            String mimeType = MimeTypeUtil.probeContentType(file);
            if (mimeType == null) mimeType = StorageConstants.MIME_OCTET_STREAM;
            String fullPath = StoragePaths.getObjectPath(this.servicePath, this.bucketId, path);
            LOGGER.debug(StorageConstants.LOG_UPLOAD_PATH, fullPath);
            HttpRequest request = newRequest(fullPath)
                    .header(StorageConstants.HDR_CONTENT_TYPE, mimeType)
                    .header(StorageConstants.HDR_X_UPSERT, String.valueOf(upsert))
                    .POST(HttpRequest.BodyPublishers.ofFile(file))
                    .build();

            return sendAsync(request, UploadResponse.class);

        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }

    /**
     * Replaces an existing file at the specified path.
     * <p>
     * This method constructs and executes a <strong>PUT</strong> request to the
     * {@code /object/{bucketId}/{path}} endpoint. It automatically detects the
     * file's MIME type using {@link MimeTypeUtil}.
     *
     * @param path   The relative path in the bucket (e.g., "avatars/avatar1.png").
     * @param file   The local file (java.nio.file.Path) to upload.
     * @param upsert Whether to overwrite the file if it exists. (Defaults to false).
     * @return A {@code CompletableFuture} containing the {@link UploadResponse}.
     */
    public CompletableFuture<UploadResponse> update(String path, Path file, boolean upsert) {
        try {
            String mimeType = MimeTypeUtil.probeContentType(file);
            if (mimeType == null) mimeType = StorageConstants.MIME_OCTET_STREAM;
            String fullPath = StoragePaths.getObjectPath(this.servicePath, this.bucketId, path);
            LOGGER.debug(StorageConstants.LOG_UPDATE_PATH, fullPath);
            HttpRequest request = newRequest(fullPath)
                    .header(StorageConstants.HDR_CONTENT_TYPE, mimeType)
                    .header(StorageConstants.HDR_X_UPSERT, String.valueOf(upsert))
                    .PUT(HttpRequest.BodyPublishers.ofFile(file))
                    .build();

            return sendAsync(request, UploadResponse.class);

        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }

    /**
     * - DOWNLOAD FILE (AUTHENTICATED) -
     * <p>
     * Downloads a file using the authenticated endpoint, which includes JWT credentials
     * in the request. This method is required for:
     * <ul>
     * <li><b>Private buckets</b>: Objects not accessible without authentication</li>
     * <li><b>RLS-protected objects</b>: When Row Level Security policies restrict access</li>
     * <li><b>User-specific content</b>: Files that require identity verification</li>
     * </ul>
     * <p>
     * The method returns raw bytes, allowing application code to handle the content
     * appropriately (write to disk, serve via HTTP, process in memory, etc.).
     * <p>
     * Error handling:
     * <ul>
     * <li><b>404</b>: Object doesn't exist or path is incorrect (case-sensitive)</li>
     * <li><b>403</b>: Insufficient permissions (check RLS policies and JWT)</li>
     * <li><b>401</b>: Missing or invalid authentication</li>
     * </ul>
     * <p>
     * REST endpoint: {@code GET /storage/v1/object/authenticated/{bucketId}/{path}}
     *
     * @param path Relative path to the object in the bucket (case-sensitive)
     * @return CompletableFuture with file content as byte array or StorageException on error
     * @throws io.github.jsupabase.core.exceptions.StorageException if HTTP status >= 400
     */
    public CompletableFuture<byte[]> download(String path) {
        String fullPath = StoragePaths.getObjectAuthenticatedPath(this.servicePath, this.bucketId, path);
        LOGGER.debug(StorageConstants.LOG_DOWNLOAD_PATH, fullPath);
        HttpRequest request = newRequest(fullPath).GET().build();

        return sendAsync(request, HttpResponse.BodyHandlers.ofByteArray())
                .thenApply(response -> {
                    if (response.statusCode() >= 400) {
                        throw new StorageException("Failed to download file: " + response.statusCode(), response.statusCode());
                    }
                    return response.body();
                });
    }

    /**
     * Deletes a single file within the bucket.
     * <p>
     * Corresponds to: {@code DELETE /object/{bucketId}/{path}}
     *
     * @param path The file path to delete (e.g., "avatars/avatar1.png").
     * @return A {@code CompletableFuture} with a list containing the deleted {@link FileObject}.
     */
    public CompletableFuture<List<FileObject>> remove(String path) {
        String fullPath = StoragePaths.getObjectPath(this.servicePath, this.bucketId, path);
        LOGGER.debug(StorageConstants.LOG_REMOVE_SINGLE, fullPath);
        HttpRequest request = newRequest(fullPath).DELETE().build();

        return sendAsync(request, new TypeReference<>() {
        });
    }

    /**
     * Deletes one or more files within the bucket.
     * <p>
     * This method constructs and executes a <strong>DELETE</strong> request (with a JSON body)
     * to the {@code /object/{id}} endpoint.
     *
     * @param paths A {@link List} of file paths to delete (e.g., ["avatars/avatar1.png"]).
     * @return A {@code CompletableFuture} with a list of the deleted {@link FileObject}s.
     */
    public CompletableFuture<List<FileObject>> remove(List<String> paths) {
        String objectPath = StoragePaths.getRemoveMultiplePath(this.servicePath, this.bucketId);
        String jsonBody = JsonUtil.toJson(new PathsRequest(paths));
        LOGGER.debug(StorageConstants.LOG_REMOVE_MULTIPLE, paths != null ? paths.size() : 0);
        HttpRequest request = newRequest(objectPath)
                .header(StorageConstants.HDR_CONTENT_TYPE, StorageConstants.MIME_JSON)
                .method("DELETE", HttpRequest.BodyPublishers.ofString(jsonBody)) // DELETE with body
                .build();

        return sendAsync(request, new TypeReference<>() {
        });
    }

    /**
     * - LIST OBJECTS WITH OPTIONS -
     * <p>
     * Lists objects within a folder prefix using advanced filtering, pagination, and
     * sorting options. This method provides full control over the listing behavior
     * including limit, offset, search terms, and sort criteria.
     * <p>
     * Listing capabilities:
     * <ul>
     * <li><b>Prefix filtering</b>: Only objects under specified path (e.g., "users/123/")</li>
     * <li><b>Pagination</b>: limit + offset for efficient large dataset navigation</li>
     * <li><b>Search</b>: Filter by filename patterns (server-side)</li>
     * <li><b>Sorting</b>: Order by name, size, or timestamp (ascending/descending)</li>
     * </ul>
     * <p>
     * Use cases:
     * <ul>
     * <li>File browsers with paginated views</li>
     * <li>Search functionality across storage objects</li>
     * <li>Administrative tools listing user uploads</li>
     * <li>Batch processing workflows requiring sorted iteration</li>
     * </ul>
     * <p>
     * REST endpoint: {@code POST /storage/v1/object/list/{bucketId}}
     *
     * @param options Listing configuration (prefix, limit, offset, search, sortBy)
     * @return CompletableFuture with list of file metadata objects matching criteria
     */
    public CompletableFuture<List<FileObject>> list(ListOptions options) {
        String listPath = StoragePaths.getListPath(this.servicePath, this.bucketId);
        String jsonBody = JsonUtil.toJson(options);
        LOGGER.debug(StorageConstants.LOG_LIST_PREFIX, options != null ? options.getPrefix() : "");
        HttpRequest request = newRequest(listPath)
                .header(StorageConstants.HDR_CONTENT_TYPE, StorageConstants.MIME_JSON)
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        return sendAsync(request, new TypeReference<>() {
        });
    }

    /**
     * (Convenience overload) Lists files at a specific path.
     *
     * @param path The folder path to list (e.g., "avatars/").
     */
    public CompletableFuture<List<FileObject>> list(String path) {
        ListOptions options = new ListOptions.Builder(path).build();
        return list(options);
    }

    /**
     * (Convenience overload) Lists files at the root of the bucket.
     */
    public CompletableFuture<List<FileObject>> list() {
        return list(new ListOptions.Builder("").build());
    }

    /**
     * Lists files using the V2 endpoint.
     * <p>
     * Corresponds to: {@code POST /object/list-v2/{id}}
     *
     * @param options The options for listing (e.g., prefix, limit, offset, search).
     * @return A {@code CompletableFuture} with a {@link List} of {@link FileObject}.
     */
    public CompletableFuture<List<FileObject>> listV2(ListOptions options) {
        String listPath = StoragePaths.getListV2Path(this.servicePath, this.bucketId);
        String jsonBody = JsonUtil.toJson(options);
        LOGGER.debug(StorageConstants.LOG_LIST_PREFIX, options != null ? options.getPrefix() : "");
        HttpRequest request = newRequest(listPath)
                .header(StorageConstants.HDR_CONTENT_TYPE, StorageConstants.MIME_JSON)
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        return sendAsync(request, new TypeReference<>() {
        });
    }

    /**
     * Moves an existing file to a new path.
     * <p>
     * This method executes a <strong>POST</strong> request to the {@code /object/move} endpoint.
     *
     * @param fromPath The original file path (e.g., "folder/image.png").
     * @param toPath   The new file path (e.g., "folder/image-new.png").
     * @return A {@code CompletableFuture} with a success {@link MessageResponse}.
     */
    public CompletableFuture<MessageResponse> move(String fromPath, String toPath) {
        String path = StoragePaths.getObjectMovePath(this.servicePath);
        MoveCopyRequest payload = new MoveCopyRequest(this.bucketId, fromPath, toPath);
        String jsonBody = JsonUtil.toJson(payload);
        LOGGER.debug(StorageConstants.LOG_MOVE_FILE, fromPath, toPath);
        HttpRequest request = newRequest(path)
                .header(StorageConstants.HDR_CONTENT_TYPE, StorageConstants.MIME_JSON)
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        return sendAsync(request, MessageResponse.class);
    }

    /**
     * Copies an existing file to a new path.
     * <p>
     * This method executes a <strong>POST</strong> request to the {@code /object/copy} endpoint.
     *
     * @param fromPath The original file path (e.g., "folder/image.png").
     * @param toPath   The new file path (e.g., "folder/image-copy.png").
     * @return A {@code CompletableFuture} with the details of the new file ({@link UploadResponse}).
     */
    public CompletableFuture<UploadResponse> copy(String fromPath, String toPath) {
        String path = StoragePaths.getObjectCopyPath(this.servicePath);
        MoveCopyRequest payload = new MoveCopyRequest(this.bucketId, fromPath, toPath);
        String jsonBody = JsonUtil.toJson(payload);
        LOGGER.debug(StorageConstants.LOG_COPY_FILE, fromPath, toPath);
        HttpRequest request = newRequest(path)
                .header(StorageConstants.HDR_CONTENT_TYPE, StorageConstants.MIME_JSON)
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        return sendAsync(request, UploadResponse.class);
    }

    /**
     * Retrieves the metadata for a specific object (authenticated).
     * <p>
     * Corresponds to: {@code GET /object/info/authenticated/{bucketName}/{wildcard}}
     *
     * @param path The path to the object (e.g., "folder/image.png").
     * @return A {@code CompletableFuture} with the object's {@link FileObject} metadata.
     */
    public CompletableFuture<FileObject> getObjectInfo(String path) {
        String fullPath = StoragePaths.getObjectInfoAuthPath(this.servicePath, this.bucketId, path);
        HttpRequest request = newRequest(fullPath).GET().build();
        return sendAsync(request, FileObject.class);
    }

    /**
     * Retrieves the public metadata for a specific object.
     * <p>
     * Corresponds to: {@code GET /object/info/public/{bucketName}/{wildcard}}
     * (Also covers the ambiguous {@code GET /object/info/{...}})
     *
     * @param path The path to the object (e.g., "folder/image.png").
     * @return A {@code CompletableFuture} with the object's {@link FileObject} metadata.
     */
    public CompletableFuture<FileObject> getPublicObjectInfo(String path) {
        String fullPath = StoragePaths.getObjectInfoPublicPath(this.servicePath, this.bucketId, path);
        HttpRequest request = newRequest(fullPath).GET().build();
        return sendAsync(request, FileObject.class);
    }

    // --- URL Generation Methods ---

    /**
     * Retrieves the public URL for an asset in a public bucket.
     * <p>
     * This method <strong>constructs the URL string locally</strong> and does not make an HTTP request.
     * The returned URL can be used by an end-user (e.g., in a browser) to
     * trigger {@code GET /object/public/{bucketId}/{wildcard}}.
     *
     * @param path The path and name of the file (e.g., "folder/image.png").
     * @return The full, absolute public URL as a String.
     */
    public String getPublicUrl(String path) {
        URI baseUrl = this.config.getSupabaseUrl();
        return StoragePaths.getPublicUrl(baseUrl, this.servicePath, this.bucketId, path);
    }

    /**
     * - CREATE SIGNED DOWNLOAD URL -
     * <p>
     * Generates a cryptographically signed URL that grants temporary download access
     * to a private object without requiring authentication headers. The URL includes
     * a token that embeds permissions and expiration time, enabling secure sharing
     * of private content.
     * <p>
     * Security characteristics:
     * <ul>
     * <li><b>Time-limited</b>: URL becomes invalid after specified seconds</li>
     * <li><b>Token-based</b>: Cryptographic signature prevents tampering</li>
     * <li><b>No auth required</b>: Recipients don't need Supabase credentials</li>
     * <li><b>Revocable</b>: Changing bucket policies invalidates old tokens</li>
     * </ul>
     * <p>
     * Common use cases:
     * <ul>
     * <li>Email attachments with expiring links</li>
     * <li>Temporary file sharing without user accounts</li>
     * <li>Pre-signed uploads for client-side direct uploads</li>
     * <li>CDN-friendly URLs for private media delivery</li>
     * </ul>
     * <p>
     * REST endpoint: {@code POST /storage/v1/object/sign/{bucketId}/{path}}
     *
     * @param path Relative path to the object requiring signed access
     * @param expiresIn Validity duration in seconds (e.g., 3600 = 1 hour)
     * @return CompletableFuture with signed URL, path, and expiration timestamp
     */
    public CompletableFuture<SignedUrlResponse> createSignedUrl(String path, int expiresIn) {
        String fullPath = StoragePaths.getSignedPath(this.servicePath, this.bucketId, path);
        String jsonBody = JsonUtil.toJson(Map.of(StorageConstants.JSON_EXPIRES_IN, expiresIn));
        HttpRequest request = newRequest(fullPath)
                .header(StorageConstants.HDR_CONTENT_TYPE, StorageConstants.MIME_JSON)
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        return sendAsync(request, SignedUrlResponse.class);
    }

    /**
     * Creates multiple signed URLs for downloading files.
     * <p>
     * This method executes a <strong>POST</strong> request to the
     * {@code /object/sign/{bucketId}} endpoint.
     *
     * @param paths     A {@link List} of file paths.
     * @param expiresIn The number of seconds until the URLs expire.
     * @return A {@code CompletableFuture} with a {@link List} of {@link SignedUrlListItem}.
     */
    public CompletableFuture<List<SignedUrlListItem>> createSignedUrls(List<String> paths, int expiresIn) {
        String fullPath = StoragePaths.getSignedUrlsPath(this.servicePath, this.bucketId);
        String jsonBody = JsonUtil.toJson(new SignedUrlRequest(paths, expiresIn));
        HttpRequest request = newRequest(fullPath)
                .header(StorageConstants.HDR_CONTENT_TYPE, StorageConstants.MIME_JSON)
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        return sendAsync(request, new TypeReference<>() {
        });
    }

    /**
     * Creates a signed URL for uploading a file.
     * <p>
     * This method executes a <strong>POST</strong> request to the
     * {@code /object/upload/sign/{bucketId}/{path}} endpoint to generate a token.
     * The returned {@link SignedUrlResponse} contains the URL (with token)
     * that an end-user can use to trigger {@code PUT /object/upload/sign/{...}}.
     *
     * @param path      The file path (e.g., "avatars/image.png").
     * @param expiresIn The number of seconds until the URL expires.
     * @return A {@code CompletableFuture} with the {@link SignedUrlResponse}.
     */
    public CompletableFuture<SignedUrlResponse> createSignedUploadUrl(String path, int expiresIn) {
        String fullPath = StoragePaths.getSignedUploadPath(this.servicePath, this.bucketId, path);
        String jsonBody = JsonUtil.toJson(Map.of(StorageConstants.JSON_EXPIRES_IN, expiresIn));
        HttpRequest request = newRequest(fullPath)
                .header(StorageConstants.HDR_CONTENT_TYPE, StorageConstants.MIME_JSON)
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        return sendAsync(request, SignedUrlResponse.class);
    }

    // --- CDN Endpoint ---

    /**
     * Purges the CDN cache for a specific object.
     * <p>
     * Corresponds to: {@code DELETE /cdn/{bucketName}/{wildcard}}
     *
     * @param path The path to the object (e.g., "folder/image.png").
     * @return A {@code CompletableFuture} containing a success {@link MessageResponse}.
     */
    public CompletableFuture<MessageResponse> purgeCache(String path) {
        String fullPath = StoragePaths.getPurgeCachePath(this.servicePath, this.bucketId, path);
        LOGGER.debug(StorageConstants.LOG_PURGE_CACHE, fullPath);
        HttpRequest request = newRequest(fullPath).DELETE().build();
        return sendAsync(request, MessageResponse.class);
    }
}
