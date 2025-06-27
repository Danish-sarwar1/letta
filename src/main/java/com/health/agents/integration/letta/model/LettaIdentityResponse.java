package com.health.agents.integration.letta.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class LettaIdentityResponse {
    private String id;
    private String name;
    private String description;
    
    @JsonProperty("identifier_key")
    private String identifierKey;
    
    @JsonProperty("identity_type")
    private String identityType;
    
    @JsonProperty("project_id")
    private String projectId;
    
    @JsonProperty("agent_ids")
    private List<String> agentIds;
    
    @JsonProperty("block_ids")
    private List<String> blockIds;
    
    @JsonProperty("organization_id")
    private String organizationId;
    
    @JsonProperty("created_at")
    private LocalDateTime createdAt;
    
    private List<LettaIdentityProperty> properties;
} 