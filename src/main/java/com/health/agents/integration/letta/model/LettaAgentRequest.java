package com.health.agents.integration.letta.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LettaAgentRequest {
    private String name;
    private String description;
    
    @JsonProperty("identity_ids")
    private List<String> identityIds;
    
    @JsonProperty("memory_blocks")
    private List<LettaMemoryBlock> memoryBlocks;
    
    private String model;
    private String embedding;
    
    // Tools should be a list of strings, not a map
    private List<String> tools;
    
    // Additional fields that might be required by the API
    @JsonProperty("context_window_limit")
    private Integer contextWindowLimit;
    
    @JsonProperty("block_ids")
    private List<String> blockIds;
    
    // Metadata should remain as map
    private Map<String, Object> metadata;
}

