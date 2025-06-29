package com.health.agents.model.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class ConversationHistory {
  private String sessionId;
  private String userId;
  private List<ConversationTurn> turns;
  private LocalDateTime createdAt;
  private LocalDateTime lastUpdated;
  private String status;
  private int totalTurns;
}

