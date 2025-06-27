package com.health.agents.integration.letta.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class LettaMessageRequest {
    private List<LettaMessage> messages;
    
    @JsonProperty("sender_id")
    private String senderId;
} 