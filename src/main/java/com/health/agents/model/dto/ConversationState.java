package com.health.agents.model.dto;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ConversationState {
  private String activeAgent;
  private String currentTopic;
  private String conversationStage;
  private String sessionId;
  private int turnCount;
  private LocalDateTime lastUpdate;
}
