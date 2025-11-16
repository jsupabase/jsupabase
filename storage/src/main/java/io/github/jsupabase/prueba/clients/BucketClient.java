package io.github.jsupabase.prueba.clients;

import com.fasterxml.jackson.core.type.TypeReference;
import io.github.jsupabase.core.client.HttpClientBase;
import io.github.jsupabase.core.config.SupabaseConfig;
import io.github.jsupabase.core.util.JsonUtil;
import io.github.jsupabase.prueba.StoragePaths;
import io.github.jsupabase.prueba.dto.Bucket;
import io.github.jsupabase.prueba.dto.MessageResponse;
import io.github.jsupabase.prueba.dto.options.CreateBucketOptions;
import io.github.jsupabase.prueba.utils.StorageConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.http.HttpRequest;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * - BUCKET CLIENT -
 * <p>
 * Manages bucket-level operations in Supabase Storage. Buckets are top-level containers
 * that hold objects (files) and define access policies, size limits, and allowed MIME types.
 * This client provides the complete lifecycle management for buckets including creation,
 * retrieval, updates, and deletion operations.
 *
 * <h3>Architecture Role:</h3>
 * This client is instantiated once by {@link io.github.jsupabase.prueba.StorageClient} and
 * cached for the lifetime of the storage client. All bucket operations are performed through
 * this single instance, which inherits authentication context from {@code HttpClientBase}.
 *
 * <h3>Bucket Lifecycle:</h3>
 * <pre>
 * 1. Create bucket with policies (public/private, size limits, MIME restrictions)
 * 2. List/Get bucket details and metadata
 * 3. Update bucket configuration (change policies, limits)
 * 4. Empty bucket (remove all objects)
 * 5. Delete bucket (must be empty first)
 * </pre>
 *
 * <h3>Use Cases:</h3>
 * <ul>
 * <li><b>Multi-tenant applications</b>: Create dedicated buckets per tenant for isolation</li>
 * <li><b>Content management</b>: Separate buckets for images, documents, videos</li>
 * <li><b>Access control</b>: Public buckets for assets, private buckets for user data</li>
 * <li><b>Resource management</b>: Size limits and MIME restrictions per bucket</li>
 * </ul>
 *
 * @author neilhdezs
 * @version 0.1.0
 * @since 0.1.0
 */
public class BucketClient extends HttpClientBase {

    /** - Logger for bucket operations - **/
    private static final Logger LOGGER = LoggerFactory.getLogger(BucketClient.class);

    /** - The path prefix for the Storage service (e.g., "/storage/v1") - **/
    private final String servicePath;

    /**
     * - BUCKET CLIENT CONSTRUCTOR -
     * <p>
     * Initializes the bucket client with shared configuration and service path.
     * This constructor is package-private and called exclusively by {@link io.github.jsupabase.prueba.StorageClient}
     * during initialization, ensuring a single instance per storage client lifecycle.
     * The client inherits authentication state (API key and JWT) from {@code HttpClientBase}.
     *
     * @param config Supabase configuration containing storage service path and authentication
     */
    public BucketClient(SupabaseConfig config) {
        super(config);
        this.servicePath = config.getStoragePath();
    }

    /**
     * - LIST ALL BUCKETS -
     * <p>
     * Retrieves all storage buckets accessible with the current authentication credentials.
     * The returned list includes bucket metadata such as ID, name, owner, public status,
     * creation timestamps, and configured policies (size limits, MIME restrictions).
     * <p>
     * This operation is useful for:
     * <ul>
     * <li>Dashboard views showing all available storage containers</li>
     * <li>Administrative tools managing multi-tenant bucket allocation</li>
     * <li>Validation of bucket existence before object operations</li>
     * <li>Auditing bucket policies and access controls</li>
     * </ul>
     * <p>
     * REST endpoint: {@code GET /storage/v1/bucket}
     *
     * @return CompletableFuture containing list of all accessible buckets with full metadata
     */
    public CompletableFuture<List<Bucket>> listBuckets() {
        String path = StoragePaths.getBucketsPath(this.servicePath);
        LOGGER.debug("Listing buckets at path: {}", path);
        HttpRequest request = newRequest(path).GET().build();
        return sendAsync(request, new TypeReference<>() {
        });
    }

