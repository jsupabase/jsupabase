package io.github.jsupabase.prueba.dto.options;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Payload for creating a new bucket.
 *
 * @author neilhdezs
 * @version 0.1.0
 * @since 0.1.0
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CreateBucketOptions {

    @JsonProperty("id")
    private final String id;

    @JsonProperty("name")
    private final String name;

    @JsonProperty("public")
    private final boolean isPublic;

    @JsonProperty("allowed_mime_types")
    private final List<String> allowedMimeTypes;

    @JsonProperty("file_size_limit")
    private final Long fileSizeLimit;

    /**
     * Private constructor for the Builder.
     */
    private CreateBucketOptions(Builder builder) {
        this.id = builder.id;
        this.name = builder.id; // Per Supabase, id and name are the same
        this.isPublic = builder.isPublic;
        this.allowedMimeTypes = builder.allowedMimeTypes;
        this.fileSizeLimit = builder.fileSizeLimit;
    }

    // --- Builder Class ---
    public static class Builder {
        private final String id;
        private boolean isPublic = false; // Defaults to private
        private List<String> allowedMimeTypes = null;
        private Long fileSizeLimit = null;

        public Builder(String id) {
            this.id = id;
        }

        public Builder setPublic(boolean isPublic) {
            this.isPublic = isPublic;
            return this;
        }

        public Builder setAllowedMimeTypes(List<String> allowedMimeTypes) {
            this.allowedMimeTypes = allowedMimeTypes;
            return this;
        }

        public Builder setFileSizeLimit(Long fileSizeLimit) {
            this.fileSizeLimit = fileSizeLimit;
            return this;
        }

        public CreateBucketOptions build() {
            return new CreateBucketOptions(this);
        }
    }
}