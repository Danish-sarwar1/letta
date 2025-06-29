package com.health.agents.service;

import com.health.agents.config.MemoryArchitectureConfig;
import com.health.agents.model.dto.SessionState;
import com.health.agents.model.dto.SessionTransition;
import com.health.agents.model.dto.CrossSessionContinuity;
import com.health.agents.model.dto.ConversationHistory;
import com.health.agents.model.dto.ConversationTurn;
import com.health.agents.model.UserAgentMapping;
import com.health.agents.integration.letta.model.LettaMessage;
import com.health.agents.integration.letta.model.LettaMessageRequest;
import com.health.agents.integration.letta.service.LettaAgentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Session State Management Service for Sub-Plan 5: Session and State Management
 * Handles sophisticated session state tracking, boundary management, and cross-session continuity
 */
@Service
@Slf4j
public class SessionStateManagementService {
    
    @Autowired
    private MemoryArchitectureConfig memoryConfig;
    
    @Autowired
    private LettaAgentService lettaAgentService;
    
    @Autowired
    private ConversationTurnService conversationTurnService;
    
    @Autowired
    private MemoryManagementService memoryManagementService;
    
    // Session state tracking (in production, this would be persisted)
    private final ConcurrentHashMap<String, SessionState> activeSessions = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, List<SessionTransition>> sessionTransitions = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, CrossSessionContinuity> userContinuityData = new ConcurrentHashMap<>();
    
    /**
     * Initialize a new session with comprehensive state tracking
     */
    public SessionState initializeSession(String sessionId, String userId, UserAgentMapping agents) {
        log.info("Initializing session {} for user {} with enhanced state management", sessionId, userId);
        
        try {
            LocalDateTime sessionStart = LocalDateTime.now();
            
            // Create comprehensive session state
            SessionState sessionState = SessionState.builder()
                .sessionId(sessionId)
                .userId(userId)
                .status("INITIALIZING")
                .createdAt(sessionStart)
                .lastActivity(sessionStart)
                .durationMinutes(0L)
                .totalTurns(0)
                .currentTopic("Initial Setup")
                .primaryTopics(new ArrayList<>())
                .conversationPhase("Session Initialization")
                .complexityScore(0.1)
                .requiresFollowUp(false)
                .sessionQuality(SessionState.SessionQuality.builder()
                    .overallQuality(0.8)
                    .contextContinuity(0.5)
                    .agentPerformance(0.8)
                    .userSatisfactionIndicator(0.5)
                    .qualityNotes("Session initialized")
                    .build())
                .memoryUsage(SessionState.MemoryUsageStats.builder()
                    .conversationHistorySize(0)
                    .activeSessionSize(0)
                    .contextSummarySize(0)
                    .memoryEfficiency(1.0)
                    .rotationTriggered(false)
                    .archivalTriggerTurns(memoryConfig.getArchival().getTriggerTurns())
                    .build())
                .userEngagement(SessionState.UserEngagement.builder()
                    .responseRate(0.0)
                    .averageResponseLength(0.0)
                    .questionsAsked(0)
                    .activeParticipation(false)
                    .engagementLevel("UNKNOWN")
                    .build())
                .sessionFlags(new HashMap<>())
                .agentInteractions(new ArrayList<>())
                .build();
            
            // Store session state
            activeSessions.put(sessionId, sessionState);
            
            // Create initialization transition
            SessionTransition initTransition = createSessionTransition(
                sessionId, userId, null, "INITIALIZING", "INITIALIZATION",
                "Session started by user", 0, "USER", true
            );
            
            recordSessionTransition(sessionId, initTransition);
            
            // Load cross-session continuity data
            CrossSessionContinuity continuity = loadCrossSessionContinuity(userId, sessionId);
            
            // Apply continuity if available
            if (continuity.hasContinuity()) {
                applyCrossSessionContinuity(sessionState, continuity, agents);
            }
            
            // Transition to ACTIVE state
            transitionSessionState(sessionId, "ACTIVE", "Session initialization completed", "SYSTEM");
            
            log.info("Session {} initialized successfully with enhanced state tracking", sessionId);
            return sessionState;
            
        } catch (Exception e) {
            log.error("Failed to initialize session {} for user {}: {}", sessionId, userId, e.getMessage(), e);
            throw new RuntimeException("Session initialization failed", e);
        }
    }
    
