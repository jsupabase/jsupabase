package io.github.jsupabase.prueba.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Generic DTO for simple {"message": "..."} responses.
 * (Used by deleteBucket, emptyBucket, updateBucket)
 *
 * @author neilhdezs
 * @version 0.1.0
 * @since 0.1.0
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class MessageResponse {

    @JsonProperty("message")
    private String message;

    public String getMessage() {
        return message;
    }
}