package com.health.agents.service;

import com.health.agents.config.MemoryArchitectureConfig;
import com.health.agents.model.dto.ConversationHistory;
import com.health.agents.model.dto.ConversationTurn;
import com.health.agents.model.UserAgentMapping;
import com.health.agents.integration.letta.model.LettaMessage;
import com.health.agents.integration.letta.model.LettaMessageRequest;
import com.health.agents.integration.letta.service.LettaAgentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Sub-Plan 6: Unit Tests for Memory Block Operations
 * Tests memory block management, conversation turn tracking, and archival triggers
 */
@SpringBootTest
@DisplayName("Memory Block Operations Tests")
class MemoryBlockOperationsTest {

    @Mock
    private LettaAgentService lettaAgentService;

    @Mock
    private MemoryArchitectureConfig memoryConfig;

    @InjectMocks
    private MemoryManagementService memoryManagementService;

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
        when(memoryConfig.shouldRotateMemory(anyInt(), anyInt())).thenReturn(false);
        when(memoryConfig.getDataFormat()).thenReturn("STRUCTURED_TEXT");
        
        MemoryArchitectureConfig.Archival archival = new MemoryArchitectureConfig.Archival();
        archival.setTriggerTurns(50);
        when(memoryConfig.getArchival()).thenReturn(archival);
        
