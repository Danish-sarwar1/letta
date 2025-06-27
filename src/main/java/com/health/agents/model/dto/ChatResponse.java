package com.health.agents.model.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class ChatResponse {
    private String message;
    private String sessionId;
    private String userId;
    private String intent;
    private Double confidence;
    private Boolean sessionActive;
    private LocalDateTime timestamp;
} 