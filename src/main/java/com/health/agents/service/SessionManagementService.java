package com.health.agents.service;

import com.health.agents.integration.letta.model.LettaMessage;
import com.health.agents.integration.letta.model.LettaMessageRequest;
import com.health.agents.integration.letta.service.LettaAgentService;
import com.health.agents.model.UserAgentMapping;
import com.health.agents.model.dto.SessionState;
import com.health.agents.model.dto.SessionTransition;
import com.health.agents.model.dto.CrossSessionContinuity;
import com.health.agents.model.dto.ConversationTurn;
import com.health.agents.model.dto.ConversationHistory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Enhanced Session Management Service for Sub-Plan 5: Session and State Management
 * Includes sophisticated session state tracking, boundary management, and cross-session continuity
 */
@Service
@Slf4j
public class SessionManagementService {
    
    @Autowired
    private LettaAgentService lettaAgentService;
    
    @Autowired
    private SessionStateManagementService sessionStateManagementService;
    
    @Autowired
    private ConversationTurnService conversationTurnService;
    
    // Legacy session tracking for backward compatibility
    private final ConcurrentHashMap<String, String> legacySessionStatus = new ConcurrentHashMap<>();
    
    /**
     * Enhanced session start with Sub-Plan 5 state management
     */
    public void startSession(String userId, String sessionId, UserAgentMapping agents) {
        log.info("Starting enhanced session {} for user {} with Sub-Plan 5 state management", sessionId, userId);
        
        try {
            // Initialize comprehensive session state
            SessionState sessionState = sessionStateManagementService.initializeSession(sessionId, userId, agents);
            
            // Legacy session context for backward compatibility
            String sessionContext = createLegacySessionContext(sessionId, userId, sessionState);
            
            // Send session start message to context extractor
            LettaMessageRequest sessionStartRequest = LettaMessageRequest.builder()
                .messages(Arrays.asList(
                    LettaMessage.builder()
                        .role("system")
                        .content("ENHANCED_SESSION_START: " + sessionContext)
                        .build()
                ))
                .senderId(agents.getIdentityId())
                .build();
                
            lettaAgentService.sendMessage(agents.getContextExtractorId(), sessionStartRequest);
            
            // Load and apply cross-session continuity
            CrossSessionContinuity continuity = sessionStateManagementService.getCrossSessionContinuity(userId);
            if (continuity != null && continuity.hasContinuity()) {
                applyContinuityToSession(agents, continuity);
            }
            
            // Update legacy tracking
            legacySessionStatus.put(sessionId, "ACTIVE");
            
            log.info("Enhanced session {} started successfully with state management", sessionId);
            
        } catch (Exception e) {
            log.error("Failed to start enhanced session {}: {}", sessionId, e.getMessage(), e);
            throw new RuntimeException("Enhanced session start failed", e);
        }
    }
    
    /**
     * Enhanced session end with Sub-Plan 5 cleanup and archival
     */
    public void endSession(String userId, String sessionId, UserAgentMapping agents) {
        log.info("Ending enhanced session {} for user {} with Sub-Plan 5 cleanup", sessionId, userId);
        
        try {
            // Get current session state
            SessionState sessionState = sessionStateManagementService.getSessionState(sessionId);
            
            if (sessionState != null) {
                // End session with comprehensive cleanup
                SessionTransition endTransition = sessionStateManagementService.endSession(
                    sessionId, "User ended session", agents);
                
                // Create enhanced session end context
                String endContext = createSessionEndContext(sessionState, endTransition);
                
                // Send enhanced session end message to context extractor
                LettaMessageRequest sessionEndRequest = LettaMessageRequest.builder()
                    .messages(Arrays.asList(
                        LettaMessage.builder()
                            .role("system")
                            .content("ENHANCED_SESSION_END: " + endContext)
                            .build()
                    ))
                    .senderId(agents.getIdentityId())
                    .build();
                    
                lettaAgentService.sendMessage(agents.getContextExtractorId(), sessionEndRequest);
                
                // Preserve context for cross-session continuity
                preserveSessionContextForContinuity(sessionState);
                
            } else {
                // Fallback to basic session end
                performBasicSessionEnd(sessionId, agents);
            }
            
            // Update legacy tracking
            legacySessionStatus.put(sessionId, "ENDED");
            
            log.info("Enhanced session {} ended successfully", sessionId);
            
        } catch (Exception e) {
            log.error("Failed to end enhanced session {}: {}", sessionId, e.getMessage(), e);
            throw new RuntimeException("Enhanced session end failed", e);
        }
    }
    
