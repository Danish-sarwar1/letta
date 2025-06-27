package com.health.agents.integration.letta.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
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
} 