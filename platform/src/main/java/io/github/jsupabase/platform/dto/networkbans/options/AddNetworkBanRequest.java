package io.github.jsupabase.platform.dto.networkbans.options;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

/**
 * DTO for the request body to add a network ban (IP/CIDR).
 *
 * @author neilhdezs
 * @version 0.1.0
 * @since 0.1.0
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AddNetworkBanRequest {

    @JsonProperty("ip_address")
    private final String ipAddress;

    /**
     * Creates a request to add a network ban.
     *
     * @param ipAddress The IP address or CIDR range to ban.
     */
    public AddNetworkBanRequest(String ipAddress) {
        this.ipAddress = Objects.requireNonNull(ipAddress, "IP Address cannot be null.");
    }
}