package com.health.agents.model.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EnrichedContext {
  private String currentMessage;
  private String conversationContext;
  private String activeAgent;
  private String routingSuggestion;
  private String confidence;
  private String reasoning;
  private String sessionId;
}

