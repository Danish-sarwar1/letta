package com.health.agents.integration.letta.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class LettaMessage {
    private String id;
    
    @JsonProperty("message_type")
    private String messageType; // "reasoning_message", "tool_call_message", "tool_call_return", "assistant_message", "system_message", "user_message"
    
    private String role; // "user", "assistant", "system"
    private String content;
    
    // Additional fields that may be present in Letta API responses
    private String date;
    private String reasoning;
    
    @JsonProperty("tool_calls")
    private Object toolCalls; // Can be complex, using Object for now
    
    @JsonProperty("tool_call_id")
    private String toolCallId;
    
    // For backward compatibility, keep the simple constructor
    public LettaMessage(String role, String content) {
        this.role = role;
        this.content = content;
    }
} 