    /**
     * Pause session with state preservation (Sub-Plan 5)
     */
    public SessionTransition pauseSession(String sessionId, String reason) {
        log.info("Pausing session {} with enhanced state preservation", sessionId);
        
        try {
            SessionTransition transition = sessionStateManagementService.pauseSession(sessionId, reason);
            legacySessionStatus.put(sessionId, "PAUSED");
            
            log.info("Session {} paused successfully", sessionId);
            return transition;
            
        } catch (Exception e) {
            log.error("Failed to pause session {}: {}", sessionId, e.getMessage(), e);
            throw new RuntimeException("Session pause failed", e);
        }
    }
    
    /**
     * Resume session with context restoration (Sub-Plan 5)
     */
    public SessionTransition resumeSession(String sessionId, UserAgentMapping agents) {
        log.info("Resuming session {} with enhanced context restoration", sessionId);
        
        try {
            SessionTransition transition = sessionStateManagementService.resumeSession(sessionId, agents);
            legacySessionStatus.put(sessionId, "ACTIVE");
            
            // Send resumption message to agents
            sendSessionResumptionMessage(sessionId, agents, transition);
            
            log.info("Session {} resumed successfully", sessionId);
            return transition;
            
        } catch (Exception e) {
            log.error("Failed to resume session {}: {}", sessionId, e.getMessage(), e);
            throw new RuntimeException("Session resume failed", e);
        }
    }
    
    /**
     * Update session state based on conversation activity (Sub-Plan 5)
     */
    public void updateSessionWithTurn(String sessionId, ConversationTurn turn) {
        try {
            SessionState updatedState = sessionStateManagementService.updateSessionState(sessionId, turn);
            
            // Check for session boundary conditions
            if (sessionStateManagementService.sessionRequiresAttention(sessionId)) {
                log.warn("Session {} requires attention: {}", sessionId, updatedState.getSessionSummary());
            }
            
            // Check for automatic state transitions
            checkAutoTransitions(sessionId, updatedState);
            
        } catch (Exception e) {
            log.error("Failed to update session state for session {}: {}", sessionId, e.getMessage(), e);
        }
    }
    
    /**
     * Get enhanced session state (Sub-Plan 5)
     */
    public SessionState getEnhancedSessionState(String sessionId) {
        return sessionStateManagementService.getSessionState(sessionId);
    }
    
    /**
     * Get session transition history (Sub-Plan 5)
     */
    public List<SessionTransition> getSessionTransitionHistory(String sessionId) {
        return sessionStateManagementService.getSessionTransitions(sessionId);
    }
    
    /**
     * Get cross-session continuity for user (Sub-Plan 5)
     */
    public CrossSessionContinuity getUserContinuityData(String userId) {
        return sessionStateManagementService.getCrossSessionContinuity(userId);
    }
    
    /**
     * Enable conversation resumption with continuity prompt (Sub-Plan 5)
     */
    public String generateResumptionWelcome(String userId) {
        CrossSessionContinuity continuity = sessionStateManagementService.getCrossSessionContinuity(userId);
        
        if (continuity != null && continuity.canResumeSeamlessly()) {
            return continuity.generateResumptionPrompt();
        }
        
        return "Welcome back! How can I help you today?";
    }
    
