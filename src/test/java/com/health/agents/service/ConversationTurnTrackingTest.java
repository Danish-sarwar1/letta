package com.health.agents.service;

import com.health.agents.config.MemoryArchitectureConfig;
import com.health.agents.model.dto.ConversationHistory;
import com.health.agents.model.dto.ConversationTurn;
import com.health.agents.model.UserAgentMapping;
import com.health.agents.integration.letta.service.LettaAgentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Sub-Plan 6: Unit Tests for Conversation Turn Tracking
 * Tests turn creation, history management, and memory rotation
 */
@SpringBootTest
@DisplayName("Conversation Turn Tracking Tests")
class ConversationTurnTrackingTest {

    @Mock
    private LettaAgentService lettaAgentService;

    @Mock
    private MemoryManagementService memoryManagementService;

    @Mock
    private MemoryArchitectureConfig memoryConfig;

    @InjectMocks
    private ConversationTurnService conversationTurnService;

    private UserAgentMapping testAgents;
    private String testSessionId;
    private String testUserId;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        testSessionId = "test-session-123";
        testUserId = "test-user-456";
        
        testAgents = UserAgentMapping.builder()
            .identityId("identity-123")
            .contextExtractorId("context-456")
            .generalHealthId("general-789")
            .mentalHealthId("mental-101")
            .build();

        // Setup memory configuration mocks
        when(memoryConfig.getConversationHistoryLimit()).thenReturn(32000);
        when(memoryConfig.getActiveSessionLimit()).thenReturn(4000);
        when(memoryConfig.getContextSummaryLimit()).thenReturn(8000);
        when(memoryConfig.getMemoryMetadataLimit()).thenReturn(2000);
        
        MemoryArchitectureConfig.Archival archival = new MemoryArchitectureConfig.Archival();
        archival.setTriggerTurns(50);
        when(memoryConfig.getArchival()).thenReturn(archival);

