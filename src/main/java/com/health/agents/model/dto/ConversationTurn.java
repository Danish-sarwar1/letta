package com.health.agents.model.dto;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

/**
 * Enhanced Conversation Turn for Sub-Plan 4: Bidirectional Memory Updates
 * Includes agent response metadata and bidirectional memory update tracking
 */
@Data
@Builder
public class ConversationTurn {
  private int turnNumber;
  private String userMessage;
  private String enrichedMessage;
  private String extractedIntent;
  private Double intentConfidence;
  private String routedAgent;
  private String agentResponse;
  private LocalDateTime timestamp;
  private String sessionId;
  private String contextUsed;
  private String reasoning;
  
  // Metadata for memory management
  private String turnType; // USER_MESSAGE, AGENT_RESPONSE, SYSTEM_MESSAGE
  private boolean isArchived;
  private String topicTags;
  private String emotionalState;
  private String urgencyLevel;
  
  // ===== SUB-PLAN 4: Bidirectional Memory Updates Fields =====
  
  /**
   * Detailed metadata about the agent response for analysis and feedback
   */
  private AgentResponseMetadata responseMetadata;
  
  /**
   * Bidirectional memory update results for this turn
   */
  private BiDirectionalMemoryUpdate memoryUpdateResult;
  
  /**
   * Context feedback generated from analyzing the agent response
   */
  private String contextFeedback;
  
  /**
   * Response quality score (0.0 to 1.0) from bidirectional analysis
   */
  private Double responseQuality;
  
  /**
   * Context utilization score - how well the agent used the provided context
   */
  private Double contextUtilization;
  
  /**
   * Response-context correlation score - how well context matched response needs
   */
  private Double responseContextCorrelation;
  
  /**
   * Whether this turn generated feedback for future context selection
   */
  private Boolean generatedContextFeedback;
  
  /**
   * Follow-up recommendations generated from response analysis
   */
  private String followUpRecommendations;
  
  /**
   * Memory consistency status after bidirectional update
   */
  private String memoryConsistencyStatus;
  
  /**
   * Pattern updates triggered by this turn's response analysis
   */
  private String patternUpdatesTriggered;
  
  /**
   * Response analysis timestamp
   */
  private LocalDateTime responseAnalysisTimestamp;
  
  /**
   * Whether the response required manual review or intervention
   */
  private Boolean requiresManualReview;
  
  /**
   * Success indicators from the bidirectional memory update
   */
  private String updateSuccessIndicators;
  
  /**
   * Error indicators if there were issues with the bidirectional update
   */
  private String updateErrorIndicators;
  
  // Helper methods for Sub-Plan 4
  
  /**
   * Check if turn has high-quality response
   */
  public boolean hasHighQualityResponse() {
    return responseQuality != null && responseQuality >= 0.8;
  }
  
  /**
   * Check if context was well utilized by the agent
   */
  public boolean hasGoodContextUtilization() {
    return contextUtilization != null && contextUtilization >= 0.7;
  }
  
  /**
   * Check if response-context correlation is strong
   */
  public boolean hasStrongResponseContextCorrelation() {
    return responseContextCorrelation != null && responseContextCorrelation >= 0.8;
  }
  
  /**
   * Check if turn is complete with all bidirectional updates
   */
  public boolean isBidirectionallyComplete() {
    return agentResponse != null && 
           responseMetadata != null && 
           memoryUpdateResult != null && 
           memoryUpdateResult.isUpdateSuccessful();
  }
  
  /**
   * Get overall turn effectiveness score combining all metrics
   */
  public double getTurnEffectiveness() {
    if (responseQuality == null || contextUtilization == null || responseContextCorrelation == null) {
      return 0.5; // Default moderate score
    }
    
    return (responseQuality + contextUtilization + responseContextCorrelation) / 3.0;
  }
  
  /**
   * Get turn status summary
   */
  public String getTurnStatusSummary() {
    StringBuilder status = new StringBuilder();
    
    status.append("Turn ").append(turnNumber).append(": ");
    
    if (hasHighQualityResponse()) {
      status.append("High Quality Response");
    } else if (responseQuality != null && responseQuality >= 0.6) {
      status.append("Good Response");
    } else {
      status.append("Response Needs Improvement");
    }
    
    if (hasGoodContextUtilization()) {
      status.append(", Good Context Use");
    }
    
    if (isBidirectionallyComplete()) {
      status.append(", Memory Synced");
    }
    
    return status.toString();
  }
}
