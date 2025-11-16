package io.github.jsupabase.platform.dto.organizations;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO for an Organization response, used when listing organizations.
 *
 * @author neilhdezs
 * @version 0.1.0
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class OrganizationResponse {

    @JsonProperty("id")
    private String id;

    @JsonProperty("name")
    private String name;

    @JsonProperty("slug")
    private String slug;

    @JsonProperty("billing_email")
    private String billingEmail;

    @JsonProperty("created_at")
    private String createdAt;

    // Getters
    public String getId() { return id; }
    public String getName() { return name; }
    public String getSlug() { return slug; }
    public String getBillingEmail() { return billingEmail; }
    public String getCreatedAt() { return createdAt; }
}