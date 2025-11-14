package io.github.jsupabase.postgrest;

import io.github.jsupabase.core.client.HttpClientBase;
import io.github.jsupabase.core.config.SupabaseConfig;
// 1. IMPORTAR LOS BUILDERS CORRECTOS
import io.github.jsupabase.postgrest.builder.PostgrestSelectBuilder; // <--- ¡Este es el correcto!
import io.github.jsupabase.postgrest.PostgrestRpcBuilder;

import java.util.Objects;

/**
 * El punto de entrada para todas las interacciones con la base de datos (PostgREST API).
 * Este cliente ahora implementa HttpClientBase y actúa como el motor de red.
 *
 * @author neilhdezs
 * @version 0.0.4 // Versión actualizada
 */
public class PostgrestClient extends HttpClientBase {

    /**
     * Crea un nuevo PostgrestClient.
     *
     * @param config La configuración del cliente.
     */
    public PostgrestClient(SupabaseConfig config) {
        super(Objects.requireNonNull(config, "SupabaseConfig cannot be null"));
    }

    /**
     * Selecciona una tabla para empezar a construir una consulta (SELECT * por defecto).
     *
     * @param table El nombre de la tabla o vista.
     * @return Una instancia de PostgrestSelectBuilder.
     */
    public PostgrestQueryBuilder from(String table) {
        return new PostgrestQueryBuilder(this, table);
    }

    /**
     * Llama a una función de PostgreSQL vía Remote Procedure Call (RPC).
     *
     * @param functionName El nombre de la función a llamar.
     * @param args Los argumentos (parámetros) para la función (POJO o Map).
     * @return Una instancia de PostgrestRpcBuilder.
     */
    public PostgrestRpcBuilder rpc(String functionName, Object args) {
        return new PostgrestRpcBuilder(this, functionName, args);
    }

    /**
     * Llama a una función de PostgreSQL vía RPC sin argumentos.
     *
     * @param functionName El nombre de la función a llamar.
     * @return Una instancia de PostgrestRpcBuilder.
     */
    public PostgrestRpcBuilder rpc(String functionName) {
        return new PostgrestRpcBuilder(this, functionName, null);
    }
}