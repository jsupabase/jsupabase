package io.github.jsupabase.platform.dto.network;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO for the SSL enforcement status response.
 *
 * @author neilhdezs
 * @version 0.1.0
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class SslEnforcementResponse {

    @JsonProperty("status")
    private boolean status;

    // Getter
    public boolean getStatus() { return status; }
}