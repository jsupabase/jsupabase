package io.github.jsupabase.prueba;

import io.github.jsupabase.prueba.dto.options.TransformOptions;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Provides static methods for constructing API endpoint paths for Supabase Storage.
 * <p>
 * This is a stateless utility class ({@code final} with a {@code private} constructor)
 * and is the single source of truth for all API paths based on the official OAS3 specification.
 * It does not execute any HTTP requests; it only builds path and URL strings.
 * <p>
 * This class includes endpoints for the Core API (Bucket, Object, CDN, Transformation),
 * as well as S3-Compatibility, Resumable Upload (TUS), and Default (Admin) endpoints
 * for complete API coverage.
 *
 * @author neilhdezs
 * @version 0.1.0
 * @since 0.1.0
 */
public final class StoragePaths {

    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private StoragePaths() {
        throw new IllegalStateException("This is a utility class and cannot be instantiated.");
    }

    /**
     * Helper method to correctly encode path segments for URLs.
     * <p>
     * The {wildcard} in the API spec refers to a file path that can contain '/' characters.
     * Standard URLEncoder encodes '/' which would break the path.
     * This method splits the path by '/', encodes each segment individually,
     * and then rejoins them, preserving the path structure.
     *
     * @param path The raw path (e.g., "folder/image with space.png").
     * @return The correctly encoded path (e.g., "folder/image%20with%20space.png").
     */
    private static String encodePath(String path) {
        // No codificar si la ruta está vacía o es nula
        if (path == null || path.isEmpty()) {
            return "";
        }

        String[] segments = path.split("/");
        for (int i = 0; i < segments.length; i++) {
            // No codificar segmentos vacíos (ej. rutas que empiezan con /)
            if (!segments[i].isEmpty()) {
                segments[i] = URLEncoder.encode(segments[i], StandardCharsets.UTF_8).replace("+", "%20");
            }
        }
        return String.join("/", segments);
    }

    // --- I. Bucket Endpoints ---

    /**
     * Gets the API path for listing or creating buckets.
     * <p>
     * Corresponds to: {@code GET /bucket} and {@code POST /bucket}
     *
     * @param servicePath The root path of the storage service (e.g., "/storage/v1").
     * @return The API path string (e.g., "/storage/v1/bucket").
     */
    public static String getBucketsPath(String servicePath) {
        return servicePath + "/bucket";
    }

    /**
     * Gets the API path for a specific bucket.
     * <p>
     * Corresponds to: {@code GET /bucket/{id}}, {@code PUT /bucket/{id}}, {@code DELETE /bucket/{id}}
     *
     * @param servicePath The root path of the storage service.
     * @param bucketId    The ID (name) of the bucket.
     * @return The API path string (e.g., "/storage/v1/bucket/avatars").
     */
    public static String getBucketPath(String servicePath, String bucketId) {
        return servicePath + "/bucket/" + bucketId;
    }

    /**
     * Gets the API path for emptying a specific bucket.
     * <p>
     * Corresponds to: {@code POST /bucket/{id}/empty}
     *
     * @param servicePath The root path of the storage service.
     * @param bucketId    The ID (name) of the bucket.
     * @return The API path string (e.g., "/storage/v1/bucket/avatars/empty").
     */
    public static String getEmptyBucketPath(String servicePath, String bucketId) {
        return servicePath + "/bucket/" + bucketId + "/empty";
    }

    // --- II. Object Endpoints ---

    /**
     * Gets the API path for a specific object (upload, download, update, single delete).
     * <p>
     * Corresponds to: {@code GET/POST/PUT/DELETE /object/{bucketId}/{wildcard}}
     *
     * @param servicePath The root path of the storage service.
     * @param bucketId    The ID of the bucket.
     * @param path        The full path to the object (e.g., "folder/image.png").
     * @return The API path string (e.g., "/storage/v1/object/avatars/folder/image.png").
     */
    public static String getObjectPath(String servicePath, String bucketId, String path) {
        return servicePath + "/object/" + bucketId + "/" + encodePath(path);
    }