    /**
     * Check if user has established interaction patterns (Sub-Plan 5)
     */
    public boolean hasEstablishedPatterns(String userId) {
        CrossSessionContinuity continuity = sessionStateManagementService.getCrossSessionContinuity(userId);
        return continuity != null && continuity.hasEstablishedPatterns();
    }
    
    // Legacy methods for backward compatibility
    
    private void loadHistoricalContext(UserAgentMapping agents, String userId, String sessionId) {
        log.debug("Loading historical context for user {} session {} with enhanced continuity", userId, sessionId);
        
        // Enhanced historical context loading with cross-session continuity
        CrossSessionContinuity continuity = sessionStateManagementService.getCrossSessionContinuity(userId);
        
        if (continuity != null && continuity.hasContinuity()) {
            String contextQuery = createEnhancedContextQuery(userId, continuity);
            
            LettaMessageRequest contextRequest = LettaMessageRequest.builder()
                .messages(Arrays.asList(
                    LettaMessage.builder()
                        .role("system")
                        .content("ENHANCED_HISTORICAL_CONTEXT: " + contextQuery)
                        .build()
                ))
                .senderId(agents.getIdentityId())
                .build();
                
            try {
                lettaAgentService.sendMessage(agents.getGeneralHealthId(), contextRequest);
                lettaAgentService.sendMessage(agents.getMentalHealthId(), contextRequest);
                log.debug("Enhanced historical context loaded for user {}", userId);
            } catch (Exception e) {
                log.warn("Failed to load enhanced historical context for user {}: {}", userId, e.getMessage());
            }
        } else {
            // Fallback to basic historical context loading
            performBasicHistoricalContextLoad(agents, userId, sessionId);
        }
    }
    
    private void archiveSessionContext(UserAgentMapping agents, String sessionId) {
        log.debug("Archiving session context for session {} with enhanced archival", sessionId);
        
        SessionState sessionState = sessionStateManagementService.getSessionState(sessionId);
        
        if (sessionState != null) {
            // Enhanced session archival with comprehensive data
            String enhancedArchivalData = createEnhancedArchivalData(sessionState);
            
            LettaMessageRequest archiveRequest = LettaMessageRequest.builder()
                .messages(Arrays.asList(
                    LettaMessage.builder()
                        .role("system")
                        .content("ENHANCED_SESSION_ARCHIVE: " + enhancedArchivalData)
                        .build()
                ))
                .senderId(agents.getIdentityId())
                .build();
                
            try {
                lettaAgentService.sendMessage(agents.getGeneralHealthId(), archiveRequest);
                lettaAgentService.sendMessage(agents.getMentalHealthId(), archiveRequest);
                log.debug("Enhanced session context archived for session {}", sessionId);
            } catch (Exception e) {
                log.warn("Failed to archive enhanced session context for session {}: {}", sessionId, e.getMessage());
            }
        } else {
            // Fallback to basic archival
            performBasicSessionArchival(agents, sessionId);
        }
    }
    
    // Private helper methods
    
    private String createLegacySessionContext(String sessionId, String userId, SessionState sessionState) {
        return String.format(
            "Session ID: %s\nUser ID: %s\nSession Start: %s\nStatus: %s\nState Management: Enhanced\n" +
            "Conversation Phase: %s\nComplexity Score: %.2f",
            sessionId, userId, sessionState.getCreatedAt(), sessionState.getStatus(),
            sessionState.getConversationPhase(), sessionState.getComplexityScore()
        );
    }
    
    private String createSessionEndContext(SessionState sessionState, SessionTransition endTransition) {
        return String.format(
            "Session End Summary:\nSession ID: %s\nDuration: %d minutes\nTotal Turns: %d\n" +
            "Primary Topics: %s\nFinal Phase: %s\nResolution Achieved: %s\nQuality Score: %.2f\n" +
            "Recommended Follow-ups: %s",
            sessionState.getSessionId(), sessionState.getDurationMinutes(), sessionState.getTotalTurns(),
            String.join(", ", sessionState.getPrimaryTopics()), sessionState.getConversationPhase(),
            sessionState.getClosureInfo() != null ? sessionState.getClosureInfo().getResolutionAchieved() : false,
            sessionState.getSessionQuality() != null ? sessionState.getSessionQuality().getOverallQuality() : 0.0,
            sessionState.getClosureInfo() != null ? 
                String.join("; ", sessionState.getClosureInfo().getRecommendedFollowUps()) : "None"
        );
    }
    
