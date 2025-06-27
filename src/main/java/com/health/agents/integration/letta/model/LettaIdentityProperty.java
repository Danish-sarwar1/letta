package com.health.agents.integration.letta.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LettaIdentityProperty {
    private String key;
    private String value;
    private String type; // "string", "number", "boolean"
} 