    /**
     * Update session state based on conversation activity
     */
    public SessionState updateSessionState(String sessionId, ConversationTurn turn) {
        log.debug("Updating session state for session {} with turn {}", sessionId, turn.getTurnNumber());
        
        try {
            SessionState sessionState = activeSessions.get(sessionId);
            if (sessionState == null) {
                log.warn("Session state not found for session {}, creating new state", sessionId);
                // This shouldn't happen in normal flow, but handle gracefully
                return initializeSession(sessionId, turn.getSessionId(), null);
            }
            
            // Update basic session metrics
            sessionState.setLastActivity(LocalDateTime.now());
            sessionState.setDurationMinutes(
                ChronoUnit.MINUTES.between(sessionState.getCreatedAt(), LocalDateTime.now()));
            sessionState.setTotalTurns(sessionState.getTotalTurns() + 1);
            
            // Update conversation phase
            updateConversationPhase(sessionState);
            
            // Update topic tracking
            updateTopicTracking(sessionState, turn);
            
            // Update complexity score
            updateComplexityScore(sessionState, turn);
            
            // Update user engagement metrics
            updateUserEngagement(sessionState, turn);
            
            // Update memory usage statistics
            updateMemoryUsageStats(sessionState);
            
            // Update agent interaction history
            updateAgentInteractions(sessionState, turn);
            
            // Update session quality assessment
            updateSessionQuality(sessionState, turn);
            
            // Check for session boundary conditions
            checkSessionBoundaryConditions(sessionState);
            
            log.debug("Session state updated for session {}: {} turns, {} phase", 
                sessionId, sessionState.getTotalTurns(), sessionState.getConversationPhase());
            
            return sessionState;
            
        } catch (Exception e) {
            log.error("Failed to update session state for session {}: {}", sessionId, e.getMessage(), e);
            throw new RuntimeException("Session state update failed", e);
        }
    }
    
    /**
     * Transition session to a new state
     */
    public SessionTransition transitionSessionState(String sessionId, String newStatus, 
                                                   String reason, String triggerSource) {
        log.debug("Transitioning session {} to status {}, reason: {}", sessionId, newStatus, reason);
        
        try {
            SessionState sessionState = activeSessions.get(sessionId);
            if (sessionState == null) {
                throw new IllegalStateException("Session state not found: " + sessionId);
            }
            
            String oldStatus = sessionState.getStatus();
            long previousDuration = sessionState.getDurationMinutes();
            
            // Create transition record
            SessionTransition transition = createSessionTransition(
                sessionId, sessionState.getUserId(), oldStatus, newStatus,
                determineTransitionType(oldStatus, newStatus), reason,
                sessionState.getTotalTurns(), triggerSource, true
            );
            
            // Update session state
            sessionState.setStatus(newStatus);
            
            // Handle specific transition types
            handleSpecificTransitions(sessionState, transition, newStatus);
            
            // Record transition
            recordSessionTransition(sessionId, transition);
            
            log.info("Session {} transitioned from {} to {} successfully", sessionId, oldStatus, newStatus);
            return transition;
            
        } catch (Exception e) {
            log.error("Failed to transition session {} to {}: {}", sessionId, newStatus, e.getMessage(), e);
            throw new RuntimeException("Session transition failed", e);
        }
    }
    
