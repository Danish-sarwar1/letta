package com.health.agents.service;

import com.health.agents.config.MemoryArchitectureConfig;
import com.health.agents.model.dto.ConversationTurn;
import com.health.agents.model.dto.ConversationHistory;
import com.health.agents.integration.letta.model.LettaMessageRequest;
import com.health.agents.integration.letta.model.LettaMessage;
import com.health.agents.integration.letta.service.LettaAgentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Conversation Turn Service for Sub-Plan 2: Conversation Turn Management
 * Implements complete conversation turn tracking and memory block updates
 */
@Service
@Slf4j
public class ConversationTurnService {
    
    @Autowired
    private MemoryManagementService memoryManagementService;
    
    @Autowired
    private MemoryArchitectureConfig memoryConfig;
    
    @Autowired
    private LettaAgentService lettaAgentService;
    
    // DateTimeFormatter for consistent timestamp formatting
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    // In-memory conversation tracking (in production, this would be persisted)
    private final ConcurrentHashMap<String, ConversationHistory> sessionHistories = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, AtomicInteger> sessionTurnCounters = new ConcurrentHashMap<>();
    
    /**
     * Add user message turn to conversation history and update memory blocks
     */
    public ConversationTurn addUserMessageTurn(String sessionId, String userId, String userMessage,
                                             String contextExtractorId, String identityId) {
        log.debug("Adding user message turn for session {}: {}", sessionId, userMessage);
        
        try {
            // Get or create conversation history
            ConversationHistory history = getOrCreateConversationHistory(sessionId, userId);
            
            // Create new conversation turn
            int turnNumber = getNextTurnNumber(sessionId);
            ConversationTurn turn = ConversationTurn.builder()
                .turnNumber(turnNumber)
                .userMessage(userMessage)
                .sessionId(sessionId)
                .timestamp(LocalDateTime.now())
                .turnType("USER_MESSAGE")
                .isArchived(false)
                .build();
            
            // Add turn to history
            history.getTurns().add(turn);
            history.setLastUpdated(LocalDateTime.now());
            history.setTotalTurns(history.getTotalTurns() + 1);
            
            // Update memory blocks in context extractor
            updateContextExtractorMemory(contextExtractorId, identityId, history, turn);
            
            log.debug("Added user message turn {} for session {}", turnNumber, sessionId);
            return turn;
            
        } catch (Exception e) {
            log.error("Failed to add user message turn for session {}: {}", sessionId, e.getMessage(), e);
            throw new RuntimeException("Failed to add user message turn", e);
        }
    }
    
    /**
     * Update conversation turn with enriched context and intent information
     */
    public ConversationTurn updateTurnWithEnrichment(String sessionId, int turnNumber, 
                                                   String enrichedMessage, String extractedIntent, 
                                                   Double intentConfidence, String contextUsed, String reasoning) {
        log.debug("Updating turn {} with enrichment for session {}", turnNumber, sessionId);
        
        try {
            ConversationHistory history = sessionHistories.get(sessionId);
            if (history == null) {
                throw new IllegalStateException("No conversation history found for session: " + sessionId);
            }
            
            // Find the turn to update
            ConversationTurn turn = history.getTurns().stream()
                .filter(t -> t.getTurnNumber() == turnNumber)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Turn not found: " + turnNumber));
            
            // Update turn with enrichment data
            turn.setEnrichedMessage(enrichedMessage);
            turn.setExtractedIntent(extractedIntent);
            turn.setIntentConfidence(intentConfidence);
            turn.setContextUsed(contextUsed);
            turn.setReasoning(reasoning);
            
            // Update history timestamp
            history.setLastUpdated(LocalDateTime.now());
            
            log.debug("Updated turn {} with enrichment for session {}", turnNumber, sessionId);
            return turn;
            
        } catch (Exception e) {
            log.error("Failed to update turn with enrichment for session {}: {}", sessionId, e.getMessage(), e);
            throw new RuntimeException("Failed to update turn with enrichment", e);
        }
    }
    
    /**
     * Add agent response to conversation turn and update memory blocks
     */
    public ConversationTurn addAgentResponseToTurn(String sessionId, int turnNumber, String routedAgent, 
                                                 String agentResponse, String contextExtractorId, String identityId) {
        log.debug("Adding agent response to turn {} for session {}", turnNumber, sessionId);
        
        try {
            ConversationHistory history = sessionHistories.get(sessionId);
            if (history == null) {
                throw new IllegalStateException("No conversation history found for session: " + sessionId);
            }
            
            // Find the turn to update
            ConversationTurn turn = history.getTurns().stream()
                .filter(t -> t.getTurnNumber() == turnNumber)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Turn not found: " + turnNumber));
            
            // Update turn with agent response
            turn.setRoutedAgent(routedAgent);
            turn.setAgentResponse(agentResponse);
            turn.setTurnType("COMPLETE_TURN"); // Now it's a complete conversation turn
            
            // Update history timestamp
            history.setLastUpdated(LocalDateTime.now());
            
            // Update memory blocks with complete turn
            updateContextExtractorMemory(contextExtractorId, identityId, history, turn);
            
            log.debug("Added agent response to turn {} for session {}", turnNumber, sessionId);
            return turn;
            
        } catch (Exception e) {
            log.error("Failed to add agent response to turn for session {}: {}", sessionId, e.getMessage(), e);
            throw new RuntimeException("Failed to add agent response to turn", e);
        }
    }
    
