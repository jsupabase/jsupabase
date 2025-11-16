package io.github.jsupabase.platform.dto.database.options;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO for the updatePostgresConfig() request body (PATCH).
 * All fields are optional. Use the .Builder() to construct.
 *
 * @author neilhdezs
 * @version 0.1.0
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UpdatePostgresConfigRequest {

    @JsonProperty("statement_timeout")
    private final String statementTimeout;

    @JsonProperty("max_connections")
    private final Integer maxConnections;

    // ... (Hay muchos más campos que se pueden actualizar)

    // Constructor privado para el Builder
    private UpdatePostgresConfigRequest(Builder builder) {
        this.statementTimeout = builder.statementTimeout;
        this.maxConnections = builder.maxConnections;
    }

    // --- Builder Class ---
    public static class Builder {
        private String statementTimeout;
        private Integer maxConnections;

        public Builder() {} // Builder vacío para actualizaciones opcionales

        public Builder statementTimeout(String statementTimeout) {
            this.statementTimeout = statementTimeout;
            return this;
        }

        public Builder maxConnections(int maxConnections) {
            this.maxConnections = maxConnections;
            return this;
        }

        // ... (Añadir más métodos de builder para otros campos)

        public UpdatePostgresConfigRequest build() {
            return new UpdatePostgresConfigRequest(this);
        }
    }
}