    /**
     * Pause session with state preservation
     */
    public SessionTransition pauseSession(String sessionId, String reason) {
        log.info("Pausing session {} with state preservation", sessionId);
        
        try {
            SessionState sessionState = activeSessions.get(sessionId);
            if (sessionState == null) {
                throw new IllegalStateException("Session not found: " + sessionId);
            }
            
            // Preserve session context
            String preservedContext = preserveSessionContext(sessionState);
            
            // Create pause transition with context preservation
            SessionTransition transition = transitionSessionState(sessionId, "PAUSED", reason, "USER");
            transition.setPreservedContext(preservedContext);
            
            // Update transition with memory operations
            SessionTransition.MemoryOperations memoryOps = SessionTransition.MemoryOperations.builder()
                .memoryPreserved(true)
                .memoryOperationDetails("Session context preserved for resumption")
                .build();
            transition.setMemoryOperations(memoryOps);
            
            log.info("Session {} paused successfully with context preservation", sessionId);
            return transition;
            
        } catch (Exception e) {
            log.error("Failed to pause session {}: {}", sessionId, e.getMessage(), e);
            throw new RuntimeException("Session pause failed", e);
        }
    }
    
    /**
     * Resume session with context restoration
     */
    public SessionTransition resumeSession(String sessionId, UserAgentMapping agents) {
        log.info("Resuming session {} with context restoration", sessionId);
        
        try {
            SessionState sessionState = activeSessions.get(sessionId);
            if (sessionState == null) {
                throw new IllegalStateException("Session not found: " + sessionId);
            }
            
            if (!"PAUSED".equals(sessionState.getStatus())) {
                throw new IllegalStateException("Session is not in PAUSED state: " + sessionState.getStatus());
            }
            
            // Restore session context
            restoreSessionContext(sessionState, agents);
            
            // Create resume transition
            SessionTransition transition = transitionSessionState(sessionId, "ACTIVE", 
                "Session resumed by user", "USER");
            
            // Update transition with memory operations
            SessionTransition.MemoryOperations memoryOps = SessionTransition.MemoryOperations.builder()
                .memoryPreserved(false)
                .memoryOperationDetails("Session context restored from preservation")
                .build();
            transition.setMemoryOperations(memoryOps);
            
            log.info("Session {} resumed successfully", sessionId);
            return transition;
            
        } catch (Exception e) {
            log.error("Failed to resume session {}: {}", sessionId, e.getMessage(), e);
            throw new RuntimeException("Session resume failed", e);
        }
    }
    
    /**
     * End session with proper cleanup and archival
     */
    public SessionTransition endSession(String sessionId, String reason, UserAgentMapping agents) {
        log.info("Ending session {} with cleanup and archival", sessionId);
        
        try {
            SessionState sessionState = activeSessions.get(sessionId);
            if (sessionState == null) {
                log.warn("Session state not found for ending session: {}", sessionId);
                return null;
            }
            
            // Create session closure information
            SessionState.SessionClosure closure = SessionState.SessionClosure.builder()
                .closureReason(reason)
                .properClosure(true)
                .finalTopic(sessionState.getCurrentTopic())
                .resolutionAchieved(determineResolutionAchieved(sessionState))
                .closureNotes("Session ended properly with state preservation")
                .recommendedFollowUps(generateFollowUpRecommendations(sessionState))
                .build();
            
            sessionState.setClosureInfo(closure);
            
            // Archive session data
            archiveSessionData(sessionState, agents);
            
            // Create termination transition
            SessionTransition transition = transitionSessionState(sessionId, "ENDED", reason, "USER");
            
            // Update cross-session continuity
            updateCrossSessionContinuity(sessionState);
            
            // Remove from active sessions
            activeSessions.remove(sessionId);
            
            log.info("Session {} ended successfully with proper cleanup", sessionId);
            return transition;
            
        } catch (Exception e) {
            log.error("Failed to end session {}: {}", sessionId, e.getMessage(), e);
            throw new RuntimeException("Session termination failed", e);
        }
    }
    
    /**
     * Get current session state
     */
    public SessionState getSessionState(String sessionId) {
        return activeSessions.get(sessionId);
    }
    
    /**
     * Get session transition history
     */
    public List<SessionTransition> getSessionTransitions(String sessionId) {
        return sessionTransitions.getOrDefault(sessionId, new ArrayList<>());
    }
    
    /**
     * Get cross-session continuity data for a user
     */
    public CrossSessionContinuity getCrossSessionContinuity(String userId) {
        return userContinuityData.get(userId);
    }
    
    /**
     * Check if session requires attention
     */
    public boolean sessionRequiresAttention(String sessionId) {
        SessionState state = activeSessions.get(sessionId);
        return state != null && state.requiresAttention();
    }
    
