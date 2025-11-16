package io.github.jsupabase.core.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

/**
 * Utility class for MIME type detection.
 * <p>
 * This class provides a reliable way to determine the MIME type of a file
 * before uploading it to Supabase Storage.
 *
 * @author neilhdezs
 * @version 0.1.0
 * @since 0.1.0
 */
public final class MimeTypeUtil {

    /**
     * The default fallback MIME type for unknown file types.
     * This type is widely accepted for any binary data stream.
     */
    public static final String DEFAULT_MIME_TYPE = "application/octet-stream";

    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private MimeTypeUtil() {
        throw new IllegalStateException("This is a utility class and cannot be instantiated.");
    }

    /**
     * Probes the given file to determine its MIME type using the default
     * system {@link java.nio.file.spi.FileTypeDetector}.
     * <p>
     * This method attempts to read the file's metadata to determine its content type.
     * If the type is unknown ({@code null}) or an {@link IOException} occurs,
     * it safely falls back to {@link #DEFAULT_MIME_TYPE}.
     *
     * @param file The file (java.nio.file.Path) to probe.
     * @return The detected MIME type (e.g., "image/png", "application/json") or
     * "application/octet-stream" as a fallback.
     */
    public static String probeContentType(Path file) {
        if (file == null) {
            return DEFAULT_MIME_TYPE;
        }

        try {
            String mimeType = Files.probeContentType(file);

            // If the OS/JVM can't determine the type (e.g., unknown extension),
            // fall back to the default binary stream type.
            return Objects.requireNonNullElse(mimeType, DEFAULT_MIME_TYPE);
        } catch (IOException e) {
            // In case of an I/O error reading the file, also fall back.
            // Consider logging this warning e.g., e.printStackTrace();
            return DEFAULT_MIME_TYPE;
        }
    }
}