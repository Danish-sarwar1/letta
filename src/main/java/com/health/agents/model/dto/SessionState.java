package com.health.agents.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Session State for Sub-Plan 5: Session and State Management
 * Comprehensive session metadata tracking with state transitions and cross-session continuity
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SessionState {
    
    /**
     * Session identifier
     */
    private String sessionId;
    
    /**
     * User identifier
     */
    private String userId;
    
    /**
     * Session status (INITIALIZING, ACTIVE, PAUSED, RESUMING, ENDED, ARCHIVED)
     */
    private String status;
    
    /**
     * Session creation timestamp
     */
    private LocalDateTime createdAt;
    
    /**
     * Last activity timestamp
     */
    private LocalDateTime lastActivity;
    
    /**
     * Session duration in minutes
     */
    private Long durationMinutes;
    
    /**
     * Total number of conversation turns
     */
    private Integer totalTurns;
    
    /**
     * Current conversation topic
     */
    private String currentTopic;
    
    /**
     * Primary topics discussed in this session
     */
    private List<String> primaryTopics;
    
    /**
     * Current conversation phase (Initial Assessment, Information Gathering, etc.)
     */
    private String conversationPhase;
    
    /**
     * Last agent that responded
     */
    private String lastAgentType;
    
    /**
     * Session complexity score (based on topics, turns, agent switches)
     */
    private Double complexityScore;
    
    /**
     * Whether session requires follow-up
     */
    private Boolean requiresFollowUp;
    
    /**
     * Follow-up scheduling information
     */
    private String followUpScheduling;
    
    /**
     * Session quality assessment
     */
    private SessionQuality sessionQuality;
    
    /**
     * Memory usage statistics for this session
     */
    private MemoryUsageStats memoryUsage;
    
    /**
     * Related previous sessions for continuity
     */
    private List<String> relatedSessionIds;
    
    /**
     * Session context that should be preserved for future sessions
     */
    private String preservedContext;
    
    /**
     * User engagement metrics
     */
    private UserEngagement userEngagement;
    
    /**
     * Archival metadata
     */
    private ArchivalMetadata archivalInfo;
    
    /**
     * Session-specific flags and indicators
     */
    private Map<String, Object> sessionFlags;
    
    /**
     * Agent routing history for this session
     */
    private List<AgentInteraction> agentInteractions;
    
    /**
     * Session closure information
     */
    private SessionClosure closureInfo;
    
    // Nested classes for complex session data
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SessionQuality {
        private Double overallQuality;
        private Double contextContinuity;
        private Double agentPerformance;
        private Double userSatisfactionIndicator;
        private String qualityNotes;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MemoryUsageStats {
        private Integer conversationHistorySize;
        private Integer activeSessionSize;
        private Integer contextSummarySize;
        private Double memoryEfficiency;
        private Boolean rotationTriggered;
        private Integer archivalTriggerTurns;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserEngagement {
        private Double responseRate;
        private Double averageResponseLength;
        private Integer questionsAsked;
        private Boolean activeParticipation;
        private String engagementLevel; // HIGH, MEDIUM, LOW
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ArchivalMetadata {
        private Boolean isArchived;
        private LocalDateTime archivalTimestamp;
        private String archivalReason;
        private String archivalLocation;
        private Boolean accessibleForContinuity;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AgentInteraction {
        private String agentType;
        private LocalDateTime interactionTime;
        private Integer turnNumber;
        private String intentHandled;
        private Double responseQuality;
        private Boolean handoffOccurred;
        private String handoffReason;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SessionClosure {
        private String closureReason; // USER_ENDED, TIMEOUT, ERROR, SYSTEM_MAINTENANCE
        private Boolean properClosure;
        private String finalTopic;
        private Boolean resolutionAchieved;
        private String closureNotes;
        private List<String> recommendedFollowUps;
    }
    
    // Helper methods for session state management
    
    /**
     * Check if session is currently active
     */
    public boolean isActive() {
        return "ACTIVE".equals(status);
    }
    
    /**
     * Check if session can be resumed
     */
    public boolean canBeResumed() {
        return "PAUSED".equals(status) && !isArchived();
    }
    
    /**
     * Check if session is archived
     */
    public boolean isArchived() {
        return archivalInfo != null && archivalInfo.isArchived != null && archivalInfo.isArchived;
    }
    
    /**
     * Check if session is long-running (more than configured threshold)
     */
    public boolean isLongRunning() {
        return durationMinutes != null && durationMinutes > 60; // More than 1 hour
    }
    
    /**
     * Check if session has high complexity
     */
    public boolean isHighComplexity() {
        return complexityScore != null && complexityScore > 0.8;
    }
    
    /**
     * Check if session requires immediate attention
     */
    public boolean requiresAttention() {
        return (requiresFollowUp != null && requiresFollowUp) ||
               (sessionQuality != null && sessionQuality.overallQuality != null && sessionQuality.overallQuality < 0.6) ||
               "ERROR".equals(status);
    }
    
    /**
     * Calculate session effectiveness
     */
    public double calculateEffectiveness() {
        if (sessionQuality == null) {
            return 0.5; // Default moderate effectiveness
        }
        
        double effectiveness = 0.0;
        int factors = 0;
        
        if (sessionQuality.overallQuality != null) {
            effectiveness += sessionQuality.overallQuality * 0.4;
            factors++;
        }
        
        if (sessionQuality.contextContinuity != null) {
            effectiveness += sessionQuality.contextContinuity * 0.3;
            factors++;
        }
        
        if (sessionQuality.agentPerformance != null) {
            effectiveness += sessionQuality.agentPerformance * 0.3;
            factors++;
        }
        
        return factors > 0 ? effectiveness : 0.5;
    }
    
    /**
     * Get session summary for logging and monitoring
     */
    public String getSessionSummary() {
        return String.format("Session %s: %s, %d turns, %s phase, %.1f min duration", 
            sessionId, status, totalTurns != null ? totalTurns : 0, 
            conversationPhase != null ? conversationPhase : "Unknown",
            durationMinutes != null ? durationMinutes : 0.0);
    }
} 