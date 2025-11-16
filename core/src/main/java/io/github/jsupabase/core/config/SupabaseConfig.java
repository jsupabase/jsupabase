package io.github.jsupabase.core.config;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Stores the configuration for a Supabase client.
 * Instantiated using the Builder pattern.
 *
 * @author neilhdezs
 * @version 0.0.4
 */
public final class SupabaseConfig {

    /** - Base URL for the Supabase API - **/
    private final URI supabaseUrl;

    /** - API Key for communication with the Supabase API - **/
    private final String supabaseKey;

    /** - Headers to include in the request - **/
    private final Map<String, String> headers;

    /** - Supabase schema to interact with - **/
    private final String schema;

    // --- Service Paths (Added in 0.0.4) ---
    /** - Path prefix for the Auth service (e.g., /auth/v1) - **/
    private final String authPath;
    /** - Path prefix for the Postgrest service (e.g., /rest/v1) - **/
    private final String postgrestPath;
    /** - Path prefix for the Storage service (e.g., /storage/v1) - **/
    private final String storagePath;
    /** - Path prefix for the Realtime service (e.g., /realtime/v1) - **/
    private final String realtimePath;


    /**
     * Private constructor to force the use of the Builder pattern.
     */
    private SupabaseConfig(URI supabaseUrl, String supabaseKey, Map<String, String> headers, String schema,
                           String authPath, String postgrestPath, String storagePath, String realtimePath) {
        this.supabaseUrl = supabaseUrl;
        this.supabaseKey = supabaseKey;
        this.headers = headers;
        this.schema = schema;
        this.authPath = authPath;
        this.postgrestPath = postgrestPath;
        this.storagePath = storagePath;
        this.realtimePath = realtimePath;
    }

    /**
     * Getter for the Supabase API base URL.
     * @return Supabase API base URL
     */
    public URI getSupabaseUrl() {
        return supabaseUrl;
    }

    /**
     * Getter for the API Key.
     * @return API Key
     */
    public String getSupabaseKey() {
        return supabaseKey;
    }

    /**
     * Getter for the Headers.
     * @return An unmodifiable Map of headers
     */
    public Map<String, String> getHeaders() {
        return Map.copyOf(headers);
    }

    /**
     * Getter for the Supabase schema.
     * @return Supabase schema name
     */
    public String getSchema() {
        return schema;
    }

    // --- New Getters (Added in 0.0.4) ---

    /**
     * Getter for the Auth service path.
     * @return Auth service path (e.g., /auth/v1)
     */
    public String getAuthPath() {
        return authPath;
    }

    /**
     * Getter for the Postgrest service path.
     * @return Postgrest service path (e.g., /rest/v1)
     */
    public String getPostgrestPath() {
        return postgrestPath;
    }

    /**
     * Getter for the Storage service path.
     * @return Storage service path (e.g., /storage/v1)
     */
    public String getStoragePath() {
        return storagePath;
    }

    /**
     * Getter for the Realtime service path.
     * @return Realtime service path (e.g., /realtime/v1)
     */
    public String getRealtimePath() {
        return realtimePath;
    }


    /**
     * Resuelve un path relativo contra la URL base de Supabase.
     * (ej. "https://id.supabase.co" + "/rest/v1/table" = "https://id.supabase.co/rest/v1/table")
     *
     * @param path El path relativo (debe empezar con '/')
     * @return La URI completa resuelta.
     */
    public URI resolveUrl(String path) {
        return this.supabaseUrl.resolve(path);
    }


    /* -------------------- BUILDER -------------------- */

    /**
     * Builder to create a SupabaseConfig instance.
     */
    public static class Builder {
        /** - Base URL for the Supabase API - **/
        private final URI supabaseUrl;

        /** - API Key for communication with the Supabase API - **/
        private final String supabaseKey;

        /** - Headers to include in the request - **/
        private final Map<String, String> headers = new HashMap<>();

        /** - Supabase schema to interact with - **/
        private String schema = "public"; // Default Supabase schema

        // --- Service Path Defaults (Added in 0.0.4) ---
        private String authPath = "/auth/v1";
        private String postgrestPath = "/rest/v1";
        private String storagePath = "/storage/v1";
        private String realtimePath = "/realtime/v1";

        /**
         * Creates a new Builder.
         *
         * @param projectUrl  The URL of your Supabase project (e.g., "https://id.supabase.co")
         * @param supabaseKey The 'anon' or 'service_role' key for your project.
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
         * Adds a custom HTTP header (e.g., "Authorization").
         * @param key   The header name
         * @param value The header value
         * @return this (for Builder chaining)
         */
        public Builder addHeader(String key, String value) {
            this.headers.put(key, value);
            return this;
        }

        /**
         * Defines the database schema to use (defaults to "public").
         * @param schemaName The name of the schema.
         * @return this (for Builder chaining)
         */
        public Builder withSchema(String schemaName) {
            Objects.requireNonNull(schemaName, "Schema name cannot be null");
            this.schema = schemaName;
            return this;
        }

        // --- Optional Path Overrides (Added in 0.0.4) ---

        /**
         * (Optional) Overrides the default Auth service path.
         * @param authPath The new path (e.g., "/auth/v2")
         * @return this (for Builder chaining)
         */
        public Builder withAuthPath(String authPath) {
            this.authPath = authPath;
            return this;
        }

        /**
         * (Optional) Overrides the default Postgrest service path.
         * @param postgrestPath The new path (e.g., "/rest/v2")
         * @return this (for Builder chaining)
         */
        public Builder withPostgrestPath(String postgrestPath) {
            this.postgrestPath = postgrestPath;
            return this;
        }

        /**
         * (Optional) Overrides the default Storage service path.
         * @param storagePath The new path (e.g., "/storage/v2")
         * @return this (for Builder chaining)
         */
        public Builder withStoragePath(String storagePath) {
            this.storagePath = storagePath;
            return this;
        }

        /**
         * (Optional) Overrides the default Realtime service path.
         * @param realtimePath The new path (e.g., "/realtime/v2")
         * @return this (for Builder chaining)
         */
        public Builder withRealtimePath(String realtimePath) {
            this.realtimePath = realtimePath;
            return this;
        }

        /**
         * Builds the final SupabaseConfig instance.
         * @return An immutable SupabaseConfig instance.
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