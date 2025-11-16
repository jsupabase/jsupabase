package io.github.jsupabase.platform.dto.network.options;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO for the update SSL enforcement status request body.
 *
 * @author neilhdezs
 * @version 0.1.0
 * @since 0.1.0
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UpdateSslEnforcementRequest {

    @JsonProperty("status")
    private final boolean status;

    /**
     * Creates a request to update the SSL enforcement status.
     *
     * @param status True to enforce SSL, false to disable.
     */
    public UpdateSslEnforcementRequest(boolean status) {
        this.status = status;
    }
}