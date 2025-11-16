package io.github.jsupabase.platform.dto.organizations.options;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;

/**
 * DTO for the request body to create a new organization.
 *
 * @author neilhdezs
 * @version 0.1.0
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CreateOrganizationRequest {

    @JsonProperty("name")
    private final String name;

    @JsonProperty("owner_id")
    private final String ownerId;

    /**
     * Creates a request to create a new organization.
     *
     * @param name The name of the organization.
     * @param ownerId The UUID of the owner/creator.
     */
    public CreateOrganizationRequest(String name, String ownerId) {
        this.name = Objects.requireNonNull(name, "Organization name cannot be null.");
        this.ownerId = Objects.requireNonNull(ownerId, "Owner ID cannot be null.");
    }
}