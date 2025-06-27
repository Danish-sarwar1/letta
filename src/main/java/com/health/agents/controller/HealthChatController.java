package com.health.agents.controller;

import com.health.agents.model.UserAgentMapping;
import com.health.agents.model.dto.ChatRequest;
import com.health.agents.model.dto.ChatResponse;
import com.health.agents.model.dto.EndChatRequest;
import com.health.agents.model.dto.StartChatRequest;
import com.health.agents.integration.letta.model.LettaIdentityResponse;
import com.health.agents.service.AgentOrchestrationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/health-chat")
@Validated
@Slf4j
public class HealthChatController {
    
    @Autowired
    private AgentOrchestrationService orchestrationService;
    
    /**
     * Start a new health consultation session
     */
    @PostMapping("/start")
    public ResponseEntity<ChatResponse> startChat(@Valid @RequestBody StartChatRequest request) {
        log.info("Starting chat session for user: {} with session: {}", 
                request.getUserId(), request.getSessionId());
        
        try {
            ChatResponse response = orchestrationService.startChat(
                request.getUserId(), 
                request.getSessionId()
            );
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Failed to start chat for user {}: {}", request.getUserId(), e.getMessage(), e);
            
            ChatResponse errorResponse = ChatResponse.builder()
                .message("Sorry, there was an error starting your health consultation session. Please try again.")
                .sessionId(request.getSessionId())
                .userId(request.getUserId())
                .sessionActive(false)
                .timestamp(LocalDateTime.now())
                .build();
                
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
    
    /**
     * Send a message in an active health consultation session
     */
    @PostMapping("/message")
    public ResponseEntity<ChatResponse> sendMessage(@Valid @RequestBody ChatRequest request) {
        log.info("Processing message for user: {} session: {}", 
                request.getUserId(), request.getSessionId());
        
        try {
            ChatResponse response = orchestrationService.processMessage(
                request.getUserId(), 
                request.getSessionId(), 
                request.getMessage()
            );
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Failed to process message for user {}: {}", request.getUserId(), e.getMessage(), e);
            
            ChatResponse errorResponse = ChatResponse.builder()
                .message("Sorry, I encountered an error processing your message. Please try again.")
                .sessionId(request.getSessionId())
                .userId(request.getUserId())
                .sessionActive(true)
                .timestamp(LocalDateTime.now())
                .build();
                
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
    
    /**
     * End an active health consultation session
     */
    @PostMapping("/end")
    public ResponseEntity<ChatResponse> endChat(@Valid @RequestBody EndChatRequest request) {
        log.info("Ending chat session for user: {} session: {}", 
                request.getUserId(), request.getSessionId());
        
        try {
            orchestrationService.endChat(request.getUserId(), request.getSessionId());
            
            ChatResponse response = ChatResponse.builder()
                .message("Your health consultation session has been ended successfully. Take care!")
                .sessionId(request.getSessionId())
                .userId(request.getUserId())
                .sessionActive(false)
                .timestamp(LocalDateTime.now())
                .build();
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Failed to end chat for user {}: {}", request.getUserId(), e.getMessage(), e);
            
            ChatResponse errorResponse = ChatResponse.builder()
                .message("There was an error ending your session, but it has been terminated.")
                .sessionId(request.getSessionId())
                .userId(request.getUserId())
                .sessionActive(false)
                .timestamp(LocalDateTime.now())
                .build();
                
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
    
    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Health Chat Service is running");
    }

    @GetMapping("/test-letta")
    public ResponseEntity<?> testLetta() {
        try {
            // Test basic Letta connectivity
            List<LettaIdentityResponse> identities = orchestrationService.testLettaConnection();
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "identities_count", identities.size(),
                "first_identity", identities.isEmpty() ? null : identities.get(0)
            ));
        } catch (Exception e) {
            log.error("Letta test failed", e);
            return ResponseEntity.ok(Map.of(
                "status", "error",
                "error", e.getMessage(),
                "error_class", e.getClass().getSimpleName()
            ));
        }
    }

    @GetMapping("/prompt-stats")
    public ResponseEntity<?> getPromptStats() {
        try {
            Map<String, Object> stats = orchestrationService.getPromptStatistics();
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "prompt_statistics", stats,
                "all_prompts_loaded", orchestrationService.areAllPromptsLoaded()
            ));
        } catch (Exception e) {
            log.error("Failed to get prompt statistics", e);
            return ResponseEntity.ok(Map.of(
                "status", "error",
                "error", e.getMessage(),
                "error_class", e.getClass().getSimpleName()
            ));
        }
    }

    @PostMapping("/debug/create-agent/{userId}")
    public ResponseEntity<?> debugCreateAgent(@PathVariable String userId) {
        try {
            UserAgentMapping result = orchestrationService.debugCreateAgent(userId);
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "user_agents", Map.of(
                    "userId", result.getUserId(),
                    "identityId", result.getIdentityId(),
                    "contextExtractorId", result.getContextExtractorId(),
                    "intentExtractorId", result.getIntentExtractorId(),
                    "generalHealthId", result.getGeneralHealthId(),
                    "mentalHealthId", result.getMentalHealthId()
                )
            ));
        } catch (Exception e) {
            log.error("Failed to create debug agent for user {}", userId, e);
            return ResponseEntity.ok(Map.of(
                "status", "error",
                "error", e.getMessage(),
                "error_class", e.getClass().getSimpleName()
            ));
        }
    }
} 