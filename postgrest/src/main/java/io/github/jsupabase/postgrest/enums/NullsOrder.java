package io.github.jsupabase.postgrest.enums; // <-- Nuevo paquete

/**
 * Defines the null sorting preference for an {@code .order()} call.
 *
 * @author neilhdezs
 * @version 0.0.3
 */
public enum NullsOrder {
    FIRST("nullsfirst"),
    LAST("nullslast");

    private final String value;

    NullsOrder(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}