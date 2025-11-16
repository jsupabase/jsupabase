package io.github.jsupabase.platform.dto.network.options;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

/**
 * DTO for the create custom hostname request body.
 *
 * @author neilhdezs
 * @version 0.1.0
 * @since 0.1.0
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CreateHostnameRequest {

    @JsonProperty("custom_hostname")
    private final String customHostname;

    /**
     * Creates a request to create a custom hostname.
     */
    public CreateHostnameRequest(String customHostname) {
        this.customHostname = Objects.requireNonNull(customHostname, "Custom Hostname cannot be null.");
    }
}