    /**
     * - GET BUCKET DETAILS -
     * <p>
     * Retrieves detailed metadata for a specific bucket by ID. Returns comprehensive
     * information including configuration policies, access controls, size limits, allowed
     * MIME types, creation timestamps, and ownership details.
     * <p>
     * Common use cases:
     * <ul>
     * <li>Verifying bucket existence before operations</li>
     * <li>Checking public/private status for URL generation logic</li>
     * <li>Auditing bucket policies and restrictions</li>
     * <li>Displaying bucket details in admin interfaces</li>
     * </ul>
     * <p>
     * REST endpoint: {@code GET /storage/v1/bucket/{id}}
     *
     * @param bucketId Unique identifier of the bucket (e.g., "avatars", "documents")
     * @return CompletableFuture with bucket metadata or error if not found/accessible
     */
    public CompletableFuture<Bucket> getBucket(String bucketId) {
        String path = StoragePaths.getBucketPath(this.servicePath, bucketId);

        HttpRequest request = newRequest(path).GET().build();

        return sendAsync(request, Bucket.class);
    }

    /**
     * - CREATE NEW BUCKET -
     * <p>
     * Creates a new storage bucket with specified policies and restrictions. Buckets act
     * as top-level containers for objects and define access controls, size limits, and
     * allowed file types that apply to all objects within them.
     * <p>
     * Configuration options include:
     * <ul>
     * <li><b>Public/Private</b>: Public buckets allow unauthenticated access via public URLs</li>
     * <li><b>File size limit</b>: Maximum size per object (prevents abuse)</li>
     * <li><b>Allowed MIME types</b>: Whitelist of acceptable content types</li>
     * </ul>
     * <p>
     * Best practices:
     * <ul>
     * <li>Keep buckets private by default, use signed URLs for secure sharing</li>
     * <li>Set appropriate size limits to prevent storage abuse</li>
     * <li>Use MIME type restrictions to ensure content consistency</li>
     * <li>Name buckets descriptively (e.g., "user-avatars", "invoices-2024")</li>
     * </ul>
     * <p>
     * REST endpoint: {@code POST /storage/v1/bucket}
     *
     * @param options Bucket creation configuration (name, public status, policies)
     * @return CompletableFuture with created bucket metadata including generated ID
     */
    public CompletableFuture<Bucket> createBucket(CreateBucketOptions options) {
        Objects.requireNonNull(options, "CreateBucketOptions cannot be null");
        String path = StoragePaths.getBucketsPath(this.servicePath);
        String jsonBody = JsonUtil.toJson(options);
        LOGGER.debug("Creating bucket at path: {}", path);
        HttpRequest request = newRequest(path)
                .header(StorageConstants.HDR_CONTENT_TYPE, StorageConstants.MIME_JSON)
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();
        return sendAsync(request, Bucket.class);
    }

