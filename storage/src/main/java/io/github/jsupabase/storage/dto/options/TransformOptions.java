package io.github.jsupabase.storage.dto.options;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.StringJoiner;

/**
 * Represents the set of image transformation options available when rendering images.
 * <p>
 * This DTO is used to construct the query parameters for Supabase Storage's
 * image transformation endpoints (e.g., {@code /render/image/...}).
 * <p>
 * An internal {@link Builder} is provided to facilitate the creation of
 * immutable transform configurations.
 *
 * @author neilhdezs
 * @version 1.0.0
 * @see <a href="https://supabase.com/docs/guides/storage/serving/image-transformations">Supabase Image Transformations</a>
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TransformOptions {

    /** - The target width of the image in pixels. Must be between 1 and 2500. - **/
    @JsonProperty("width")
    private final Integer width;

    /** - The target height of the image in pixels. Must be between 1 and 2500. - **/
    @JsonProperty("height")
    private final Integer height;

    /** - The image quality (compression level) from 20 to 100. Defaults to 80. - **/
    @JsonProperty("quality")
    private final Integer quality;

    /** - The resize mode to use. - **/
    @JsonProperty("resize")
    private final Resize resize;

    /** - The target format. 'origin' preserves the original format. - **/
    @JsonProperty("format")
    private final String format;

    /**
     * Defines the available resize modes for image transformation.
     */
    public enum Resize {
        /**
         * (Default) Resizes the image while keeping the aspect ratio to fill a given
         * size and crops projecting parts.
         */
        COVER("cover"),

        /**
         * Resizes the image while keeping the aspect ratio to fit a given size.
         */
        CONTAIN("contain"),

        /**
         * Resizes the image without keeping the aspect ratio.
         */
        FILL("fill");

        private final String value;
        Resize(String value) { this.value = value; }

        @Override
        public String toString() { return value; }
    }

    /**
     * Private constructor, accessible only via the Builder.
     */
    private TransformOptions(Builder builder) {
        this.width = builder.width;
        this.height = builder.height;
        this.quality = builder.quality;
        this.resize = builder.resize;
        this.format = builder.format;
    }

    // --- Getters ---

    public Integer getWidth() { return width; }
    public Integer getHeight() { return height; }
    public Integer getQuality() { return quality; }
    public Resize getResize() { return resize; }
    public String getFormat() { return format; }

    /**
     * Serializes the set options into a URL-safe query string.
     *
     * @return A query string (e.g., "?width=300&height=300&resize=cover").
     */
    public String toQueryParameters() {
        StringJoiner joiner = new StringJoiner("&", "?", "");

        if (width != null) {
            joiner.add(urlEncode("width") + "=" + urlEncode(width.toString()));
        }
        if (height != null) {
            joiner.add(urlEncode("height") + "=" + urlEncode(height.toString()));
        }
        if (quality != null) {
            joiner.add(urlEncode("quality") + "=" + urlEncode(quality.toString()));
        }
        if (resize != null) {
            joiner.add(urlEncode("resize") + "=" + urlEncode(resize.toString()));
        }
        if (format != null) {
            joiner.add(urlEncode("format") + "=" + urlEncode(format));
        }

        String queryString = joiner.toString();
        // Return an empty string if no options were added, otherwise return the query string
        return (queryString.length() == 1) ? "" : queryString;
    }

    /**
     * Helper method for URL encoding.
     */
    private String urlEncode(String value) {
        try {
            return URLEncoder.encode(value, StandardCharsets.UTF_8);
        } catch (Exception e) {
            return value; // Fallback
        }
    }

    /**
     * Builder class for creating {@link TransformOptions} instances.
     */
    public static class Builder {
        private Integer width;
        private Integer height;
        private Integer quality;
        private Resize resize;
        private String format;

        public Builder width(int width) {
            this.width = width;
            return this;
        }

        public Builder height(int height) {
            this.height = height;
            return this;
        }

        public Builder quality(int quality) {
            this.quality = quality;
            return this;
        }

        public Builder resize(Resize resize) {
            this.resize = resize;
            return this;
        }

        public Builder format(String format) {
            this.format = format;
            return this;
        }

        /**
         * Builds the immutable {@link TransformOptions} object.
         *
         * @return A new instance of TransformOptions.
         */
        public TransformOptions build() {
            return new TransformOptions(this);
        }
    }
}