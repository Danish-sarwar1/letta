package com.health.agents.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Agent Response Metadata for Sub-Plan 4: Bidirectional Memory Updates  
 * Captures detailed metadata about agent responses for analysis and feedback loops
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentResponseMetadata {
    
    /**
     * The agent that generated the response
     */
    private String agentId;
    
    /**
     * Type of agent (e.g., "General Health", "Mental Health", "Emergency")
     */
    private String agentType;
    
    /**
     * Timestamp when the response was generated
     */
    private LocalDateTime responseTimestamp;
    
    /**
     * Time taken to generate the response (in milliseconds)
     */
    private Long responseTimeMs;
    
    /**
     * Length of the response in characters
     */
    private Integer responseLength;
    
    /**
     * Quality score of the response (0.0 to 1.0)
     */
    private Double responseQuality;
    
    /**
     * Relevance score of the response to the context (0.0 to 1.0)
     */
    private Double contextRelevance;
    
    /**
     * Confidence score of the response (0.0 to 1.0)
     */
    private Double responseConfidence;
    
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
     * Whether the response required follow-up
     */
    private Boolean requiresFollowUp;
    
    /**
     * Detected sentiment of the response (e.g., "Supportive", "Professional", "Empathetic")
     */
    private String responseSentiment;
    
    /**
     * Key topics addressed in the response
     */
    private String addressedTopics;
    
    /**
     * Medical advice level (e.g., "General", "Specific", "Referral")
     */
    private String medicalAdviceLevel;
    
    /**
     * Whether the response included safety warnings
     */
    private Boolean includedSafetyWarnings;
    
    /**
     * Context utilization analysis - how well the agent used the provided context
     */
    private String contextUtilization;
    
    /**
     * Feedback for context selection - what context would have been more helpful
     */
    private String contextFeedback;
    
    /**
     * Response pattern analysis - recurring patterns in responses
     */
    private String responsePatterns;
    
    /**
     * Additional analysis metadata
     */
    private Map<String, Object> analysisMetadata;
    
    /**
     * Error indicators if the response had issues
     */
    private String errorIndicators;
    
    /**
     * Success indicators showing response effectiveness
     */
    private String successIndicators;
    
    /**
     * Get overall response effectiveness score
     */
    public double getOverallEffectiveness() {
        if (responseQuality == null || contextRelevance == null || responseConfidence == null) {
            return 0.5; // Default moderate score
        }
        
        return (responseQuality + contextRelevance + responseConfidence) / 3.0;
    }
    
    /**
     * Check if response is high quality
     */
    public boolean isHighQuality() {
        return getOverallEffectiveness() >= 0.8;
    }
    
    /**
     * Check if response needs improvement
     */
    public boolean needsImprovement() {
        return getOverallEffectiveness() < 0.6;
    }
    
    /**
     * Get quality level as string
     */
    public String getQualityLevel() {
        double effectiveness = getOverallEffectiveness();
        if (effectiveness >= 0.8) {
            return "EXCELLENT";
        } else if (effectiveness >= 0.6) {
            return "GOOD";
        } else if (effectiveness >= 0.4) {
            return "FAIR";
        } else {
            return "POOR";
        }
    }
} 