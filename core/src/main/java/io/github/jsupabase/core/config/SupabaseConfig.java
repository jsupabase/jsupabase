package io.github.jsupabase.core.config;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * - SUPABASE CLIENT CONFIGURATION -
 * <p>
 * Immutable configuration container for Supabase client instances that stores connection
 * parameters, authentication keys, HTTP headers, and service endpoint paths. This class
 * uses the Builder pattern to ensure proper configuration validation and provides
 * thread-safe access to configuration values across all SDK modules.
 * <p>
 * The configuration supports both anonymous and authenticated modes, with automatic
 * header management for API key authentication and JWT token authorization. Service
 * paths can be customized to support different API versions or custom Supabase deployments.
 *
 * @author neilhdezs
 * @version 0.1.0
 * @since 0.1.0
 */
public final class SupabaseConfig {

    /** - Base URL of the Supabase project for all API communications - */
    private final URI supabaseUrl;

    /** - API key for authentication with Supabase services (anon or service_role) - */
    private final String supabaseKey;

    /** - HTTP headers map for request customization including authentication and API key headers - */
    private final Map<String, String> headers;

    /** - Database schema name for PostgreSQL operations (defaults to 'public') - */
    private final String schema;

    /** - Authentication service endpoint path for GoTrue API operations - */
    private final String authPath;

    /** - PostgreSQL REST service endpoint path for database operations - */
    private final String postgrestPath;

    /** - Storage service endpoint path for file management operations - */
    private final String storagePath;

    /** - Real-time service endpoint path for WebSocket subscriptions - */
    private final String realtimePath;


    /**
     * - PRIVATE CONSTRUCTOR -
     * <p>
     * Creates an immutable configuration instance with all required parameters.
     * Use the Builder pattern for public instantiation to ensure proper validation.
     */
    private SupabaseConfig(URI supabaseUrl, String supabaseKey, Map<String, String> headers, String schema, String authPath, String postgrestPath, String storagePath, String realtimePath) {
        this.supabaseUrl = supabaseUrl;
        this.supabaseKey = supabaseKey;
        this.headers = headers;
        this.schema = schema;
        this.authPath = authPath;
        this.postgrestPath = postgrestPath;
        this.storagePath = storagePath;
        this.realtimePath = realtimePath;
    }

    /** - Returns the base URL of the Supabase project for API endpoint construction - */
    public URI getSupabaseUrl() {
        return supabaseUrl;
    }

    /** - Returns the API key used for authentication with Supabase services - */
    public String getSupabaseKey() {
        return supabaseKey;
    }

    /** - Returns an immutable copy of HTTP headers for request customization - */
    public Map<String, String> getHeaders() {
        return Map.copyOf(headers);
    }

    /** - Returns the database schema name for PostgreSQL operations - */
    public String getSchema() {
        return schema;
    }

    /** - Returns the authentication service endpoint path for GoTrue API operations - */
    public String getAuthPath() {
        return authPath;
    }

    /** - Returns the PostgreSQL REST service endpoint path for database operations - */
    public String getPostgrestPath() {
        return postgrestPath;
    }

    /** - Returns the storage service endpoint path for file management operations - */
    public String getStoragePath() {
        return storagePath;
    }

    /** - Returns the real-time service endpoint path for WebSocket subscriptions - */
    public String getRealtimePath() {
        return realtimePath;
    }

    /**
     * - URL RESOLUTION UTILITY -
     * <p>
     * Resolves a relative path against the base Supabase URL to create complete endpoint URLs.
     * This utility method simplifies URL construction for service-specific API calls across
     * all SDK modules by providing consistent URL resolution behavior.
     *
     * @param path Relative path string that must start with '/' (e.g., "/rest/v1/table")
     * @return Complete resolved URI combining base URL with the provided path
     */
    public URI resolveUrl(String path) {
        return this.supabaseUrl.resolve(path);
    }


    /**
     * - CONFIGURATION BUILDER -
     * <p>
     * Builder pattern implementation for creating immutable SupabaseConfig instances
     * with proper validation and default values. Provides a fluent API for setting
     * configuration parameters while ensuring required fields are properly initialized
     * and optional parameters have sensible defaults.
     */
    public static class Builder {
        /** - Base URL for the Supabase project API - */
        private final URI supabaseUrl;

        /** - API key for authentication with Supabase services - */
        private final String supabaseKey;

        /** - Mutable headers map for request customization during builder configuration - */
        private final Map<String, String> headers = new HashMap<>();

        /** - Database schema name with default value for PostgreSQL operations - */
        private String schema = "public";

        /** - Authentication service path with default API version - */
        private String authPath = "/auth/v1";

        /** - PostgreSQL REST service path with default API version - */
        private String postgrestPath = "/rest/v1";

