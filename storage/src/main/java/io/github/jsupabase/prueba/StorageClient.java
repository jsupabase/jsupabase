package io.github.jsupabase.prueba;

import io.github.jsupabase.core.config.SupabaseConfig;
import io.github.jsupabase.prueba.clients.BucketClient;
import io.github.jsupabase.prueba.clients.ObjectClient;
import io.github.jsupabase.prueba.clients.TransformationClient;

/**
 * - SUPABASE STORAGE API CLIENT -
 * <p>
 * Gateway client for Supabase Storage operations providing comprehensive file management
 * capabilities including bucket management, object operations, and image transformations.
 * This client implements the Gateway pattern to provide organized access to different
 * /**
 * - SUPABASE STORAGE API CLIENT -
 * <p>
 * Gateway client for Supabase Storage operations providing comprehensive file management
 * capabilities including bucket management, object operations, and image transformations.
 * This client implements the Gateway pattern to provide organized access to different
 * storage service endpoints as defined by the Supabase Storage API specification.
 * <p>
 * The client serves as a factory for specialized sub-clients that handle specific
 * operation categories: bucket lifecycle management, file upload/download/manipulation,
 * and image transformation services. Each sub-client inherits authentication context
 * and configuration from the parent gateway client.
 * <p>
 * Storage operations support both public and private buckets with fine-grained access
 * controls, file metadata management, CDN integration for optimized delivery, and
 * real-time image transformation capabilities for responsive web applications.
 *
 * @author neilhdezs
 * @version 0.1.0
 * @since 0.1.0
 */
public class StorageClient {

    /** - Immutable configuration containing storage service settings and authentication context - */
    private final SupabaseConfig config;

    /** - Specialized client for storage bucket lifecycle management and operations - */
    private final BucketClient bucketClient;


    /**
     * - STORAGE CLIENT CONSTRUCTOR -
     * <p>
     * Initializes the storage gateway client with the provided configuration and
     * creates specialized sub-clients for different storage operation categories.
     * The client inherits authentication context and service settings from the
     * provided configuration.
     *
     * @param config Supabase configuration containing storage service settings
     */
    public StorageClient(SupabaseConfig config) {
        this.config = config;
        this.bucketClient = new BucketClient(this.config);
    }

    /**
     * - BUCKET OPERATIONS CLIENT ACCESS -
     * <p>
     * Provides access to the specialized client for storage bucket management
     * operations including bucket creation, deletion, listing, and configuration.
     * Bucket operations manage the top-level containers for file storage with
     * configurable access policies and settings.
     *
     * @return BucketClient instance for bucket lifecycle management
     */
    public BucketClient bucket() {
        return this.bucketClient;
    }

    /**
     * - OBJECT OPERATIONS CLIENT FACTORY -
     * <p>
     * Creates a new specialized client for file object operations within a specific
     * storage bucket. Object operations include file upload, download, deletion,
     * metadata management, and CDN URL generation. Each object client is bound
     * to a specific bucket and inherits authentication context from the parent client.
     *
     * @param bucketId Storage bucket identifier for file operations (e.g., "avatars", "documents")
     * @return New ObjectClient instance configured for the specified bucket
     */
    public ObjectClient object(String bucketId) {
        return new ObjectClient(this.config, bucketId);
    }

    /**
     * - IMAGE TRANSFORMATION CLIENT FACTORY -
     * <p>
     * Creates a new specialized client for real-time image transformation operations
     * within a specific storage bucket. Transformation operations provide on-the-fly
     * image resizing, format conversion, quality adjustment, and optimization for
     * responsive web applications and content delivery networks.
     *
     * @param bucketId Storage bucket identifier containing images for transformation
     * @return New TransformationClient instance configured for the specified bucket
     */
    public TransformationClient transformation(String bucketId) {
        return new TransformationClient(this.config, bucketId);
    }

}