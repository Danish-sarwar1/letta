package com.health.agents.controller;

import com.health.agents.model.dto.ConversationHistory;
import com.health.agents.model.dto.ConversationTurn;
import com.health.agents.service.AgentOrchestrationService;
import com.health.agents.service.ConversationTurnService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Conversation Management API Controller for Sub-Plan 2
 * Provides endpoints for conversation turn management, memory rotation, and conversation analytics
 */
@RestController
@RequestMapping("/api/conversation")
@Slf4j
public class ConversationController {
    
    @Autowired
    private AgentOrchestrationService agentOrchestrationService;
    
    @Autowired
    private ConversationTurnService conversationTurnService;
    
    /**
     * Get complete conversation history for a session
     */
    @GetMapping("/history/{sessionId}")
    public ResponseEntity<ConversationHistory> getConversationHistory(@PathVariable String sessionId) {
        log.info("Retrieving conversation history for session: {}", sessionId);
        
        try {
            ConversationHistory history = agentOrchestrationService.getConversationHistory(sessionId);
            
            if (history == null) {
                return ResponseEntity.notFound().build();
            }
            
            return ResponseEntity.ok(history);
            
        } catch (Exception e) {
            log.error("Failed to retrieve conversation history for session {}: {}", sessionId, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Get conversation statistics for a session
     */
    @GetMapping("/stats/{sessionId}")
    public ResponseEntity<Map<String, Object>> getConversationStats(@PathVariable String sessionId) {
        log.info("Retrieving conversation statistics for session: {}", sessionId);
        
        try {
            ConversationHistory history = agentOrchestrationService.getConversationHistory(sessionId);
            
            if (history == null) {
                return ResponseEntity.notFound().build();
            }
            
            Map<String, Object> stats = new HashMap<>();
            stats.put("sessionId", sessionId);
            stats.put("userId", history.getUserId());
            stats.put("totalTurns", history.getTotalTurns());
            stats.put("status", history.getStatus());
            stats.put("createdAt", history.getCreatedAt());
            stats.put("lastUpdated", history.getLastUpdated());
            stats.put("memoryRotationNeeded", agentOrchestrationService.isMemoryRotationNeeded(sessionId));
            
            // Calculate turn types distribution
            Map<String, Long> turnTypeCounts = history.getTurns().stream()
                .collect(java.util.stream.Collectors.groupingBy(
                    ConversationTurn::getTurnType,
                    java.util.stream.Collectors.counting()
                ));
            stats.put("turnTypeDistribution", turnTypeCounts);
            
            // Calculate average response time (if available)
            // Note: This would require storing response times in turns
            
            return ResponseEntity.ok(stats);
            
        } catch (Exception e) {
            log.error("Failed to retrieve conversation stats for session {}: {}", sessionId, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Get recent conversation turns for a session
     */
    @GetMapping("/recent/{sessionId}")
    public ResponseEntity<List<ConversationTurn>> getRecentTurns(
            @PathVariable String sessionId,
            @RequestParam(defaultValue = "5") int limit) {
        log.info("Retrieving recent {} turns for session: {}", limit, sessionId);
        
        try {
            ConversationHistory history = agentOrchestrationService.getConversationHistory(sessionId);
            
            if (history == null) {
                return ResponseEntity.notFound().build();
            }
            
            List<ConversationTurn> allTurns = history.getTurns();
            int startIndex = Math.max(0, allTurns.size() - limit);
            List<ConversationTurn> recentTurns = allTurns.subList(startIndex, allTurns.size());
            
            return ResponseEntity.ok(recentTurns);
            
        } catch (Exception e) {
            log.error("Failed to retrieve recent turns for session {}: {}", sessionId, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Check if memory rotation is needed for a session
     */
    @GetMapping("/memory-status/{sessionId}")
    public ResponseEntity<Map<String, Object>> getMemoryStatus(@PathVariable String sessionId) {
        log.info("Checking memory status for session: {}", sessionId);
        
        try {
            ConversationHistory history = agentOrchestrationService.getConversationHistory(sessionId);
            
            if (history == null) {
                return ResponseEntity.notFound().build();
            }
            
            boolean rotationNeeded = agentOrchestrationService.isMemoryRotationNeeded(sessionId);
            
            Map<String, Object> status = new HashMap<>();
            status.put("sessionId", sessionId);
            status.put("memoryRotationNeeded", rotationNeeded);
            status.put("totalTurns", history.getTotalTurns());
            status.put("lastUpdated", history.getLastUpdated());
            status.put("checkTime", LocalDateTime.now());
            
            return ResponseEntity.ok(status);
            
        } catch (Exception e) {
            log.error("Failed to check memory status for session {}: {}", sessionId, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Manually trigger memory rotation for a session
     */
    @PostMapping("/memory-rotation/{userId}/{sessionId}")
    public ResponseEntity<Map<String, Object>> triggerMemoryRotation(
            @PathVariable String userId,
            @PathVariable String sessionId) {
        log.info("Manually triggering memory rotation for user {} session {}", userId, sessionId);
        
        try {
            agentOrchestrationService.triggerMemoryRotation(userId, sessionId);
            
            Map<String, Object> result = new HashMap<>();
            result.put("status", "success");
            result.put("message", "Memory rotation triggered successfully");
            result.put("sessionId", sessionId);
            result.put("userId", userId);
            result.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("Failed to trigger memory rotation for session {}: {}", sessionId, e.getMessage(), e);
            
            Map<String, Object> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", "Failed to trigger memory rotation: " + e.getMessage());
            error.put("sessionId", sessionId);
            error.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.internalServerError().body(error);
        }
    }
    
    /**
     * Get conversation turn by turn number
     */
    @GetMapping("/turn/{sessionId}/{turnNumber}")
    public ResponseEntity<ConversationTurn> getConversationTurn(
            @PathVariable String sessionId,
            @PathVariable int turnNumber) {
        log.info("Retrieving turn {} for session: {}", turnNumber, sessionId);
        
        try {
            ConversationHistory history = agentOrchestrationService.getConversationHistory(sessionId);
            
            if (history == null) {
                return ResponseEntity.notFound().build();
            }
            
            ConversationTurn turn = history.getTurns().stream()
                .filter(t -> t.getTurnNumber() == turnNumber)
                .findFirst()
                .orElse(null);
            
            if (turn == null) {
                return ResponseEntity.notFound().build();
            }
            
            return ResponseEntity.ok(turn);
            
        } catch (Exception e) {
            log.error("Failed to retrieve turn {} for session {}: {}", turnNumber, sessionId, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Search conversation turns by content
     */
    @GetMapping("/search/{sessionId}")
    public ResponseEntity<List<ConversationTurn>> searchConversationTurns(
            @PathVariable String sessionId,
            @RequestParam String query,
            @RequestParam(defaultValue = "10") int limit) {
        log.info("Searching conversation turns for session {} with query: {}", sessionId, query);
        
        try {
            ConversationHistory history = agentOrchestrationService.getConversationHistory(sessionId);
            
            if (history == null) {
                return ResponseEntity.notFound().build();
            }
            
            List<ConversationTurn> matchingTurns = history.getTurns().stream()
                .filter(turn -> 
                    (turn.getUserMessage() != null && turn.getUserMessage().toLowerCase().contains(query.toLowerCase())) ||
                    (turn.getAgentResponse() != null && turn.getAgentResponse().toLowerCase().contains(query.toLowerCase())) ||
                    (turn.getTopicTags() != null && turn.getTopicTags().toLowerCase().contains(query.toLowerCase()))
                )
                .limit(limit)
                .collect(java.util.stream.Collectors.toList());
            
            return ResponseEntity.ok(matchingTurns);
            
        } catch (Exception e) {
            log.error("Failed to search conversation turns for session {}: {}", sessionId, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Get conversation analytics across all sessions
     */
    @GetMapping("/analytics")
    public ResponseEntity<Map<String, Object>> getConversationAnalytics() {
        log.info("Retrieving conversation analytics");
        
        try {
            // Note: This would require additional tracking across sessions
            // For now, return basic analytics structure
            
            Map<String, Object> analytics = new HashMap<>();
            analytics.put("timestamp", LocalDateTime.now());
            analytics.put("status", "Sub-Plan 2: Conversation Turn Management Active");
            analytics.put("features", List.of(
                "Complete conversation turn tracking",
                "Memory block update mechanisms", 
                "Memory rotation with error handling",
                "Atomic turn operations with rollback",
                "Enhanced context enrichment",
                "Bidirectional memory updates"
            ));
            
            return ResponseEntity.ok(analytics);
            
        } catch (Exception e) {
            log.error("Failed to retrieve conversation analytics: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
} 