    private void applyContinuityToSession(UserAgentMapping agents, CrossSessionContinuity continuity) {
        if (continuity.hasRelevantPreservedContext()) {
            String continuityMessage = String.format(
                "Cross-Session Continuity Applied:\nPrevious Sessions: %d\nLast Topic: %s\n" +
                "Preserved Context: %s\nUser Patterns: %s",
                continuity.getRelatedSessions() != null ? continuity.getRelatedSessions().size() : 0,
                continuity.getPreservedContext() != null ? continuity.getPreservedContext().getLastTopicDiscussed() : "None",
                continuity.hasRelevantPreservedContext() ? "Available" : "None",
                continuity.hasEstablishedPatterns() ? "Detected" : "None"
            );
            
            LettaMessageRequest continuityRequest = LettaMessageRequest.builder()
                .messages(Arrays.asList(
                    LettaMessage.builder()
                        .role("system")
                        .content("CROSS_SESSION_CONTINUITY: " + continuityMessage)
                        .build()
                ))
                .senderId(agents.getIdentityId())
                .build();
                
            try {
                lettaAgentService.sendMessage(agents.getContextExtractorId(), continuityRequest);
            } catch (Exception e) {
                log.warn("Failed to apply continuity to session: {}", e.getMessage());
            }
        }
    }
    
    private void preserveSessionContextForContinuity(SessionState sessionState) {
        if (sessionState.getTotalTurns() >= 3) { // Only preserve meaningful sessions
            CrossSessionContinuity.PreservedContext preserved = CrossSessionContinuity.PreservedContext.builder()
                .lastTopicDiscussed(sessionState.getCurrentTopic())
                .preservationMethod("SESSION_SUMMARY")
                .preservationTimestamp(LocalDateTime.now())
                .contextRelevance(0.8) // High relevance for recent sessions
                .build();
            
            // This would be integrated with the continuity data in a full implementation
            log.debug("Preserved session context for continuity: {}", sessionState.getSessionId());
        }
    }
    
    private void sendSessionResumptionMessage(String sessionId, UserAgentMapping agents, SessionTransition transition) {
        String resumptionMessage = String.format(
            "Session Resumed: %s\nPrevious Duration: %s\nContext Restored: %s",
            sessionId, 
            transition.getPreviousStateDurationMinutes() != null ? 
                transition.getPreviousStateDurationMinutes() + " minutes" : "Unknown",
            transition.getPreservedContext() != null ? "Yes" : "No"
        );
        
        LettaMessageRequest request = LettaMessageRequest.builder()
            .messages(Arrays.asList(
                LettaMessage.builder()
                    .role("system")
                    .content("SESSION_RESUMPTION: " + resumptionMessage)
                    .build()
            ))
            .senderId(agents.getIdentityId())
            .build();
            
        try {
            lettaAgentService.sendMessage(agents.getContextExtractorId(), request);
        } catch (Exception e) {
            log.warn("Failed to send session resumption message: {}", e.getMessage());
        }
    }
    
    private void checkAutoTransitions(String sessionId, SessionState sessionState) {
        // Check for long-running session
        if (sessionState.isLongRunning() && !"PAUSED".equals(sessionState.getStatus())) {
            log.info("Session {} is long-running ({} minutes), consider pause suggestion", 
                sessionId, sessionState.getDurationMinutes());
        }
        
        // Check for quality issues
        if (sessionState.requiresAttention()) {
            log.warn("Session {} requires attention: {}", sessionId, sessionState.getSessionSummary());
        }
    }
    
