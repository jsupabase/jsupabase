package io.github.jsupabase.core.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.jsupabase.core.exceptions.SupabaseException;

import java.util.Map;

/**
 * Utility class for JSON serialization and deserialization.
 * Uses a Singleton pattern for the ObjectMapper (which is thread-safe).
 *
 * @author neilhdezs
 * @version 0.1.0
 * @since 0.1.0
 */
public final class JsonUtil {

    /** - Singleton, thread-safe ObjectMapper instance (from Jackson) - **/
    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Private constructor to prevent this utility class from being instantiated.
     */
    private JsonUtil() {
        throw new IllegalStateException("This is a utility class and cannot be instantiated.");
    }

    /**
     * Serializes a Java object (POJO, Map, List) to a JSON String.
     *
     * @param data The object to serialize.
     * @return The resulting JSON String.
     * @throws SupabaseException if the Jackson serialization fails.
     */
    public static String toJson(Object data) {
        try {
            return objectMapper.writeValueAsString(data);
        } catch (JsonProcessingException e) {
            throw new SupabaseException("Failed to serialize object to JSON", e);
        }
    }


    /**
     * Deserializes a JSON String into a Java object (POJO, Map, List).
     *
     * @param json         The JSON String to deserialize.
     * @param responseType The Class of the object to create (e.g., MyPojo.class).
     * @param <T>          The generic type of the response.
     * @return An instance of the responseType.
     * @throws SupabaseException if the Jackson deserialization fails.
     */
    public static <T> T fromJson(String json, Class<T> responseType) {
        try {
            return objectMapper.readValue(json, responseType);
        } catch (JsonProcessingException e) {
            throw new SupabaseException("Failed to deserialize JSON to " + responseType.getSimpleName(), e);
        }
    }


    /**
     * Deserializes a JSON String into a complex generic Java object (e.g., List<T>).
     *
     * @param json    The JSON String to deserialize.
     * @param typeRef The TypeReference of the object (e.g., new TypeReference<List<Bucket>>() {}).
     * @param <T>     The generic type of the response.
     * @return An instance of the responseType.
     * @throws SupabaseException if the Jackson deserialization fails.
     */
    public static <T> T fromJson(String json, TypeReference<T> typeRef) {
        try {
            return objectMapper.readValue(json, typeRef);
        } catch (JsonProcessingException e) {
            throw new SupabaseException("Failed to deserialize JSON to " + typeRef.getType(), e);
        }
    }

    /**
     * Converts a Java object (like a Map) into another type (like a query string map).
     *
     * @param fromValue      The object to convert (e.g., a POJO or Map).
     * @param toValueTypeRef The target type (e.g., new TypeReference<Map<String, String>>() {}).
     * @param <T>            The generic type of the response.
     * @return The converted object.
     * @throws SupabaseException if the conversion fails.
     */
    public static <T> T convertValue(Object fromValue, TypeReference<T> toValueTypeRef) {
        try {
            return objectMapper.convertValue(fromValue, toValueTypeRef);
        } catch (Exception e) {
            throw new SupabaseException("Failed to convert object", e);
        }
    }

    /**
     * Converts a Java object (POJO) into a generic Map<String, Object>.
     * This is useful for adding/modifying properties before final serialization,
     * such as injecting 'bucketId' into a ListOptions DTO.
     *
     * @param fromValue The object to convert (e.g., a POJO like ListOptions).
     * @return A Map<String, Object> representing the object's properties.
     * @throws SupabaseException if the conversion fails.
     */
    public static Map<String, Object> toMap(Object fromValue) {
        TypeReference<Map<String, Object>> mapTypeRef = new TypeReference<>() {
        };
        return convertValue(fromValue, mapTypeRef);
    }
}