package io.github.jsupabase.platform.dto.secrets.options;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * DTO for the bulk creation of secrets request body.
 *
 * @author neilhdezs
 * @version 0.1.0
 */
public class BulkCreateSecretsRequest {

    /** Inner class representing a single secret entry **/
    public static class Secret {
        @JsonProperty("name")
        private final String name;
        @JsonProperty("value")
        private final String value;

        public Secret(String name, String value) {
            this.name = name;
            this.value = value;
        }
    }

    @JsonProperty("secrets")
    private final List<Secret> secrets;

    public BulkCreateSecretsRequest(List<Secret> secrets) {
        this.secrets = secrets;
    }
}