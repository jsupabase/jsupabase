package io.github.jsupabase.platform.dto.networkbans;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO for listing active network bans (IP/CIDR restrictions).
 *
 * @author neilhdezs
 * @version 0.1.0
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class NetworkBansResponse {

    @JsonProperty("ip_address")
    private String ipAddress;

    @JsonProperty("created_at")
    private String createdAt;

    // Getters
    public String getIpAddress() { return ipAddress; }
    public String getCreatedAt() { return createdAt; }
}