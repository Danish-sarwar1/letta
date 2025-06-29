package com.health.agents.integration;

import com.health.agents.config.MemoryArchitectureConfig;
import com.health.agents.model.dto.ChatResponse;
import com.health.agents.model.dto.ConversationHistory;
import com.health.agents.model.dto.ConversationTurn;
import com.health.agents.model.dto.ContextEnrichmentResult;
import com.health.agents.model.dto.SessionState;
import com.health.agents.model.UserAgentMapping;
import com.health.agents.service.*;
import com.health.agents.integration.letta.service.LettaAgentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Sub-Plan 6: Integration Tests for Complete Centralized Context Management System
 * Tests end-to-end functionality including all Sub-Plans 1-5
 */
@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Centralized Context Management Integration Tests")
class CentralizedContextManagementIntegrationTest {

    @Autowired
    private AgentOrchestrationService agentOrchestrationService;

    @Autowired
    private ConversationTurnService conversationTurnService;

    @Autowired
    private ContextEnrichmentService contextEnrichmentService;

    @Autowired
    private MemoryManagementService memoryManagementService;

    @Autowired
    private SessionStateManagementService sessionStateManagementService;

    @Autowired
    private BiDirectionalMemoryService biDirectionalMemoryService;

    @Autowired
    private MemoryArchitectureConfig memoryConfig;

    @MockBean
    private LettaAgentService lettaAgentService;

    @MockBean
    private UserIdentityService userIdentityService;

    private String testUserId;
    private String testSessionId;
    private UserAgentMapping testAgents;

    @BeforeEach
    void setUp() {
        testUserId = "integration-test-user";
        testSessionId = "integration-test-session";
        
        testAgents = UserAgentMapping.builder()
            .identityId("identity-integration")
            .contextExtractorId("context-integration")
            .generalHealthId("general-integration")
            .mentalHealthId("mental-integration")
            .build();

        // Mock user identity service
        when(userIdentityService.getOrCreateUserAgents(testUserId)).thenReturn(testAgents);

        // Mock Letta service responses
        when(lettaAgentService.sendMessage(anyString(), anyString(), any()))
            .thenReturn(createMockLettaResponse("Mocked agent response"));
    }

    @Test
    @DisplayName("Complete conversation flow with all Sub-Plans integrated")
    void testCompleteConversationFlow() throws Exception {
        // Test Scenario: User reports headache symptoms, gets routed to appropriate agent,
        // memory is managed, context is enriched, and session state is tracked

        String[] userMessages = {
            "Hello, I've been having headaches for the past week",
            "The headaches happen mostly in the morning",
            "I'm worried it could be something serious",
            "What tests should I consider getting?"
        };

        String[] expectedTopics = {"headaches", "morning symptoms", "health concerns", "medical tests"};

        // Process each message and verify complete system integration
        for (int i = 0; i < userMessages.length; i++) {
            // When - Process message through complete system
            ChatResponse response = agentOrchestrationService.processMessage(testUserId, testSessionId, userMessages[i]);

            // Then - Verify complete response
            assertNotNull(response, "Response should not be null for message: " + userMessages[i]);
            assertNotNull(response.getMessage(), "Response message should not be null");
            assertEquals(testSessionId, response.getSessionId());
            assertEquals(testUserId, response.getUserId());
            assertTrue(response.getConfidence() > 0.0);
            
            // Verify conversation history is maintained (Sub-Plan 2)
            ConversationHistory history = conversationTurnService.getConversationHistory(testSessionId);
            assertNotNull(history, "Conversation history should be maintained");
            assertTrue(history.getTotalTurns() >= i + 1, "Turn count should increase");
            
            // Verify context enrichment (Sub-Plan 3)
            if (i > 0) { // After first message, context should be enriched
                assertTrue(response.getRelevantTurns() > 0, "Should have relevant turns for context");
                assertTrue(response.getContextConfidence() > 0.0, "Should have context confidence");
            }
            
            // Verify session state tracking (Sub-Plan 5)
            SessionState sessionState = sessionStateManagementService.getSessionState(testSessionId);
            if (sessionState != null) {
                assertTrue(sessionState.isActive(), "Session should be active");
                assertEquals(history.getTotalTurns(), sessionState.getTotalTurns());
            }
        }

        // Verify final system state
        verifyFinalSystemState();
    }

