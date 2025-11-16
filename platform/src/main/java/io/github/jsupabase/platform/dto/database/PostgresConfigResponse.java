package io.github.jsupabase.platform.dto.database;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO for the main Postgres config response.
 *
 * @author neilhdezs
 * @version 0.1.0
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class PostgresConfigResponse {

    @JsonProperty("statement_timeout")
    private String statementTimeout;

    @JsonProperty("effective_cache_size")
    private String effectiveCacheSize;

    @JsonProperty("shared_buffers")
    private String sharedBuffers;

    // ... (Hay muchos más campos)

    // Getters
    public String getStatementTimeout() { return statementTimeout; }
    public String getEffectiveCacheSize() { return effectiveCacheSize; }
    public String getSharedBuffers() { return sharedBuffers; }
}