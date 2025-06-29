package com.health.agents.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Context Selection Result for Sub-Plan 3: Enhanced Context Extraction
 * Contains the results of the sophisticated context selection algorithm
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContextSelectionResult {
    
    /**
     * List of conversation turns selected as relevant for context
     */
    private List<ConversationTurn> selectedTurns;
    
    /**
     * Confidence score for the context selection (0.0 to 1.0)
     */
    private double confidenceScore;
    
    /**
     * Reasoning behind the selection of these turns
     */
    private String selectionReasoning;
    
    /**
     * Human-readable description of the context that was selected
     */
    private String contextDescription;
    
    /**
     * Strategy used for context selection (e.g., "Recent", "Topic-based", "Medical")
     */
    private String selectionStrategy;
    
    /**
     * Metadata about the selection process
     */
    private java.util.Map<String, Object> selectionMetadata;
    
    /**
     * Get the number of selected turns
     */
    public int getSelectedTurnCount() {
        return selectedTurns != null ? selectedTurns.size() : 0;
    }
    
    /**
     * Check if the selection has high confidence
     */
    public boolean hasHighConfidence() {
        return confidenceScore >= 0.8;
    }
    
    /**
     * Check if the selection has medium confidence
     */
    public boolean hasMediumConfidence() {
        return confidenceScore >= 0.5 && confidenceScore < 0.8;
    }
    
    /**
     * Check if the selection has low confidence
     */
    public boolean hasLowConfidence() {
        return confidenceScore < 0.5;
    }
    
    /**
     * Get confidence level as string
     */
    public String getConfidenceLevel() {
        if (hasHighConfidence()) {
            return "HIGH";
        } else if (hasMediumConfidence()) {
            return "MEDIUM";
        } else {
            return "LOW";
        }
    }
} 