    @Test
    @DisplayName("Memory rotation and archival integration")
    void testMemoryRotationIntegration() throws Exception {
        // Create a long conversation to trigger memory rotation
        for (int i = 1; i <= 30; i++) {
            String message = String.format("Message %d: I have various health concerns", i);
            agentOrchestrationService.processMessage(testUserId, testSessionId, message);
        }

        // Verify memory management is working
        ConversationHistory history = conversationTurnService.getConversationHistory(testSessionId);
        assertNotNull(history);
        
        boolean rotationTriggered = conversationTurnService.isMemoryRotationNeeded(testSessionId);
        if (rotationTriggered) {
            // Perform rotation
            conversationTurnService.performMemoryRotation(testSessionId, 
                testAgents.getContextExtractorId(), testAgents.getIdentityId());
            
            // Verify rotation worked
            ConversationHistory postRotationHistory = conversationTurnService.getConversationHistory(testSessionId);
            assertNotNull(postRotationHistory);
            
            // Check that some turns are marked as archived
            long archivedCount = postRotationHistory.getTurns().stream()
                .mapToLong(turn -> turn.isArchived() ? 1 : 0)
                .sum();
            assertTrue(archivedCount > 0, "Some turns should be archived after rotation");
        }
    }

    @Test
    @DisplayName("Cross-session continuity integration")
    void testCrossSessionContinuityIntegration() throws Exception {
        // First session
        String firstSessionId = testSessionId + "-1";
        agentOrchestrationService.processMessage(testUserId, firstSessionId, "I have chronic headaches");
        agentOrchestrationService.processMessage(testUserId, firstSessionId, "I've been tracking them for weeks");
        
        // End first session
        sessionStateManagementService.endSession(firstSessionId, "First consultation completed", "Follow-up needed");

        // Second session - should have continuity
        String secondSessionId = testSessionId + "-2";
        ChatResponse continuityResponse = agentOrchestrationService.processMessage(
            testUserId, secondSessionId, "Following up on my headache issue");

        // Verify continuity
        assertNotNull(continuityResponse);
        assertTrue(continuityResponse.getContextConfidence() > 0.3, 
            "Second session should have context from first session");
        
        // Verify cross-session data is maintained
        var continuity = sessionStateManagementService.getCrossSessionContinuity(testUserId);
        if (continuity != null) {
            assertTrue(continuity.getRelatedSessions().size() >= 1, 
                "Should have previous session data");
        }
    }

    @Test
    @DisplayName("Context enrichment with pattern recognition")
    void testContextEnrichmentWithPatterns() throws Exception {
        // Create conversation with clear patterns
        String[] patternedMessages = {
            "I get headaches every morning",
            "The headaches always start at 8 AM",
            "They happen right after I wake up",
            "This morning pattern has been consistent"
        };

        for (String message : patternedMessages) {
            agentOrchestrationService.processMessage(testUserId, testSessionId, message);
        }

        // Test pattern recognition in context enrichment
        ContextEnrichmentResult enrichmentResult = contextEnrichmentService.enrichContext(
            testSessionId, "Why do I get these morning headaches?", 5);

        assertNotNull(enrichmentResult);
        assertTrue(enrichmentResult.getContextConfidence() > 0.5);
        assertTrue(enrichmentResult.getRelevantTurns() > 0);
        
        // Context should contain pattern information
        String enrichedContext = enrichmentResult.getEnrichedMessage();
        assertTrue(enrichedContext.contains("morning") || enrichedContext.contains("pattern"));
    }

    @Test
    @DisplayName("Bidirectional memory updates integration")
    void testBidirectionalMemoryIntegration() throws Exception {
        // Send a message that will trigger bidirectional memory update
        ChatResponse response = agentOrchestrationService.processMessage(
            testUserId, testSessionId, "I need comprehensive information about my symptoms");

        // Verify response quality analysis
        assertNotNull(response);
        
        // Check conversation history for bidirectional update indicators
        ConversationHistory history = conversationTurnService.getConversationHistory(testSessionId);
        assertNotNull(history);
        
        if (!history.getTurns().isEmpty()) {
            ConversationTurn lastTurn = history.getTurns().get(history.getTurns().size() - 1);
            
            // Verify bidirectional analysis fields are populated
            if (lastTurn.getResponseQuality() != null) {
                assertTrue(lastTurn.getResponseQuality() >= 0.0);
                assertTrue(lastTurn.getResponseQuality() <= 1.0);
            }
        }
    }

    @Test
    @DisplayName("Error handling and recovery integration")
    void testErrorHandlingIntegration() throws Exception {
        // Simulate service failures
        when(lettaAgentService.sendMessage(anyString(), anyString(), any()))
            .thenThrow(new RuntimeException("Service temporarily unavailable"));

        // System should handle errors gracefully
        ChatResponse errorResponse = agentOrchestrationService.processMessage(
            testUserId, testSessionId, "Test message during service failure");

        assertNotNull(errorResponse);
        assertNotNull(errorResponse.getMessage());
        assertTrue(errorResponse.getMessage().contains("trouble") || 
                  errorResponse.getMessage().contains("error") ||
                  errorResponse.getMessage().contains("sorry"));
    }

