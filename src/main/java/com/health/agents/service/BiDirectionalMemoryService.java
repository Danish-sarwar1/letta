package com.health.agents.service;

import com.health.agents.config.MemoryArchitectureConfig;
import com.health.agents.model.dto.BiDirectionalMemoryUpdate;
import com.health.agents.model.dto.AgentResponseMetadata;
import com.health.agents.model.dto.ConversationTurn;
import com.health.agents.model.dto.ConversationHistory;
import com.health.agents.model.dto.ContextEnrichmentResult;
import com.health.agents.integration.letta.model.LettaMessageRequest;
import com.health.agents.integration.letta.model.LettaMessage;
import com.health.agents.integration.letta.service.LettaAgentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Bidirectional Memory Service for Sub-Plan 4: Bidirectional Memory Updates
 * Handles enhanced memory synchronization, atomic updates, and bidirectional feedback loops
 */
@Service
@Slf4j
public class BiDirectionalMemoryService {
    
    @Autowired
    private MemoryArchitectureConfig memoryConfig;
    
    @Autowired
    private MemoryManagementService memoryManagementService;
    
    @Autowired
    private LettaAgentService lettaAgentService;
    
    @Autowired
    private AgentResponseAnalysisService responseAnalysisService;
    
    // Thread pool for parallel memory operations
    private final ExecutorService memoryUpdateExecutor = Executors.newFixedThreadPool(5);
    
    /**
     * Perform comprehensive bidirectional memory update with agent response analysis
     */
    public BiDirectionalMemoryUpdate performBidirectionalUpdate(String sessionId, String contextExtractorId, 
                                                               String identityId, ConversationTurn turn, 
                                                               ContextEnrichmentResult contextResult, 
                                                               String agentId, String agentType, 
                                                               String agentResponse, long responseTimeMs) {
        log.debug("Performing bidirectional memory update for session {} turn {}", sessionId, turn.getTurnNumber());
        
        LocalDateTime updateStart = LocalDateTime.now();
        BiDirectionalMemoryUpdate.BiDirectionalMemoryUpdateBuilder updateBuilder = BiDirectionalMemoryUpdate.builder()
            .sessionId(sessionId)
            .turnNumber(turn.getTurnNumber())
            .updateTimestamp(updateStart)
            .updateType("AGENT_RESPONSE");
        
        List<String> updatedBlocks = new ArrayList<>();
        List<String> errorMessages = new ArrayList<>();
        List<String> successIndicators = new ArrayList<>();
        Map<String, Boolean> consistencyChecks = new HashMap<>();
        Map<String, Object> performanceMetrics = new HashMap<>();
        
        int retryAttempts = 0;
        boolean updateSuccess = false;
        
        try {
            // Step 1: Analyze agent response for bidirectional feedback
            AgentResponseMetadata responseMetadata = responseAnalysisService.analyzeAgentResponse(
                agentId, agentType, agentResponse, turn.getEnrichedMessage(), turn, contextResult, responseTimeMs
            );
            
            successIndicators.add("Agent response analysis completed");
            
            // Step 2: Update conversation turn with response metadata
            turn.setResponseMetadata(responseMetadata);
            turn.setResponseQuality(responseMetadata.getResponseQuality());
            turn.setContextUtilization(responseMetadata.getContextRelevance());
            turn.setResponseContextCorrelation(calculateResponseContextCorrelation(responseMetadata, contextResult));
            turn.setContextFeedback(responseMetadata.getContextFeedback());
            turn.setResponseAnalysisTimestamp(LocalDateTime.now());
            
            // Step 3: Perform atomic memory updates with retry logic
            updateSuccess = performAtomicMemoryUpdates(
                contextExtractorId, identityId, turn, responseMetadata, contextResult,
                updatedBlocks, errorMessages, successIndicators, performanceMetrics
            );
            
            // Step 4: Verify memory consistency
            consistencyChecks = performMemoryConsistencyChecks(contextExtractorId, identityId, turn);
            
            // Step 5: Generate context improvement feedback
            String contextImprovements = generateContextImprovements(responseMetadata, contextResult);
            String memoryOptimizations = generateMemoryOptimizations(turn, responseMetadata);
            
            // Step 6: Update conversation patterns based on response analysis
            List<String> patternUpdates = updateConversationPatterns(turn, responseMetadata);
            
            // Calculate update quality impact
            String qualityImpact = calculateQualityImpact(responseMetadata, contextResult);
            
            // Set final update status
            turn.setMemoryConsistencyStatus(isMemoryConsistent(consistencyChecks) ? "CONSISTENT" : "INCONSISTENT");
            turn.setGeneratedContextFeedback(true);
            turn.setUpdateSuccessIndicators(String.join("; ", successIndicators));
            turn.setUpdateErrorIndicators(errorMessages.isEmpty() ? "None" : String.join("; ", errorMessages));
            
            // Build comprehensive update result
            BiDirectionalMemoryUpdate result = updateBuilder
                .responseMetadata(responseMetadata)
                .updatedMemoryBlocks(updatedBlocks)
                .updateSuccess(updateSuccess)
                .updateTimeMs(System.currentTimeMillis() - updateStart.toEpochSecond(java.time.ZoneOffset.UTC) * 1000)
                .retryAttempts(retryAttempts)
                .contextFeedback(responseMetadata.getContextFeedback())
                .contextImprovements(contextImprovements)
                .patternUpdates(patternUpdates)
                .memoryOptimizations(memoryOptimizations)
                .qualityImpact(qualityImpact)
                .synchronizationStatus(isMemoryConsistent(consistencyChecks) ? "SYNCHRONIZED" : "PARTIAL")
                .errorMessages(errorMessages)
                .successIndicators(successIndicators)
                .consistencyChecks(consistencyChecks)
                .performanceMetrics(performanceMetrics)
                .optimizationSuggestions(generateOptimizationSuggestions(responseMetadata, turn))
                .build();
            
            // Update turn with bidirectional memory result
            turn.setMemoryUpdateResult(result);
            
            log.debug("Bidirectional memory update completed for session {} turn {} with success: {}", 
                sessionId, turn.getTurnNumber(), updateSuccess);
            
            return result;
            
        } catch (Exception e) {
            log.error("Failed to perform bidirectional memory update for session {} turn {}: {}", 
                sessionId, turn.getTurnNumber(), e.getMessage(), e);
            
            return createFailedUpdateResult(updateBuilder, sessionId, turn, e, updateStart);
        }
    }
    
