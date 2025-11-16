package io.github.jsupabase.platform.utils;

/**
 * - PLATFORM CONSTANTS -
 * <p>
 * Centralized constants for headers, endpoints, and log messages
 * used across the Platform Management API module.
 *
 * @author neilhdezs
 * @version 0.1.0
 * @since 0.1.0
 */
public final class PlatformConstants {

    private PlatformConstants() {}

    /** - Platform API host URL - **/
    public static final String PLATFORM_HOST = "https://api.supabase.com";

    /** - Default API version - **/
    public static final String DEFAULT_VERSION = "v1";

    /** - HTTP header Authorization - **/
    public static final String HDR_AUTHORIZATION = "Authorization";
    /** - HTTP header Content-Type - **/
    public static final String HDR_CONTENT_TYPE = "Content-Type";

    /** - MIME type for JSON - **/
    public static final String MIME_JSON = "application/json";

    /** - Authorization Bearer prefix - **/
    public static final String AUTH_BEARER = "Bearer ";

    /** - Endpoint: organizations - **/
    public static final String ENDPOINT_ORGANIZATIONS = "/organizations";
    /** - Endpoint: projects - **/
    public static final String ENDPOINT_PROJECTS = "/projects";
    /** - Endpoint: functions - **/
    public static final String ENDPOINT_FUNCTIONS = "/functions";
    /** - Endpoint: secrets - **/
    public static final String ENDPOINT_SECRETS = "/secrets";
    /** - Endpoint: database config - **/
    public static final String ENDPOINT_DATABASE_CONFIG = "/database/config";
    /** - Endpoint: network bans - **/
    public static final String ENDPOINT_NETWORK_BANS = "/network-bans";
    /** - Endpoint: SSO providers - **/
    public static final String ENDPOINT_SSO_PROVIDERS = "/sso-providers";

    /** - Log: Listing organizations - **/
    public static final String LOG_LIST_ORGANIZATIONS = "Listing organizations";
    /** - Log: Listing projects - **/
    public static final String LOG_LIST_PROJECTS = "Listing projects";
    /** - Log: Getting project - **/
    public static final String LOG_GET_PROJECT = "Getting project: {}";
    /** - Log: Creating project - **/
    public static final String LOG_CREATE_PROJECT = "Creating project in organization: {}";
    /** - Log: Deleting project - **/
    public static final String LOG_DELETE_PROJECT = "Deleting project: {}";

    /** - Error: Invalid PAT - **/
    public static final String ERROR_INVALID_PAT = "Personal Access Token cannot be null or empty";
    /** - Error: Invalid API version - **/
    public static final String ERROR_INVALID_VERSION = "API version cannot be null or empty";
    /** - Error: Request failed - **/
    public static final String ERROR_REQUEST_FAILED = "Platform API request failed: {}";
}

