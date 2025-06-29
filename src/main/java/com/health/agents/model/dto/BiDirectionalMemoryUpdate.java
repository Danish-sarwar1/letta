package com.health.agents.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Bidirectional Memory Update Result for Sub-Plan 4: Bidirectional Memory Updates
 * Tracks the results and effectiveness of bidirectional memory update operations
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BiDirectionalMemoryUpdate {
    
    /**
     * Session ID for the memory update
     */
    private String sessionId;
    
    /**
     * Turn number associated with this update
     */
    private Integer turnNumber;
    
    /**
     * Timestamp of the memory update operation
     */
    private LocalDateTime updateTimestamp;
    
    /**
     * Type of update operation (e.g., "AGENT_RESPONSE", "CONTEXT_FEEDBACK", "PATTERN_UPDATE")
     */
    private String updateType;
    
    /**
     * Agent response metadata that triggered this update
     */
    private AgentResponseMetadata responseMetadata;
    
    /**
     * Memory blocks that were updated
     */
    private List<String> updatedMemoryBlocks;
    
    /**
     * Success status of the memory update
     */
    private Boolean updateSuccess;
    
    /**
     * Total time taken for the update operation (in milliseconds)
     */
    private Long updateTimeMs;
    
    /**
     * Number of retry attempts made
     */
    private Integer retryAttempts;
    
    /**
     * Context feedback generated from the agent response
     */
    private String contextFeedback;
    
    /**
     * Context improvement suggestions based on response analysis
     */
    private String contextImprovements;
    
    /**
     * Pattern updates made to conversation patterns
     */
    private List<String> patternUpdates;
    
    /**
     * Memory optimization recommendations
     */
    private String memoryOptimizations;
    
    /**
     * Response quality impact on memory updates
     */
    private String qualityImpact;
    
    /**
     * Synchronization status across memory blocks
     */
    private String synchronizationStatus;
    
    /**
     * Error messages if update failed
     */
    private List<String> errorMessages;
    
    /**
     * Success indicators for the update
     */
    private List<String> successIndicators;
    
    /**
     * Memory consistency checks performed
     */
    private Map<String, Boolean> consistencyChecks;
    
    /**
     * Performance metrics for the update operation
     */
    private Map<String, Object> performanceMetrics;
    
    /**
     * Rollback information if update failed
     */
    private String rollbackInfo;
    
    /**
     * Future memory optimization suggestions
     */
    private List<String> optimizationSuggestions;
    
    /**
     * Check if update was successful
     */
    public boolean isUpdateSuccessful() {
        return updateSuccess != null && updateSuccess;
    }
    
    /**
     * Check if update required retries
     */
    public boolean requiredRetries() {
        return retryAttempts != null && retryAttempts > 0;
    }
    
    /**
     * Check if update was fast (completed within threshold)
     */
    public boolean isFastUpdate() {
        return updateTimeMs != null && updateTimeMs < 1000; // Less than 1 second
    }
    
    /**
     * Check if memory is synchronized
     */
    public boolean isMemorySynchronized() {
        return "SYNCHRONIZED".equals(synchronizationStatus);
    }
    
    /**
     * Check if there are consistency issues
     */
    public boolean hasConsistencyIssues() {
        return consistencyChecks != null && 
               consistencyChecks.values().stream().anyMatch(check -> !check);
    }
    
    /**
     * Get overall update quality score
     */
    public double getUpdateQualityScore() {
        double score = 0.0;
        int factors = 0;
        
        // Success factor
        if (isUpdateSuccessful()) {
            score += 0.4;
        }
        factors++;
        
        // Speed factor
        if (isFastUpdate()) {
            score += 0.2;
        }
        factors++;
        
        // Synchronization factor
        if (isMemorySynchronized()) {
            score += 0.2;
        }
        factors++;
        
        // Consistency factor
        if (!hasConsistencyIssues()) {
            score += 0.2;
        }
        factors++;
        
        return score;
    }
    
    /**
     * Get update summary
     */
    public String getUpdateSummary() {
        StringBuilder summary = new StringBuilder();
        
        summary.append("Update ").append(isUpdateSuccessful() ? "SUCCESS" : "FAILED");
        
        if (updatedMemoryBlocks != null) {
            summary.append(" (").append(updatedMemoryBlocks.size()).append(" blocks)");
        }
        
        if (updateTimeMs != null) {
            summary.append(" in ").append(updateTimeMs).append("ms");
        }
        
        if (requiredRetries()) {
            summary.append(" with ").append(retryAttempts).append(" retries");
        }
        
        return summary.toString();
    }
} 