    // Private helper methods
    
    private SessionTransition createSessionTransition(String sessionId, String userId, 
                                                    String fromStatus, String toStatus, String transitionType,
                                                    String reason, int turnNumber, String triggerSource, 
                                                    boolean success) {
        return SessionTransition.builder()
            .sessionId(sessionId)
            .userId(userId)
            .fromStatus(fromStatus)
            .toStatus(toStatus)
            .transitionTime(LocalDateTime.now())
            .transitionType(transitionType)
            .transitionReason(reason)
            .turnNumber(turnNumber)
            .triggerSource(triggerSource)
            .automaticTransition("SYSTEM".equals(triggerSource))
            .transitionSuccess(success)
            .transitionMetadata(new HashMap<>())
            .build();
    }
    
    private void recordSessionTransition(String sessionId, SessionTransition transition) {
        sessionTransitions.computeIfAbsent(sessionId, k -> new ArrayList<>()).add(transition);
        log.debug("Recorded transition for session {}: {}", sessionId, transition.getTransitionSummary());
    }
    
    private String determineTransitionType(String fromStatus, String toStatus) {
        if (fromStatus == null && "INITIALIZING".equals(toStatus)) {
            return "INITIALIZATION";
        } else if ("INITIALIZING".equals(fromStatus) && "ACTIVE".equals(toStatus)) {
            return "ACTIVATION";
        } else if ("ACTIVE".equals(fromStatus) && "PAUSED".equals(toStatus)) {
            return "PAUSE";
        } else if ("PAUSED".equals(fromStatus) && "ACTIVE".equals(toStatus)) {
            return "RESUME";
        } else if ("ACTIVE".equals(fromStatus) && "ENDED".equals(toStatus)) {
            return "TERMINATION";
        } else if ("ENDED".equals(fromStatus) && "ARCHIVED".equals(toStatus)) {
            return "ARCHIVAL";
        } else {
            return "STATE_CHANGE";
        }
    }
    
    private CrossSessionContinuity loadCrossSessionContinuity(String userId, String sessionId) {
        CrossSessionContinuity existing = userContinuityData.get(userId);
        if (existing != null) {
            existing.setCurrentSessionId(sessionId);
            return existing;
        }
        
        // Create new continuity data for new users
        return CrossSessionContinuity.builder()
            .currentSessionId(sessionId)
            .userId(userId)
            .relatedSessions(new ArrayList<>())
            .analytics(CrossSessionContinuity.CrossSessionAnalytics.builder()
                .totalSessions(0)
                .firstSessionDate(LocalDateTime.now())
                .averageSessionQuality(0.5)
                .resolutionsAchieved(0)
                .commonIssues(new ArrayList<>())
                .build())
            .build();
    }
    
    private void applyCrossSessionContinuity(SessionState sessionState, CrossSessionContinuity continuity, 
                                           UserAgentMapping agents) {
        log.debug("Applying cross-session continuity for session {}", sessionState.getSessionId());
        
        if (continuity.hasRelevantPreservedContext()) {
            CrossSessionContinuity.PreservedContext preserved = continuity.getPreservedContext();
            sessionState.setCurrentTopic(preserved.getLastTopicDiscussed());
            sessionState.getPrimaryTopics().add(preserved.getLastTopicDiscussed());
            
            // Add to session flags
            sessionState.getSessionFlags().put("hasPreviousContext", true);
            sessionState.getSessionFlags().put("preservedEmotionalState", preserved.getEmotionalState());
        }
        
        if (continuity.hasEstablishedPatterns()) {
            CrossSessionContinuity.HistoricalPatterns patterns = continuity.getHistoricalPatterns();
            sessionState.getSessionFlags().put("hasEstablishedPatterns", true);
            sessionState.getSessionFlags().put("preferredAgents", patterns.getPreferredAgentTypes());
        }
    }
    
