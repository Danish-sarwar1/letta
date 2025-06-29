package com.health.agents.model.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * Chat Response DTO enhanced for Sub-Plan 4: Bidirectional Memory Updates
 * Includes context confidence, conversation patterns, bidirectional memory analysis, and response quality metrics
 */
@Data
@Builder
public class ChatResponse {
    private String message;
    private String sessionId;
    private String userId;
    private String intent;
    private Double confidence;
    private Boolean sessionActive;
    private LocalDateTime timestamp;
    
    // ===== SUB-PLAN 3: Enhanced Context Fields =====
    
    /**
     * Confidence score for the context enrichment (0.0 to 1.0)
     */
    private Double contextConfidence;
    
    /**
     * Number of relevant turns used for context enrichment
     */
    private Integer relevantTurns;
    
    /**
     * Current phase of the conversation (e.g., "Initial Assessment", "Active Discussion")
     */
    private String conversationPhase;
    
    /**
     * Context enrichment strategy used
     */
    private String contextStrategy;
    
    /**
     * Indication of whether conversation patterns were detected
     */
    private Boolean patternsDetected;
    
    /**
     * Confidence level of the context analysis (HIGH, MEDIUM, LOW)
     */
    private String contextConfidenceLevel;
    
    // ===== SUB-PLAN 4: Bidirectional Memory Updates Fields =====
    
    /**
     * Quality score of the agent response (0.0 to 1.0)
     */
    private Double responseQuality;
    
    /**
     * How well the agent utilized the provided context (0.0 to 1.0)
     */
    private Double contextUtilization;
    
    /**
     * Correlation between response quality and context quality (0.0 to 1.0)
     */
    private Double responseContextCorrelation;
    
    /**
     * Whether bidirectional memory update was successful
     */
    private Boolean bidirectionalUpdateSuccess;
    
    /**
     * Number of memory blocks updated during bidirectional sync
     */
    private Integer updatedMemoryBlocks;
    
    /**
     * Time taken for bidirectional memory update (in milliseconds)
     */
    private Long bidirectionalUpdateTimeMs;
    
    /**
     * Whether the response generated feedback for improving future context selection
     */
    private Boolean generatedContextFeedback;
    
    /**
     * Response quality level (EXCELLENT, GOOD, FAIR, POOR)
     */
    private String responseQualityLevel;
    
    /**
     * Medical accuracy score for health-related responses (0.0 to 1.0)
     */
    private Double medicalAccuracy;
    
    /**
     * Emotional appropriateness score (0.0 to 1.0)
     */
    private Double emotionalAppropriateness;
    
    /**
     * Whether the response addressed the user's concern
     */
    private Boolean addressedConcern;
    
    /**
     * Whether the response indicates a follow-up is needed
     */
    private Boolean requiresFollowUp;
    
    /**
     * Memory consistency status after bidirectional update
     */
    private String memoryConsistencyStatus;
    
    /**
     * Overall effectiveness score combining all metrics (0.0 to 1.0)
     */
    private Double overallEffectiveness;
    
    /**
     * Get context confidence level as string
     */
    public String getContextConfidenceLevel() {
        if (contextConfidence == null) {
            return "UNKNOWN";
        }
        
        if (contextConfidence >= 0.8) {
            return "HIGH";
        } else if (contextConfidence >= 0.5) {
            return "MEDIUM";
        } else {
            return "LOW";
        }
    }
    
    /**
     * Check if context has high confidence
     */
    public boolean hasHighContextConfidence() {
        return contextConfidence != null && contextConfidence >= 0.8;
    }
    
    /**
     * Check if patterns were detected
     */
    public boolean hasPatternsDetected() {
        return patternsDetected != null && patternsDetected;
    }
    
    // ===== SUB-PLAN 4: New Helper Methods =====
    
    /**
     * Check if response has high quality
     */
    public boolean hasHighQualityResponse() {
        return responseQuality != null && responseQuality >= 0.8;
    }
    
