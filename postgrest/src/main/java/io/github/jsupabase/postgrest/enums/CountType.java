package io.github.jsupabase.postgrest.enums; // <-- Nuevo paquete

/**
 * Enum for the different count algorithms PostgREST supports.
 * Used in SELECT, UPDATE, INSERT, DELETE, and RPC.
 *
 * @author neilhdezs
 * @version 0.0.3
 */
public enum CountType {
    EXACT("exact"),
    PLANNED("planned"),
    ESTIMATED("estimated");

    private final String value;

    CountType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}