package com.health.agents.service;

import com.health.agents.integration.letta.model.LettaMessage;
import com.health.agents.integration.letta.model.LettaMessageRequest;
import com.health.agents.integration.letta.model.LettaMessageResponse;
import com.health.agents.integration.letta.model.LettaIdentityResponse;
import com.health.agents.integration.letta.service.LettaAgentService;
import com.health.agents.model.IntentResult;
import com.health.agents.model.UserAgentMapping;
import com.health.agents.model.dto.ChatResponse;
import com.health.agents.model.enums.IntentType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.List;

@Service
@Slf4j
public class AgentOrchestrationService {
    
    @Autowired
    private UserIdentityService userIdentityService;
    
    @Autowired
    private SessionManagementService sessionManagementService;
    
    @Autowired
    private LettaAgentService lettaAgentService;
    
    public ChatResponse startChat(String userId, String sessionId) {
        log.info("Starting chat for user {} with session {}", userId, sessionId);
        
        try {
            // 1. Get or create user's agents
            UserAgentMapping agents = userIdentityService.getOrCreateUserAgents(userId);
            
            // 2. Initialize session
            sessionManagementService.startSession(userId, sessionId, agents);
            
            return ChatResponse.builder()
                .message("Health consultation session started successfully. How can I help you today?")
                .sessionId(sessionId)
                .userId(userId)
                .sessionActive(true)
                .timestamp(LocalDateTime.now())
                .build();
                
        } catch (Exception e) {
            log.error("Failed to start chat for user {}: {}", userId, e.getMessage(), e);
            return ChatResponse.builder()
                .message("Sorry, there was an error starting your session. Please try again.")
                .sessionId(sessionId)
                .userId(userId)
                .sessionActive(false)
                .timestamp(LocalDateTime.now())
                .build();
        }
    }
    
    public ChatResponse processMessage(String userId, String sessionId, String message) {
        log.info("Processing message for user {} session {}: {}", userId, sessionId, message);
        
        try {
            // 1. Get user's agents
            UserAgentMapping agents = userIdentityService.getOrCreateUserAgents(userId);
            
            // 2. Send to Context Coordinator first
            String contextualMessage = processWithContextCoordinator(
                agents.getContextCoordinatorId(), 
                agents.getIdentityId(),
                message, 
                sessionId
            );
            
            // 3. Classify intent
            IntentResult intent = classifyIntent(
                agents.getIntentClassifierId(),
                agents.getIdentityId(), 
                contextualMessage
            );
            
            // 4. Route to appropriate health agent
            String response = routeToHealthAgent(agents, intent, contextualMessage);
            
            return ChatResponse.builder()
                .message(response)
                .sessionId(sessionId)
                .userId(userId)
                .intent(intent.getIntent().toString())
                .confidence(intent.getConfidence())
                .sessionActive(true)
                .timestamp(LocalDateTime.now())
                .build();
                
        } catch (Exception e) {
            log.error("Failed to process message for user {}: {}", userId, e.getMessage(), e);
            return ChatResponse.builder()
                .message("Sorry, I encountered an error processing your message. Please try again.")
                .sessionId(sessionId)
                .userId(userId)
                .sessionActive(true)
                .timestamp(LocalDateTime.now())
                .build();
        }
    }
    
    public void endChat(String userId, String sessionId) {
        log.info("Ending chat for user {} session {}", userId, sessionId);
        
        try {
            UserAgentMapping agents = userIdentityService.getOrCreateUserAgents(userId);
            sessionManagementService.endSession(userId, sessionId, agents);
        } catch (Exception e) {
            log.error("Failed to end chat for user {}: {}", userId, e.getMessage(), e);
        }
    }
    
    private String processWithContextCoordinator(String agentId, String identityId, 
                                               String message, String sessionId) {
        log.debug("Processing with Context Coordinator: {}", agentId);
        
        LettaMessageRequest request = LettaMessageRequest.builder()
            .messages(Arrays.asList(
                LettaMessage.builder()
                    .role("user")
                    .content(String.format("Session: %s\nMessage: %s", sessionId, message))
                    .build()
            ))
            .senderId(identityId)
            .build();
            
        LettaMessageResponse response = lettaAgentService.sendMessage(agentId, request);
        
        // Extract contextual message from response
        return extractAssistantMessage(response);
    }
    
