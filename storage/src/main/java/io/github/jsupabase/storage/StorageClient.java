package io.github.jsupabase.storage;

import io.github.jsupabase.core.config.SupabaseConfig;
import io.github.jsupabase.storage.clients.BucketClient;
import io.github.jsupabase.storage.clients.ObjectClient;
import io.github.jsupabase.storage.clients.TransformationClient;

/**
 * Main entry point for the Supabase Storage API.
 * <p>
 * This class acts as a <strong>Gateway</strong> (or Factory) that provides access
 * to specialized clients for each of the 6 major API endpoint groups (Bucket, Object, S3, etc.),
 * as defined by the official OAS3 specification.
 * <p>
 * It does not make any API calls itself; it only instantiates and provides
 * the clients that perform the work. It is initialized by the main {@code SupabaseClient}.
 *
 * @author neilhdezs
 * @version 1.0.0
 */
public class StorageClient {

    /** - The immutable *anonymous* configuration for this client - **/
    private final SupabaseConfig config;

    /** - Client for managing Buckets (GET/POST/PUT/DELETE /bucket) - **/
    private final BucketClient bucketClient;


    /**
     * Creates a new StorageClient (Gateway).
     *
     * @param config The client configuration.
     */
    public StorageClient(SupabaseConfig config) {
        this.config = config;
        this.bucketClient = new BucketClient(this.config);
    }

    // --- Accessors for Specialized Clients ---

    /**
     * Provides access to the client for <strong>Bucket</strong> operations.
     * <p>
     * Corresponds to the {@code /bucket} endpoint group.
     * (e.g., listBuckets, createBucket, deleteBucket).
     *
     * @return The singleton BucketClient instance.
     */
    public BucketClient bucket() {
        return this.bucketClient;
    }

    /**
     * Provides access to the client for <strong>Object</strong> operations within a specific bucket.
     * <p>
     * Corresponds to the {@code /object} and {@code /cdn} endpoint groups.
     * This is a factory method that creates a new client instance
     * bound to the specified {@code bucketId}.
     *
     * @param bucketId The ID (name) of the bucket to interact with (e.g., "avatars").
     * @return A new {@link ObjectClient} instance bound to that bucket.
     */
    public ObjectClient object(String bucketId) {
        return new ObjectClient(this.config, bucketId);
    }

    /**
     * Provides access to the client for <strong>Image Transformation</strong> operations
     * within a specific bucket.
     * <p>
     * Corresponds to the {@code /render/image} endpoint group.
     * This is a factory method that creates a new client instance
     * bound to the specified {@code bucketId}.
     *
     * @param bucketId The ID (name) of the bucket containing the images (e.g., "avatars").
     * @return A new {@link TransformationClient} instance bound to that bucket.
     */
    public TransformationClient transformation(String bucketId) {
        return new TransformationClient(this.config, bucketId);
    }

}