    /**
     * Get conversation history for a session
     */
    public ConversationHistory getConversationHistory(String sessionId) {
        return sessionHistories.get(sessionId);
    }
    
    /**
     * Check if memory rotation is needed for a session
     */
    public boolean isMemoryRotationNeeded(String sessionId) {
        ConversationHistory history = sessionHistories.get(sessionId);
        if (history == null) {
            return false;
        }
        
        // Check if we need rotation based on turn count or memory usage
        return memoryManagementService.isArchivalNeeded(history.getTotalTurns()) ||
               shouldRotateBasedOnMemoryUsage(history);
    }
    
    /**
     * Perform memory rotation for a session
     */
    public void performMemoryRotation(String sessionId, String contextExtractorId, String identityId) {
        log.info("Performing memory rotation for session {}", sessionId);
        
        try {
            ConversationHistory history = sessionHistories.get(sessionId);
            if (history == null) {
                log.warn("No history found for session rotation: {}", sessionId);
                return;
            }
            
            // Keep recent 75% of turns, archive oldest 25%
            int totalTurns = history.getTurns().size();
            int turnsToKeep = (int) (totalTurns * 0.75);
            int turnsToArchive = totalTurns - turnsToKeep;
            
            if (turnsToArchive > 0) {
                // Mark oldest turns as archived
                for (int i = 0; i < turnsToArchive; i++) {
                    history.getTurns().get(i).setArchived(true);
                }
                
                // Update memory with rotated history
                updateContextExtractorMemory(contextExtractorId, identityId, history, null);
                
                log.info("Rotated {} turns for session {}, keeping {} recent turns", 
                    turnsToArchive, sessionId, turnsToKeep);
            }
            
        } catch (Exception e) {
            log.error("Failed to perform memory rotation for session {}: {}", sessionId, e.getMessage(), e);
        }
    }
    
    /**
     * Update context extractor memory blocks with conversation data
     */
    private void updateContextExtractorMemory(String contextExtractorId, String identityId, 
                                            ConversationHistory history, ConversationTurn currentTurn) {
        try {
            log.debug("Updating context extractor memory for session: {}", history.getSessionId());
            
            // Format conversation history for storage
            String conversationContent = memoryManagementService.formatConversationHistory(history);
            
            // Create active session content
            String sessionContent = memoryManagementService.createActiveSessionContent(
                history.getSessionId(), 
                history.getUserId(),
                history.getStatus(),
                extractCurrentTopic(history),
                history.getTotalTurns()
            );
            
            // Create context summary
            String contextSummary = memoryManagementService.createContextSummary(history);
            
            // Create memory metadata
            String memoryMetadata = memoryManagementService.createMemoryMetadata(
                history.getSessionId(),
                conversationContent.length(),
                sessionContent.length(),
                contextSummary.length()
            );
            
            // Update all memory blocks with safe update mechanism
            safeUpdateMemoryBlock(contextExtractorId, identityId, 
                MemoryArchitectureConfig.CONVERSATION_HISTORY, conversationContent,
                memoryConfig.getConversationHistoryLimit());
                
            safeUpdateMemoryBlock(contextExtractorId, identityId, 
                MemoryArchitectureConfig.ACTIVE_SESSION, sessionContent,
                memoryConfig.getActiveSessionLimit());
            
            // Update context summary every 5 turns or when explicitly requested
            if (currentTurn == null || history.getTotalTurns() % 5 == 0) {
                safeUpdateMemoryBlock(contextExtractorId, identityId, 
                    MemoryArchitectureConfig.CONTEXT_SUMMARY, contextSummary,
                    memoryConfig.getContextSummaryLimit());
            }
            
            // Always update memory metadata
            safeUpdateMemoryBlock(contextExtractorId, identityId, 
                MemoryArchitectureConfig.MEMORY_METADATA, memoryMetadata,
                memoryConfig.getMemoryMetadataLimit());
            
            log.debug("Successfully updated all memory blocks for session: {}", history.getSessionId());
            
        } catch (Exception e) {
            log.error("Critical failure updating context extractor memory for session {}: {}", 
                history.getSessionId(), e.getMessage(), e);
            // Continue execution with degraded memory functionality
        }
    }
    
