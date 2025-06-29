package com.health.agents.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Cross-Session Continuity for Sub-Plan 5: Session and State Management
 * Manages historical session data and enables conversation resumption across sessions
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CrossSessionContinuity {
    
    /**
     * Current session identifier
     */
    private String currentSessionId;
    
    /**
     * User identifier
     */
    private String userId;
    
    /**
     * Related session history for continuity
     */
    private List<SessionSummary> relatedSessions;
    
    /**
     * Preserved context from previous sessions
     */
    private PreservedContext preservedContext;
    
    /**
     * Session linking information
     */
    private SessionLinking sessionLinking;
    
    /**
     * Conversation resumption data
     */
    private ConversationResumption resumptionData;
    
    /**
     * Historical patterns across sessions
     */
    private HistoricalPatterns historicalPatterns;
    
    /**
     * Cross-session analytics
     */
    private CrossSessionAnalytics analytics;
    
    /**
     * Archival memory integration
     */
    private ArchivalMemoryIntegration archivalIntegration;
    
    /**
     * Continuity quality assessment
     */
    private ContinuityQuality continuityQuality;
    
    // Nested classes for complex continuity data
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SessionSummary {
        private String sessionId;
        private LocalDateTime sessionDate;
        private Long durationMinutes;
        private Integer totalTurns;
        private String primaryTopic;
        private List<String> topicsDiscussed;
        private String finalOutcome;
        private Boolean resolutionAchieved;
        private String lastAgentType;
        private Double sessionQuality;
        private Boolean archived;
        private String archivalLocation;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PreservedContext {
        private String lastTopicDiscussed;
        private String emotionalState;
        private String medicalContext;
        private String urgencyLevel;
        private List<String> ongoingConcerns;
        private String followUpNeeds;
        private String preservationMethod; // FULL_CONTEXT, SUMMARY, KEY_POINTS
        private LocalDateTime preservationTimestamp;
        private Double contextRelevance;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SessionLinking {
        private String linkingMethod; // AUTOMATIC, MANUAL, USER_REQUESTED
        private Double linkingConfidence;
        private String linkingReasoning;
        private List<String> linkedSessionIds;
        private String continuityBridge; // How sessions are connected
        private Boolean seamlessContinuity;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ConversationResumption {
        private Boolean canResume;
        private String resumptionPoint; // Where to resume conversation
        private String resumptionContext;
        private String resumptionPrompt; // What to say when resuming
        private List<String> suggestedQuestions;
        private String resumptionStrategy; // FULL_CONTEXT, SUMMARY_CONTEXT, FRESH_START
        private Double resumptionConfidence;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HistoricalPatterns {
        private Map<String, Integer> topicFrequency;
        private List<String> preferredAgentTypes;
        private String communicationStyle;
        private List<String> recurringConcerns;
        private String engagementPatterns;
        private Double averageSessionDuration;
        private String timePreferences; // When user typically interacts
        private String progressionPatterns; // How issues evolve over time
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CrossSessionAnalytics {
        private Integer totalSessions;
        private LocalDateTime firstSessionDate;
        private LocalDateTime lastSessionDate;
        private Double averageSessionQuality;
        private String overallProgressTrend; // IMPROVING, STABLE, DECLINING
        private Integer resolutionsAchieved;
        private List<String> commonIssues;
        private String userSatisfactionTrend;
        private Double continuityEffectiveness;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ArchivalMemoryIntegration {
        private Boolean archivalAccessible;
        private List<String> archivalLocations;
        private String archivalQueryStrategy;
        private Double archivalRelevance;
        private String archivalContextSummary;
        private LocalDateTime lastArchivalAccess;
        private String archivalIntegrationMethod; // QUERY_BASED, SUMMARY_BASED, FULL_RETRIEVAL
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ContinuityQuality {
        private Double overallQuality;
        private Double contextPreservation;
        private Double sessionLinking;
        private Double resumptionEffectiveness;
        private Double userExperience;
        private String qualityNotes;
        private List<String> improvementSuggestions;
    }
    
    // Helper methods for cross-session continuity
    
    /**
     * Check if continuity is available
     */
    public boolean hasContinuity() {
        return relatedSessions != null && !relatedSessions.isEmpty();
    }
    
    /**
     * Check if conversation can be resumed seamlessly
     */
    public boolean canResumeSeamlessly() {
        return resumptionData != null && 
               resumptionData.canResume != null && 
               resumptionData.canResume &&
               resumptionData.resumptionConfidence != null &&
               resumptionData.resumptionConfidence > 0.7;
    }
    
    /**
     * Check if preserved context is still relevant
     */
    public boolean hasRelevantPreservedContext() {
        return preservedContext != null && 
               preservedContext.contextRelevance != null &&
               preservedContext.contextRelevance > 0.6;
    }
    
    /**
     * Check if archival memory is accessible
     */
    public boolean hasArchivalAccess() {
        return archivalIntegration != null && 
               archivalIntegration.archivalAccessible != null &&
               archivalIntegration.archivalAccessible;
    }
    
    /**
     * Get the most recent related session
     */
    public SessionSummary getMostRecentSession() {
        if (relatedSessions == null || relatedSessions.isEmpty()) {
            return null;
        }
        
        return relatedSessions.stream()
            .filter(session -> session.sessionDate != null)
            .max((s1, s2) -> s1.sessionDate.compareTo(s2.sessionDate))
            .orElse(null);
    }
    
    /**
     * Calculate overall continuity effectiveness
     */
    public double calculateContinuityEffectiveness() {
        if (continuityQuality == null) {
            return 0.5; // Default moderate effectiveness
        }
        
        double effectiveness = 0.0;
        int factors = 0;
        
        if (continuityQuality.overallQuality != null) {
            effectiveness += continuityQuality.overallQuality * 0.3;
            factors++;
        }
        
        if (continuityQuality.contextPreservation != null) {
            effectiveness += continuityQuality.contextPreservation * 0.25;
            factors++;
        }
        
        if (continuityQuality.sessionLinking != null) {
            effectiveness += continuityQuality.sessionLinking * 0.2;
            factors++;
        }
        
        if (continuityQuality.resumptionEffectiveness != null) {
            effectiveness += continuityQuality.resumptionEffectiveness * 0.25;
            factors++;
        }
        
        return factors > 0 ? effectiveness : 0.5;
    }
    
    /**
     * Generate resumption prompt for the current session
     */
    public String generateResumptionPrompt() {
        if (resumptionData == null || !canResumeSeamlessly()) {
            return "Welcome back! How can I help you today?";
        }
        
        if (resumptionData.resumptionPrompt != null && !resumptionData.resumptionPrompt.isEmpty()) {
            return resumptionData.resumptionPrompt;
        }
        
        StringBuilder prompt = new StringBuilder("Welcome back! ");
        
        if (preservedContext != null && preservedContext.lastTopicDiscussed != null) {
            prompt.append("I see we were discussing ").append(preservedContext.lastTopicDiscussed).append(". ");
        }
        
        if (preservedContext != null && preservedContext.followUpNeeds != null) {
            prompt.append(preservedContext.followUpNeeds).append(" ");
        }
        
        prompt.append("How are you feeling today?");
        
        return prompt.toString();
    }
    
    /**
     * Get continuity summary for logging and monitoring
     */
    public String getContinuitySummary() {
        StringBuilder summary = new StringBuilder();
        summary.append("Continuity for user ").append(userId).append(": ");
        
        if (hasContinuity()) {
            summary.append(relatedSessions.size()).append(" related sessions, ");
        }
        
        if (canResumeSeamlessly()) {
            summary.append("seamless resumption available, ");
        }
        
        if (hasRelevantPreservedContext()) {
            summary.append("relevant context preserved, ");
        }
        
        summary.append("effectiveness: ").append(String.format("%.2f", calculateContinuityEffectiveness()));
        
        return summary.toString();
    }
    
    /**
     * Check if user has established interaction patterns
     */
    public boolean hasEstablishedPatterns() {
        return historicalPatterns != null && 
               analytics != null && 
               analytics.totalSessions != null && 
               analytics.totalSessions >= 3;
    }
} 