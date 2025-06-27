package com.health.agents.integration.letta.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LettaIdentityRequest {
    private String name;
    
    @JsonProperty("identifier_key")
    private String identifierKey;
    
    @JsonProperty("identity_type")
    private String identityType;
} 