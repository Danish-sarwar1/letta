package com.health.agents.model.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;

@Data
public class StartChatRequest {
    @NotBlank(message = "User ID is required")
    private String userId;
    
    @NotBlank(message = "Session ID is required")
    private String sessionId;
} 