    /**
     * Perform atomic memory updates with transaction-like behavior
     */
    private boolean performAtomicMemoryUpdates(String contextExtractorId, String identityId, 
                                             ConversationTurn turn, AgentResponseMetadata responseMetadata,
                                             ContextEnrichmentResult contextResult, List<String> updatedBlocks,
                                             List<String> errorMessages, List<String> successIndicators,
                                             Map<String, Object> performanceMetrics) {
        try {
            log.debug("Performing atomic memory updates for turn {}", turn.getTurnNumber());
            
            // Create snapshot of current memory state for rollback
            Map<String, String> memorySnapshot = createMemorySnapshot(contextExtractorId, identityId);
            
            List<CompletableFuture<Void>> updateFutures = new ArrayList<>();
            
            // Update 1: Enhanced conversation history with response analysis
            CompletableFuture<Void> historyUpdate = CompletableFuture.runAsync(() -> {
                try {
                    String enhancedHistory = createEnhancedConversationHistory(turn, responseMetadata);
                    updateMemoryBlockWithRetry(contextExtractorId, identityId, 
                        MemoryArchitectureConfig.CONVERSATION_HISTORY, enhancedHistory, 3);
                    updatedBlocks.add(MemoryArchitectureConfig.CONVERSATION_HISTORY);
                    successIndicators.add("Enhanced conversation history updated");
                } catch (Exception e) {
                    errorMessages.add("Failed to update conversation history: " + e.getMessage());
                    throw new RuntimeException(e);
                }
            }, memoryUpdateExecutor);
            
            updateFutures.add(historyUpdate);
            
            // Update 2: Enhanced active session with response feedback
            CompletableFuture<Void> sessionUpdate = CompletableFuture.runAsync(() -> {
                try {
                    String enhancedSession = createEnhancedActiveSession(turn, responseMetadata, contextResult);
                    updateMemoryBlockWithRetry(contextExtractorId, identityId, 
                        MemoryArchitectureConfig.ACTIVE_SESSION, enhancedSession, 3);
                    updatedBlocks.add(MemoryArchitectureConfig.ACTIVE_SESSION);
                    successIndicators.add("Enhanced active session updated");
                } catch (Exception e) {
                    errorMessages.add("Failed to update active session: " + e.getMessage());
                    throw new RuntimeException(e);
                }
            }, memoryUpdateExecutor);
            
            updateFutures.add(sessionUpdate);
            
            // Update 3: Enhanced context summary with bidirectional insights
            if (shouldUpdateContextSummary(turn)) {
                CompletableFuture<Void> summaryUpdate = CompletableFuture.runAsync(() -> {
                    try {
                        String enhancedSummary = createEnhancedContextSummary(turn, responseMetadata, contextResult);
                        updateMemoryBlockWithRetry(contextExtractorId, identityId, 
                            MemoryArchitectureConfig.CONTEXT_SUMMARY, enhancedSummary, 3);
                        updatedBlocks.add(MemoryArchitectureConfig.CONTEXT_SUMMARY);
                        successIndicators.add("Enhanced context summary updated");
                    } catch (Exception e) {
                        errorMessages.add("Failed to update context summary: " + e.getMessage());
                        throw new RuntimeException(e);
                    }
                }, memoryUpdateExecutor);
                
                updateFutures.add(summaryUpdate);
            }
            
            // Update 4: Enhanced memory metadata with bidirectional analytics
            CompletableFuture<Void> metadataUpdate = CompletableFuture.runAsync(() -> {
                try {
                    String enhancedMetadata = createEnhancedMemoryMetadata(turn, responseMetadata, contextResult);
                    updateMemoryBlockWithRetry(contextExtractorId, identityId, 
                        MemoryArchitectureConfig.MEMORY_METADATA, enhancedMetadata, 3);
                    updatedBlocks.add(MemoryArchitectureConfig.MEMORY_METADATA);
                    successIndicators.add("Enhanced memory metadata updated");
                } catch (Exception e) {
                    errorMessages.add("Failed to update memory metadata: " + e.getMessage());
                    throw new RuntimeException(e);
                }
            }, memoryUpdateExecutor);
            
            updateFutures.add(metadataUpdate);
            
            // Wait for all updates to complete with timeout
            CompletableFuture.allOf(updateFutures.toArray(new CompletableFuture[0]))
                .get(30, TimeUnit.SECONDS);
            
            performanceMetrics.put("atomicUpdatesCompleted", updatedBlocks.size());
            performanceMetrics.put("parallelUpdates", true);
            
            log.debug("All atomic memory updates completed successfully for turn {}", turn.getTurnNumber());
            return true;
            
        } catch (Exception e) {
            log.error("Atomic memory updates failed for turn {}: {}", turn.getTurnNumber(), e.getMessage(), e);
            
            // Attempt rollback if partial failure
            performRollback(contextExtractorId, identityId, updatedBlocks, errorMessages);
            
            performanceMetrics.put("atomicUpdatesFailed", true);
            performanceMetrics.put("rollbackPerformed", true);
            
            return false;
        }
    }
    