    /**
     * Gets the API path for removing multiple objects (uses a body).
     * <p>
     * Corresponds to: {@code DELETE /object/{bucketId}}
     *
     * @param servicePath The root path of the storage service.
     * @param bucketId    The ID of the bucket.
     * @return The API path string (e.g., "/storage/v1/object/avatars").
     */
    public static String getRemoveMultiplePath(String servicePath, String bucketId) {
        return servicePath + "/object/" + bucketId;
    }

    /**
     * Gets the API path for retrieving an object via authenticated GET.
     * <p>
     * Corresponds to: {@code GET /object/authenticated/{bucketName}/{wildcard}}
     *
     * @param servicePath The root path of the storage service.
     * @param bucketId    The ID of the bucket.
     * @param path        The full path to the object.
     * @return The API path string.
     */
    public static String getObjectAuthenticatedPath(String servicePath, String bucketId, String path) {
        return servicePath + "/object/authenticated/" + bucketId + "/" + encodePath(path);
    }

    /**
     * Gets the API path for creating a signed URL for uploading.
     * <p>
     * Corresponds to: {@code POST /object/upload/sign/{bucketName}/{wildcard}}
     *
     * @param servicePath The root path of the storage service.
     * @param bucketId    The ID of the bucket.
     * @param path        The full path to the object.
     * @return The API path string.
     */
    public static String getSignedUploadPath(String servicePath, String bucketId, String path) {
        return servicePath + "/object/upload/sign/" + bucketId + "/" + encodePath(path);
    }

    /**
     * Gets the API path for uploading an object via a presigned URL.
     * <p>
     * Corresponds to: {@code PUT /object/upload/sign/{bucketName}/{wildcard}}
     *
     * @param servicePath The root path of the storage service.
     * @param bucketId    The ID of the bucket.
     * @param path        The full path to the object.
     * @return The API path string.
     */
    public static String getUploadWithSignedUrlPath(String servicePath, String bucketId, String path) {
        return servicePath + "/object/upload/sign/" + bucketId + "/" + encodePath(path);
    }

    /**
     * Gets the API path for creating a single signed URL for downloading.
     * <p>
     * Corresponds to: {@code POST /object/sign/{bucketName}/{wildcard}}
     *
     * @param servicePath The root path of the storage service.
     * @param bucketId    The ID of the bucket.
     * @param path        The full path to the object.
     * @return The API path string.
     */
    public static String getSignedPath(String servicePath, String bucketId, String path) {
        return servicePath + "/object/sign/" + bucketId + "/" + encodePath(path);
    }

    /**
     * Gets the API path for retrieving an object via a presigned URL.
     * <p>
     * Corresponds to: {@code GET /object/sign/{bucketName}/{wildcard}}
     *
     * @param servicePath The root path of the storage service.
     * @param bucketId    The ID of the bucket.
     * @param path        The full path to the object.
     * @return The API path string.
     */
    public static String getRetrieveWithSignedUrlPath(String servicePath, String bucketId, String path) {
        return servicePath + "/object/sign/" + bucketId + "/" + encodePath(path);
    }

    /**
     * Gets the API path for creating multiple signed URLs for downloading.
     * <p>
     * Corresponds to: {@code POST /object/sign/{bucketName}}
     *
     * @param servicePath The root path of the storage service.
     * @param bucketId    The ID of the bucket.
     * @return The API path string.
     */
    public static String getSignedUrlsPath(String servicePath, String bucketId) {
        return servicePath + "/object/sign/" + bucketId;
    }

    /**
     * Gets the general API path for object 'move' operations.
     * <p>
     * Corresponds to: {@code POST /object/move}
     *
     * @param servicePath The root path of the storage service.
     * @return The API path string ("/storage/v1/object/move").
     */
    public static String getObjectMovePath(String servicePath) {
        return servicePath + "/object/move";
    }

    /**
     * Gets the API path for listing objects (v1).
     * <p>
     * Corresponds to: {@code POST /object/list/{bucketId}}
     *
     * @param servicePath The root path of the storage service.
     * @param bucketId    The ID of the bucket.
     * @return The API path string (e.g., "/storage/v1/object/list/avatars").
     */
    public static String getListPath(String servicePath, String bucketId) {
        return servicePath + "/object/list/" + bucketId;
    }

