package com.health.agents.integration.letta.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class LettaLlmConfig {
    private String model;
    
    @JsonProperty("model_endpoint_type")
    private String modelEndpointType;
    
    @JsonProperty("context_window")
    private Integer contextWindow;
    
    @JsonProperty("model_endpoint")
    private String modelEndpoint;
    
    @JsonProperty("provider_name")
    private String providerName;
    
    @JsonProperty("provider_category")
    private String providerCategory;
    
    @JsonProperty("model_wrapper")
    private String modelWrapper;
    
    @JsonProperty("put_inner_thoughts_in_kwargs")
    private Boolean putInnerThoughtsInKwargs;
    
    private String handle;
    private Double temperature;
    
    @JsonProperty("max_tokens")
    private Integer maxTokens;
    
    @JsonProperty("enable_reasoner")
    private Boolean enableReasoner;
    
    @JsonProperty("reasoning_effort")
    private String reasoningEffort;
    
    @JsonProperty("max_reasoning_tokens")
    private Integer maxReasoningTokens;
} 