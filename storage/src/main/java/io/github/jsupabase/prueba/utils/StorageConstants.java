package io.github.jsupabase.prueba.utils;

/**
 * - STORAGE CONSTANTS -
 * <p>
 * Centralized constants for headers, payload keys, log messages and defaults
 * used across the Storage module.
 *
 * @author neilhdezs
 * @version 0.1.0
 * @since 0.1.0
 */
public final class StorageConstants {

    private StorageConstants() {}

    /** - HTTP header Content-Type - **/
    public static final String HDR_CONTENT_TYPE = "Content-Type";
    /** - HTTP header X-Upsert for object uploads - **/
    public static final String HDR_X_UPSERT = "X-Upsert";

    /** - MIME type for JSON - **/
    public static final String MIME_JSON = "application/json";
    /** - MIME default when unknown - **/
    public static final String MIME_OCTET_STREAM = "application/octet-stream";

    /** - JSON key for expiresIn when creating signed URLs - **/
    public static final String JSON_EXPIRES_IN = "expiresIn";

    /** - Log: uploading file to path - **/
    public static final String LOG_UPLOAD_PATH = "Uploading object to path: {}";
    /** - Log: updating file to path - **/
    public static final String LOG_UPDATE_PATH = "Updating object at path: {}";
    /** - Log: download starting - **/
    public static final String LOG_DOWNLOAD_PATH = "Downloading object from path: {}";
    /** - Log: remove single path - **/
    public static final String LOG_REMOVE_SINGLE = "Removing object at path: {}";
    /** - Log: remove multiple count - **/
    public static final String LOG_REMOVE_MULTIPLE = "Removing {} objects";
    /** - Log: list with prefix - **/
    public static final String LOG_LIST_PREFIX = "Listing objects with prefix: {}";
    /** - Log: move file - **/
    public static final String LOG_MOVE_FILE = "Moving object from {} to {}";
    /** - Log: copy file - **/
    public static final String LOG_COPY_FILE = "Copying object from {} to {}";
    /** - Log: purge cache - **/
    public static final String LOG_PURGE_CACHE = "Purging CDN cache for path: {}";

    /** - Error: HTTP download failure - **/
    public static final String ERROR_DOWNLOAD_FAILED = "Failed to download file: {}";
}
