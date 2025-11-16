package io.github.jsupabase.storage.clients;

import com.fasterxml.jackson.core.type.TypeReference;
import io.github.jsupabase.core.client.HttpClientBase;
import io.github.jsupabase.core.config.SupabaseConfig;
import io.github.jsupabase.core.util.JsonUtil;
import io.github.jsupabase.storage.StoragePaths;
import io.github.jsupabase.storage.dto.Bucket;
import io.github.jsupabase.storage.dto.MessageResponse;
import io.github.jsupabase.storage.dto.options.CreateBucketOptions;

import java.net.http.HttpRequest;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * Client for managing Supabase Storage Buckets.
 * <p>
 * This class implements all methods corresponding to the {@code /bucket}
 * endpoint group in the Supabase Storage API. It handles operations
 * such as creating, listing, updating, and deleting buckets.
 * <p>
 * This client is instantiated once by {@link io.github.jsupabase.storage.StorageClient}
 * and accessed via {@code storage.bucket()}.
 *
 * @author neilhdezs
 * @version 1.0.0
 */
public class BucketClient extends HttpClientBase {

    /** - The path prefix for the Storage service (e.g., "/storage/v1") - **/
    private final String servicePath;

    /**
     * Package-private constructor for the BucketClient.
     *
     * @param config The SupabaseConfig, used to get the service path.
     */
    public BucketClient(SupabaseConfig config) {
        super(config);
        this.servicePath = config.getStoragePath();
    }

    /**
     * Retrieves a list of all storage buckets in the project.
     * <p>
     * This method constructs and executes a <strong>GET</strong> request to the
     * {@code /bucket} endpoint, retrieving the path from
     * {@link StoragePaths#getBucketsPath(String)}.
     *
     * @return A {@code CompletableFuture} containing a {@link List} of {@link Bucket} objects.
     */
    public CompletableFuture<List<Bucket>> listBuckets() {
        String path = StoragePaths.getBucketsPath(this.servicePath);
        HttpRequest request = newRequest(path).GET().build();
        return sendAsync(request, new TypeReference<>() {
        });
    }

    /**
     * Retrieves the details of a specific bucket.
     * <p>
     * This method constructs and executes a <strong>GET</strong> request to the
     * {@code /bucket/{id}} endpoint, retrieving the path from
     * {@link StoragePaths#getBucketPath(String, String)}.
     *
     * @param bucketId The ID (name) of the bucket to retrieve.
     * @return A {@code CompletableFuture} containing the {@link Bucket} details.
     */
    public CompletableFuture<Bucket> getBucket(String bucketId) {
        String path = StoragePaths.getBucketPath(this.servicePath, bucketId);
        HttpRequest request = newRequest(path).GET().build();
        return sendAsync(request, Bucket.class);
    }

    /**
     * Creates a new storage bucket.
     * <p>
     * This method serializes the {@link CreateBucketOptions} into a JSON body
     * and executes a <strong>POST</strong> request to the {@code /bucket} endpoint.
     *
     * @param options The options for creating the bucket (e.g., name, public status).
     * @return A {@code CompletableFuture} containing the details of the newly created {@link Bucket}.
     */
    public CompletableFuture<Bucket> createBucket(CreateBucketOptions options) {
        Objects.requireNonNull(options, "CreateBucketOptions cannot be null");
        String path = StoragePaths.getBucketsPath(this.servicePath);
        String jsonBody = JsonUtil.toJson(options);

        HttpRequest request = newRequest(path)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        return sendAsync(request, Bucket.class);
    }

    /**
     * Updates the properties of an existing bucket.
     * <p>
     * This method serializes a properties map into a JSON body
     * and executes a <strong>PUT</strong> request to the {@code /bucket/{id}} endpoint.
     *
     * @param bucketId The ID (name) of the bucket to update.
     * @param options  A {@link Map} containing the properties to update (e.g., "public", "file_size_limit").
     * @return A {@code CompletableFuture} containing a success {@link MessageResponse}.
     */
    public CompletableFuture<MessageResponse> updateBucket(String bucketId, Map<String, Object> options) {
        Objects.requireNonNull(options, "Options map cannot be null");
        String path = StoragePaths.getBucketPath(this.servicePath, bucketId);
        String jsonBody = JsonUtil.toJson(options);

        HttpRequest request = newRequest(path)
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        return sendAsync(request, MessageResponse.class);
    }

    /**
     * Deletes an existing bucket.
     * <p>
     * Note: The bucket must be empty before it can be deleted.
     * This method constructs and executes a <strong>DELETE</strong> request to the
     * {@code /bucket/{id}} endpoint.
     *
     * @param bucketId The ID (name) of the bucket to delete.
     * @return A {@code CompletableFuture} containing a success {@link MessageResponse}.
     */
    public CompletableFuture<MessageResponse> deleteBucket(String bucketId) {
        String path = StoragePaths.getBucketPath(this.servicePath, bucketId);
        HttpRequest request = newRequest(path).DELETE().build();
        return sendAsync(request, MessageResponse.class);
    }

    /**
     * Removes all objects inside a single bucket (empties the bucket).
     * <p>
     * This method constructs and executes a <strong>POST</strong> request to the
     * {@code /bucket/{id}/empty} endpoint.
     *
     * @param bucketId The ID (name) of the bucket to empty.
     * @return A {@code CompletableFuture} containing a success {@link MessageResponse}.
     */
    public CompletableFuture<MessageResponse> emptyBucket(String bucketId) {
        String path = StoragePaths.getEmptyBucketPath(this.servicePath, bucketId);
        HttpRequest request = newRequest(path)
                .POST(HttpRequest.BodyPublishers.noBody()) // No body required
                .build();
        return sendAsync(request, MessageResponse.class);
    }
}