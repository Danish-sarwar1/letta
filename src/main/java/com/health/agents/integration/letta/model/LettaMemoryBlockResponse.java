package com.health.agents.integration.letta.model;

import java.util.Map;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class LettaMemoryBlockResponse {
    private String value;
    private Integer limit;
    private String name;
    @JsonProperty("is_template")
    private Boolean isTemplate;
    @JsonProperty("preserve_on_migration")
    private Boolean preserveOnMigration;
    private String label;
    @JsonProperty("read_only")
    private Boolean readOnly;
    private String description;
    private Map<String, Object> metadata;
    private String id;
    @JsonProperty("created_by_id")
    private String createdById;
    @JsonProperty("last_updated_by_id")
    private String lastUpdatedById;
    @JsonProperty("organization_id")
    private String organizationId;
}