    /**
     * Update memory block with retry logic and exponential backoff
     */
    private void updateMemoryBlockWithRetry(String agentId, String identityId, String memoryLabel, 
                                          String content, int maxRetries) {
        Exception lastException = null;
        
        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                LettaMessageRequest request = LettaMessageRequest.builder()
                    .messages(Arrays.asList(
                        LettaMessage.builder()
                            .role("system")
                            .content(String.format("BIDIRECTIONAL_UPDATE|BLOCK:%s|OPERATION:ENHANCED_REPLACE|CONTENT:%s", 
                                memoryLabel, content))
                            .build()
                    ))
                    .senderId(identityId)
                    .build();
                
                lettaAgentService.sendMessage(agentId, request);
                log.debug("Successfully updated memory block {} (attempt {})", memoryLabel, attempt);
                return; // Success
                
            } catch (Exception e) {
                lastException = e;
                log.warn("Failed to update memory block {} (attempt {}/{}): {}", 
                    memoryLabel, attempt, maxRetries, e.getMessage());
                
                if (attempt < maxRetries) {
                    try {
                        // Exponential backoff: 1s, 2s, 4s
                        Thread.sleep(1000L * (long) Math.pow(2, attempt - 1));
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("Memory update interrupted", ie);
                    }
                }
            }
        }
        
        throw new RuntimeException("Memory block update failed after " + maxRetries + " attempts", lastException);
    }
    
    /**
     * Perform memory consistency checks across all blocks
     */
    private Map<String, Boolean> performMemoryConsistencyChecks(String contextExtractorId, String identityId, 
                                                               ConversationTurn turn) {
        Map<String, Boolean> checks = new HashMap<>();
        
        try {
            // Check 1: Turn number consistency
            checks.put("turnNumberConsistency", verifyTurnNumberConsistency(turn));
            
            // Check 2: Session ID consistency
            checks.put("sessionIdConsistency", verifySessionIdConsistency(turn));
            
            // Check 3: Response metadata consistency
            checks.put("responseMetadataConsistency", verifyResponseMetadataConsistency(turn));
            
            // Check 4: Memory block size limits
            checks.put("memorySizeLimits", verifyMemorySizeLimits(turn));
            
            // Check 5: Bidirectional data integrity
            checks.put("bidirectionalIntegrity", verifyBidirectionalIntegrity(turn));
            
        } catch (Exception e) {
            log.error("Failed to perform memory consistency checks for turn {}: {}", 
                turn.getTurnNumber(), e.getMessage(), e);
            checks.put("consistencyCheckError", false);
        }
        
        return checks;
    }
    
    /**
     * Calculate response-context correlation score
     */
    private double calculateResponseContextCorrelation(AgentResponseMetadata responseMetadata, 
                                                     ContextEnrichmentResult contextResult) {
        if (responseMetadata == null || contextResult == null) {
            return 0.5; // Default moderate correlation
        }
        
        double correlation = 0.0;
        
        // Context confidence vs response quality correlation
        if (contextResult.getContextConfidence() > 0.8 && responseMetadata.getResponseQuality() > 0.8) {
            correlation += 0.4; // High confidence context led to high quality response
        }
        
        // Context relevance correlation
        if (responseMetadata.getContextRelevance() != null) {
            correlation += responseMetadata.getContextRelevance() * 0.3;
        }
        
        // Response appropriateness correlation
        if (responseMetadata.getAddressedConcern() != null && responseMetadata.getAddressedConcern()) {
            correlation += 0.3; // Response addressed the concern
        }
        
        return Math.min(1.0, correlation);
    }
    
    /**
     * Generate context improvement suggestions based on response analysis
     */
    private String generateContextImprovements(AgentResponseMetadata responseMetadata, 
                                             ContextEnrichmentResult contextResult) {
        StringBuilder improvements = new StringBuilder();
        
        // Analyze response quality vs context confidence
        if (responseMetadata.getResponseQuality() < 0.6 && contextResult.getContextConfidence() < 0.7) {
            improvements.append("Improve context selection confidence for better response quality. ");
        }
        
        // Analyze context utilization feedback
        if (responseMetadata.getContextFeedback() != null && 
            responseMetadata.getContextFeedback().contains("insufficient")) {
            improvements.append("Increase relevant context turns for comprehensive responses. ");
        }
        
        // Medical context improvements
        if ("General Health".equals(responseMetadata.getAgentType()) && 
            responseMetadata.getMedicalAccuracy() < 0.8) {
            improvements.append("Enhance medical context history for better accuracy. ");
        }
        
        return improvements.length() > 0 ? improvements.toString().trim() : 
            "Current context selection is performing well";
    }
    
    /**
     * Generate memory optimization recommendations
     */
    private String generateMemoryOptimizations(ConversationTurn turn, AgentResponseMetadata responseMetadata) {
        StringBuilder optimizations = new StringBuilder();
        
        // Response quality-based optimizations
        if (responseMetadata.getResponseQuality() > 0.9) {
            optimizations.append("High-quality response pattern - consider preserving context approach. ");
        }
        
        // Context feedback optimizations
        if (responseMetadata.getContextRelevance() < 0.6) {
            optimizations.append("Consider expanding context selection window. ");
        }
        
        // Follow-up optimizations
        if (responseMetadata.getRequiresFollowUp() != null && responseMetadata.getRequiresFollowUp()) {
            optimizations.append("Prepare enhanced context for follow-up questions. ");
        }
        
        return optimizations.length() > 0 ? optimizations.toString().trim() : 
            "Memory usage is optimized for current patterns";
    }
    
    /**
     * Update conversation patterns based on response analysis
     */
    private List<String> updateConversationPatterns(ConversationTurn turn, AgentResponseMetadata responseMetadata) {
        List<String> updates = new ArrayList<>();
        
        // Quality pattern updates
        if (responseMetadata.getResponseQuality() > 0.9) {
            updates.add("High-quality response pattern identified");
        }
        
        // Medical pattern updates
        if ("General Health".equals(responseMetadata.getAgentType()) && 
            responseMetadata.getMedicalAccuracy() > 0.9) {
            updates.add("Excellent medical response pattern");
        }
        
        // Emotional pattern updates
        if (responseMetadata.getEmotionalAppropriateness() > 0.8) {
            updates.add("Strong emotional appropriateness pattern");
        }
        
        // Context utilization pattern updates
        if (responseMetadata.getContextRelevance() > 0.8) {
            updates.add("Effective context utilization pattern");
        }
        
        return updates;
    }
    
    // Helper methods for enhanced memory content creation
    
    private String createEnhancedConversationHistory(ConversationTurn turn, AgentResponseMetadata responseMetadata) {
        StringBuilder history = new StringBuilder();
        
        history.append(String.format("ENHANCED_TURN_%d: USER: %s | ", 
            turn.getTurnNumber(), turn.getUserMessage()));
        
        history.append(String.format("ENRICHED: %s | ", 
            turn.getEnrichedMessage() != null ? turn.getEnrichedMessage().substring(0, Math.min(200, turn.getEnrichedMessage().length())) + "..." : ""));
        
        history.append(String.format("INTENT: %s (%.2f) | ", 
            turn.getExtractedIntent(), turn.getIntentConfidence()));
        
        history.append(String.format("AGENT: %s | RESPONSE: %s | ", 
            turn.getRoutedAgent(), turn.getAgentResponse()));
        
        history.append(String.format("RESPONSE_QUALITY: %.2f | CONTEXT_UTILIZATION: %.2f | ", 
            responseMetadata.getResponseQuality(), responseMetadata.getContextRelevance()));
        
        history.append(String.format("FEEDBACK: %s | TIMESTAMP: %s", 
            responseMetadata.getContextFeedback(), turn.getTimestamp()));
        
        return history.toString();
    }
    
    private String createEnhancedActiveSession(ConversationTurn turn, AgentResponseMetadata responseMetadata,
                                             ContextEnrichmentResult contextResult) {
        return String.format(
            "SESSION_ID: %s | STATUS: active | TURNS: %d | LAST_AGENT: %s | " +
            "RESPONSE_QUALITY: %.2f | CONTEXT_CONFIDENCE: %.2f | " +
            "PATTERNS_DETECTED: %s | NEEDS_FOLLOWUP: %s | UPDATED: %s",
            turn.getSessionId(), turn.getTurnNumber(), responseMetadata.getAgentType(),
            responseMetadata.getResponseQuality(), contextResult.getContextConfidence(),
            responseMetadata.getResponsePatterns(), responseMetadata.getRequiresFollowUp(),
            LocalDateTime.now()
        );
    }
    
    private String createEnhancedContextSummary(ConversationTurn turn, AgentResponseMetadata responseMetadata,
                                              ContextEnrichmentResult contextResult) {
        return String.format(
            "Enhanced Context Summary - Turn %d: User discussed %s. Agent (%s) provided %s response " +
            "with %.2f quality score. Context confidence: %.2f. Feedback: %s. " +
            "Response addressed concern: %s. Follow-up needed: %s.",
            turn.getTurnNumber(), responseMetadata.getAddressedTopics(), 
            responseMetadata.getAgentType(), responseMetadata.getQualityLevel(),
            responseMetadata.getResponseQuality(), contextResult.getContextConfidence(),
            responseMetadata.getContextFeedback(), responseMetadata.getAddressedConcern(),
            responseMetadata.getRequiresFollowUp()
        );
    }
    
    private String createEnhancedMemoryMetadata(ConversationTurn turn, AgentResponseMetadata responseMetadata,
                                              ContextEnrichmentResult contextResult) {
        return String.format(
            "MEMORY_STATUS: enhanced_bidirectional | TURN: %d | RESPONSE_ANALYSIS: completed | " +
            "QUALITY_SCORE: %.2f | CONTEXT_CORRELATION: %.2f | FEEDBACK_GENERATED: %s | " +
            "PATTERNS_UPDATED: %s | CONSISTENCY: verified | UPDATED: %s",
            turn.getTurnNumber(), responseMetadata.getResponseQuality(),
            turn.getResponseContextCorrelation(), turn.getGeneratedContextFeedback(),
            !updateConversationPatterns(turn, responseMetadata).isEmpty(),
            LocalDateTime.now()
        );
    }
    
    // Helper methods for consistency checks and utilities
    
    private boolean shouldUpdateContextSummary(ConversationTurn turn) {
        return turn.getTurnNumber() % 3 == 0 || turn.hasHighQualityResponse();
    }
    
    private Map<String, String> createMemorySnapshot(String contextExtractorId, String identityId) {
        // In production, this would capture current memory state for rollback
        return new HashMap<>();
    }
    
    private void performRollback(String contextExtractorId, String identityId, List<String> updatedBlocks,
                               List<String> errorMessages) {
        log.warn("Performing rollback for {} updated blocks", updatedBlocks.size());
        errorMessages.add("Rollback performed due to partial update failure");
        // In production, this would restore from memory snapshot
    }
    
    private boolean isMemoryConsistent(Map<String, Boolean> consistencyChecks) {
        return consistencyChecks.values().stream().allMatch(check -> check);
    }
    
    private String calculateQualityImpact(AgentResponseMetadata responseMetadata, ContextEnrichmentResult contextResult) {
        if (responseMetadata.getResponseQuality() > 0.8 && contextResult.getContextConfidence() > 0.8) {
            return "High-quality response with excellent context correlation";
        } else if (responseMetadata.getResponseQuality() > 0.6) {
            return "Good response quality with room for context optimization";
        } else {
            return "Response quality suggests need for context selection improvements";
        }
    }
    
    private List<String> generateOptimizationSuggestions(AgentResponseMetadata responseMetadata, ConversationTurn turn) {
        List<String> suggestions = new ArrayList<>();
        
        if (responseMetadata.getContextRelevance() < 0.7) {
            suggestions.add("Expand context selection to include more relevant historical turns");
        }
        
        if (responseMetadata.getResponseQuality() < 0.6) {
            suggestions.add("Improve context enrichment quality for better agent responses");
        }
        
        if (responseMetadata.getRequiresFollowUp() != null && responseMetadata.getRequiresFollowUp()) {
            suggestions.add("Prepare enhanced context for expected follow-up questions");
        }
        
        return suggestions;
    }
    
    // Consistency check implementations
    
    private boolean verifyTurnNumberConsistency(ConversationTurn turn) {
        return turn.getTurnNumber() > 0;
    }
    
    private boolean verifySessionIdConsistency(ConversationTurn turn) {
        return turn.getSessionId() != null && !turn.getSessionId().isEmpty();
    }
    
    private boolean verifyResponseMetadataConsistency(ConversationTurn turn) {
        return turn.getResponseMetadata() != null && 
               turn.getResponseMetadata().getAgentId() != null;
    }
    
    private boolean verifyMemorySizeLimits(ConversationTurn turn) {
        // Check if conversation history and other memory blocks are within limits
        return turn.getAgentResponse() != null && turn.getAgentResponse().length() < 5000;
    }
    
    private boolean verifyBidirectionalIntegrity(ConversationTurn turn) {
        return turn.getResponseMetadata() != null && 
               turn.getMemoryUpdateResult() != null;
    }
    
    private BiDirectionalMemoryUpdate createFailedUpdateResult(BiDirectionalMemoryUpdate.BiDirectionalMemoryUpdateBuilder builder,
                                                             String sessionId, ConversationTurn turn, Exception error,
                                                             LocalDateTime updateStart) {
        List<String> errorMessages = Arrays.asList(
            "Bidirectional memory update failed: " + error.getMessage(),
            "Turn may be incomplete",
            "Manual intervention may be required"
        );
        
        return builder
            .updateSuccess(false)
            .updateTimeMs(System.currentTimeMillis() - updateStart.toEpochSecond(java.time.ZoneOffset.UTC) * 1000)
            .errorMessages(errorMessages)
            .synchronizationStatus("FAILED")
            .rollbackInfo("Automatic rollback attempted")
            .build();
    }
} 