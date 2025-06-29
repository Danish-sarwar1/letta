package com.health.agents.service;

import com.health.agents.config.MemoryArchitectureConfig;
import com.health.agents.model.dto.ConversationTurn;
import com.health.agents.model.dto.ConversationHistory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Memory Management Service for Centralized Context Management
 * Implements memory block operations, conversation turn tracking, and memory rotation
 */
@Service
@Slf4j
public class MemoryManagementService {
    
    @Autowired
    private MemoryArchitectureConfig memoryConfig;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    /**
     * Format conversation turn for memory storage based on configured data format
     */
    public String formatConversationTurn(ConversationTurn turn) {
        try {
            if ("JSON".equals(memoryConfig.getDataFormat())) {
                return objectMapper.writeValueAsString(turn);
            } else {
                return formatAsStructuredText(turn);
            }
        } catch (JsonProcessingException e) {
            log.error("Failed to format conversation turn: {}", e.getMessage());
            return formatAsStructuredText(turn); // Fallback to structured text
        }
    }
    
    /**
     * Format conversation history for memory storage
     */
    public String formatConversationHistory(ConversationHistory history) {
        if (history.getTurns() == null || history.getTurns().isEmpty()) {
            return "No conversation turns yet for session: " + history.getSessionId();
        }
        
        StringBuilder formatted = new StringBuilder();
        formatted.append(String.format("SESSION_ID: %s | TOTAL_TURNS: %d | STATUS: %s%n", 
            history.getSessionId(), history.getTotalTurns(), history.getStatus()));
        
        for (ConversationTurn turn : history.getTurns()) {
            formatted.append("TURN_").append(turn.getTurnNumber()).append(": ");
            if ("JSON".equals(memoryConfig.getDataFormat())) {
                try {
                    formatted.append(objectMapper.writeValueAsString(turn));
                } catch (JsonProcessingException e) {
                    formatted.append(formatAsStructuredText(turn));
                }
            } else {
                formatted.append(formatAsStructuredText(turn));
            }
            formatted.append("%n");
        }
        
        return formatted.toString();
    }
    
    /**
     * Create active session memory content
     */
    public String createActiveSessionContent(String sessionId, String userId, String status, 
                                          String currentTopic, int totalTurns) {
        try {
            if ("JSON".equals(memoryConfig.getDataFormat())) {
                var sessionData = new ActiveSessionData(sessionId, userId, status, currentTopic, 
                    totalTurns, LocalDateTime.now());
                return objectMapper.writeValueAsString(sessionData);
            } else {
                return String.format(
                    "SESSION_ID: %s | USER_ID: %s | STATUS: %s | TOPIC: %s | TURNS: %d | UPDATED: %s",
                    sessionId, userId, status, currentTopic, totalTurns, LocalDateTime.now().format(formatter)
                );
            }
        } catch (JsonProcessingException e) {
            log.error("Failed to create active session content: {}", e.getMessage());
            return String.format("SESSION_ID: %s | USER_ID: %s | STATUS: %s", sessionId, userId, status);
        }
    }
    
    /**
     * Create context summary from conversation history
     */
    public String createContextSummary(ConversationHistory history) {
        if (history.getTurns() == null || history.getTurns().isEmpty()) {
            return "No conversation context available.";
        }
        
        StringBuilder summary = new StringBuilder();
        summary.append(String.format("CONVERSATION SUMMARY for %s:%n", history.getSessionId()));
        summary.append(String.format("Duration: %s to %s%n", history.getCreatedAt(), history.getLastUpdated()));
        summary.append(String.format("Total Turns: %d%n%n", history.getTotalTurns()));
        
        // Identify key topics and themes
        var topics = history.getTurns().stream()
            .filter(turn -> turn.getTopicTags() != null && !turn.getTopicTags().isEmpty())
            .map(ConversationTurn::getTopicTags)
            .distinct()
            .collect(Collectors.toList());
        
        if (!topics.isEmpty()) {
            summary.append("KEY TOPICS: ").append(String.join(", ", topics)).append("%n%n");
        }
        
        // Recent context (last few turns)
        int recentWindow = Math.min(memoryConfig.getRecentTurnsWindow(), history.getTurns().size());
        List<ConversationTurn> recentTurns = history.getTurns().subList(
            Math.max(0, history.getTurns().size() - recentWindow), history.getTurns().size());
        
        summary.append("RECENT CONTEXT:%n");
        for (ConversationTurn turn : recentTurns) {
            summary.append(String.format("Turn %d: %s -> %s%n", 
                turn.getTurnNumber(), 
                turn.getUserMessage() != null ? turn.getUserMessage().substring(0, Math.min(50, turn.getUserMessage().length())) : "N/A",
                turn.getRoutedAgent()));
        }
        
        return summary.toString();
    }
    
