package com.health.agents.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Session Transition for Sub-Plan 5: Session and State Management
 * Tracks session state changes, transitions, and boundary events
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SessionTransition {
    
    /**
     * Session identifier
     */
    private String sessionId;
    
    /**
     * User identifier
     */
    private String userId;
    
    /**
     * Previous session status
     */
    private String fromStatus;
    
    /**
     * New session status
     */
    private String toStatus;
    
    /**
     * Transition timestamp
     */
    private LocalDateTime transitionTime;
    
    /**
     * Transition type (INITIALIZATION, ACTIVATION, PAUSE, RESUME, TERMINATION, ARCHIVAL)
     */
    private String transitionType;
    
    /**
     * Reason for the transition
     */
    private String transitionReason;
    
    /**
     * Turn number when transition occurred
     */
    private Integer turnNumber;
    
    /**
     * Duration of previous state in minutes
     */
    private Long previousStateDurationMinutes;
    
    /**
     * Triggered by user action or system event
     */
    private String triggerSource; // USER, SYSTEM, TIMEOUT, ERROR
    
    /**
     * Whether this is an automatic or manual transition
     */
    private Boolean automaticTransition;
    
    /**
     * Context preserved during transition
     */
    private String preservedContext;
    
    /**
     * Memory operations performed during transition
     */
    private MemoryOperations memoryOperations;
    
    /**
     * Agent state during transition
     */
    private AgentStateTransition agentStateTransition;
    
    /**
     * Additional metadata about the transition
     */
    private Map<String, Object> transitionMetadata;
    
    /**
     * Success status of the transition
     */
    private Boolean transitionSuccess;
    
    /**
     * Error information if transition failed
     */
    private String errorInfo;
    
    /**
     * Cross-session continuity information
     */
    private CrossSessionContinuity crossSessionInfo;
    
    // Nested classes for complex transition data
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MemoryOperations {
        private Boolean memoryArchived;
        private Boolean memoryCleared;
        private Boolean memoryPreserved;
        private String archivalLocation;
        private Integer memoryBlocksAffected;
        private String memoryOperationDetails;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AgentStateTransition {
        private String activeAgentBefore;
        private String activeAgentAfter;
        private Boolean agentHandoffOccurred;
        private String agentStatePreserved;
        private String agentContextTransferred;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CrossSessionContinuity {
        private String previousSessionId;
        private String nextSessionId;
        private Boolean contextLinked;
        private String continuityMethod; // MEMORY_TRANSFER, CONTEXT_SUMMARY, FRESH_START
        private Double continuityScore;
        private String continuityNotes;
    }
    
    // Helper methods for transition analysis
    
    /**
     * Check if this is a session boundary transition (start/end)
     */
    public boolean isSessionBoundary() {
        return "INITIALIZATION".equals(transitionType) || 
               "TERMINATION".equals(transitionType) ||
               "ARCHIVAL".equals(transitionType);
    }
    
    /**
     * Check if this is a state preservation transition
     */
    public boolean isStatePreservation() {
        return "PAUSE".equals(transitionType) || 
               "RESUME".equals(transitionType);
    }
    
    /**
     * Check if transition was successful
     */
    public boolean wasSuccessful() {
        return transitionSuccess != null && transitionSuccess;
    }
    
    /**
     * Check if memory was handled properly during transition
     */
    public boolean wasMemoryHandledProperly() {
        return memoryOperations != null && 
               (memoryOperations.memoryArchived || 
                memoryOperations.memoryCleared || 
                memoryOperations.memoryPreserved);
    }
    
    /**
     * Check if cross-session continuity was maintained
     */
    public boolean hasCrossSessionContinuity() {
        return crossSessionInfo != null && 
               crossSessionInfo.contextLinked != null && 
               crossSessionInfo.contextLinked;
    }
    
    /**
     * Get transition effectiveness score
     */
    public double getTransitionEffectiveness() {
        double score = 0.0;
        int factors = 0;
        
        // Success factor
        if (wasSuccessful()) {
            score += 0.4;
        }
        factors++;
        
        // Memory handling factor
        if (wasMemoryHandledProperly()) {
            score += 0.3;
        }
        factors++;
        
        // Continuity factor
        if (hasCrossSessionContinuity() && crossSessionInfo.continuityScore != null) {
            score += crossSessionInfo.continuityScore * 0.3;
        } else {
            score += 0.15; // Default moderate continuity
        }
        factors++;
        
        return score;
    }
    
    /**
     * Get transition summary for logging
     */
    public String getTransitionSummary() {
        return String.format("Session %s: %s -> %s (%s) at turn %d, trigger: %s, success: %s", 
            sessionId, fromStatus, toStatus, transitionType, 
            turnNumber != null ? turnNumber : 0, triggerSource, 
            wasSuccessful() ? "YES" : "NO");
    }
    
    /**
     * Check if transition indicates session quality issues
     */
    public boolean indicatesQualityIssues() {
        return "ERROR".equals(triggerSource) || 
               !wasSuccessful() ||
               ("TERMINATION".equals(transitionType) && previousStateDurationMinutes != null && previousStateDurationMinutes < 5);
    }
} 