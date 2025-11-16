package io.github.jsupabase.platform.dto.projects;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * DTO for a Project response.
 *
 * @author neilhdezs
 * @version 0.1.0
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProjectResponse {

    @JsonProperty("id")
    private String id;

    @JsonProperty("ref")
    private String ref; // project_ref

    @JsonProperty("name")
    private String name;

    @JsonProperty("status")
    private String status;

    @JsonProperty("organization_id")
    private String organizationId;

    @JsonProperty("region")
    private String region;

    @JsonProperty("db_url")
    private String dbUrl;

    @JsonProperty("service_key")
    private String serviceKey; // Clave del rol 'supabase_admin'

    // Getters
    public String getId() { return id; }
    public String getRef() { return ref; }
    public String getName() { return name; }
    public String getStatus() { return status; }
    public String getOrganizationId() { return organizationId; }
    public String getRegion() { return region; }
    public String getDbUrl() { return dbUrl; }
    public String getServiceKey() { return serviceKey; }
}