package com.health.agents.integration.letta.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class LettaTool {
    private String id;
    
    @JsonProperty("tool_type")
    private String toolType;
    
    private String description;
    
    @JsonProperty("source_type")
    private String sourceType;
    
    private String name;
    private List<String> tags;
    
    @JsonProperty("source_code")
    private String sourceCode;
    
    @JsonProperty("json_schema")
    private Map<String, Object> jsonSchema;
    
    @JsonProperty("args_json_schema")
    private Map<String, Object> argsJsonSchema;
    
    @JsonProperty("return_char_limit")
    private Integer returnCharLimit;
    
    @JsonProperty("pip_requirements")
    private List<PipRequirement> pipRequirements;
    
    @JsonProperty("created_by_id")
    private String createdById;
    
    @JsonProperty("last_updated_by_id")
    private String lastUpdatedById;
    
    @JsonProperty("metadata_")
    private Map<String, Object> metadata;
    
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PipRequirement {
        private String name;
        private String version;
    }
} 