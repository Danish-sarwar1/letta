package com.health.agents.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Context Enrichment Result for Sub-Plan 3: Enhanced Context Extraction
 * Contains enriched context information with confidence scores and conversation patterns
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContextEnrichmentResult {
    
    /**
     * The enriched context message with all context sections
     */
    private String enrichedMessage;
    
    /**
     * Description of the context used for enrichment
     */
    private String contextUsed;
    
    /**
     * Reasoning behind the context selection
     */
    private String reasoning;
    
    /**
     * Number of relevant turns used for context
     */
    private int relevantTurns;
    
    /**
     * Confidence score for the context enrichment (0.0 to 1.0)
     */
    private double contextConfidence;
    
    /**
     * Analysis of conversation patterns
     */
    private ConversationPatterns conversationPatterns;
    
    /**
     * Timestamp of when the enrichment was performed
     */
    private java.time.LocalDateTime enrichmentTimestamp;
    
    /**
     * Additional metadata about the enrichment process
     */
    private java.util.Map<String, Object> enrichmentMetadata;
} 