    /**
     * Update a specific memory block using Letta's core memory operations with error handling
     */
    private void updateMemoryBlock(String agentId, String identityId, String memoryLabel, String content) {
        int maxRetries = 3;
        int retryCount = 0;
        Exception lastException = null;
        
        while (retryCount < maxRetries) {
            try {
                // Try to update the memory block
                LettaMessageRequest request = LettaMessageRequest.builder()
                    .messages(Arrays.asList(
                        LettaMessage.builder()
                            .role("system")
                            .content(String.format("MEMORY_UPDATE|BLOCK:%s|OPERATION:REPLACE|CONTENT:%s", 
                                memoryLabel, content))
                            .build()
                    ))
                    .senderId(identityId)
                    .build();
                
                lettaAgentService.sendMessage(agentId, request);
                log.debug("Successfully updated memory block {} for agent {} (attempt {})", 
                    memoryLabel, agentId, retryCount + 1);
                return; // Success, exit retry loop
                
            } catch (Exception e) {
                lastException = e;
                retryCount++;
                log.warn("Failed to update memory block {} for agent {} (attempt {}/{}): {}", 
                    memoryLabel, agentId, retryCount, maxRetries, e.getMessage());
                
                if (retryCount < maxRetries) {
                    try {
                        // Exponential backoff: wait 1s, 2s, 4s
                        Thread.sleep(1000 * (long) Math.pow(2, retryCount - 1));
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("Memory update interrupted", ie);
                    }
                }
            }
        }
        
        // All retries failed
        log.error("Failed to update memory block {} after {} attempts", memoryLabel, maxRetries);
        throw new RuntimeException("Memory block update failed after retries", lastException);
    }
    
    /**
     * Validate memory content before update
     */
    private boolean validateMemoryContent(String memoryLabel, String content, int maxSize) {
        if (content == null) {
            log.warn("Memory content is null for block: {}", memoryLabel);
            return false;
        }
        
        if (content.length() > maxSize) {
            log.warn("Memory content exceeds limit for block {}: {} > {}", 
                memoryLabel, content.length(), maxSize);
            return false;
        }
        
        // Check for minimum content requirements
        if (content.trim().isEmpty()) {
            log.warn("Memory content is empty for block: {}", memoryLabel);
            return false;
        }
        
        return true;
    }
    
    /**
     * Safe memory update with validation and error handling
     */
    private void safeUpdateMemoryBlock(String agentId, String identityId, String memoryLabel, 
                                     String content, int maxSize) {
        try {
            // Validate content before update
            if (!validateMemoryContent(memoryLabel, content, maxSize)) {
                // Create fallback content
                content = createFallbackContent(memoryLabel);
                log.info("Using fallback content for memory block: {}", memoryLabel);
            }
            
            // Truncate if necessary while preserving structure
            if (content.length() > maxSize) {
                content = truncateContentSafely(content, maxSize, memoryLabel);
                log.info("Truncated content for memory block {} to {} characters", 
                    memoryLabel, content.length());
            }
            
            // Perform the update with retry logic
            updateMemoryBlock(agentId, identityId, memoryLabel, content);
            
        } catch (Exception e) {
            log.error("Failed to safely update memory block {}: {}", memoryLabel, e.getMessage(), e);
            // Don't re-throw here - log error and continue with degraded functionality
        }
    }
    
    /**
     * Create fallback content when memory update fails
     */
    private String createFallbackContent(String memoryLabel) {
        LocalDateTime now = LocalDateTime.now();
        
        switch (memoryLabel) {
            case MemoryArchitectureConfig.CONVERSATION_HISTORY:
                return "Conversation history temporarily unavailable - " + now.format(formatter);
                
            case MemoryArchitectureConfig.ACTIVE_SESSION:
                return String.format("SESSION_ID: unknown | STATUS: active | TURNS: 0 | UPDATED: %s", 
                    now.format(formatter));
                
            case MemoryArchitectureConfig.CONTEXT_SUMMARY:
                return "Context summary temporarily unavailable - " + now.format(formatter);
                
            case MemoryArchitectureConfig.MEMORY_METADATA:
                return String.format("MEMORY_STATUS: fallback_mode | UPDATED: %s", now.format(formatter));
                
            default:
                return "Memory block temporarily unavailable - " + now.format(formatter);
        }
    }
    
    /**
     * Safely truncate content while preserving structure
     */
    private String truncateContentSafely(String content, int maxSize, String memoryLabel) {
        if (content.length() <= maxSize) {
            return content;
        }
        
        // For conversation history, preserve recent turns
        if (MemoryArchitectureConfig.CONVERSATION_HISTORY.equals(memoryLabel)) {
            return truncateConversationHistory(content, maxSize);
        }
        
        // For other content, simple truncation with warning
        String truncated = content.substring(0, maxSize - 100); // Leave buffer
        truncated += "\n[TRUNCATED - Content exceeded memory limit]";
        return truncated;
    }
    