    /**
     * Check if context was well utilized
     */
    public boolean hasGoodContextUtilization() {
        return contextUtilization != null && contextUtilization >= 0.7;
    }
    
    /**
     * Check if bidirectional update was successful
     */
    public boolean isBidirectionalUpdateSuccessful() {
        return bidirectionalUpdateSuccess != null && bidirectionalUpdateSuccess;
    }
    
    /**
     * Check if memory is consistent
     */
    public boolean isMemoryConsistent() {
        return "CONSISTENT".equals(memoryConsistencyStatus) || "SYNCHRONIZED".equals(memoryConsistencyStatus);
    }
    
    /**
     * Get response quality level as string
     */
    public String getResponseQualityLevel() {
        if (responseQuality == null) {
            return "UNKNOWN";
        }
        
        if (responseQuality >= 0.8) {
            return "EXCELLENT";
        } else if (responseQuality >= 0.6) {
            return "GOOD";
        } else if (responseQuality >= 0.4) {
            return "FAIR";
        } else {
            return "POOR";
        }
    }
    
    /**
     * Calculate overall effectiveness combining all metrics
     */
    public double calculateOverallEffectiveness() {
        if (contextConfidence == null || responseQuality == null) {
            return 0.5; // Default moderate effectiveness
        }
        
        double effectiveness = 0.0;
        int factors = 0;
        
        // Context confidence factor (weight: 0.3)
        effectiveness += contextConfidence * 0.3;
        factors++;
        
        // Response quality factor (weight: 0.4)
        effectiveness += responseQuality * 0.4;
        factors++;
        
        // Context utilization factor (weight: 0.2)
        if (contextUtilization != null) {
            effectiveness += contextUtilization * 0.2;
            factors++;
        }
        
        // Bidirectional update success factor (weight: 0.1)
        if (isBidirectionalUpdateSuccessful()) {
            effectiveness += 0.1;
        }
        factors++;
        
        return effectiveness;
    }
    
    /**
     * Get a comprehensive summary of the enhanced context and bidirectional information
     */
    public String getContextSummary() {
        if (contextConfidence == null) {
            return "No context analysis available";
        }
        
        StringBuilder summary = new StringBuilder();
        summary.append("Context: ").append(getContextConfidenceLevel());
        
        if (relevantTurns != null && relevantTurns > 0) {
            summary.append(", ").append(relevantTurns).append(" turns");
        }
        
        if (conversationPhase != null) {
            summary.append(", Phase: ").append(conversationPhase);
        }
        
        // Add bidirectional memory information
        if (responseQuality != null) {
            summary.append(", Response: ").append(getResponseQualityLevel());
        }
        
        if (isBidirectionalUpdateSuccessful()) {
            summary.append(", Memory: Synced");
        }
        
        return summary.toString();
    }
    
    /**
     * Get a detailed performance summary for monitoring
     */
    public String getPerformanceSummary() {
        StringBuilder performance = new StringBuilder();
        
        performance.append("Effectiveness: ").append(String.format("%.1f%%", calculateOverallEffectiveness() * 100));
        
        if (bidirectionalUpdateTimeMs != null) {
            performance.append(", Update Time: ").append(bidirectionalUpdateTimeMs).append("ms");
        }
        
        if (updatedMemoryBlocks != null) {
            performance.append(", Blocks Updated: ").append(updatedMemoryBlocks);
        }
        
        if (generatedContextFeedback != null && generatedContextFeedback) {
            performance.append(", Feedback Generated");
        }
        
        return performance.toString();
    }
    
    /**
     * Check if the response requires special attention or review
     */
    public boolean requiresSpecialAttention() {
        return (responseQuality != null && responseQuality < 0.6) ||
               (contextConfidence != null && contextConfidence < 0.5) ||
               (bidirectionalUpdateSuccess != null && !bidirectionalUpdateSuccess) ||
               (requiresFollowUp != null && requiresFollowUp);
    }
} 