    /**
     * Create memory metadata content
     */
    public String createMemoryMetadata(String sessionId, int conversationHistorySize, 
                                     int activeSessionSize, int contextSummarySize) {
        StringBuilder metadata = new StringBuilder();
        metadata.append(String.format("MEMORY_USAGE for %s:%n", sessionId));
        metadata.append(String.format("conversation_history: %d/%d (%.1f%%)%n", 
            conversationHistorySize, memoryConfig.getConversationHistoryLimit(),
            (double) conversationHistorySize / memoryConfig.getConversationHistoryLimit() * 100));
        metadata.append(String.format("active_session: %d/%d (%.1f%%)%n",
            activeSessionSize, memoryConfig.getActiveSessionLimit(),
            (double) activeSessionSize / memoryConfig.getActiveSessionLimit() * 100));
        metadata.append(String.format("context_summary: %d/%d (%.1f%%)%n",
            contextSummarySize, memoryConfig.getContextSummaryLimit(),
            (double) contextSummarySize / memoryConfig.getContextSummaryLimit() * 100));
        
        // Rotation and archival triggers
        boolean needsRotation = memoryConfig.shouldRotateMemory(conversationHistorySize, 
            memoryConfig.getConversationHistoryLimit());
        metadata.append(String.format("ROTATION_NEEDED: %s%n", needsRotation));
        metadata.append(String.format("LAST_UPDATED: %s%n", LocalDateTime.now().format(formatter)));
        
        return metadata.toString();
    }
    
    /**
     * Check if memory rotation is needed
     */
    public boolean isMemoryRotationNeeded(String memoryContent, int maxSize) {
        return memoryConfig.shouldRotateMemory(memoryContent.length(), maxSize);
    }
    
    /**
     * Check if archival is needed
     */
    public boolean isArchivalNeeded(int totalTurns) {
        return memoryConfig.shouldTriggerArchival(totalTurns);
    }
    
    /**
     * Format turn as structured text
     */
    private String formatAsStructuredText(ConversationTurn turn) {
        return String.format(
            "USER: %s | ENRICHED: %s | INTENT: %s(%.2f) | AGENT: %s | RESPONSE: %s | TIME: %s",
            turn.getUserMessage() != null ? turn.getUserMessage() : "N/A",
            turn.getEnrichedMessage() != null ? turn.getEnrichedMessage() : "N/A",
            turn.getExtractedIntent() != null ? turn.getExtractedIntent() : "N/A",
            turn.getIntentConfidence() != null ? turn.getIntentConfidence() : 0.0,
            turn.getRoutedAgent() != null ? turn.getRoutedAgent() : "N/A",
            turn.getAgentResponse() != null ? 
                turn.getAgentResponse().substring(0, Math.min(100, turn.getAgentResponse().length())) + "..." : "N/A",
            turn.getTimestamp() != null ? turn.getTimestamp().format(formatter) : "N/A"
        );
    }
    
    /**
     * Inner class for active session data when using JSON format
     */
    private static class ActiveSessionData {
        public String sessionId;
        public String userId;
        public String status;
        public String currentTopic;
        public int totalTurns;
        public LocalDateTime lastUpdated;
        
        public ActiveSessionData(String sessionId, String userId, String status, 
                               String currentTopic, int totalTurns, LocalDateTime lastUpdated) {
            this.sessionId = sessionId;
            this.userId = userId;
            this.status = status;
            this.currentTopic = currentTopic;
            this.totalTurns = totalTurns;
            this.lastUpdated = lastUpdated;
        }
    }
} 