package io.github.jsupabase.prueba.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

/**
 * Represents a file or folder within a Supabase Storage bucket.
 * (DTO for listFiles)
 *
 * @author neilhdezs
 * @version 0.1.0
 * @since 0.1.0
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class FileObject {

    @JsonProperty("name")
    private String name;

    @JsonProperty("id")
    private String id;

    @JsonProperty("updated_at")
    private String updatedAt;

    @JsonProperty("created_at")
    private String createdAt;

    @JsonProperty("last_accessed_at")
    private String lastAccessedAt;

    @JsonProperty("metadata")
    private Map<String, Object> metadata;

    // Getters
    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public String getLastAccessedAt() {
        return lastAccessedAt;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }
}