    @Test
    @DisplayName("Performance under load")
    void testPerformanceIntegration() throws Exception {
        long startTime = System.currentTimeMillis();
        
        // Process multiple messages rapidly
        for (int i = 0; i < 10; i++) {
            agentOrchestrationService.processMessage(testUserId, testSessionId, 
                "Performance test message " + i);
        }
        
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        
        // Should complete within reasonable time (10 seconds for 10 messages)
        assertTrue(duration < 10000, "Performance test should complete within 10 seconds");
        
        // Verify all messages were processed
        ConversationHistory history = conversationTurnService.getConversationHistory(testSessionId);
        assertNotNull(history);
        assertEquals(10, history.getTotalTurns());
    }

    @Test
    @DisplayName("Memory consistency across concurrent operations")
    void testConcurrentMemoryConsistency() throws Exception {
        // Simulate concurrent message processing
        Thread[] threads = new Thread[3];
        
        for (int i = 0; i < threads.length; i++) {
            final int threadId = i;
            threads[i] = new Thread(() -> {
                try {
                    for (int j = 0; j < 5; j++) {
                        agentOrchestrationService.processMessage(testUserId, testSessionId, 
                            String.format("Concurrent message Thread-%d Message-%d", threadId, j));
                    }
                } catch (Exception e) {
                    fail("Concurrent processing failed: " + e.getMessage());
                }
            });
        }
        
        // Start all threads
        for (Thread thread : threads) {
            thread.start();
        }
        
        // Wait for completion
        for (Thread thread : threads) {
            thread.join();
        }
        
        // Verify final state consistency
        ConversationHistory history = conversationTurnService.getConversationHistory(testSessionId);
        assertNotNull(history);
        assertEquals(15, history.getTotalTurns()); // 3 threads * 5 messages each
        
        // Verify turn numbers are unique and sequential
        List<Integer> turnNumbers = history.getTurns().stream()
            .map(ConversationTurn::getTurnNumber)
            .sorted()
            .toList();
        
        for (int i = 0; i < turnNumbers.size(); i++) {
            assertEquals(i + 1, turnNumbers.get(i).intValue(), 
                "Turn numbers should be sequential");
        }
    }

    @Test
    @DisplayName("Complete system health check")
    void testSystemHealthCheck() {
        // Verify all core services are properly initialized
        assertNotNull(agentOrchestrationService, "AgentOrchestrationService should be initialized");
        assertNotNull(conversationTurnService, "ConversationTurnService should be initialized");
        assertNotNull(contextEnrichmentService, "ContextEnrichmentService should be initialized");
        assertNotNull(memoryManagementService, "MemoryManagementService should be initialized");
        assertNotNull(sessionStateManagementService, "SessionStateManagementService should be initialized");
        assertNotNull(biDirectionalMemoryService, "BiDirectionalMemoryService should be initialized");
        assertNotNull(memoryConfig, "MemoryArchitectureConfig should be initialized");
        
        // Verify configuration is loaded
        assertTrue(memoryConfig.getConversationHistoryLimit() > 0);
        assertTrue(memoryConfig.getActiveSessionLimit() > 0);
        assertTrue(memoryConfig.getContextSummaryLimit() > 0);
        assertTrue(memoryConfig.getMemoryMetadataLimit() > 0);
    }

    // Helper methods

    private void verifyFinalSystemState() {
        ConversationHistory history = conversationTurnService.getConversationHistory(testSessionId);
        assertNotNull(history, "Final conversation history should exist");
        assertTrue(history.getTotalTurns() > 0, "Should have conversation turns");
        assertEquals("ACTIVE", history.getStatus(), "History should be active");
        
        SessionState sessionState = sessionStateManagementService.getSessionState(testSessionId);
        if (sessionState != null) {
            assertTrue(sessionState.isActive(), "Session should be active");
            assertTrue(sessionState.getTotalTurns() > 0, "Session should have turns");
            assertNotNull(sessionState.getUserEngagement(), "User engagement should be tracked");
            assertNotNull(sessionState.getMemoryUsage(), "Memory usage should be tracked");
        }
    }

    private com.health.agents.integration.letta.model.LettaMessageResponse createMockLettaResponse(String message) {
        // Create a mock response - adjust based on actual LettaMessageResponse structure
        var response = new com.health.agents.integration.letta.model.LettaMessageResponse();
        // Set appropriate fields based on the actual class structure
        return response;
    }
} 