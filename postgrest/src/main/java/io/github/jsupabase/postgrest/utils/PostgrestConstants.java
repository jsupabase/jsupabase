package io.github.jsupabase.postgrest.utils;

/**
 * - POSTGREST CONSTANTS -
 * <p>
 * Centralized constants for headers, query parameters, operators, and log messages
 * used across the PostgREST module.
 *
 * @author neilhdezs
 * @version 0.1.0
 * @since 0.1.0
 */
public final class PostgrestConstants {

    private PostgrestConstants() {}

    /** - HTTP header Prefer - **/
    public static final String HDR_PREFER = "Prefer";
    /** - HTTP header Content-Range for count - **/
    public static final String HDR_CONTENT_RANGE = "Content-Range";
    /** - HTTP header Content-Profile for schema - **/
    public static final String HDR_CONTENT_PROFILE = "Content-Profile";
    /** - HTTP header Accept-Profile for schema - **/
    public static final String HDR_ACCEPT_PROFILE = "Accept-Profile";

    /** - Prefer value return=minimal - **/
    public static final String PREFER_RETURN_MINIMAL = "return=minimal";
    /** - Prefer value return=representation - **/
    public static final String PREFER_RETURN_REPRESENTATION = "return=representation";
    /** - Prefer value count=exact - **/
    public static final String PREFER_COUNT_EXACT = "count=exact";
    /** - Prefer value count=planned - **/
    public static final String PREFER_COUNT_PLANNED = "count=planned";
    /** - Prefer value count=estimated - **/
    public static final String PREFER_COUNT_ESTIMATED = "count=estimated";
    /** - Prefer value resolution=merge-duplicates - **/
    public static final String PREFER_RESOLUTION_MERGE = "resolution=merge-duplicates";
    /** - Prefer value resolution=ignore-duplicates - **/
    public static final String PREFER_RESOLUTION_IGNORE = "resolution=ignore-duplicates";

    /** - Query parameter select - **/
    public static final String PARAM_SELECT = "select";
    /** - Query parameter order - **/
    public static final String PARAM_ORDER = "order";
    /** - Query parameter limit - **/
    public static final String PARAM_LIMIT = "limit";
    /** - Query parameter offset - **/
    public static final String PARAM_OFFSET = "offset";

    /** - Filter operator equals - **/
    public static final String OP_EQ = "eq";
    /** - Filter operator not equals - **/
    public static final String OP_NEQ = "neq";
    /** - Filter operator greater than - **/
    public static final String OP_GT = "gt";
    /** - Filter operator greater than or equal - **/
    public static final String OP_GTE = "gte";
    /** - Filter operator less than - **/
    public static final String OP_LT = "lt";
    /** - Filter operator less than or equal - **/
    public static final String OP_LTE = "lte";
    /** - Filter operator like - **/
    public static final String OP_LIKE = "like";
    /** - Filter operator ilike (case-insensitive) - **/
    public static final String OP_ILIKE = "ilike";
    /** - Filter operator is - **/
    public static final String OP_IS = "is";
    /** - Filter operator in - **/
    public static final String OP_IN = "in";
    /** - Filter operator contains - **/
    public static final String OP_CS = "cs";
    /** - Filter operator contained by - **/
    public static final String OP_CD = "cd";
    /** - Filter operator overlaps - **/
    public static final String OP_OV = "ov";
    /** - Filter operator not - **/
    public static final String OP_NOT = "not";
    /** - Filter operator or - **/
    public static final String OP_OR = "or";
    /** - Filter operator and - **/
    public static final String OP_AND = "and";

    /** - Log: Executing SELECT query - **/
    public static final String LOG_EXECUTE_SELECT = "Executing SELECT: {}";
    /** - Log: Executing INSERT query - **/
    public static final String LOG_EXECUTE_INSERT = "Executing INSERT: {}";
    /** - Log: Executing UPDATE query - **/
    public static final String LOG_EXECUTE_UPDATE = "Executing UPDATE: {}";
    /** - Log: Executing DELETE query - **/
    public static final String LOG_EXECUTE_DELETE = "Executing DELETE: {}";
    /** - Log: Executing RPC call - **/
    public static final String LOG_EXECUTE_RPC = "Executing RPC: {}";

    /** - Error: Failed to build request - **/
    public static final String ERROR_BUILD_REQUEST = "Failed to build PostgREST {} request";
}

