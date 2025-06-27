package com.health.agents.integration.letta.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LettaMessage {
    private String role; // "user", "assistant", "system"
    private String content;
} 