package io.github.jsupabase.platform;

import com.fasterxml.jackson.core.type.TypeReference;
import io.github.jsupabase.core.client.HttpClientBase;
import io.github.jsupabase.core.exceptions.PlatformException;
import io.github.jsupabase.core.util.JsonUtil;
import io.github.jsupabase.platform.dto.organizations.OrganizationResponse;
import io.github.jsupabase.platform.dto.projects.ProjectResponse;
import io.github.jsupabase.platform.utils.PlatformConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * - SUPABASE PLATFORM CLIENT -
 * <p>
 * Management API client for Supabase Platform operations including organization management,
 * project provisioning, function deployment, and infrastructure configuration.
 * and requires a Personal Access Token (PAT) for authentication.
 * <p>
 * The Platform API provides administrative capabilities for managing Supabase projects,
 * organizations, databases, Edge Functions, secrets, and network configurations programmatically.
 * This is essential for Infrastructure-as-Code workflows, CI/CD pipelines, and multi-tenant
 * SaaS applications that dynamically provision Supabase resources.
 *
 * <h3>Authentication:</h3>
 * Uses Personal Access Token (PAT) obtained from the Supabase Dashboard rather than project-level
 * API keys. The PAT grants access to all organizations and projects associated with your account.
 *
 * <h3>Use Cases:</h3>
 * <ul>
 * <li>Automated project provisioning for multi-tenant applications</li>
 * <li>Infrastructure-as-Code deployments with Terraform/Pulumi</li>
 * <li>CI/CD pipelines for Edge Functions and database migrations</li>
 * <li>Programmatic backup and disaster recovery management</li>
 * <li>Organization and team management automation</li>
 * </ul>
 *
 * @author neilhdezs
 * @version 0.1.0
 * @since 0.1.0
 */
public class SupabasePlatformClient {

    /** - Logger for Platform API operations - **/
    private static final Logger LOGGER = LoggerFactory.getLogger(SupabasePlatformClient.class);

    /** - The fully resolved base URL (e.g., "https://api.supabase.com/v1") - **/
    private final URI baseUrl;

    /** - The shared HttpClient from the core module - **/
    private final HttpClient httpClient;

    /** - The Personal Access Token (PAT) for Platform API authentication - **/
    private final String personalAccessToken;

    /**
     * - PLATFORM CLIENT CONSTRUCTOR (DEFAULT VERSION) -
     * <p>
     * Creates a new Platform API client using the default API version (v1).
     * The client authenticates using a Personal Access Token obtained from
     * the Supabase Dashboard Settings page.
     *
     * @param personalAccessToken Personal Access Token from supabase.com/dashboard/account/tokens
     * @throws PlatformException if PAT is null or empty
     */
    public SupabasePlatformClient(String personalAccessToken) {
        this(personalAccessToken, PlatformConstants.DEFAULT_VERSION);
    }

    /**
     * - PLATFORM CLIENT CONSTRUCTOR (CUSTOM VERSION) -
     * <p>
     * Creates a new Platform API client using a specific API version.
     * Allows targeting different API versions for compatibility or testing purposes.
     *
     * @param personalAccessToken Personal Access Token for authentication
     * @param apiVersion API version to use (e.g., "v1", "v2")
     * @throws PlatformException if PAT or version is invalid
     */
    public SupabasePlatformClient(String personalAccessToken, String apiVersion) {
        if (personalAccessToken == null || personalAccessToken.isBlank()) {
            throw new PlatformException(PlatformConstants.ERROR_INVALID_PAT);
        }
        if (apiVersion == null || apiVersion.isBlank()) {
            throw new PlatformException(PlatformConstants.ERROR_INVALID_VERSION);
        }

        this.httpClient = HttpClientBase.getSharedHttpClient();
        this.personalAccessToken = personalAccessToken;

        String platformPath = "/" + apiVersion.replaceAll("^/|/$", "");
        this.baseUrl = URI.create(PlatformConstants.PLATFORM_HOST + platformPath);

        LOGGER.debug("Platform client initialized with version: {}", apiVersion);
    }

    /**
     * - LIST ORGANIZATIONS -
     * <p>
     * Retrieves all organizations your account has access to. Organizations are the
     * top-level grouping entity in Supabase that contain projects, team members, and
     * billing information.
     * <p>
     * REST endpoint: {@code GET /v1/organizations}
     *
     * @return CompletableFuture with list of organizations accessible by this PAT
     */
    public CompletableFuture<List<OrganizationResponse>> listOrganizations() {
        LOGGER.debug(PlatformConstants.LOG_LIST_ORGANIZATIONS);
        HttpRequest request = newRequest(PlatformConstants.ENDPOINT_ORGANIZATIONS).GET().build();
        return sendAsync(request, new TypeReference<>() {});
    }

    /**
     * - LIST PROJECTS -
     * <p>
     * Retrieves all projects associated with your account across all organizations.
     * Each project represents a Supabase instance with its own database, authentication,
     * storage, and Edge Functions.
     * <p>
     * REST endpoint: {@code GET /v1/projects}
     *
     * @return CompletableFuture with list of all accessible projects
     */
    public CompletableFuture<List<ProjectResponse>> listProjects() {
        LOGGER.debug(PlatformConstants.LOG_LIST_PROJECTS);
        HttpRequest request = newRequest(PlatformConstants.ENDPOINT_PROJECTS).GET().build();
        return sendAsync(request, new TypeReference<>() {});
    }

    // (Aquí irían los demás métodos: getProject(id), createProject(), etc.)

    // --- Private Helpers ---

    /**
     * - HTTP REQUEST BUILDER -
     * <p>
     * Creates a pre-configured HTTP request builder with the Platform API base URL
     * and authentication headers. All requests use Bearer token authentication with
     * the Personal Access Token and JSON content type.
     *
     * @param path Endpoint path (e.g., "/projects", "/organizations")
     * @return HttpRequest.Builder configured for Platform API
     */
    private HttpRequest.Builder newRequest(String path) {
        URI uri = this.baseUrl.resolve(path);

        return HttpRequest.newBuilder()
                .uri(uri)
                .header(PlatformConstants.HDR_AUTHORIZATION, PlatformConstants.AUTH_BEARER + this.personalAccessToken)
                .header(PlatformConstants.HDR_CONTENT_TYPE, PlatformConstants.MIME_JSON);
    }

    /**
     * - ASYNC REQUEST EXECUTOR -
     * <p>
     * Sends an asynchronous HTTP request and deserializes the JSON response into
     * the specified type. Handles HTTP error status codes by throwing PlatformException
     * with the error response body and status code.
     *
     * @param request HTTP request to execute
     * @param typeRef Jackson TypeReference for deserializing response
     * @param <T> Response type
     * @return CompletableFuture with deserialized response or null for empty bodies
     * @throws PlatformException if HTTP status code >= 400
     */
    private <T> CompletableFuture<T> sendAsync(HttpRequest request, TypeReference<T> typeRef) {
        return this.httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8))
                .thenApply(httpResponse -> {
                    if (httpResponse.statusCode() >= 400) {
                        LOGGER.error(PlatformConstants.ERROR_REQUEST_FAILED, httpResponse.body());
                        throw new PlatformException(httpResponse.body(), httpResponse.statusCode());
                    }
                    String body = httpResponse.body();
                    if (body == null || body.isEmpty()) {
                        return null;
                    }
                    return JsonUtil.fromJson(body, typeRef);
                });
    }
}