    private void updateConversationPhase(SessionState sessionState) {
        int turns = sessionState.getTotalTurns();
        if (turns <= 2) {
            sessionState.setConversationPhase("Initial Assessment");
        } else if (turns <= 5) {
            sessionState.setConversationPhase("Information Gathering");
        } else if (turns <= 10) {
            sessionState.setConversationPhase("Active Discussion");
        } else if (turns <= 20) {
            sessionState.setConversationPhase("Extended Consultation");
        } else {
            sessionState.setConversationPhase("Deep Engagement");
        }
    }
    
    private void updateTopicTracking(SessionState sessionState, ConversationTurn turn) {
        if (turn.getTopicTags() != null && !turn.getTopicTags().isEmpty()) {
            sessionState.setCurrentTopic(turn.getTopicTags());
            if (!sessionState.getPrimaryTopics().contains(turn.getTopicTags())) {
                sessionState.getPrimaryTopics().add(turn.getTopicTags());
            }
        }
    }
    
    private void updateComplexityScore(SessionState sessionState, ConversationTurn turn) {
        double complexity = 0.1; // Base complexity
        
        // Add complexity based on turns
        complexity += Math.min(0.4, sessionState.getTotalTurns() * 0.02);
        
        // Add complexity based on topic diversity
        complexity += Math.min(0.3, sessionState.getPrimaryTopics().size() * 0.1);
        
        // Add complexity based on agent switches
        complexity += Math.min(0.2, sessionState.getAgentInteractions().size() * 0.05);
        
        sessionState.setComplexityScore(Math.min(1.0, complexity));
    }
    
    private void updateUserEngagement(SessionState sessionState, ConversationTurn turn) {
        SessionState.UserEngagement engagement = sessionState.getUserEngagement();
        
        if (engagement == null) {
            engagement = SessionState.UserEngagement.builder()
                .responseRate(1.0)
                .averageResponseLength(0.0)
                .questionsAsked(0)
                .activeParticipation(true)
                .engagementLevel("MEDIUM")
                .build();
            sessionState.setUserEngagement(engagement);
        }
        
        // Update metrics
        if (turn.getUserMessage() != null) {
            double currentAvg = engagement.getAverageResponseLength();
            int messageLength = turn.getUserMessage().length();
            engagement.setAverageResponseLength((currentAvg + messageLength) / 2.0);
            
            // Count questions
            if (turn.getUserMessage().contains("?")) {
                engagement.setQuestionsAsked(engagement.getQuestionsAsked() + 1);
            }
        }
        
        // Determine engagement level
        if (engagement.getAverageResponseLength() > 100 && engagement.getQuestionsAsked() > 2) {
            engagement.setEngagementLevel("HIGH");
        } else if (engagement.getAverageResponseLength() > 50) {
            engagement.setEngagementLevel("MEDIUM");
        } else {
            engagement.setEngagementLevel("LOW");
        }
    }
    
    private void updateMemoryUsageStats(SessionState sessionState) {
        ConversationHistory history = conversationTurnService.getConversationHistory(sessionState.getSessionId());
        if (history != null) {
            String historyContent = memoryManagementService.formatConversationHistory(history);
            
            SessionState.MemoryUsageStats stats = sessionState.getMemoryUsage();
            if (stats == null) {
                stats = SessionState.MemoryUsageStats.builder().build();
                sessionState.setMemoryUsage(stats);
            }
            
            stats.setConversationHistorySize(historyContent.length());
            stats.setMemoryEfficiency(calculateMemoryEfficiency(historyContent.length(), 
                memoryConfig.getConversationHistoryLimit()));
            stats.setRotationTriggered(memoryManagementService.isMemoryRotationNeeded(
                historyContent, memoryConfig.getConversationHistoryLimit()));
        }
    }
    
    private void updateAgentInteractions(SessionState sessionState, ConversationTurn turn) {
        if (turn.getRoutedAgent() != null) {
            SessionState.AgentInteraction interaction = SessionState.AgentInteraction.builder()
                .agentType(turn.getRoutedAgent())
                .interactionTime(turn.getTimestamp())
                .turnNumber(turn.getTurnNumber())
                .intentHandled(turn.getExtractedIntent())
                .responseQuality(turn.getResponseQuality())
                .handoffOccurred(false) // Will be determined by agent switching logic
                .build();
            
            sessionState.getAgentInteractions().add(interaction);
            sessionState.setLastAgentType(turn.getRoutedAgent());
        }
    }
    
