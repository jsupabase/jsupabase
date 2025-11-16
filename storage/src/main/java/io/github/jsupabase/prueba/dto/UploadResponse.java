package io.github.jsupabase.prueba.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.nio.file.Path;

/**
 * DTO for a successful upload response.
 *
 * @author neilhdezs
 * @version 0.1.0
 * @since 0.1.0
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class UploadResponse {

    private String path;

    @JsonProperty("Key")
    private String fullPath;

    @JsonProperty("Id")
    private String id;

    // Getters
    public String getPath() {
        return Path.of(fullPath).getFileName().toString();
    }

    public String getFullPath() {
        return fullPath;
    }

    public String getId() {
        return id;
    }
}