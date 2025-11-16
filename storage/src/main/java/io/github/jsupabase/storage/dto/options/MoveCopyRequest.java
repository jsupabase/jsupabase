package io.github.jsupabase.storage.dto.options;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO for move() and copy() request bodies.
 *
 * @author neilhdezs
 * @version 0.1.0
 */
public class MoveCopyRequest {

    @JsonProperty("bucketId")
    private final String bucketId;

    @JsonProperty("sourceKey")
    private final String sourceKey;

    @JsonProperty("destinationKey")
    private final String destinationKey;

    public MoveCopyRequest(String bucketId, String sourceKey, String destinationKey) {
        this.bucketId = bucketId;
        this.sourceKey = sourceKey;
        this.destinationKey = destinationKey;
    }
}