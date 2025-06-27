package com.health.agents.service;

import com.health.agents.integration.letta.model.LettaMessage;
import com.health.agents.integration.letta.model.LettaMessageRequest;
import com.health.agents.integration.letta.service.LettaAgentService;
import com.health.agents.model.UserAgentMapping;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;

@Service
@Slf4j
public class SessionManagementService {
    
    @Autowired
    private LettaAgentService lettaAgentService;
    
    public void startSession(String userId, String sessionId, UserAgentMapping agents) {
        log.info("Starting session {} for user {}", sessionId, userId);
        
        // Update context coordinator with new session
        String sessionContext = String.format(
            "Session ID: %s\nUser ID: %s\nSession Start: %s\nStatus: ACTIVE", 
            sessionId, userId, LocalDateTime.now()
        );
        
        // Send session start message to context coordinator
        LettaMessageRequest sessionStartRequest = LettaMessageRequest.builder()
            .messages(Arrays.asList(
                LettaMessage.builder()
                    .role("system")
                    .content("Starting new session: " + sessionContext)
                    .build()
            ))
            .senderId(agents.getIdentityId())
            .build();
            
        lettaAgentService.sendMessage(agents.getContextCoordinatorId(), sessionStartRequest);
        
        // Load relevant historical context from archival memory
        loadHistoricalContext(agents, userId, sessionId);
    }
    
    public void endSession(String userId, String sessionId, UserAgentMapping agents) {
        log.info("Ending session {} for user {}", sessionId, userId);
        
        // Archive current session context
        archiveSessionContext(agents, sessionId);
        
        // Clear session context
        String clearedContext = String.format(
            "Previous Session: %s\nSession End: %s\nStatus: ENDED", 
            sessionId, LocalDateTime.now()
        );
        
        // Send session end message to context coordinator
        LettaMessageRequest sessionEndRequest = LettaMessageRequest.builder()
            .messages(Arrays.asList(
                LettaMessage.builder()
                    .role("system")
                    .content("Ending session: " + clearedContext)
                    .build()
            ))
            .senderId(agents.getIdentityId())
            .build();
            
        lettaAgentService.sendMessage(agents.getContextCoordinatorId(), sessionEndRequest);
    }
    
    private void loadHistoricalContext(UserAgentMapping agents, String userId, String sessionId) {
        log.debug("Loading historical context for user {} session {}", userId, sessionId);
        
        // Query archival memory for relevant historical context
        // This leverages Letta's automatic archival memory system
        String contextQuery = String.format("User %s recent health conversations", userId);
        
        // Send context loading message to health agents
        LettaMessageRequest contextRequest = LettaMessageRequest.builder()
            .messages(Arrays.asList(
                LettaMessage.builder()
                    .role("system")
                    .content("Loading historical context for session: " + sessionId + ". Query: " + contextQuery)
                    .build()
            ))
            .senderId(agents.getIdentityId())
            .build();
            
        // Both health agents load their respective historical contexts
        try {
            lettaAgentService.sendMessage(agents.getGeneralHealthId(), contextRequest);
            lettaAgentService.sendMessage(agents.getMentalHealthId(), contextRequest);
            log.debug("Historical context loaded for user {}", userId);
        } catch (Exception e) {
            log.warn("Failed to load historical context for user {}: {}", userId, e.getMessage());
        }
    }
    
    private void archiveSessionContext(UserAgentMapping agents, String sessionId) {
        log.debug("Archiving session context for session {}", sessionId);
        
        // Get current session context from context coordinator
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
            
        // Send to health agents to archive relevant conversation context
        try {
            lettaAgentService.sendMessage(agents.getGeneralHealthId(), archiveRequest);
            lettaAgentService.sendMessage(agents.getMentalHealthId(), archiveRequest);
            log.debug("Session context archived for session {}", sessionId);
        } catch (Exception e) {
            log.warn("Failed to archive session context for session {}: {}", sessionId, e.getMessage());
        }
    }
} 