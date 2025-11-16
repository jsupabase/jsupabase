package io.github.jsupabase.platform;

import com.fasterxml.jackson.core.type.TypeReference;
import io.github.jsupabase.core.client.HttpClientBase;
import io.github.jsupabase.core.exception.SupabaseException;
import io.github.jsupabase.core.util.JsonUtil;
import io.github.jsupabase.platform.dto.organizations.OrganizationResponse;
import io.github.jsupabase.platform.dto.projects.ProjectResponse;


import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Client for the Supabase Platform (Management) API.
 * This client is separate from the project-specific SupabaseClient.
 * It uses a Personal Access Token (PAT) for authentication.
 *
 * @author neilhdezs
 * @version 0.1.2 // Version updated
 */
public class SupabasePlatformClient {

    /** - The static host for the Supabase Management API - **/
    private static final String PLATFORM_HOST = "https://api.supabase.com";

    /** - The default API version to use if none is specified - **/
    private static final String DEFAULT_VERSION = "v1";

    /** - The fully resolved base URL (e.g., "https://api.supabase.com/v1") - **/
    private final URI baseUrl;

    /** - The shared HttpClient from the 'core' module - **/
    private final HttpClient httpClient;

    /** - The Personal Access Token (PAT) - **/
    private final String personalAccessToken;

    /**
     * Creates a new client for the Supabase Platform API using the default version (v1).
     *
     * @param personalAccessToken Your Personal Access Token (from supabase.com/dashboard).
     */
    public SupabasePlatformClient(String personalAccessToken) {
        // Llama al constructor principal con la versión por defecto
        this(personalAccessToken, DEFAULT_VERSION);
    }

    /**
     * Creates a new client for the Supabase Platform API using a specific version.
     *
     * @param personalAccessToken Your Personal Access Token (from supabase.com/dashboard).
     * @param apiVersion The API version to use (e.g., "v1", "v2").
     */
    public SupabasePlatformClient(String personalAccessToken, String apiVersion) {
        if (personalAccessToken == null || personalAccessToken.isBlank()) {
            throw new IllegalArgumentException("Personal Access Token cannot be null or empty.");
        }
        if (apiVersion == null || apiVersion.isBlank()) {
            throw new IllegalArgumentException("API version cannot be null or empty.");
        }

        this.httpClient = HttpClientBase.getSharedHttpClient();
        this.personalAccessToken = personalAccessToken;

        // Construye la URL base usando la versión inyectada
        String platformPath = "/" + apiVersion.replaceAll("^/|/$", ""); // Limpia las barras
        this.baseUrl = URI.create(PLATFORM_HOST + platformPath);
    }

    /**
     * Lists all organizations your account has access to.
     * Corresponds to: GET /v1/organizations
     *
     * @return A CompletableFuture with a list of organizations.
     */
    public CompletableFuture<List<OrganizationResponse>> listOrganizations() {
        HttpRequest request = newRequest("/organizations").GET().build();
        return sendAsync(request, new TypeReference<>() {});
    }

    /**
     * Lists all projects associated with your account.
     * Corresponds to: GET /v1/projects
     *
     * @return A CompletableFuture with a list of projects.
     */
    public CompletableFuture<List<ProjectResponse>> listProjects() {
        HttpRequest request = newRequest("/projects").GET().build();
        return sendAsync(request, new TypeReference<>() {});
    }

    // (Aquí irían los demás métodos: getProject(id), createProject(), etc.)

    // --- Private Helpers ---

    /**
     * Creates a new HttpRequest.Builder pre-configured with
     * the Platform API base URL and the Personal Access Token.
     *
     * @param path El path del endpoint (ej. "/projects")
     */
    private HttpRequest.Builder newRequest(String path) {
        // Resuelve el path del endpoint contra la URL base (ahora dinámica)
        URI uri = this.baseUrl.resolve(path);

        return HttpRequest.newBuilder()
                .uri(uri)
                .header("Authorization", "Bearer " + this.personalAccessToken)
                .header("Content-Type", "application/json");
    }

    /**
     * Sends an async request and deserializes the JSON response.
     */
    private <T> CompletableFuture<T> sendAsync(HttpRequest request, TypeReference<T> typeRef) {
        return this.httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8))
                .thenApply(httpResponse -> {
                    if (httpResponse.statusCode() >= 400) {
                        throw new SupabaseException(httpResponse.body(), httpResponse.statusCode());
                    }
                    String body = httpResponse.body();
                    if (body == null || body.isEmpty()) {
                        return null;
                    }
                    return JsonUtil.fromJson(body, typeRef);
                });
    }
}