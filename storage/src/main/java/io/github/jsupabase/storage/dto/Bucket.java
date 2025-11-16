package io.github.jsupabase.storage.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * Represents a Supabase Storage Bucket.
 * (DTO for getBucket and listBuckets)
 *
 * @author neilhdezs
 * @version 0.1.0
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Bucket {

    @JsonProperty("id")
    private String id;

    @JsonProperty("name")
    private String name;

    @JsonProperty("owner")
    private String owner;

    @JsonProperty("public")
    private boolean isPublic;

    @JsonProperty("created_at")
    private String createdAt;

    @JsonProperty("updated_at")
    private String updatedAt;

    @JsonProperty("allowed_mime_types")
    private List<String> allowedMimeTypes;

    @JsonProperty("file_size_limit")
    private Long fileSizeLimit;

    // --- Getters ---
    public String getId() { return id; }
    public String getName() { return name; }
    public String getOwner() { return owner; }
    public boolean isPublic() { return isPublic; }
    public String getCreatedAt() { return createdAt; }
    public String getUpdatedAt() { return updatedAt; }
    public List<String> getAllowedMimeTypes() { return allowedMimeTypes; }
    public Long getFileSizeLimit() { return fileSizeLimit; }
}