    /**
     * Gets the API path for listing objects (v2).
     * <p>
     * Corresponds to: {@code POST /object/list-v2/{bucketId}}
     *
     * @param servicePath The root path of the storage service.
     * @param bucketId    The ID of the bucket.
     * @return The API path string.
     */
    public static String getListV2Path(String servicePath, String bucketId) {
        return servicePath + "/object/list-v2/" + bucketId;
    }

    /**
     * Gets the API path for retrieving authenticated object metadata.
     * <p>
     * Corresponds to: {@code GET /object/info/authenticated/{bucketName}/{wildcard}}
     *
     * @param servicePath The root path of the storage service.
     * @param bucketId    The ID of the bucket.
     * @param path        The full path to the object.
     * @return The API path string.
     */
    public static String getObjectInfoAuthPath(String servicePath, String bucketId, String path) {
        return servicePath + "/object/info/authenticated/" + bucketId + "/" + encodePath(path);
    }

    /**
     * Gets the API path for retrieving public object metadata.
     * <p>
     * Corresponds to: {@code GET /object/info/public/{bucketName}/{wildcard}}
     * (Note: {@code GET /object/info/{bucketId}/{wildcard}} from doc is likely an alias for this)
     *
     * @param servicePath The root path of the storage service.
     * @param bucketId    The ID of the bucket.
     * @param path        The full path to the object.
     * @return The API path string.
     */
    public static String getObjectInfoPublicPath(String servicePath, String bucketId, String path) {
        return servicePath + "/object/info/public/" + bucketId + "/" + encodePath(path);
    }

    /**
     * Gets the general API path for object 'copy' operations.
     * <p>
     * Corresponds to: {@code POST /object/copy}
     *
     * @param servicePath The root path of the storage service.
     * @return The API path string ("/storage/v1/object/copy").
     */
    public static String getObjectCopyPath(String servicePath) {
        return servicePath + "/object/copy";
    }

    /**
     * Retrieves the public URL for an asset in a public bucket.
     * <p>
     * Corresponds to: {@code GET /object/public/{bucketId}/{wildcard}}
     *
     * @param baseUrl     The base URL of the Supabase project.
     * @param servicePath The root path of the storage service.
     * @param bucketId    The ID of the bucket.
     * @param path        The path and name of the file.
     * @return The full, absolute public URL as a String.
     */
    public static String getPublicUrl(URI baseUrl, String servicePath, String bucketId, String path) {
        String fullPath = servicePath + "/object/public/" + bucketId + "/" + encodePath(path);
        return baseUrl.resolve(fullPath).toString();
    }

    // --- III. CDN Endpoints ---

    /**
     * Gets the API path for purging the CDN cache for an object.
     * <p>
     * Corresponds to: {@code DELETE /cdn/{bucketName}/{wildcard}}
     *
     * @param servicePath The root path of the storage service.
     * @param bucketId    The ID of the bucket.
     * @param path        The full path to the object.
     * @return The API path string (e.g., "/storage/v1/cdn/avatars/folder/image.png").
     */
    public static String getPurgeCachePath(String servicePath, String bucketId, String path) {
        return servicePath + "/cdn/" + bucketId + "/" + encodePath(path);
    }

    // --- IV. Transformation Endpoints ---

    /**
     * Retrieves the public URL for a transformed image.
     * <p>
     * Corresponds to: {@code GET /render/image/public/{bucketId}/{wildcard}}
     *
     * @param baseUrl     The base URL of the Supabase project.
     * @param servicePath The root path of the storage service.
     * @param bucketId    The ID of the bucket.
     * @param path        The path to the image.
     * @param options     The transformation options (width, height, etc.).
     * @return The full, absolute public URL for the transformed image.
     */
    public static String getPublicRenderUrl(URI baseUrl, String servicePath, String bucketId, String path, TransformOptions options) {
        String fullPath = servicePath + "/render/image/public/" + bucketId + "/" + encodePath(path);
        String baseUrlString = baseUrl.resolve(fullPath).toString();

        String queryParams = (options != null) ? options.toQueryParameters() : "";
        // toQueryParameters() already includes the '?' if params exist
        return baseUrlString + queryParams;
    }

