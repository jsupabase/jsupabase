package io.github.jsupabase.storage.dto; // Usaremos el paquete de DTOs del módulo storage para esta prueba

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO para la tabla de pruebas 'tabla'.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TestRecord {
    @JsonProperty("id")
    public Long id;

    @JsonProperty("username")
    public String username;

    @JsonProperty("status")
    public String status;

    // Constructor vacío para Jackson
    public TestRecord() {}

    public TestRecord(Long id, String username, String status) {
        this.id = id;
        this.username = username;
        this.status = status;
    }

    // Usaremos un método toString simple para la depuración
    @Override
    public String toString() {
        return "TestRecord{id=" + id + ", username='" + username + "', status='" + status + "'}";
    }
}