package io.github.jsupabase.storage.clients;

import com.fasterxml.jackson.core.type.TypeReference;
import io.github.jsupabase.core.client.HttpClientBase;
import io.github.jsupabase.core.config.SupabaseConfig;
import io.github.jsupabase.core.exception.SupabaseException;
import io.github.jsupabase.core.util.JsonUtil;
import io.github.jsupabase.core.util.MimeTypeUtil;
import io.github.jsupabase.storage.StoragePaths;
import io.github.jsupabase.storage.dto.*;
import io.github.jsupabase.storage.dto.options.ListOptions;
import io.github.jsupabase.storage.dto.options.MoveCopyRequest;
import io.github.jsupabase.storage.dto.options.PathsRequest;
import io.github.jsupabase.storage.dto.options.SignedUrlRequest;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * Client for object-level operations within a specific Supabase Storage Bucket.
 * <p>
 * This class implements all methods corresponding to the {@code /object} and
 * {@code /cdn} endpoint groups in the Supabase Storage API.
 * <p>
 * This client is instantiated by {@link io.github.jsupabase.storage.StorageClient#object(String)}
 * and is bound to a specific {@code bucketId} for all its operations.
 *
 * @author neilhdezs
 * @version 1.0.0
 */
public class ObjectClient extends HttpClientBase {

    /** - The path prefix for the Storage service (e.g., "/storage/v1") - **/
    private final String servicePath;

    /** - The ID/Name of the bucket this client is bound to (e.g., "avatars") - **/
    private final String bucketId;

    /**
     * Package-private constructor for the ObjectClient.
     *
     * @param config      The SupabaseConfig, used to get the service path and base URL.
     * @param bucketId    The ID (name) of the bucket this client will operate on.
     */
    public ObjectClient(SupabaseConfig config, String bucketId) {
        super(config);
        this.servicePath = config.getStoragePath();
        this.bucketId = Objects.requireNonNull(bucketId, "bucketId cannot be null");
    }

    // --- Core Object Operations ---

    /**
     * Uploads a file to the bucket.
     * <p>
     * This method constructs and executes a <strong>POST</strong> request to the
     * {@code /object/{bucketId}/{path}} endpoint. It automatically detects the
     * file's MIME type using {@link MimeTypeUtil}.
     *
     * @param path      The relative path in the bucket (e.g., "avatars/avatar1.png").
     * @param file      The local file (java.nio.file.Path) to upload.
     * @param upsert    Whether to overwrite the file if it exists. (Defaults to false).
     * @return A {@code CompletableFuture} containing the {@link UploadResponse}.
     */
    public CompletableFuture<UploadResponse> upload(String path, Path file, boolean upsert) {
        try {
            String mimeType = MimeTypeUtil.probeContentType(file);
            String fullPath = StoragePaths.getObjectPath(this.servicePath, this.bucketId, path);

            HttpRequest request = newRequest(fullPath)
                    .header("Content-Type", mimeType)
                    .header("X-Upsert", String.valueOf(upsert))
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
     * @param path      The relative path in the bucket (e.g., "avatars/avatar1.png").
     * @param file      The local file (java.nio.file.Path) to upload.
     * @param upsert    Whether to overwrite the file if it exists. (Defaults to false).
     * @return A {@code CompletableFuture} containing the {@link UploadResponse}.
     */
    public CompletableFuture<UploadResponse> update(String path, Path file, boolean upsert) {
        try {
            String mimeType = MimeTypeUtil.probeContentType(file);
            String fullPath = StoragePaths.getObjectPath(this.servicePath, this.bucketId, path);

            HttpRequest request = newRequest(fullPath)
                    .header("Content-Type", mimeType)
                    .header("X-Upsert", String.valueOf(upsert))
                    .PUT(HttpRequest.BodyPublishers.ofFile(file)) // PUT instead of POST
                    .build();

            return sendAsync(request, UploadResponse.class);

        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }

    /**
     * Downloads a file from the bucket using authentication (JWT).
     * <p>
     * This method uses the authenticated endpoint and is suitable for private buckets
     * or objects that require RLS policies.
     * <p>
     * Corresponds to: {@code GET /object/authenticated/{bucketId}/{path}}
     *
     * @param path The relative path in the bucket (e.g., "avatars/avatar1.png").
     * @return A {@code CompletableFuture} with the file's raw bytes.
     */
    public CompletableFuture<byte[]> download(String path) {
        String fullPath = StoragePaths.getObjectAuthenticatedPath(this.servicePath, this.bucketId, path);
        HttpRequest request = newRequest(fullPath).GET().build();

        return sendAsync(request, HttpResponse.BodyHandlers.ofByteArray())
                .thenApply(response -> {
                    if (response.statusCode() >= 400) {
                        throw new SupabaseException("Failed to download file: " + response.statusCode(), response.statusCode());
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
        HttpRequest request = newRequest(fullPath).DELETE().build();
        return sendAsync(request, new TypeReference<>() {});
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

        HttpRequest request = newRequest(objectPath)
                .header("Content-Type", "application/json")
                .method("DELETE", HttpRequest.BodyPublishers.ofString(jsonBody)) // DELETE with body
                .build();

        return sendAsync(request, new TypeReference<>() {});
    }

    /**
     * Lists files within a folder in the bucket using complex options.
     * <p>
     * Corresponds to: {@code POST /object/list/{id}}
     *
     * @param options The options for listing (e.g., prefix, limit, offset, search).
     * @return A {@code CompletableFuture} with a {@link List} of {@link FileObject}.
     */
    public CompletableFuture<List<FileObject>> list(ListOptions options) {
        String listPath = StoragePaths.getListPath(this.servicePath, this.bucketId);
        String jsonBody = JsonUtil.toJson(options);

        HttpRequest request = newRequest(listPath)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        return sendAsync(request, new TypeReference<>() {});
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

        HttpRequest request = newRequest(listPath)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        return sendAsync(request, new TypeReference<>() {});
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

        HttpRequest request = newRequest(path)
                .header("Content-Type", "application/json")
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

        HttpRequest request = newRequest(path)
                .header("Content-Type", "application/json")
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
     * Creates a signed URL for downloading a file.
     * <p>
     * This method executes a <strong>POST</strong> request to the
     * {@code /object/sign/{bucketId}/{path}} endpoint to generate a token.
     * The returned {@link SignedUrlResponse} contains the URL (with token)
     * that an end-user can use to trigger {@code GET /object/sign/{...}}.
     *
     * @param path      The file path (e.g., "folder/image.png").
     * @param expiresIn The number of seconds until the URL expires.
     * @return A {@code CompletableFuture} with the {@link SignedUrlResponse}.
     */
    public CompletableFuture<SignedUrlResponse> createSignedUrl(String path, int expiresIn) {
        String fullPath = StoragePaths.getSignedPath(this.servicePath, this.bucketId, path);
        String jsonBody = JsonUtil.toJson(Map.of("expiresIn", expiresIn));

        HttpRequest request = newRequest(fullPath)
                .header("Content-Type", "application/json")
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
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        return sendAsync(request, new TypeReference<>() {});
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
        String jsonBody = JsonUtil.toJson(Map.of("expiresIn", expiresIn));

        HttpRequest request = newRequest(fullPath)
                .header("Content-Type", "application/json")
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
        HttpRequest request = newRequest(fullPath).DELETE().build();
        return sendAsync(request, MessageResponse.class);
    }
}