package io.github.jsupabase.platform.dto.secrets;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO for a Secret response (when listing secrets).
 *
 * @author neilhdezs
 * @version 0.1.0
 * @since 0.1.0
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class SecretResponse {

    @JsonProperty("name")
    private String name;

    @JsonProperty("created_at")
    private String createdAt;

    // Getters
    public String getName() {
        return name;
    }

    public String getCreatedAt() {
        return createdAt;
    }
}