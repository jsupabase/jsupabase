package io.github.jsupabase.platform.dto.database;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO for the Pgbouncer config response.
 *
 * @author neilhdezs
 * @version 0.1.0
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class PgbouncerConfigResponse {

    @JsonProperty("pool_mode")
    private String poolMode;

    @JsonProperty("max_client_conn")
    private Integer maxClientConn;

    @JsonProperty("default_pool_size")
    private Integer defaultPoolSize;

    // ... (Hay muchos más campos, puedes añadirlos según los necesites)

    // Getters
    public String getPoolMode() { return poolMode; }
    public Integer getMaxClientConn() { return maxClientConn; }
    public Integer getDefaultPoolSize() { return defaultPoolSize; }
}