    /**
     * Gets the API path for retrieving an authenticated transformed image.
     * <p>
     * Corresponds to: {@code GET /render/image/authenticated/{bucketName}/{wildcard}}
     *
     * @param servicePath The root path of the storage service.
     * @param bucketId    The ID of the bucket.
     * @param path        The path to the image.
     * @return The API path string.
     */
    public static String getAuthenticatedRenderPath(String servicePath, String bucketId, String path) {
        return servicePath + "/render/image/authenticated/" + bucketId + "/" + encodePath(path);
    }

    /**
     * Gets the API path for a signed transformed image.
     * <p>
     * Corresponds to: {@code GET /render/image/sign/{bucketName}/{wildcard}}
     * (Note: This path is for *retrieving* the image with a token, not creating the signed URL)
     *
     * @param servicePath The root path of the storage service.
     * @param bucketId    The ID of the bucket.
     * @param path        The path to the image.
     * @return The API path string.
     */
    public static String getSignedRenderPath(String servicePath, String bucketId, String path) {
        return servicePath + "/render/image/sign/" + bucketId + "/" + encodePath(path);
    }

    // --- V. S3-Compatibility Endpoints ---

    /**
     * Gets the API path for S3 service (List Buckets).
     * <p>
     * Corresponds to: {@code GET /s3/}
     */
    public static String getS3ServicePath(String servicePath) {
        return servicePath + "/s3/";
    }

    /**
     * Gets the API path for S3 bucket operations (no trailing slash).
     * <p>
     * Corresponds to: {@code GET/PUT/POST/DELETE /s3/{Bucket}}
     */
    public static String getS3BucketPath(String servicePath, String bucketId) {
        return servicePath + "/s3/" + bucketId;
    }

    /**
     * Gets the API path for S3 bucket operations (with trailing slash).
     * <p>
     * Corresponds to: {@code GET/PUT/POST/DELETE /s3/{Bucket}/}
     */
    public static String getS3BucketPathSlash(String servicePath, String bucketId) {
        return servicePath + "/s3/" + bucketId + "/";
    }

    /**
     * Gets the API path for S3 object operations.
     * <p>
     * Corresponds to: {@code GET/PUT/POST/DELETE /s3/{Bucket}/{wildcard}}
     */
    public static String getS3ObjectPath(String servicePath, String bucketId, String path) {
        return servicePath + "/s3/" + bucketId + "/" + encodePath(path);
    }

    // --- VI. Resumable (TUS) Endpoints ---

    /**
     * Gets the API path for TUS resumable uploads (no wildcard).
     * <p>
     * Corresponds to: {@code POST/OPTIONS /upload/resumable/}
     */
    public static String getResumableUploadPath(String servicePath) {
        return servicePath + "/upload/resumable/";
    }

    /**
     * Gets the API path for TUS resumable uploads (with wildcard).
     * <p>
     * Corresponds to: {@code POST/PUT/PATCH/DELETE/OPTIONS /upload/resumable/{wildcard}}
     */
    public static String getResumableUploadWildcardPath(String servicePath, String wildcard) {
        return servicePath + "/upload/resumable/" + encodePath(wildcard);
    }

    /**
     * Gets the API path for TUS signed resumable uploads (no wildcard).
     * <p>
     * Corresponds to: {@code POST/OPTIONS /upload/resumable/sign/}
     */
    public static String getResumableSignedUploadPath(String servicePath) {
        return servicePath + "/upload/resumable/sign/";
    }

    /**
     * Gets the API path for TUS signed resumable uploads (with wildcard).
     * <p>
     * Corresponds to: {@code POST/PUT/PATCH/DELETE/OPTIONS /upload/resumable/sign/{wildcard}}
     */
    public static String getResumableSignedUploadWildcardPath(String servicePath, String wildcard) {
        return servicePath + "/upload/resumable/sign/" + encodePath(wildcard);
    }

    // --- VII. Default (Admin/Platform) Endpoints ---

    /**
     * Gets the API path for metrics.
     * <p>
     * Corresponds to: {@code GET /metrics}
     */
    public static String getMetricsPath(String servicePath) {
        return servicePath + "/metrics";
    }

    /**
     * Gets the API path for health checks.
     * <p>
     * Corresponds to: {@code GET /health}
     */
    public static String getHealthPath(String servicePath) {
        return servicePath + "/health";
    }
}