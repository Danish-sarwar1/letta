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
import java.util.Map;

@Service
@Slf4j
public class AgentOrchestrationService {

    @Autowired
    private UserIdentityService userIdentityService;

    @Autowired
    private SessionManagementService sessionManagementService;

    @Autowired
    private LettaAgentService lettaAgentService;

    @Autowired
    private AgentPromptService agentPromptService;



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

            // 2. Process with Context Extractor - maintain complete conversation history and enrich current message
            String enrichedMessage = extractAndEnrichContext(
                agents.getContextExtractorId(), 
                agents.getIdentityId(),
                message, 
                sessionId
            );

            // 3. Extract intent from enriched message (pure classification)
            IntentResult intent = extractIntent(
                agents.getIntentExtractorId(),
                agents.getIdentityId(), 
                enrichedMessage
            );

            // 4. Route to appropriate health agent with enriched context
            String response = routeToHealthAgent(agents, intent, enrichedMessage, sessionId);

            // 5. Update conversation history with the agent's response
            updateConversationHistory(agents.getContextExtractorId(), agents.getIdentityId(), response, sessionId);

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

    /**
     * Context Extractor: Maintains complete conversation history in Letta memory and enriches current message
     */
    private String extractAndEnrichContext(String contextExtractorId, String identityId, 
                                          String message, String sessionId) {
        log.debug("Extracting and enriching context with agent: {}", contextExtractorId);

        // Send message to Context Extractor to update conversation history and provide enriched context
        LettaMessageRequest request = LettaMessageRequest.builder()
            .messages(Arrays.asList(
                LettaMessage.builder()
                    .role("user")
                    .content(String.format("SESSION:%s\nNEW_MESSAGE:%s\n\nPlease add this new message to your conversation_history by appending it to the existing history with the next sequential message number. Do NOT restart numbering from MSG1. Provide the enriched message with relevant context from the entire conversation for intent classification.", 
                        sessionId, message))
                    .build()
            ))
            .senderId(identityId)
            .build();

        LettaMessageResponse response = lettaAgentService.sendMessage(contextExtractorId, request);

        // Extract enriched message from response
        return extractAssistantMessage(response);
    }

    /**
     * Intent Extractor: Pure intent classification without context management
     */
    private IntentResult extractIntent(String intentExtractorId, String identityId, String enrichedMessage) {
        log.debug("Extracting intent with agent: {}", intentExtractorId);

        // Send enriched message to Intent Extractor for pure classification
        LettaMessageRequest request = LettaMessageRequest.builder()
            .messages(Arrays.asList(
                LettaMessage.builder()
                    .role("user")
                    .content(String.format("ENRICHED_MESSAGE_FOR_CLASSIFICATION:\n%s\n\nPlease classify the intent as: GENERAL_HEALTH, MENTAL_HEALTH, EMERGENCY, or UNCLEAR. Provide your classification with confidence score.", enrichedMessage))
                    .build()
            ))
            .senderId(identityId)
            .build();

        LettaMessageResponse response = lettaAgentService.sendMessage(intentExtractorId, request);

        // Parse intent classification from response
        return parseIntentFromResponse(response);
    }

    private String routeToHealthAgent(UserAgentMapping agents, IntentResult intent, 
                                    String enrichedMessage, String sessionId) {
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

        // First, get the full conversation history from the Context Extractor
        String conversationHistory = getConversationHistory(agents.getContextExtractorId(), agents.getIdentityId(), sessionId);

        // Send both the enriched message and conversation history to the health agent
        LettaMessageRequest request = LettaMessageRequest.builder()
            .messages(Arrays.asList(
                LettaMessage.builder()
                    .role("system")
                    .content(String.format("CONVERSATION_HISTORY:\n%s", conversationHistory))
                    .build(),
                LettaMessage.builder()
                    .role("user")
                    .content(enrichedMessage)
                    .build()
            ))
            .senderId(agents.getIdentityId())
            .build();

        LettaMessageResponse response = lettaAgentService.sendMessage(targetAgentId, request);

        return extractAssistantMessage(response);
    }

    private String getConversationHistory(String contextExtractorId, String identityId, String sessionId) {
        LettaMessageRequest request = LettaMessageRequest.builder()
            .messages(Arrays.asList(
                LettaMessage.builder()
                    .role("system")
                    .content("Please provide the current conversation_history for this session.")
                    .build()
            ))
            .senderId(identityId)
            .build();

        LettaMessageResponse response = lettaAgentService.sendMessage(contextExtractorId, request);
        return extractAssistantMessage(response);
    }

    private String extractAssistantMessage(LettaMessageResponse response) {
        if (response == null || response.getMessages() == null || response.getMessages().isEmpty()) {
            return "I apologize, but I didn't receive a proper response. Please try again.";
        }

        // Find the last assistant message
        for (int i = response.getMessages().size() - 1; i >= 0; i--) {
            LettaMessage message = response.getMessages().get(i);
            if ("assistant_message".equals(message.getMessageType())) {
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

    private void updateConversationHistory(String contextExtractorId, String identityId, 
                                         String agentResponse, String sessionId) {
        log.debug("Updating conversation history with agent response");

        // Send the agent's response to be added to conversation history
        LettaMessageRequest request = LettaMessageRequest.builder()
            .messages(Arrays.asList(
                LettaMessage.builder()
                    .role("system")
                    .content(String.format("SESSION:%s\nAGENT_RESPONSE:%s\n\nPlease append this agent response to the conversation_history.", 
                        sessionId, agentResponse))
                    .build()
            ))
            .senderId(identityId)
            .build();

        lettaAgentService.sendMessage(contextExtractorId, request);
    }

    public List<LettaIdentityResponse> testLettaConnection() {
        return userIdentityService.testLettaConnection();
    }

    public UserAgentMapping debugCreateAgent(String userId) {
        return userIdentityService.getOrCreateUserAgents(userId);
    }

    public Map<String, Object> getPromptStatistics() {
        return agentPromptService.getPromptStatistics();
    }

    public boolean areAllPromptsLoaded() {
        return agentPromptService.areAllPromptsLoaded();
    }
} 
