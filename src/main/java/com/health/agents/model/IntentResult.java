package com.health.agents.model;

import com.health.agents.model.enums.IntentType;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class IntentResult {
    private IntentType intent;
    private Double confidence;
    private List<String> primaryKeywords;
    private String reasoning;
    private IntentType secondaryDomain;
    private String urgencyLevel;
    private String routingNotes;
} 