package com.health.agents.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder; 
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Conversation Trends for Sub-Plan 3: Enhanced Context Extraction
 * Captures detected trends in conversation patterns for better context understanding
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConversationTrends {
    
    /**
     * Whether the conversation is becoming more complex over time
     */
    private boolean increasingComplexity;
    
    /**
     * Whether there has been a shift in topics during the conversation
     */
    private boolean topicShift;
    
    /**
     * Depth of the conversation (number of turns)
     */
    private int conversationDepth;
    
    /**
     * Whether the user is showing signs of escalating concern
     */
    private boolean escalatingConcern;
    
    /**
     * Whether the conversation is moving towards resolution
     */
    private boolean movingTowardsResolution;
    
    /**
     * Dominant trend direction (e.g., "Deepening", "Broadening", "Stabilizing")
     */
    private String dominantTrend;
    
    /**
     * Confidence in trend analysis (0.0 to 1.0)
     */
    private double trendConfidence;
    
    /**
     * Get a human-readable description of the conversation trends
     */
    public String getTrendDescription() {
        StringBuilder description = new StringBuilder();
        
        if (increasingComplexity) {
            description.append("The conversation is becoming more detailed and complex. ");
        }
        
        if (topicShift) {
            description.append("There has been a shift in discussion topics. ");
        }
        
        if (escalatingConcern) {
            description.append("The user is showing increasing concern. ");
        }
        
        if (movingTowardsResolution) {
            description.append("The conversation is progressing towards resolution. ");
        }
        
        if (description.length() == 0) {
            description.append("The conversation is maintaining a steady pattern.");
        }
        
        return description.toString().trim();
    }
    
    /**
     * Determine if the conversation requires special attention
     */
    public boolean requiresSpecialAttention() {
        return escalatingConcern || (increasingComplexity && conversationDepth > 10);
    }
} 