    private String createEnhancedContextQuery(String userId, CrossSessionContinuity continuity) {
        StringBuilder query = new StringBuilder();
        query.append("Enhanced Context Query for User ").append(userId).append(":\n");
        
        if (continuity.hasContinuity()) {
            query.append("Previous Sessions: ").append(continuity.getRelatedSessions().size()).append("\n");
            
            CrossSessionContinuity.SessionSummary recent = continuity.getMostRecentSession();
            if (recent != null) {
                query.append("Recent Topic: ").append(recent.getPrimaryTopic()).append("\n");
                query.append("Recent Quality: ").append(recent.getSessionQuality()).append("\n");
            }
        }
        
        if (continuity.hasEstablishedPatterns()) {
            query.append("User Patterns: Established\n");
        }
        
        return query.toString();
    }
    
    private String createEnhancedArchivalData(SessionState sessionState) {
        return String.format(
            "Enhanced Session Archive:\nSession: %s\nUser: %s\nDuration: %d minutes\n" +
            "Turns: %d\nTopics: %s\nComplexity: %.2f\nQuality: %.2f\nResolution: %s",
            sessionState.getSessionId(), sessionState.getUserId(), sessionState.getDurationMinutes(),
            sessionState.getTotalTurns(), String.join(", ", sessionState.getPrimaryTopics()),
            sessionState.getComplexityScore(),
            sessionState.getSessionQuality() != null ? sessionState.getSessionQuality().getOverallQuality() : 0.0,
            sessionState.getClosureInfo() != null ? sessionState.getClosureInfo().getResolutionAchieved() : false
        );
    }
    
    // Fallback methods for backward compatibility
    
    private void performBasicSessionEnd(String sessionId, UserAgentMapping agents) {
        String clearedContext = String.format(
            "Previous Session: %s\nSession End: %s\nStatus: ENDED", 
            sessionId, LocalDateTime.now()
        );
        
        LettaMessageRequest sessionEndRequest = LettaMessageRequest.builder()
            .messages(Arrays.asList(
                LettaMessage.builder()
                    .role("system")
                    .content("Ending session: " + clearedContext)
                    .build()
            ))
            .senderId(agents.getIdentityId())
            .build();
            
        try {
            lettaAgentService.sendMessage(agents.getContextExtractorId(), sessionEndRequest);
        } catch (Exception e) {
            log.warn("Failed to send basic session end message: {}", e.getMessage());
        }
    }
    
    private void performBasicHistoricalContextLoad(UserAgentMapping agents, String userId, String sessionId) {
        String contextQuery = String.format("User %s recent health conversations", userId);
        
        LettaMessageRequest contextRequest = LettaMessageRequest.builder()
            .messages(Arrays.asList(
                LettaMessage.builder()
                    .role("system")
                    .content("Loading historical context for session: " + sessionId + ". Query: " + contextQuery)
                    .build()
            ))
            .senderId(agents.getIdentityId())
            .build();
            
        try {
            lettaAgentService.sendMessage(agents.getGeneralHealthId(), contextRequest);
            lettaAgentService.sendMessage(agents.getMentalHealthId(), contextRequest);
        } catch (Exception e) {
            log.warn("Failed to load basic historical context: {}", e.getMessage());
        }
    }
    
    private void performBasicSessionArchival(UserAgentMapping agents, String sessionId) {
        String sessionSummary = String.format("Archiving session %s context", sessionId);
        
        LettaMessageRequest archiveRequest = LettaMessageRequest.builder()
            .messages(Arrays.asList(
                LettaMessage.builder()
                    .role("system")
                    .content("Archive session context: " + sessionSummary)
                    .build()
            ))
            .senderId(agents.getIdentityId())
            .build();
            
        try {
            lettaAgentService.sendMessage(agents.getGeneralHealthId(), archiveRequest);
            lettaAgentService.sendMessage(agents.getMentalHealthId(), archiveRequest);
        } catch (Exception e) {
            log.warn("Failed to perform basic session archival: {}", e.getMessage());
        }
    }
} 