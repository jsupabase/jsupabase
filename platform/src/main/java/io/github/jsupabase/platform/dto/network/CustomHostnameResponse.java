package io.github.jsupabase.platform.dto.network;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO for the custom hostname status response.
 *
 * @author neilhdezs
 * @version 0.1.0
 * @since 0.1.0
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class CustomHostnameResponse {

    @JsonProperty("id")
    private String id;

    @JsonProperty("custom_hostname")
    private String customHostname;

    @JsonProperty("status")
    private String status;

    @JsonProperty("ssl_status")
    private String sslStatus;

    // Getters
    public String getId() {
        return id;
    }

    public String getCustomHostname() {
        return customHostname;
    }

    public String getStatus() {
        return status;
    }

    public String getSslStatus() {
        return sslStatus;
    }
}