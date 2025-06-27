package com.health.agents.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Service for managing agent prompts and system messages.
 * Loads prompt templates from resource files and provides them to agents.
 */
@Service
@Slf4j
public class AgentPromptService {
    
    public enum AgentType {
        CONTEXT_COORDINATOR,
        INTENT_CLASSIFIER,
        GENERAL_HEALTH,
        MENTAL_HEALTH
    }
    
    private final Map<AgentType, String> promptCache = new HashMap<>();
    
    @PostConstruct
    public void loadPrompts() {
        for (AgentType agentType : AgentType.values()) {
            String prompt = getAgentPrompt(agentType);
            promptCache.put(agentType, prompt);
        }
    }
    
    /**
     * Get the system prompt for a specific agent type
     */
    public String getAgentPrompt(AgentType agentType) {
        return promptCache.computeIfAbsent(agentType, type -> {
            try {
                return loadPromptFromFile(type);
            } catch (IOException e) {
                log.error("Failed to load prompt for {}: {}", type, e.getMessage());
                return getDefaultPrompt(type);
            }
        });
    }
    
    /**
     * Get the system prompt with additional context injection
     */
    public String getAgentPromptWithContext(AgentType agentType, String additionalContext) {
        String basePrompt = getAgentPrompt(agentType);
        if (additionalContext != null && !additionalContext.trim().isEmpty()) {
            return basePrompt + "\n\n## Additional Context:\n" + additionalContext;
        }
        return basePrompt;
    }
    
    /**
     * Get context coordinator prompt with session information
     */
    public String getContextCoordinatorPrompt(String userId, String sessionId) {
        String basePrompt = getAgentPrompt(AgentType.CONTEXT_COORDINATOR);
        String sessionContext = String.format(
            "\n\n## Current Session Information:\n" +
            "- User ID: %s\n" +
            "- Session ID: %s\n" +
            "- Your role: Manage this specific session's context and conversation flow",
            userId, sessionId
        );
        return basePrompt + sessionContext;
    }
    
    /**
     * Get intent classifier prompt with classification guidelines
     */
    public String getIntentClassifierPrompt(String userHistoryContext) {
        String basePrompt = getAgentPrompt(AgentType.INTENT_CLASSIFIER);
        if (userHistoryContext != null && !userHistoryContext.trim().isEmpty()) {
            String historyContext = String.format(
                "\n\n## User History Context:\n%s\n" +
                "Use this historical context to improve classification accuracy.",
                userHistoryContext
            );
            return basePrompt + historyContext;
        }
        return basePrompt;
    }
    
    /**
     * Get general health agent prompt with user health history
     */
    public String getGeneralHealthPrompt(String healthHistory, String currentSymptoms) {
        String basePrompt = getAgentPrompt(AgentType.GENERAL_HEALTH);
        StringBuilder contextBuilder = new StringBuilder();
        
        if (healthHistory != null && !healthHistory.trim().isEmpty()) {
            contextBuilder.append("\n\n## User Health History:\n")
                         .append(healthHistory);
        }
        
        if (currentSymptoms != null && !currentSymptoms.trim().isEmpty()) {
            contextBuilder.append("\n\n## Current Symptoms/Concerns:\n")
                         .append(currentSymptoms);
        }
        
        if (contextBuilder.length() > 0) {
            contextBuilder.append("\n\nUse this information to provide personalized health guidance.");
            return basePrompt + contextBuilder.toString();
        }
        
        return basePrompt;
    }
    
    /**
     * Get mental health agent prompt with therapeutic context
     */
    public String getMentalHealthPrompt(String mentalHealthHistory, String currentEmotionalState) {
        String basePrompt = getAgentPrompt(AgentType.MENTAL_HEALTH);
        StringBuilder contextBuilder = new StringBuilder();
        
        if (mentalHealthHistory != null && !mentalHealthHistory.trim().isEmpty()) {
            contextBuilder.append("\n\n## User Mental Health History:\n")
                         .append(mentalHealthHistory);
        }
        
        if (currentEmotionalState != null && !currentEmotionalState.trim().isEmpty()) {
            contextBuilder.append("\n\n## Current Emotional State:\n")
                         .append(currentEmotionalState);
        }
        
        if (contextBuilder.length() > 0) {
            contextBuilder.append("\n\nUse this information to provide personalized therapeutic support.");
            return basePrompt + contextBuilder.toString();
        }
        
        return basePrompt;
    }
    
    /**
     * Create a system message for Letta agent initialization
     */
    public String createSystemMessage(AgentType agentType, String additionalContext) {
        return getAgentPromptWithContext(agentType, additionalContext);
    }
    
    /**
     * Get emergency response prompt for crisis situations
     */
    public String getEmergencyResponsePrompt(AgentType agentType) {
        String basePrompt = getAgentPrompt(agentType);
        String emergencyAddition = "\n\n## EMERGENCY PROTOCOL ACTIVATED:\n" +
            "This appears to be a potential emergency situation. Prioritize user safety above all else. " +
            "Provide immediate crisis resources and strongly encourage professional intervention.";
        return basePrompt + emergencyAddition;
    }
    
    private String loadPromptFromFile(AgentType agentType) throws IOException {
        String filename = getPromptFilename(agentType);
        try {
            ClassPathResource resource = new ClassPathResource("prompts/" + filename);
            String prompt = StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
            log.info("Loaded prompt for {}: {} characters", agentType, prompt.length());
            return prompt;
        } catch (IOException e) {
            log.error("Failed to load prompt for {}: {}", agentType, e.getMessage());
            return getDefaultPrompt(agentType);
        }
    }
    
    private String getPromptFilename(AgentType agentType) {
        switch (agentType) {
            case CONTEXT_COORDINATOR:
                return "context-coordinator.txt";
            case INTENT_CLASSIFIER:
                return "intent-classifier.txt";
            case GENERAL_HEALTH:
                return "general-health.txt";
            case MENTAL_HEALTH:
                return "mental-health.txt";
            default:
                throw new IllegalArgumentException("Unknown agent type: " + agentType);
        }
    }
    
    private String getDefaultPrompt(AgentType agentType) {
        switch (agentType) {
            case CONTEXT_COORDINATOR:
                return "You are a Context Coordinator for health consultations. " +
                       "Manage session context and conversation flow effectively.";
            case INTENT_CLASSIFIER:
                return "You are an Intent Classifier for health queries. " +
                       "Classify user messages as GENERAL_HEALTH or MENTAL_HEALTH with confidence scores.";
            case GENERAL_HEALTH:
                return "You are a General Health consultant. " +
                       "Provide helpful health information while maintaining medical disclaimers.";
            case MENTAL_HEALTH:
                return "You are a Mental Health support agent. " +
                       "Provide empathetic support while maintaining professional boundaries.";
            default:
                return "You are a helpful health assistant.";
        }
    }
    
    /**
     * Validate that all required prompts are loaded
     */
    public boolean areAllPromptsLoaded() {
        return promptCache.size() == AgentType.values().length &&
               promptCache.values().stream().noneMatch(prompt -> prompt == null || prompt.trim().isEmpty());
    }
    
    /**
     * Get prompt statistics for monitoring
     */
    public Map<String, Object> getPromptStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalPrompts", promptCache.size());
        stats.put("expectedPrompts", AgentType.values().length);
        stats.put("allLoaded", areAllPromptsLoaded());
        
        Map<String, Integer> promptLengths = new HashMap<>();
        promptCache.forEach((type, prompt) -> 
            promptLengths.put(type.name(), prompt.length()));
        stats.put("promptLengths", promptLengths);
        
        return stats;
    }
} 