    private IntentResult classifyIntent(String agentId, String identityId, String contextualMessage) {
        log.debug("Classifying intent with agent: {}", agentId);
        
        LettaMessageRequest request = LettaMessageRequest.builder()
            .messages(Arrays.asList(
                LettaMessage.builder()
                    .role("user")
                    .content("Classify intent: " + contextualMessage)
                    .build()
            ))
            .senderId(identityId)
            .build();
            
        LettaMessageResponse response = lettaAgentService.sendMessage(agentId, request);
        
        // Parse intent classification from response
        return parseIntentFromResponse(response);
    }
    
    private String routeToHealthAgent(UserAgentMapping agents, IntentResult intent, 
                                    String contextualMessage) {
        String targetAgentId;
        String agentType;
        
        switch (intent.getIntent()) {
            case GENERAL_HEALTH:
                targetAgentId = agents.getGeneralHealthId();
                agentType = "General Health";
                break;
            case MENTAL_HEALTH:
                targetAgentId = agents.getMentalHealthId();
                agentType = "Mental Health";
                break;
            default:
                targetAgentId = agents.getGeneralHealthId(); // Default to general health
                agentType = "General Health (default)";
        }
        
        log.debug("Routing to {} agent: {}", agentType, targetAgentId);
        
        LettaMessageRequest request = LettaMessageRequest.builder()
            .messages(Arrays.asList(
                LettaMessage.builder()
                    .role("user")
                    .content(contextualMessage)
                    .build()
            ))
            .senderId(agents.getIdentityId())
            .build();
            
        LettaMessageResponse response = lettaAgentService.sendMessage(targetAgentId, request);
        
        return extractAssistantMessage(response);
    }
    
    private String extractAssistantMessage(LettaMessageResponse response) {
        if (response == null || response.getMessages() == null || response.getMessages().isEmpty()) {
            return "I apologize, but I didn't receive a proper response. Please try again.";
        }
        
        // Find the last assistant message
        for (int i = response.getMessages().size() - 1; i >= 0; i--) {
            LettaMessage message = response.getMessages().get(i);
            if ("assistant".equals(message.getRole())) {
                return message.getContent();
            }
        }
        
        // If no assistant message found, return the last message
        return response.getMessages().get(response.getMessages().size() - 1).getContent();
    }
    
    private IntentResult parseIntentFromResponse(LettaMessageResponse response) {
        String content = extractAssistantMessage(response);
        
        // Simple parsing logic - in production, this could be more sophisticated
        IntentType intent = IntentType.UNKNOWN;
        Double confidence = 0.5;
        String reasoning = "Default classification";
        
        // Look for intent indicators in the response
        if (content.toLowerCase().contains("general_health") || 
            content.toLowerCase().contains("physical") ||
            content.toLowerCase().contains("medical")) {
            intent = IntentType.GENERAL_HEALTH;
            confidence = 0.8;
            reasoning = "Physical health indicators detected";
        } else if (content.toLowerCase().contains("mental_health") || 
                   content.toLowerCase().contains("emotional") ||
                   content.toLowerCase().contains("psychological") ||
                   content.toLowerCase().contains("anxiety") ||
                   content.toLowerCase().contains("depression")) {
            intent = IntentType.MENTAL_HEALTH;
            confidence = 0.8;
            reasoning = "Mental health indicators detected";
        }
        
        // Try to extract confidence from structured response
        Pattern confidencePattern = Pattern.compile("confidence[\":\\s]*(\\d+\\.?\\d*)");
        Matcher matcher = confidencePattern.matcher(content.toLowerCase());
        if (matcher.find()) {
            try {
                confidence = Double.parseDouble(matcher.group(1));
            } catch (NumberFormatException e) {
                log.debug("Failed to parse confidence from response: {}", matcher.group(1));
            }
        }
        
        return IntentResult.builder()
            .intent(intent)
            .confidence(confidence)
            .reasoning(reasoning)
            .urgencyLevel("NORMAL")
            .routingNotes("Processed by intent classifier")
            .build();
    }

    public List<LettaIdentityResponse> testLettaConnection() {
        return userIdentityService.testLettaConnection();
    }

    public UserAgentMapping debugCreateAgent(String userId) {
        return userIdentityService.getOrCreateUserAgents(userId);
    }
} 