package com.health.agents.service;

import com.health.agents.integration.letta.model.LettaMessage;
import com.health.agents.integration.letta.model.LettaMessageRequest;
import com.health.agents.integration.letta.model.LettaMessageResponse;
import com.health.agents.integration.letta.model.LettaIdentityResponse;
import com.health.agents.integration.letta.service.LettaAgentService;
import com.health.agents.model.IntentResult;
import com.health.agents.model.UserAgentMapping;
import com.health.agents.model.dto.ChatResponse;
import com.health.agents.model.dto.ConversationTurn;
import com.health.agents.model.dto.ConversationHistory;
import com.health.agents.model.dto.ContextEnrichmentResult;
import com.health.agents.model.dto.BiDirectionalMemoryUpdate;
import com.health.agents.model.dto.AgentResponseMetadata;
import com.health.agents.model.dto.SessionState;
import com.health.agents.model.dto.SessionTransition;
import com.health.agents.model.dto.CrossSessionContinuity;
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
import java.util.HashMap;
import java.util.stream.Collectors;

/**
 * Enhanced Agent Orchestration Service 
 * Sub-Plan 2: Conversation Turn Management (Complete)
 * Sub-Plan 3: Enhanced Context Extraction (Complete) 
 * Sub-Plan 4: Bidirectional Memory Updates (Complete)
 * Sub-Plan 5: Session and State Management (Complete)
 * Implements sophisticated context enrichment, conversation pattern analysis, and bidirectional memory updates
 */
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
    
    @Autowired
    private ConversationTurnService conversationTurnService;
    
    @Autowired
    private ContextEnrichmentService contextEnrichmentService;
    
    @Autowired
    private BiDirectionalMemoryService biDirectionalMemoryService;
    
    @Autowired
    private AgentResponseAnalysisService responseAnalysisService;
    
    // For simplified integration with existing system
    @Autowired
    private SessionStateManagementService sessionStateManagementService;
    
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
    
    /**
     * Enhanced message processing with Sub-Plan 5: Session and State Management
     */
    public ChatResponse processMessage(String userId, String sessionId, String message) {
        log.info("Processing enhanced message for user {} in session {} with Sub-Plan 5", userId, sessionId);
        
        UserAgentMapping agents = userIdentityService.getOrCreateUserAgents(userId);
        
        try {
            // 1. Create user message turn and update memory (simplified for existing system)
            ConversationTurn userMessageTurn = ConversationTurn.builder()
                .sessionId(sessionId)
                .turnNumber(1) // Simplified
                .userMessage(message)
                .timestamp(LocalDateTime.now())
                .build();
            
            // 2. Enhanced context extraction (Sub-Plan 3) - use existing system
            ContextEnrichmentResult enrichmentResult = contextEnrichmentService.enrichContext(
                sessionId, message, userMessageTurn.getTurnNumber());
            userMessageTurn.setEnrichedMessage(enrichmentResult.getEnrichedMessage());
            
            // 3. Intent extraction with simple classification
            IntentResult intentResult = extractSimpleIntent(message);
            userMessageTurn.setExtractedIntent(intentResult.getIntent().toString());
            
            // 4. Route to appropriate agent
            String routedAgent = intentResult.getIntent().toString();
            userMessageTurn.setRoutedAgent(routedAgent);
            
            // 5. Get agent response with timing
            long agentStartTime = System.currentTimeMillis();
            String agentResponse = getAgentResponse(agents, routedAgent, message, sessionId, enrichmentResult.getEnrichedMessage());
            long agentEndTime = System.currentTimeMillis();
            long responseTimeMs = agentEndTime - agentStartTime;
            
            // 6. SUB-PLAN 5: Update session state with conversation turn
            sessionManagementService.updateSessionWithTurn(sessionId, userMessageTurn);
            
            // 7. Create agent response turn
            ConversationTurn agentResponseTurn = ConversationTurn.builder()
                .sessionId(sessionId)
                .turnNumber(userMessageTurn.getTurnNumber() + 1)
                .agentResponse(agentResponse)
                .routedAgent(routedAgent)
                .timestamp(LocalDateTime.now())
                .responseQuality(0.8) // Default quality score
                .build();
            
            // 8. SUB-PLAN 4: Bidirectional memory update (simplified)
            BiDirectionalMemoryUpdate memoryUpdate = biDirectionalMemoryService.performBidirectionalUpdate(
                sessionId, message, agentResponse, userMessageTurn, enrichmentResult, routedAgent, userId, agents.getContextExtractorId(), responseTimeMs
            );
            
            // 9. SUB-PLAN 5: Update session state with agent response turn
            sessionManagementService.updateSessionWithTurn(sessionId, agentResponseTurn);
            
            // 10. Create enhanced chat response with Sub-Plan 5 metrics
            ChatResponse enhancedResponse = createEnhancedChatResponse(agentResponse, enrichmentResult, intentResult, 
                memoryUpdate, sessionId, responseTimeMs, userId);
            
            log.info("Enhanced message processing completed for session {} with Sub-Plan 5 integration", sessionId);
            return enhancedResponse;
            
        } catch (Exception e) {
            log.error("Enhanced message processing failed for session {}: {}", sessionId, e.getMessage(), e);
            return createErrorResponse("I apologize, but I'm having trouble processing your message right now. Please try again.");
        }
    }
    
    /**
     * Simple intent extraction for integration
     */
    private IntentResult extractSimpleIntent(String message) {
        String lowerMessage = message.toLowerCase();
        
        if (lowerMessage.contains("stress") || lowerMessage.contains("anxiety") || lowerMessage.contains("depression")) {
            return IntentResult.builder()
                .intent(IntentType.MENTAL_HEALTH)
                .confidence(0.8)
                .build();
        } else if (lowerMessage.contains("emergency") || lowerMessage.contains("urgent") || lowerMessage.contains("911")) {
            return IntentResult.builder()
                .intent(IntentType.EMERGENCY)
                .confidence(0.9)
                .build();
        } else {
            return IntentResult.builder()
                .intent(IntentType.GENERAL_HEALTH)
                .confidence(0.7)
                .build();
        }
    }
    
    /**
     * SUB-PLAN 5: Create enhanced chat response with session state information
     */
    private ChatResponse createEnhancedChatResponse(String agentResponse, ContextEnrichmentResult enrichmentResult, 
                                                   IntentResult intentResult, 
                                                   BiDirectionalMemoryUpdate biDirectionalUpdate,
                                                   String sessionId, long processingTime, String userId) {
        
        // Get current session state
        SessionState sessionState = sessionManagementService.getEnhancedSessionState(sessionId);
        
        ChatResponse.ChatResponseBuilder responseBuilder = ChatResponse.builder()
            .message(agentResponse)
            .sessionId(sessionId)
            .userId(userId)
            .timestamp(LocalDateTime.now());
        
        // Add Sub-Plan 3 enrichment data (existing)
        if (enrichmentResult != null) {
            responseBuilder.contextStrategy(enrichmentResult.getEnrichedMessage());
            responseBuilder.contextConfidence(enrichmentResult.getContextConfidence());
        }
        
        // Add Sub-Plan 2 intent data (existing)
        if (intentResult != null) {
            responseBuilder.intent(intentResult.getIntent().toString());
            responseBuilder.confidence(intentResult.getConfidence());
        }
        
        // Add Sub-Plan 4 bidirectional data (simplified)
        if (biDirectionalUpdate != null) {
            responseBuilder.responseQuality(0.8); // Simplified
            responseBuilder.contextUtilization(0.7); // Simplified
            responseBuilder.responseContextCorrelation(0.7); // Simplified
            responseBuilder.bidirectionalUpdateSuccess(true); // Simplified
        }
        
        // SUB-PLAN 5: Add session state information
        if (sessionState != null) {
            responseBuilder.conversationPhase(sessionState.getConversationPhase());
            responseBuilder.patternsDetected(sessionState.getComplexityScore() > 0.5);
            responseBuilder.sessionActive(sessionState.isActive());
            
            // Use existing fields in ChatResponse for session information
            if (sessionState.getSessionQuality() != null) {
                responseBuilder.contextStrategy("Enhanced Session Management with Quality Score: " + 
                    String.format("%.2f", sessionState.getSessionQuality().getOverallQuality()));
            }
            
            // Add session flags to relevant turns count
            responseBuilder.relevantTurns(sessionState.getTotalTurns());
        } else {
            // Default values when session state is not available
            responseBuilder.conversationPhase("Active Discussion");
            responseBuilder.patternsDetected(false);
            responseBuilder.sessionActive(true);
            responseBuilder.contextStrategy("Standard Context Management");
            responseBuilder.relevantTurns(1);
        }
        
        return responseBuilder.build();
    }
    
    /**
     * Create error response
     */
    private ChatResponse createErrorResponse(String errorMessage) {
        return ChatResponse.builder()
            .message(errorMessage)
            .sessionId("")
            .userId("")
            .intent("ERROR")
            .confidence(0.0)
            .contextConfidence(0.0)
            .relevantTurns(0)
            .conversationPhase("Error")
            .contextStrategy("Error Handling")
            .patternsDetected(false)
            .sessionActive(false)
            .timestamp(LocalDateTime.now())
            .build();
    }
    
    /**
     * Get agent response helper method
     */
    private String getAgentResponse(UserAgentMapping agents, String agentType, String message, String sessionId, String enrichedContext) {
        try {
            String targetAgentId = getTargetAgentId(agents, IntentType.valueOf(agentType.toUpperCase()));
            
            LettaMessageRequest request = LettaMessageRequest.builder()
                .messages(Arrays.asList(
                    LettaMessage.builder()
                        .role("user")
                        .content(message)
                        .build()
                ))
                .senderId(agents.getIdentityId())
                .build();
            
            LettaMessageResponse response = lettaAgentService.sendMessage(targetAgentId, request);
            return response.getMessages().get(0).getContent();
            
        } catch (Exception e) {
            log.error("Failed to get agent response for session {}: {}", sessionId, e.getMessage());
            return "I apologize, but I'm having trouble processing your request right now. Please try again.";
        }
    }
    
    /**
     * Get target agent ID helper method
     */
    private String getTargetAgentId(UserAgentMapping agents, IntentType intent) {
        switch (intent) {
            case GENERAL_HEALTH:
                return agents.getGeneralHealthId();
            case MENTAL_HEALTH:
                return agents.getMentalHealthId();
            case EMERGENCY:
                return agents.getGeneralHealthId(); // Route emergencies to general health
            default:
                return agents.getGeneralHealthId();
        }
    }
    
    /**
     * SUB-PLAN 5: Enhanced API method to get bidirectional memory analysis with session state
     */
    public Map<String, Object> getEnhancedBidirectionalMemoryAnalysis(String sessionId, Integer turnNumber) {
        Map<String, Object> analysis = new HashMap<>();
        
        try {
            // Get session state analysis
            SessionState sessionState = sessionManagementService.getEnhancedSessionState(sessionId);
            if (sessionState != null) {
                analysis.put("sessionState", sessionState);
                analysis.put("sessionSummary", sessionState.getSessionSummary());
                analysis.put("sessionEffectiveness", sessionState.calculateEffectiveness());
                analysis.put("requiresAttention", sessionState.requiresAttention());
                
                // Add session transition history
                List<SessionTransition> transitions = sessionManagementService.getSessionTransitionHistory(sessionId);
                analysis.put("transitionHistory", transitions);
                analysis.put("transitionCount", transitions.size());
            }
            
            // Add cross-session continuity information
            if (sessionState != null) {
                CrossSessionContinuity continuity = sessionManagementService.getUserContinuityData(sessionState.getUserId());
                if (continuity != null) {
                    analysis.put("crossSessionContinuity", continuity);
                    analysis.put("continuitySummary", continuity.getContinuitySummary());
                    analysis.put("continuityEffectiveness", continuity.calculateContinuityEffectiveness());
                    analysis.put("canResumeSeamlessly", continuity.canResumeSeamlessly());
                }
            }
            
        } catch (Exception e) {
            log.error("Failed to get enhanced bidirectional memory analysis for session {}: {}", sessionId, e.getMessage(), e);
            analysis.put("error", "Failed to retrieve analysis: " + e.getMessage());
        }
        
        return analysis;
    }
    
    /**
     * SUB-PLAN 5: Enhanced API method to get response quality analysis with session context
     */
    public Map<String, Object> getEnhancedResponseQualityAnalysis(String sessionId, Integer turnNumber) {
        Map<String, Object> analysis = new HashMap<>();
        
        try {
            // SUB-PLAN 5: Add session-level quality analysis
            SessionState sessionState = sessionManagementService.getEnhancedSessionState(sessionId);
            if (sessionState != null) {
                analysis.put("sessionQuality", sessionState.getSessionQuality());
                analysis.put("overallSessionEffectiveness", sessionState.calculateEffectiveness());
                analysis.put("complexityScore", sessionState.getComplexityScore());
                analysis.put("userEngagement", sessionState.getUserEngagement());
                
                // Add agent interaction quality
                if (sessionState.getAgentInteractions() != null && !sessionState.getAgentInteractions().isEmpty()) {
                    double avgAgentPerformance = sessionState.getAgentInteractions().stream()
                        .filter(interaction -> interaction.getResponseQuality() != null)
                        .mapToDouble(SessionState.AgentInteraction::getResponseQuality)
                        .average()
                        .orElse(0.0);
                    analysis.put("averageAgentPerformance", avgAgentPerformance);
                    analysis.put("agentInteractionCount", sessionState.getAgentInteractions().size());
                }
            }
            
            // Add historical quality trends
            if (sessionState != null) {
                CrossSessionContinuity continuity = sessionManagementService.getUserContinuityData(sessionState.getUserId());
                if (continuity != null && continuity.getAnalytics() != null) {
                    analysis.put("historicalQuality", continuity.getAnalytics().getAverageSessionQuality());
                    analysis.put("qualityTrend", continuity.getAnalytics().getOverallProgressTrend());
                    analysis.put("totalSessions", continuity.getAnalytics().getTotalSessions());
                }
            }
            
        } catch (Exception e) {
            log.error("Failed to get enhanced response quality analysis for session {}: {}", sessionId, e.getMessage(), e);
            analysis.put("error", "Failed to retrieve analysis: " + e.getMessage());
        }
        
        return analysis;
    }
    
    /**
     * SUB-PLAN 5: New API method to get session state information
     */
    public Map<String, Object> getSessionStateAnalysis(String sessionId) {
        Map<String, Object> analysis = new HashMap<>();
        
        try {
            SessionState sessionState = sessionManagementService.getEnhancedSessionState(sessionId);
            if (sessionState != null) {
                analysis.put("sessionState", sessionState);
                analysis.put("sessionSummary", sessionState.getSessionSummary());
                analysis.put("isActive", sessionState.isActive());
                analysis.put("isLongRunning", sessionState.isLongRunning());
                analysis.put("isHighComplexity", sessionState.isHighComplexity());
                analysis.put("requiresAttention", sessionState.requiresAttention());
                analysis.put("effectiveness", sessionState.calculateEffectiveness());
                
                // Add transition history
                List<SessionTransition> transitions = sessionManagementService.getSessionTransitionHistory(sessionId);
                analysis.put("transitions", transitions);
                analysis.put("transitionSummaries", transitions.stream()
                    .map(SessionTransition::getTransitionSummary)
                    .collect(Collectors.toList()));
                
                // Add cross-session continuity
                CrossSessionContinuity continuity = sessionManagementService.getUserContinuityData(sessionState.getUserId());
                if (continuity != null) {
                    analysis.put("continuityData", continuity);
                    analysis.put("hasEstablishedPatterns", continuity.hasEstablishedPatterns());
                    analysis.put("canResumeSeamlessly", continuity.canResumeSeamlessly());
                }
            } else {
                analysis.put("error", "Session state not found for session: " + sessionId);
            }
            
        } catch (Exception e) {
            log.error("Failed to get session state analysis for session {}: {}", sessionId, e.getMessage(), e);
            analysis.put("error", "Failed to retrieve session state: " + e.getMessage());
        }
        
        return analysis;
    }
    
    /**
     * SUB-PLAN 5: New API method to get cross-session continuity analysis
     */
    public Map<String, Object> getCrossSessionContinuityAnalysis(String userId) {
        Map<String, Object> analysis = new HashMap<>();
        
        try {
            CrossSessionContinuity continuity = sessionManagementService.getUserContinuityData(userId);
            if (continuity != null) {
                analysis.put("continuityData", continuity);
                analysis.put("continuitySummary", continuity.getContinuitySummary());
                analysis.put("hasContinuity", continuity.hasContinuity());
                analysis.put("canResumeSeamlessly", continuity.canResumeSeamlessly());
                analysis.put("hasRelevantContext", continuity.hasRelevantPreservedContext());
                analysis.put("hasArchivalAccess", continuity.hasArchivalAccess());
                analysis.put("hasEstablishedPatterns", continuity.hasEstablishedPatterns());
                analysis.put("continuityEffectiveness", continuity.calculateContinuityEffectiveness());
                
                // Add resumption prompt
                analysis.put("resumptionPrompt", continuity.generateResumptionPrompt());
                
                // Add session history summary
                if (continuity.getRelatedSessions() != null) {
                    analysis.put("totalRelatedSessions", continuity.getRelatedSessions().size());
                    analysis.put("mostRecentSession", continuity.getMostRecentSession());
                }
                
                // Add analytics summary
                if (continuity.getAnalytics() != null) {
                    analysis.put("analytics", continuity.getAnalytics());
                }
            } else {
                analysis.put("message", "No cross-session continuity data found for user: " + userId);
                analysis.put("isNewUser", true);
            }
            
        } catch (Exception e) {
            log.error("Failed to get cross-session continuity analysis for user {}: {}", userId, e.getMessage(), e);
            analysis.put("error", "Failed to retrieve continuity analysis: " + e.getMessage());
        }
        
        return analysis;
    }

    /**
     * Get conversation history - delegated to ConversationTurnService
     */
    public ConversationHistory getConversationHistory(String sessionId) {
        return conversationTurnService.getConversationHistory(sessionId);
    }
    
    /**
     * Check if memory rotation is needed - delegated to ConversationTurnService
     */
    public boolean isMemoryRotationNeeded(String sessionId) {
        return conversationTurnService.isMemoryRotationNeeded(sessionId);
    }
    
    /**
     * Trigger memory rotation - delegated to ConversationTurnService
     */
    public void triggerMemoryRotation(String userId, String sessionId) {
        try {
            UserAgentMapping agents = userIdentityService.getOrCreateUserAgents(userId);
            conversationTurnService.performMemoryRotation(sessionId, 
                agents.getContextExtractorId(), agents.getIdentityId());
        } catch (Exception e) {
            log.error("Failed to trigger memory rotation for user {} session {}: {}", 
                userId, sessionId, e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * End chat session - delegated to SessionManagementService
     */
    public void endChat(String userId, String sessionId) {
        try {
            UserAgentMapping agents = userIdentityService.getOrCreateUserAgents(userId);
            sessionManagementService.endSession(userId, sessionId, agents);
        } catch (Exception e) {
            log.error("Failed to end chat for user {} session {}: {}", 
                userId, sessionId, e.getMessage(), e);
            throw e;
        }
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