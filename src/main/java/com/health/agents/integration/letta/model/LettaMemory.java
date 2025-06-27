package com.health.agents.integration.letta.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class LettaMemory {
    private List<LettaMemoryBlockFull> blocks;
    
    @JsonProperty("file_blocks")
    private List<LettaMemoryBlockFull> fileBlocks;
    
    @JsonProperty("prompt_template")
    private String promptTemplate;
} 