        /** - Storage service path with default API version - */
        private String storagePath = "/storage/v1";

        /** - Real-time service path with default API version - */
        private String realtimePath = "/realtime/v1";

        /**
         * - BUILDER CONSTRUCTOR -
         * <p>
         * Initializes a new configuration builder with required Supabase project parameters.
         * Automatically sets up default headers including the API key for service authentication.
         * All service paths are initialized with default values that can be overridden using
         * the fluent builder methods.
         *
         * @param projectUrl  Complete URL of the Supabase project (e.g., "https://id.supabase.co")
         * @param supabaseKey Anonymous or service role API key for project authentication
         */
        public Builder(String projectUrl, String supabaseKey) {
            Objects.requireNonNull(projectUrl, "Project URL cannot be null");
            Objects.requireNonNull(supabaseKey, "API key (supabaseKey) cannot be null");

            this.supabaseUrl = URI.create(projectUrl);
            this.supabaseKey = supabaseKey;

            // Add default Supabase headers
            this.headers.put("apiKey", supabaseKey);
        }

        /**
         * - CUSTOM HEADER ADDITION -
         * <p>
         * Adds a custom HTTP header to be included in all API requests. Commonly used
         * for authorization tokens, custom authentication, or API-specific headers.
         * Headers added here will override any default headers with the same key.
         *
         * @param key   HTTP header name (e.g., "Authorization", "X-Custom-Header")
         * @param value HTTP header value
         * @return Builder instance for method chaining
         */
        public Builder addHeader(String key, String value) {
            this.headers.put(key, value);
            return this;
        }

        /**
         * - DATABASE SCHEMA CONFIGURATION -
         * <p>
         * Specifies the PostgreSQL schema to use for database operations. Defaults to "public"
         * which is the standard schema for most Supabase projects. Custom schemas can be
         * specified for multi-tenant applications or advanced database configurations.
         *
         * @param schemaName Database schema name for PostgreSQL operations
         * @return Builder instance for method chaining
         */
        public Builder withSchema(String schemaName) {
            Objects.requireNonNull(schemaName, "Schema name cannot be null");
            this.schema = schemaName;
            return this;
        }

        /**
         * - AUTHENTICATION SERVICE PATH OVERRIDE -
         * <p>
         * Overrides the default authentication service endpoint path. Useful for custom
         * Supabase deployments or when using different API versions. Default is "/auth/v1".
         *
         * @param authPath Custom authentication service path (e.g., "/auth/v2")
         * @return Builder instance for method chaining
         */
        public Builder withAuthPath(String authPath) {
            this.authPath = authPath;
            return this;
        }

        /**
         * - POSTGRESQL REST SERVICE PATH OVERRIDE -
         * <p>
         * Overrides the default PostgREST service endpoint path. Allows targeting different
         * API versions or custom PostgREST deployments. Default is "/rest/v1".
         *
         * @param postgrestPath Custom PostgREST service path (e.g., "/rest/v2")
         * @return Builder instance for method chaining
         */
        public Builder withPostgrestPath(String postgrestPath) {
            this.postgrestPath = postgrestPath;
            return this;
        }

        /**
         * - STORAGE SERVICE PATH OVERRIDE -
         * <p>
         * Overrides the default storage service endpoint path. Enables targeting different
         * storage API versions or custom storage service deployments. Default is "/storage/v1".
         *
         * @param storagePath Custom storage service path (e.g., "/storage/v2")
         * @return Builder instance for method chaining
         */
        public Builder withStoragePath(String storagePath) {
            this.storagePath = storagePath;
            return this;
        }

        /**
         * - REAL-TIME SERVICE PATH OVERRIDE -
         * <p>
         * Overrides the default real-time service endpoint path. Allows connecting to
         * different real-time API versions or custom WebSocket endpoints. Default is "/realtime/v1".
         *
         * @param realtimePath Custom real-time service path (e.g., "/realtime/v2")
         * @return Builder instance for method chaining
         */
        public Builder withRealtimePath(String realtimePath) {
            this.realtimePath = realtimePath;
            return this;
        }

        /**
         * - CONFIGURATION BUILD -
         * <p>
         * Creates the final immutable SupabaseConfig instance with all configured parameters.
         * Automatically ensures proper Authorization header setup if not explicitly provided.
         * All builder parameters are validated and the resulting configuration is thread-safe.
         *
         * @return Immutable SupabaseConfig instance ready for client initialization
         */
        public SupabaseConfig build() {
            if (!this.headers.containsKey("Authorization")) {
                this.headers.put("Authorization", "Bearer " + this.supabaseKey);
            }
            return new SupabaseConfig(this.supabaseUrl, this.supabaseKey, this.headers, this.schema,
                    this.authPath, this.postgrestPath, this.storagePath, this.realtimePath);
        }
    }
}