    private void updateSessionQuality(SessionState sessionState, ConversationTurn turn) {
        SessionState.SessionQuality quality = sessionState.getSessionQuality();
        if (quality == null) {
            quality = SessionState.SessionQuality.builder()
                .overallQuality(0.8)
                .contextContinuity(0.7)
                .agentPerformance(0.8)
                .userSatisfactionIndicator(0.5)
                .build();
            sessionState.setSessionQuality(quality);
        }
        
        // Update based on turn quality
        if (turn.getResponseQuality() != null) {
            double currentPerformance = quality.getAgentPerformance();
            quality.setAgentPerformance((currentPerformance + turn.getResponseQuality()) / 2.0);
        }
        
        // Update overall quality
        quality.setOverallQuality((quality.getAgentPerformance() + quality.getContextContinuity()) / 2.0);
    }
    
    private void checkSessionBoundaryConditions(SessionState sessionState) {
        // Check for automatic archival conditions
        if (sessionState.getTotalTurns() >= memoryConfig.getArchival().getTriggerTurns()) {
            sessionState.getSessionFlags().put("archivalRequired", true);
        }
        
        // Check for long-running session
        if (sessionState.isLongRunning()) {
            sessionState.getSessionFlags().put("longRunning", true);
        }
        
        // Check for quality issues
        if (sessionState.getSessionQuality() != null && 
            sessionState.getSessionQuality().getOverallQuality() < 0.6) {
            sessionState.getSessionFlags().put("qualityIssues", true);
        }
    }
    
    private void handleSpecificTransitions(SessionState sessionState, SessionTransition transition, String newStatus) {
        switch (newStatus) {
            case "PAUSED":
                sessionState.getSessionFlags().put("wasPaused", true);
                break;
            case "ENDED":
                sessionState.getSessionFlags().put("properlyEnded", true);
                break;
            case "ARCHIVED":
                sessionState.setArchivalInfo(SessionState.ArchivalMetadata.builder()
                    .isArchived(true)
                    .archivalTimestamp(LocalDateTime.now())
                    .archivalReason("Session lifecycle completed")
                    .accessibleForContinuity(true)
                    .build());
                break;
        }
    }
    
    private String preserveSessionContext(SessionState sessionState) {
        // Create preserved context summary
        StringBuilder preserved = new StringBuilder();
        preserved.append("Session Context Preservation:\n");
        preserved.append("Topic: ").append(sessionState.getCurrentTopic()).append("\n");
        preserved.append("Phase: ").append(sessionState.getConversationPhase()).append("\n");
        preserved.append("Duration: ").append(sessionState.getDurationMinutes()).append(" minutes\n");
        preserved.append("Turns: ").append(sessionState.getTotalTurns()).append("\n");
        preserved.append("Last Agent: ").append(sessionState.getLastAgentType()).append("\n");
        
        return preserved.toString();
    }
    
    private void restoreSessionContext(SessionState sessionState, UserAgentMapping agents) {
        log.debug("Restoring session context for session {}", sessionState.getSessionId());
        
        // Send context restoration message to agents
        String restorationMessage = String.format(
            "Session resumed: %s\nPrevious topic: %s\nSession duration: %d minutes", 
            sessionState.getSessionId(), sessionState.getCurrentTopic(), sessionState.getDurationMinutes()
        );
        
        LettaMessageRequest request = LettaMessageRequest.builder()
            .messages(Arrays.asList(
                LettaMessage.builder()
                    .role("system")
                    .content("CONTEXT_RESTORATION: " + restorationMessage)
                    .build()
            ))
            .senderId(agents.getIdentityId())
            .build();
        
        try {
            lettaAgentService.sendMessage(agents.getContextExtractorId(), request);
        } catch (Exception e) {
            log.warn("Failed to send context restoration message: {}", e.getMessage());
        }
    }
    
