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
 * @version 0.0.3
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


    /**
     * Private constructor to force the use of the Builder pattern.
     */
    private SupabaseConfig(URI supabaseUrl, String supabaseKey, Map<String, String> headers, String schema) {
        this.supabaseUrl = supabaseUrl;
        this.supabaseKey = supabaseKey;
        this.headers = headers;
        this.schema = schema;
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

        /**
         * Builds the final SupabaseConfig instance.
         * @return An immutable SupabaseConfig instance.
         */
        public SupabaseConfig build() {
            if (!this.headers.containsKey("Authorization")) {
                this.headers.put("Authorization", "Bearer " + this.supabaseKey);
            }
            return new SupabaseConfig(this.supabaseUrl, this.supabaseKey, this.headers, this.schema);
        }
    }
}