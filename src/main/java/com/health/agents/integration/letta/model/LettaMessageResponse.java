package com.health.agents.integration.letta.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class LettaMessageResponse {
    private String id;
    private List<LettaMessage> messages;
    
    @JsonProperty("agent_id")
    private String agentId;
    
    @JsonProperty("sender_id")
    private String senderId;
    
    @JsonProperty("created_at")
    private LocalDateTime createdAt;
    
    private String status;
    
    // Usage information that comes with the response
    private Usage usage;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Usage {
        @JsonProperty("completion_tokens")
        private Integer completionTokens;
        
        @JsonProperty("prompt_tokens")
        private Integer promptTokens;
        
        @JsonProperty("total_tokens")
        private Integer totalTokens;
        
        @JsonProperty("step_count")
        private Integer stepCount;
    }
} 