    private void archiveSessionData(SessionState sessionState, UserAgentMapping agents) {
        log.debug("Archiving session data for session {}", sessionState.getSessionId());
        
        // Create archival summary
        String archivalSummary = String.format(
            "Session Archive: %s\nDuration: %d minutes\nTurns: %d\nTopics: %s\nQuality: %.2f", 
            sessionState.getSessionId(), sessionState.getDurationMinutes(), sessionState.getTotalTurns(),
            String.join(", ", sessionState.getPrimaryTopics()),
            sessionState.getSessionQuality() != null ? sessionState.getSessionQuality().getOverallQuality() : 0.0
        );
        
        // Send archival message to context extractor
        LettaMessageRequest request = LettaMessageRequest.builder()
            .messages(Arrays.asList(
                LettaMessage.builder()
                    .role("system")
                    .content("SESSION_ARCHIVE: " + archivalSummary)
                    .build()
            ))
            .senderId(agents.getIdentityId())
            .build();
        
        try {
            lettaAgentService.sendMessage(agents.getContextExtractorId(), request);
        } catch (Exception e) {
            log.warn("Failed to send session archival message: {}", e.getMessage());
        }
    }
    
    private boolean determineResolutionAchieved(SessionState sessionState) {
        // Simple heuristic - could be enhanced with ML
        return sessionState.getTotalTurns() >= 3 && 
               sessionState.getSessionQuality() != null &&
               sessionState.getSessionQuality().getOverallQuality() > 0.7;
    }
    
    private List<String> generateFollowUpRecommendations(SessionState sessionState) {
        List<String> recommendations = new ArrayList<>();
        
        if (sessionState.isHighComplexity()) {
            recommendations.add("Schedule follow-up for complex health concern");
        }
        
        if (sessionState.getTotalTurns() > 10 && !determineResolutionAchieved(sessionState)) {
            recommendations.add("Consider specialist consultation");
        }
        
        if (sessionState.getCurrentTopic() != null && sessionState.getCurrentTopic().contains("pain")) {
            recommendations.add("Monitor pain levels and symptoms");
        }
        
        return recommendations;
    }
    
    private void updateCrossSessionContinuity(SessionState sessionState) {
        String userId = sessionState.getUserId();
        CrossSessionContinuity continuity = userContinuityData.computeIfAbsent(userId, 
            k -> CrossSessionContinuity.builder()
                .userId(userId)
                .relatedSessions(new ArrayList<>())
                .analytics(CrossSessionContinuity.CrossSessionAnalytics.builder()
                    .totalSessions(0)
                    .firstSessionDate(LocalDateTime.now())
                    .build())
                .build());
        
        // Add current session to history
        CrossSessionContinuity.SessionSummary summary = CrossSessionContinuity.SessionSummary.builder()
            .sessionId(sessionState.getSessionId())
            .sessionDate(sessionState.getCreatedAt())
            .durationMinutes(sessionState.getDurationMinutes())
            .totalTurns(sessionState.getTotalTurns())
            .primaryTopic(sessionState.getCurrentTopic())
            .topicsDiscussed(sessionState.getPrimaryTopics())
            .finalOutcome(sessionState.getClosureInfo() != null ? 
                sessionState.getClosureInfo().getClosureNotes() : "Session completed")
            .resolutionAchieved(determineResolutionAchieved(sessionState))
            .lastAgentType(sessionState.getLastAgentType())
            .sessionQuality(sessionState.getSessionQuality() != null ? 
                sessionState.getSessionQuality().getOverallQuality() : 0.5)
            .archived(false)
            .build();
        
        continuity.getRelatedSessions().add(summary);
        
        // Update analytics
        CrossSessionContinuity.CrossSessionAnalytics analytics = continuity.getAnalytics();
        analytics.setTotalSessions(analytics.getTotalSessions() + 1);
        analytics.setLastSessionDate(LocalDateTime.now());
        
        userContinuityData.put(userId, continuity);
    }
    
    private double calculateMemoryEfficiency(int usedMemory, int maxMemory) {
        if (maxMemory == 0) return 1.0;
        return Math.max(0.0, 1.0 - ((double) usedMemory / maxMemory));
    }
} 