    /**
     * - UPDATE BUCKET CONFIGURATION -
     * <p>
     * Updates bucket policies and restrictions dynamically without recreating the bucket.
     * This allows runtime adjustments to access controls, size limits, and MIME type
     * restrictions based on changing application requirements.
     * <p>
     * Updatable properties:
     * <ul>
     * <li><b>public</b>: Toggle between public and private access</li>
     * <li><b>file_size_limit</b>: Adjust maximum object size</li>
     * <li><b>allowed_mime_types</b>: Modify content type whitelist</li>
     * </ul>
     * <p>
     * Common scenarios:
     * <ul>
     * <li>Switching from private to public after content review</li>
     * <li>Tightening size limits after detecting abuse</li>
     * <li>Expanding MIME types to support new content formats</li>
     * </ul>
     * <p>
     * REST endpoint: {@code PUT /storage/v1/bucket/{id}}
     *
     * @param bucketId Unique identifier of the bucket to update
     * @param options Map of properties to update (public, file_size_limit, allowed_mime_types)
     * @return CompletableFuture with success confirmation message
     */
    public CompletableFuture<MessageResponse> updateBucket(String bucketId, Map<String, Object> options) {
        Objects.requireNonNull(options, "Options map cannot be null");
        String path = StoragePaths.getBucketPath(this.servicePath, bucketId);
        String jsonBody = JsonUtil.toJson(options);
        LOGGER.debug("Updating bucket {} via path: {}", bucketId, path);
        HttpRequest request = newRequest(path)
                .header(StorageConstants.HDR_CONTENT_TYPE, StorageConstants.MIME_JSON)
                .PUT(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();
        return sendAsync(request, MessageResponse.class);
    }

    /**
     * - DELETE BUCKET -
     * <p>
     * Permanently removes a storage bucket and its configuration. This operation is
     * <strong>irreversible</strong> and requires the bucket to be empty before deletion.
     * If the bucket contains objects, the operation will fail with a 400 error.
     * <p>
     * Deletion workflow:
     * <ol>
     * <li>Ensure bucket is empty (use {@link #emptyBucket(String)} or delete objects individually)</li>
     * <li>Call deleteBucket to remove the container</li>
     * <li>Verify deletion success via response or subsequent getBucket (404 expected)</li>
     * </ol>
     * <p>
     * Use cases:
     * <ul>
     * <li>Cleanup after tenant/project deletion in multi-tenant applications</li>
     * <li>Removing test/staging buckets in development workflows</li>
     * <li>Resource deallocation in temporary storage scenarios</li>
     * </ul>
     * <p>
     * REST endpoint: {@code DELETE /storage/v1/bucket/{id}}
     *
     * @param bucketId Unique identifier of the bucket to delete (must be empty)
     * @return CompletableFuture with success confirmation or error if bucket not empty
     */
    public CompletableFuture<MessageResponse> deleteBucket(String bucketId) {
        String path = StoragePaths.getBucketPath(this.servicePath, bucketId);
        LOGGER.debug("Deleting bucket {} via path: {}", bucketId, path);
        HttpRequest request = newRequest(path).DELETE().build();
        return sendAsync(request, MessageResponse.class);
    }

    /**
     * - EMPTY BUCKET -
     * <p>
     * Removes all objects from a bucket in a single atomic operation. This is the most
     * efficient way to clear bucket contents and is <strong>irreversible</strong> - deleted
     * objects cannot be recovered. The bucket itself remains intact with its configuration.
     * <p>
     * This operation is significantly faster than deleting objects individually because:
     * <ul>
     * <li>Single server-side batch operation vs. multiple round trips</li>
     * <li>Optimized database transaction handling</li>
     * <li>No need to list objects before deletion</li>
     * </ul>
     * <p>
     * Common scenarios:
     * <ul>
     * <li>Preparing bucket for deletion (required prerequisite)</li>
     * <li>Clearing temporary/cache buckets periodically</li>
     * <li>Resetting test environments between runs</li>
     * <li>Implementing data retention policies (e.g., purge after 90 days)</li>
     * </ul>
     * <p>
     * REST endpoint: {@code POST /storage/v1/bucket/{id}/empty}
     *
     * @param bucketId Unique identifier of the bucket to empty (all objects will be deleted)
     * @return CompletableFuture with success confirmation indicating objects removed
     */
    public CompletableFuture<MessageResponse> emptyBucket(String bucketId) {
        String path = StoragePaths.getEmptyBucketPath(this.servicePath, bucketId);
        LOGGER.debug("Emptying bucket {} via path: {}", bucketId, path);
        HttpRequest request = newRequest(path)
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();

        return sendAsync(request, MessageResponse.class);
    }
}