        // Mock memory management service responses
        when(memoryManagementService.formatConversationHistory(any())).thenReturn("Formatted conversation history");
        when(memoryManagementService.createActiveSessionContent(anyString(), anyString(), anyString(), anyString(), anyInt()))
            .thenReturn("Formatted session content");
        when(memoryManagementService.createContextSummary(any())).thenReturn("Context summary");
        when(memoryManagementService.createMemoryMetadata(anyString(), anyInt(), anyInt(), anyInt()))
            .thenReturn("Memory metadata");
        when(memoryManagementService.isArchivalNeeded(anyInt())).thenReturn(false);
    }

    @Test
    @DisplayName("Should successfully add user message turn")
    void testAddUserMessageTurn() throws Exception {
        // Given
        String userMessage = "I have been experiencing headaches for the past few days";

        // When
        ConversationTurn result = conversationTurnService.addUserMessageTurn(
            testSessionId, testUserId, userMessage, testAgents.getContextExtractorId(), testAgents.getIdentityId());

        // Then
        assertNotNull(result);
        assertEquals(testSessionId, result.getSessionId());
        assertEquals(userMessage, result.getUserMessage());
        assertNull(result.getAgentResponse());
        assertEquals(1, result.getTurnNumber());
        assertNotNull(result.getTimestamp());
        assertFalse(result.isArchived());
    }

    @Test
    @DisplayName("Should successfully add agent response turn")
    void testAddAgentResponseTurn() throws Exception {
        // Given
        String userMessage = "I have a headache";
        String agentResponse = "I understand you're experiencing headaches. Can you tell me more about the symptoms?";
        String agentType = "GENERAL_HEALTH";

        // First add user message
        conversationTurnService.addUserMessageTurn(
            testSessionId, testUserId, userMessage, testAgents.getContextExtractorId(), testAgents.getIdentityId());

        // When
        ConversationTurn result = conversationTurnService.addAgentResponseTurn(
            testSessionId, agentResponse, agentType, testAgents.getContextExtractorId(), testAgents.getIdentityId());

        // Then
        assertNotNull(result);
        assertEquals(testSessionId, result.getSessionId());
        assertEquals(agentResponse, result.getAgentResponse());
        assertNull(result.getUserMessage());
        assertEquals(agentType, result.getRoutedAgent());
        assertEquals(2, result.getTurnNumber());
        assertNotNull(result.getTimestamp());
        assertFalse(result.isArchived());
    }

    @Test
    @DisplayName("Should maintain correct turn numbering sequence")
    void testTurnNumberingSequence() throws Exception {
        // Given
        String[] userMessages = {
            "I have a headache",
            "It started this morning",
            "The pain is getting worse"
        };
        String[] agentResponses = {
            "Tell me more about your headache",
            "When did you first notice the pain?",
            "I recommend you see a doctor"
        };

        // When - Add alternating user messages and agent responses
        for (int i = 0; i < userMessages.length; i++) {
            ConversationTurn userTurn = conversationTurnService.addUserMessageTurn(
                testSessionId, testUserId, userMessages[i], 
                testAgents.getContextExtractorId(), testAgents.getIdentityId());
            
            ConversationTurn agentTurn = conversationTurnService.addAgentResponseTurn(
                testSessionId, agentResponses[i], "GENERAL_HEALTH",
                testAgents.getContextExtractorId(), testAgents.getIdentityId());

            // Then - Verify turn numbers
            assertEquals((i * 2) + 1, userTurn.getTurnNumber());
            assertEquals((i * 2) + 2, agentTurn.getTurnNumber());
        }

        // Verify conversation history
        ConversationHistory history = conversationTurnService.getConversationHistory(testSessionId);
        assertEquals(6, history.getTotalTurns());
        assertEquals(6, history.getTurns().size());
    }

    @Test
    @DisplayName("Should retrieve complete conversation history")
    void testGetConversationHistory() throws Exception {
        // Given
        conversationTurnService.addUserMessageTurn(
            testSessionId, testUserId, "I have a headache", 
            testAgents.getContextExtractorId(), testAgents.getIdentityId());
        conversationTurnService.addAgentResponseTurn(
            testSessionId, "Tell me more about your symptoms", "GENERAL_HEALTH",
            testAgents.getContextExtractorId(), testAgents.getIdentityId());

        // When
        ConversationHistory history = conversationTurnService.getConversationHistory(testSessionId);

        // Then
        assertNotNull(history);
        assertEquals(testSessionId, history.getSessionId());
        assertEquals(testUserId, history.getUserId());
        assertEquals(2, history.getTotalTurns());
        assertEquals(2, history.getTurns().size());
        assertEquals("ACTIVE", history.getStatus());
        assertNotNull(history.getCreatedAt());
        assertNotNull(history.getLastUpdated());
    }

    @Test
    @DisplayName("Should detect memory rotation needs correctly")
    void testMemoryRotationDetection() throws Exception {
        // Given - Create a conversation with many turns
        when(memoryManagementService.isArchivalNeeded(anyInt())).thenReturn(true);

        for (int i = 0; i < 25; i++) {
            conversationTurnService.addUserMessageTurn(
                testSessionId, testUserId, "Message " + i, 
                testAgents.getContextExtractorId(), testAgents.getIdentityId());
            conversationTurnService.addAgentResponseTurn(
                testSessionId, "Response " + i, "GENERAL_HEALTH",
                testAgents.getContextExtractorId(), testAgents.getIdentityId());
        }

        // When
        boolean needsRotation = conversationTurnService.isMemoryRotationNeeded(testSessionId);

        // Then
        assertTrue(needsRotation, "Memory rotation should be needed for long conversations");
    }

    @Test
    @DisplayName("Should perform memory rotation correctly")
    void testMemoryRotation() throws Exception {
        // Given - Create conversation with many turns
        for (int i = 0; i < 20; i++) {
            conversationTurnService.addUserMessageTurn(
                testSessionId, testUserId, "Message " + i, 
                testAgents.getContextExtractorId(), testAgents.getIdentityId());
            conversationTurnService.addAgentResponseTurn(
                testSessionId, "Response " + i, "GENERAL_HEALTH",
                testAgents.getContextExtractorId(), testAgents.getIdentityId());
        }

        ConversationHistory beforeRotation = conversationTurnService.getConversationHistory(testSessionId);
        int totalTurnsBefore = beforeRotation.getTotalTurns();

        // When
        conversationTurnService.performMemoryRotation(
            testSessionId, testAgents.getContextExtractorId(), testAgents.getIdentityId());

        // Then
        ConversationHistory afterRotation = conversationTurnService.getConversationHistory(testSessionId);
        
        // Check that oldest turns are marked as archived
        long archivedCount = afterRotation.getTurns().stream()
            .mapToLong(turn -> turn.isArchived() ? 1 : 0)
            .sum();
        
        assertTrue(archivedCount > 0, "Some turns should be marked as archived");
        assertEquals(totalTurnsBefore, afterRotation.getTotalTurns(), "Total turns should remain the same");
    }

    @Test
    @DisplayName("Should handle concurrent turn additions correctly")
    void testConcurrentTurnAdditions() throws Exception {
        // Given
        int numberOfThreads = 5;
        int turnsPerThread = 4;
        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch latch = new CountDownLatch(numberOfThreads);

        // When - Multiple threads add turns concurrently
        for (int i = 0; i < numberOfThreads; i++) {
            final int threadId = i;
            executor.submit(() -> {
                try {
                    for (int j = 0; j < turnsPerThread; j++) {
                        conversationTurnService.addUserMessageTurn(
                            testSessionId, testUserId, 
                            String.format("Thread %d Message %d", threadId, j),
                            testAgents.getContextExtractorId(), testAgents.getIdentityId());
                    }
                } catch (Exception e) {
                    fail("Concurrent turn addition failed: " + e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(); // Wait for all threads to complete
        executor.shutdown();

        // Then
        ConversationHistory history = conversationTurnService.getConversationHistory(testSessionId);
        assertEquals(numberOfThreads * turnsPerThread, history.getTotalTurns());
        
        // Verify turn numbers are unique and sequential
        for (int i = 1; i <= history.getTotalTurns(); i++) {
            final int expectedTurnNumber = i;
            assertTrue(history.getTurns().stream()
                .anyMatch(turn -> turn.getTurnNumber() == expectedTurnNumber),
                "Turn number " + expectedTurnNumber + " should exist");
        }
    }

    @Test
    @DisplayName("Should clear session correctly")
    void testClearSession() throws Exception {
        // Given
        conversationTurnService.addUserMessageTurn(
            testSessionId, testUserId, "Test message", 
            testAgents.getContextExtractorId(), testAgents.getIdentityId());

        ConversationHistory beforeClear = conversationTurnService.getConversationHistory(testSessionId);
        assertNotNull(beforeClear);
        assertEquals(1, beforeClear.getTotalTurns());

        // When
        conversationTurnService.clearSession(testSessionId);

        // Then
        ConversationHistory afterClear = conversationTurnService.getConversationHistory(testSessionId);
        assertNull(afterClear, "Session should be cleared and return null");
    }

    @Test
    @DisplayName("Should handle empty session gracefully")
    void testEmptySession() {
        // When
        ConversationHistory history = conversationTurnService.getConversationHistory("non-existent-session");

        // Then
        assertNull(history, "Non-existent session should return null");
    }

    @Test
    @DisplayName("Should track conversation timestamps correctly")
    void testConversationTimestamps() throws Exception {
        // Given
        LocalDateTime startTime = LocalDateTime.now();

        // When
        ConversationTurn turn1 = conversationTurnService.addUserMessageTurn(
            testSessionId, testUserId, "First message", 
            testAgents.getContextExtractorId(), testAgents.getIdentityId());

        Thread.sleep(10); // Small delay to ensure different timestamps

        ConversationTurn turn2 = conversationTurnService.addAgentResponseTurn(
            testSessionId, "First response", "GENERAL_HEALTH",
            testAgents.getContextExtractorId(), testAgents.getIdentityId());

        ConversationHistory history = conversationTurnService.getConversationHistory(testSessionId);

        // Then
        assertTrue(turn1.getTimestamp().isAfter(startTime) || turn1.getTimestamp().isEqual(startTime));
        assertTrue(turn2.getTimestamp().isAfter(turn1.getTimestamp()));
        assertTrue(history.getCreatedAt().isAfter(startTime) || history.getCreatedAt().isEqual(startTime));
        assertTrue(history.getLastUpdated().isAfter(history.getCreatedAt()) || 
                  history.getLastUpdated().isEqual(history.getCreatedAt()));
    }

    @Test
    @DisplayName("Should validate turn data integrity")
    void testTurnDataIntegrity() throws Exception {
        // Given
        String userMessage = "I need help with my symptoms";
        String agentResponse = "I'm here to help you. Please describe your symptoms.";

        // When
        ConversationTurn userTurn = conversationTurnService.addUserMessageTurn(
            testSessionId, testUserId, userMessage, 
            testAgents.getContextExtractorId(), testAgents.getIdentityId());

        ConversationTurn agentTurn = conversationTurnService.addAgentResponseTurn(
            testSessionId, agentResponse, "GENERAL_HEALTH",
            testAgents.getContextExtractorId(), testAgents.getIdentityId());

        // Then - Verify user turn data integrity
        assertNotNull(userTurn.getSessionId());
        assertNotNull(userTurn.getUserMessage());
        assertNull(userTurn.getAgentResponse());
        assertNull(userTurn.getRoutedAgent());
        assertTrue(userTurn.getTurnNumber() > 0);
        assertNotNull(userTurn.getTimestamp());
        assertFalse(userTurn.isArchived());

        // Then - Verify agent turn data integrity
        assertNotNull(agentTurn.getSessionId());
        assertNull(agentTurn.getUserMessage());
        assertNotNull(agentTurn.getAgentResponse());
        assertNotNull(agentTurn.getRoutedAgent());
        assertTrue(agentTurn.getTurnNumber() > userTurn.getTurnNumber());
        assertNotNull(agentTurn.getTimestamp());
        assertFalse(agentTurn.isArchived());
    }

    @Test
    @DisplayName("Should update memory blocks correctly")
    void testMemoryBlockUpdates() throws Exception {
        // Given
        String userMessage = "I have been feeling anxious lately";

        // When
        conversationTurnService.addUserMessageTurn(
            testSessionId, testUserId, userMessage, 
            testAgents.getContextExtractorId(), testAgents.getIdentityId());

        // Then - Verify memory management methods were called
        verify(memoryManagementService, atLeastOnce()).formatConversationHistory(any(ConversationHistory.class));
        verify(memoryManagementService, atLeastOnce()).createActiveSessionContent(
            eq(testSessionId), eq(testUserId), eq("ACTIVE"), anyString(), anyInt());
        verify(memoryManagementService, atLeastOnce()).createContextSummary(any(ConversationHistory.class));
        verify(memoryManagementService, atLeastOnce()).createMemoryMetadata(
            eq(testSessionId), anyInt(), anyInt(), anyInt());
    }
} 