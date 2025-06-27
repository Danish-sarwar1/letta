package com.health.agents.integration.letta.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class LettaEmbeddingConfig {
    @JsonProperty("embedding_endpoint_type")
    private String embeddingEndpointType;
    
    @JsonProperty("embedding_model")
    private String embeddingModel;
    
    @JsonProperty("embedding_dim")
    private Integer embeddingDim;
    
    @JsonProperty("embedding_endpoint")
    private String embeddingEndpoint;
    
    @JsonProperty("embedding_chunk_size")
    private Integer embeddingChunkSize;
    
    private String handle;
    
    @JsonProperty("azure_endpoint")
    private String azureEndpoint;
    
    @JsonProperty("azure_version")
    private String azureVersion;
    
    @JsonProperty("azure_deployment")
    private String azureDeployment;
} 