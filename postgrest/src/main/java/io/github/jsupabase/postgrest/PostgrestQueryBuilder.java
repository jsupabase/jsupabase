package io.github.jsupabase.postgrest;

// NO MÁS 'SupabaseConfig'
import io.github.jsupabase.postgrest.builder.PostgrestDeleteBuilder;
import io.github.jsupabase.postgrest.builder.PostgrestInsertBuilder;
import io.github.jsupabase.postgrest.builder.PostgrestSelectBuilder;
import io.github.jsupabase.postgrest.builder.PostgrestUpdateBuilder;

import java.util.Objects;

/**
 * El "menú" principal para una consulta de tabla.
 *
 * @author neilhdezs
 * @version 0.0.3 // Versión actualizada
 */
public class PostgrestQueryBuilder {

    /** - El cliente de red para este módulo - **/
    private final PostgrestClient client;

    /** - La tabla de la BD a la que apunta esta consulta - **/
    private final String table;

    /**
     * Crea un nuevo PostgrestQueryBuilder.
     *
     * @param client El cliente Postgrest activo. // <-- 2. CAMBIO
     * @param table  La tabla de la base de datos a consultar.
     */
    public PostgrestQueryBuilder(PostgrestClient client, String table) { // <-- 2. CAMBIO
        this.client = Objects.requireNonNull(client, "PostgrestClient cannot be null");
        this.table = Objects.requireNonNull(table, "Table name cannot be null");
    }

    /**
     * Prepara una consulta SELECT.
     *
     * @param columns Las columnas a obtener.
     * @return Un PostgrestSelectBuilder.
     */
    public PostgrestSelectBuilder select(String columns) {
        return new PostgrestSelectBuilder(this.client, this.table, columns);
    }

    /**
     * Prepara una consulta SELECT para todas las columnas ("*").
     */
    public PostgrestSelectBuilder select() {
        return this.select("*");
    }

    /**
     * Prepara una operación INSERT.
     *
     * @param data Los datos a insertar (POJO, Map, o List<...>).
     * @return Un PostgrestInsertBuilder.
     */
    public PostgrestInsertBuilder insert(Object data) {
        return new PostgrestInsertBuilder(this.client, this.table, data);
    }

    /**
     * Prepara una operación UPDATE.
     *
     * @param data Los datos (POJO o Map) a actualizar.
     * @return Un PostgrestUpdateBuilder.
     */
    public PostgrestUpdateBuilder update(Object data) {
        return new PostgrestUpdateBuilder(this.client, this.table, data);
    }

    /**
     * Prepara una operación DELETE.
     *
     * @return Un PostgrestDeleteBuilder.
     */
    public PostgrestDeleteBuilder delete() {
        return new PostgrestDeleteBuilder(this.client, this.table);
    }
}