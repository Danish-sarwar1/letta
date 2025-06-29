package com.health.agents.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * Conversation Patterns for Sub-Plan 3: Enhanced Context Extraction
 * Analyzes conversation patterns to provide better context understanding
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConversationPatterns {
    
    /**
     * Frequency of different topics in the conversation
     */
    private Map<String, Integer> topicFrequency;
    
    /**
     * Progression of topics throughout the conversation
     */
    private List<String> topicProgression;
    
    /**
     * Patterns of which agents were used for different types of queries
     */
    private Map<String, Integer> agentRoutingPatterns;
    
    /**
     * Progression of emotional states throughout the conversation
     */
    private List<String> emotionalProgression;
    
    /**
     * Current phase of the conversation (e.g., "Initial Assessment", "Active Discussion")
     */
    private String conversationPhase;
    
    /**
     * Detected trends in the conversation
     */
    private ConversationTrends conversationTrends;
    
    /**
     * Total number of turns in the conversation
     */
    private int totalTurns;
    
    /**
     * Duration of the conversation session
     */
    private String sessionDuration;
    
    /**
     * Most frequent topic in the conversation
     */
    public String getMostFrequentTopic() {
        if (topicFrequency == null || topicFrequency.isEmpty()) {
            return "General Discussion";
        }
        
        return topicFrequency.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse("General Discussion");
    }
    
    /**
     * Most recent topic in the conversation
     */
    public String getMostRecentTopic() {
        if (topicProgression == null || topicProgression.isEmpty()) {
            return "General Discussion";
        }
        
        return topicProgression.get(topicProgression.size() - 1);
    }
    
    /**
     * Most frequently used agent
     */
    public String getMostUsedAgent() {
        if (agentRoutingPatterns == null || agentRoutingPatterns.isEmpty()) {
            return "General Health";
        }
        
        return agentRoutingPatterns.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse("General Health");
    }
    
    /**
     * Current emotional state (most recent)
     */
    public String getCurrentEmotionalState() {
        if (emotionalProgression == null || emotionalProgression.isEmpty()) {
            return "Neutral";
        }
        
        return emotionalProgression.get(emotionalProgression.size() - 1);
    }
} 