        MemoryArchitectureConfig.ContextWindow contextWindow = new MemoryArchitectureConfig.ContextWindow();
        contextWindow.setRecentTurns(5);
        when(memoryConfig.getContextWindow()).thenReturn(contextWindow);
        when(memoryConfig.getRecentTurnsWindow()).thenReturn(5);
    }

    @Test
    @DisplayName("Should successfully create conversation history memory block")
    void testCreateConversationHistoryBlock() {
        // Given
        ConversationHistory history = ConversationHistory.builder()
            .sessionId(testSessionId)
            .userId(testUserId)
            .totalTurns(3)
            .status("ACTIVE")
            .turns(Arrays.asList(
                createTestTurn(1, "I have a headache", null),
                createTestTurn(2, null, "I understand you're experiencing a headache..."),
                createTestTurn(3, "It started this morning", null)
            ))
            .build();

        // When
        String memoryBlock = memoryManagementService.formatConversationHistory(history);

        // Then
        assertNotNull(memoryBlock);
        assertTrue(memoryBlock.contains(testSessionId));
        assertTrue(memoryBlock.contains("headache"));
        assertTrue(memoryBlock.contains("TURN_1"));
        assertTrue(memoryBlock.contains("TURN_3"));
        assertTrue(memoryBlock.length() < memoryConfig.getConversationHistoryLimit());
    }

    @Test
    @DisplayName("Should handle memory block size limits correctly")
    void testMemoryBlockSizeLimits() {
        // Given
        StringBuilder largeContent = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            largeContent.append("This is a very long conversation turn that will help test memory limits. ");
        }
        
        ConversationTurn largeTurn = createTestTurn(1, largeContent.toString(), null);
        ConversationHistory largeHistory = ConversationHistory.builder()
            .sessionId(testSessionId)
            .userId(testUserId)
            .totalTurns(1)
            .status("ACTIVE")
            .turns(Arrays.asList(largeTurn))
            .build();

        // When
        String memoryBlock = memoryManagementService.formatConversationHistory(largeHistory);
        boolean shouldRotate = memoryManagementService.isMemoryRotationNeeded(
            memoryBlock, memoryConfig.getConversationHistoryLimit());

        // Then
        assertNotNull(memoryBlock);
        if (memoryBlock.length() > memoryConfig.getConversationHistoryLimit() * 0.8) {
            assertTrue(shouldRotate, "Memory rotation should be triggered for large content");
        }
    }

    @Test
    @DisplayName("Should correctly format active session memory block")
    void testActiveSessionMemoryBlock() {
        // Given
        String currentTopic = "headache symptoms";
        int totalTurns = 5;

        // When
        String memoryBlock = memoryManagementService.createActiveSessionContent(
            testSessionId, testUserId, "ACTIVE", currentTopic, totalTurns);

        // Then
        assertNotNull(memoryBlock);
        assertTrue(memoryBlock.contains(testSessionId));
        assertTrue(memoryBlock.contains("ACTIVE"));
        assertTrue(memoryBlock.contains(currentTopic));
        assertTrue(memoryBlock.contains("5"));
        assertTrue(memoryBlock.length() < memoryConfig.getActiveSessionLimit());
    }

    @Test
    @DisplayName("Should trigger archival at configured turn threshold")
    void testArchivalTriggers() {
        // Given
        ConversationHistory longHistory = ConversationHistory.builder()
            .sessionId(testSessionId)
            .userId(testUserId)
            .totalTurns(55) // Above the trigger threshold of 50
            .status("ACTIVE")
            .turns(createManyTurns(55))
            .build();

        // When
        boolean shouldArchive = memoryManagementService.isArchivalNeeded(longHistory.getTotalTurns());

        // Then
        assertTrue(shouldArchive, "Archival should be triggered when turns exceed threshold");
    }

    @Test
    @DisplayName("Should maintain memory block consistency during updates")
    void testMemoryBlockConsistency() {
        // Given
        ConversationHistory initialHistory = ConversationHistory.builder()
            .sessionId(testSessionId)
            .userId(testUserId)
            .totalTurns(2)
            .status("ACTIVE")
            .turns(Arrays.asList(
                createTestTurn(1, "Initial message", null),
                createTestTurn(2, null, "Initial response")
            ))
            .build();

        ConversationTurn newTurn = createTestTurn(3, "Follow-up message", null);

        // When
        String initialBlock = memoryManagementService.formatConversationHistory(initialHistory);
        
        // Add new turn
        initialHistory.getTurns().add(newTurn);
        initialHistory.setTotalTurns(3);
        
        String updatedBlock = memoryManagementService.formatConversationHistory(initialHistory);

        // Then
        assertNotNull(initialBlock);
        assertNotNull(updatedBlock);
        assertNotEquals(initialBlock, updatedBlock);
        assertTrue(updatedBlock.contains("TURN_3"));
        assertTrue(updatedBlock.contains("Follow-up message"));
        assertTrue(updatedBlock.contains("TOTAL_TURNS: 3"));
    }

    @Test
    @DisplayName("Should handle empty conversation history gracefully")
    void testEmptyConversationHistory() {
        // Given
        ConversationHistory emptyHistory = ConversationHistory.builder()
            .sessionId(testSessionId)
            .userId(testUserId)
            .totalTurns(0)
            .status("ACTIVE")
            .turns(Arrays.asList())
            .build();

        // When
        String memoryBlock = memoryManagementService.formatConversationHistory(emptyHistory);

        // Then
        assertNotNull(memoryBlock);
        assertTrue(memoryBlock.contains("No conversation turns yet"));
        assertTrue(memoryBlock.contains(testSessionId));
        assertFalse(memoryBlock.contains("TURN_1"));
    }

    @Test
    @DisplayName("Should format context summary memory block correctly")
    void testContextSummaryMemoryBlock() {
        // Given
        ConversationHistory history = ConversationHistory.builder()
            .sessionId(testSessionId)
            .userId(testUserId)
            .totalTurns(3)
            .status("ACTIVE")
            .createdAt(LocalDateTime.now().minusMinutes(30))
            .lastUpdated(LocalDateTime.now())
            .turns(Arrays.asList(
                createTestTurn(1, "I have a headache", null),
                createTestTurn(2, null, "I understand you're experiencing a headache..."),
                createTestTurn(3, "It started this morning", null)
            ))
            .build();

        // When
        String contextSummary = memoryManagementService.createContextSummary(history);

        // Then
        assertNotNull(contextSummary);
        assertTrue(contextSummary.contains("CONVERSATION SUMMARY"));
        assertTrue(contextSummary.contains(testSessionId));
        assertTrue(contextSummary.contains("Total Turns: 3"));
        assertTrue(contextSummary.contains("RECENT CONTEXT"));
        assertTrue(contextSummary.length() < memoryConfig.getContextSummaryLimit());
    }

    @Test
    @DisplayName("Should detect memory rotation needs correctly")
    void testMemoryRotationDetection() {
        // Given
        String smallContent = "Small memory content";
        String largeContent = "X".repeat(30000); // Larger than typical limit

        // When
        boolean smallNeedsRotation = memoryManagementService.isMemoryRotationNeeded(
            smallContent, memoryConfig.getConversationHistoryLimit());
        boolean largeNeedsRotation = memoryManagementService.isMemoryRotationNeeded(
            largeContent, memoryConfig.getConversationHistoryLimit());

        // Then
        assertFalse(smallNeedsRotation, "Small content should not need rotation");
        assertTrue(largeNeedsRotation, "Large content should need rotation");
    }

    @Test
    @DisplayName("Should handle memory metadata block creation")
    void testMemoryMetadataBlock() {
        // Given
        int conversationSize = 1500;
        int sessionSize = 800;
        int summarySize = 600;

        // When
        String memoryBlock = memoryManagementService.createMemoryMetadata(
            testSessionId, conversationSize, sessionSize, summarySize);

        // Then
        assertNotNull(memoryBlock);
        assertTrue(memoryBlock.contains("MEMORY_USAGE"));
        assertTrue(memoryBlock.contains(testSessionId));
        assertTrue(memoryBlock.contains("conversation_history"));
        assertTrue(memoryBlock.contains("active_session"));
        assertTrue(memoryBlock.contains("context_summary"));
        assertTrue(memoryBlock.contains("ROTATION_NEEDED"));
        assertTrue(memoryBlock.length() < memoryConfig.getMemoryMetadataLimit());
    }

    @Test
    @DisplayName("Should format conversation turn correctly")
    void testConversationTurnFormatting() {
        // Given
        ConversationTurn turn = createTestTurn(1, "Test user message", "Test agent response");

        // When
        String formattedTurn = memoryManagementService.formatConversationTurn(turn);

        // Then
        assertNotNull(formattedTurn);
        assertTrue(formattedTurn.contains("Test user message"));
        assertTrue(formattedTurn.contains("Test agent response"));
        assertTrue(formattedTurn.contains("1")); // Turn number
    }

    @Test
    @DisplayName("Should check archival needs correctly")
    void testArchivalNeeds() {
        // Given
        int lowTurns = 10;
        int highTurns = 60;

        // When
        boolean lowNeedsArchival = memoryManagementService.isArchivalNeeded(lowTurns);
        boolean highNeedsArchival = memoryManagementService.isArchivalNeeded(highTurns);

        // Then
        assertFalse(lowNeedsArchival, "Low turn count should not need archival");
        assertTrue(highNeedsArchival, "High turn count should need archival");
    }

    // Helper methods

    private ConversationTurn createTestTurn(int turnNumber, String userMessage, String agentResponse) {
        ConversationTurn.ConversationTurnBuilder builder = ConversationTurn.builder()
            .sessionId(testSessionId)
            .turnNumber(turnNumber)
            .timestamp(LocalDateTime.now().minusMinutes(10 - turnNumber));
            
        if (userMessage != null) {
            builder.userMessage(userMessage);
        }
        
        if (agentResponse != null) {
            builder.agentResponse(agentResponse)
                   .routedAgent("GENERAL_HEALTH");
        }
            
        return builder.build();
    }

    private List<ConversationTurn> createManyTurns(int count) {
        List<ConversationTurn> turns = new java.util.ArrayList<>();
        for (int i = 1; i <= count; i++) {
            if (i % 2 == 1) {
                turns.add(createTestTurn(i, "User message " + i, null));
            } else {
                turns.add(createTestTurn(i, null, "Agent response " + i));
            }
        }
        return turns;
    }
} 