package io.github.jsupabase.postgrest.enums; // <-- Nuevo paquete

/**
 * Defines the sort direction for an {@code .order()} call.
 *
 * @author neilhdezs
 * @version 0.0.3
 */
public enum Order {
    ASC("asc"),
    DESC("desc");

    private final String value;

    Order(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}