    /**
     * Intelligently truncate conversation history to preserve recent turns
     */
    private String truncateConversationHistory(String content, int maxSize) {
        String[] lines = content.split("\n");
        StringBuilder result = new StringBuilder();
        
        // Always include session header if present
        if (lines.length > 0 && lines[0].contains("SESSION_ID:")) {
            result.append(lines[0]).append("\n");
        }
        
        // Work backwards to preserve most recent turns
        int currentSize = result.length();
        for (int i = lines.length - 1; i >= 1 && currentSize < maxSize - 200; i--) {
            String line = lines[i];
            if (currentSize + line.length() + 1 <= maxSize - 200) {
                result.insert(result.indexOf("\n") + 1, line + "\n");
                currentSize += line.length() + 1;
            } else {
                break;
            }
        }
        
        result.append("[TRUNCATED - Showing most recent turns]");
        return result.toString();
    }
    
    /**
     * Get or create conversation history for a session
     */
    private ConversationHistory getOrCreateConversationHistory(String sessionId, String userId) {
        return sessionHistories.computeIfAbsent(sessionId, id -> {
            log.debug("Creating new conversation history for session {}", id);
            return ConversationHistory.builder()
                .sessionId(id)
                .userId(userId)
                .turns(new ArrayList<>())
                .createdAt(LocalDateTime.now())
                .lastUpdated(LocalDateTime.now())
                .status("ACTIVE")
                .totalTurns(0)
                .build();
        });
    }
    
    /**
     * Get next turn number for a session
     */
    private int getNextTurnNumber(String sessionId) {
        return sessionTurnCounters.computeIfAbsent(sessionId, id -> new AtomicInteger(0))
            .incrementAndGet();
    }
    
    /**
     * Check if memory rotation is needed based on memory usage
     */
    private boolean shouldRotateBasedOnMemoryUsage(ConversationHistory history) {
        String conversationContent = memoryManagementService.formatConversationHistory(history);
        return memoryManagementService.isMemoryRotationNeeded(
            conversationContent, memoryConfig.getConversationHistoryLimit());
    }
    
    /**
     * Extract current topic from conversation history
     */
    private String extractCurrentTopic(ConversationHistory history) {
        if (history.getTurns().isEmpty()) {
            return "Initial Setup";
        }
        
        // Get the most recent turn with topic tags
        return history.getTurns().stream()
            .filter(turn -> turn.getTopicTags() != null && !turn.getTopicTags().isEmpty())
            .reduce((first, second) -> second) // Get last element
            .map(ConversationTurn::getTopicTags)
            .orElse("General Health Discussion");
    }
    
    /**
     * Clear conversation history for a session (used when session ends)
     */
    public void clearSession(String sessionId) {
        sessionHistories.remove(sessionId);
        sessionTurnCounters.remove(sessionId);
        log.debug("Cleared conversation history for session {}", sessionId);
    }
    
    /**
     * Atomic turn addition with rollback capability
     */
    public ConversationTurn addUserMessageTurnAtomic(String sessionId, String userId, String userMessage,
                                                    String contextExtractorId, String identityId) {
        ConversationHistory originalHistory = null;
        ConversationTurn newTurn = null;
        
        try {
            // Save current state for potential rollback
            ConversationHistory currentHistory = sessionHistories.get(sessionId);
            if (currentHistory != null) {
                originalHistory = deepCopyHistory(currentHistory);
            }
            
            // Perform the turn addition
            newTurn = addUserMessageTurn(sessionId, userId, userMessage, contextExtractorId, identityId);
            
            log.debug("Atomic turn addition successful for session: {}", sessionId);
            return newTurn;
            
        } catch (Exception e) {
            log.error("Atomic turn addition failed for session {}, attempting rollback: {}", 
                sessionId, e.getMessage(), e);
            
            // Attempt rollback
            if (originalHistory != null) {
                sessionHistories.put(sessionId, originalHistory);
                log.info("Successfully rolled back conversation history for session: {}", sessionId);
            }
            
            throw new RuntimeException("Atomic turn addition failed", e);
        }
    }
    
    /**
     * Deep copy conversation history for rollback operations
     */
    private ConversationHistory deepCopyHistory(ConversationHistory original) {
        // In production, this would use a proper deep cloning library
        // For now, create a basic copy
        return ConversationHistory.builder()
            .sessionId(original.getSessionId())
            .userId(original.getUserId())
            .turns(new ArrayList<>(original.getTurns()))
            .createdAt(original.getCreatedAt())
            .lastUpdated(original.getLastUpdated())
            .status(original.getStatus())
            .totalTurns(original.getTotalTurns())
            .build();
    }
} 