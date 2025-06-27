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
public class LettaMemoryBlock {
    private String label;
    private String value;
    private String description;
    private Integer limit;
    
    @JsonProperty("read_only")
    private Boolean readOnly;
} 