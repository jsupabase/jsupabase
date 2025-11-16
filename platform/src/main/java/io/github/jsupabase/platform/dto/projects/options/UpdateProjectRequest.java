package io.github.jsupabase.platform.dto.projects.options;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO for the updateProject() request body (PATCH).
 * Todos los campos son opcionales. Usa el .Builder() para construir.
 *
 * @author neilhdezs
 * @version 0.1.0
 * @since 0.1.0
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UpdateProjectRequest {

    @JsonProperty("name")
    private final String name;

    @JsonProperty("db_pass")
    private final String dbPass;

    // Otros campos opcionales como 'plan' se podrían añadir aquí

    // Constructor privado para el Builder
    private UpdateProjectRequest(Builder builder) {
        this.name = builder.name;
        this.dbPass = builder.dbPass;
    }

    // --- Builder Class ---
    public static class Builder {
        private String name;
        private String dbPass;

        public Builder() {
        } // Builder vacío para actualizaciones opcionales

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder dbPass(String dbPass) {
            this.dbPass = dbPass;
            return this;
        }

        public UpdateProjectRequest build() {
            return new UpdateProjectRequest(this);
        }
    }
}