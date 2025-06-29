package com.health.agents.service;

import com.health.agents.config.MemoryArchitectureConfig;
import com.health.agents.model.dto.ConversationHistory;
import com.health.agents.model.dto.ConversationTurn;
import com.health.agents.model.dto.SessionState;
import com.health.agents.model.dto.SessionTransition;
import com.health.agents.model.dto.CrossSessionContinuity;
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
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Sub-Plan 6: Unit Tests for Session State Management
 * Tests session initialization, state transitions, and cross-session continuity
 */
@SpringBootTest
@DisplayName("Session State Management Tests")
class SessionStateManagementTest {

    @Mock
    private LettaAgentService lettaAgentService;

    @Mock
    private ConversationTurnService conversationTurnService;

    @Mock
    private MemoryManagementService memoryManagementService;

    @Mock
    private MemoryArchitectureConfig memoryConfig;

    @InjectMocks
    private SessionStateManagementService sessionStateManagementService;

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
        MemoryArchitectureConfig.SessionStateConfig sessionConfig = new MemoryArchitectureConfig.SessionStateConfig();
        when(memoryConfig.getSessionState()).thenReturn(sessionConfig);

        // Mock memory management service responses
        when(memoryManagementService.formatConversationHistory(any())).thenReturn("Formatted conversation history");
        when(memoryManagementService.createActiveSessionContent(anyString(), anyString(), anyString(), anyString(), anyInt()))
            .thenReturn("Formatted session content");
    }

    @Test
    @DisplayName("Should initialize new session state correctly")
    void testInitializeSession() {
        // When
        SessionState sessionState = sessionStateManagementService.initializeSession(
            testSessionId, testUserId, testAgents);

        // Then
        assertNotNull(sessionState);
        assertEquals(testSessionId, sessionState.getSessionId());
        assertEquals(testUserId, sessionState.getUserId());
        assertEquals(SessionState.SessionStatus.INITIALIZING, sessionState.getStatus());
        assertNotNull(sessionState.getCreatedAt());
        assertTrue(sessionState.isActive());
        assertEquals(0, sessionState.getTotalTurns());
        assertEquals("Initial Assessment", sessionState.getConversationPhase());
        assertNotNull(sessionState.getUserEngagement());
        assertNotNull(sessionState.getMemoryUsage());
    }

    @Test
    @DisplayName("Should update session state with conversation turn")
    void testUpdateSessionWithTurn() {
        // Given
        SessionState sessionState = sessionStateManagementService.initializeSession(
            testSessionId, testUserId, testAgents);
        
        ConversationTurn turn = createTestTurn(1, "I have been experiencing headaches", null);

        // When
        sessionStateManagementService.updateSessionWithTurn(testSessionId, turn);

        // Then
        SessionState updatedState = sessionStateManagementService.getSessionState(testSessionId);
        assertNotNull(updatedState);
        assertEquals(1, updatedState.getTotalTurns());
        assertNotNull(updatedState.getLastUpdated());
        assertTrue(updatedState.getLastUpdated().isAfter(sessionState.getCreatedAt()));
        assertEquals(SessionState.SessionStatus.ACTIVE, updatedState.getStatus());
    }

    @Test
    @DisplayName("Should track conversation phase progression")
    void testConversationPhaseProgression() {
        // Given
        SessionState sessionState = sessionStateManagementService.initializeSession(
            testSessionId, testUserId, testAgents);

        // When - Add multiple turns to progress through phases
        for (int i = 1; i <= 10; i++) {
            ConversationTurn turn;
            if (i % 2 == 1) {
                turn = createTestTurn(i, "User message " + i, null);
            } else {
                turn = createTestTurn(i, null, "Agent response " + i);
            }
            sessionStateManagementService.updateSessionWithTurn(testSessionId, turn);
        }

        // Then
        SessionState updatedState = sessionStateManagementService.getSessionState(testSessionId);
        assertNotNull(updatedState);
        assertEquals(10, updatedState.getTotalTurns());
        
        // Should have progressed beyond initial phase
        assertNotEquals("Initial Assessment", updatedState.getConversationPhase());
        assertTrue(Arrays.asList("Information Gathering", "Active Discussion", "Extended Consultation")
            .contains(updatedState.getConversationPhase()));
    }

    @Test
    @DisplayName("Should calculate complexity score correctly")
    void testComplexityScoreCalculation() {
        // Given
        SessionState sessionState = sessionStateManagementService.initializeSession(
            testSessionId, testUserId, testAgents);

        // Add turns with diverse topics and agent switches
        ConversationTurn turn1 = createTestTurn(1, "I have headaches", null);
        turn1.setTopicTags("headache,pain");
        ConversationTurn turn2 = createTestTurn(2, null, "General health response");
        turn2.setRoutedAgent("GENERAL_HEALTH");
        ConversationTurn turn3 = createTestTurn(3, "I'm also feeling anxious", null);
        turn3.setTopicTags("anxiety,mental health");
        ConversationTurn turn4 = createTestTurn(4, null, "Mental health response");
        turn4.setRoutedAgent("MENTAL_HEALTH");

        // When
        sessionStateManagementService.updateSessionWithTurn(testSessionId, turn1);
        sessionStateManagementService.updateSessionWithTurn(testSessionId, turn2);
        sessionStateManagementService.updateSessionWithTurn(testSessionId, turn3);
        sessionStateManagementService.updateSessionWithTurn(testSessionId, turn4);

        // Then
        SessionState updatedState = sessionStateManagementService.getSessionState(testSessionId);
        assertTrue(updatedState.getComplexityScore() > 0.0);
        assertTrue(updatedState.getComplexityScore() <= 1.0);
        
        // Should have higher complexity due to topic diversity and agent switches
        assertTrue(updatedState.getComplexityScore() > 0.3);
    }

    @Test
    @DisplayName("Should track user engagement correctly")
    void testUserEngagementTracking() {
        // Given
        SessionState sessionState = sessionStateManagementService.initializeSession(
            testSessionId, testUserId, testAgents);

        // Add turns with varying message lengths and questions
        ConversationTurn turn1 = createTestTurn(1, "I have a headache", null);
        ConversationTurn turn2 = createTestTurn(2, "Can you tell me more about the symptoms and when they started? I'm really concerned about this.", null);
        ConversationTurn turn3 = createTestTurn(3, "What should I do? Should I see a doctor?", null);

        // When
        sessionStateManagementService.updateSessionWithTurn(testSessionId, turn1);
        sessionStateManagementService.updateSessionWithTurn(testSessionId, turn2);
        sessionStateManagementService.updateSessionWithTurn(testSessionId, turn3);

        // Then
        SessionState updatedState = sessionStateManagementService.getSessionState(testSessionId);
        SessionState.UserEngagement engagement = updatedState.getUserEngagement();
        
        assertNotNull(engagement);
        assertTrue(engagement.getAverageResponseLength() > 0);
        assertTrue(engagement.getQuestionsAsked() >= 2); // Turn 2 and 3 have questions
        assertTrue(engagement.isActiveParticipation());
        assertEquals("HIGH", engagement.getEngagementLevel()); // High due to long messages and questions
    }

    @Test
    @DisplayName("Should pause and resume sessions correctly")
    void testSessionPauseAndResume() {
        // Given
        SessionState sessionState = sessionStateManagementService.initializeSession(
            testSessionId, testUserId, testAgents);
        
        // Add some activity
        ConversationTurn turn = createTestTurn(1, "I need to pause this conversation", null);
        sessionStateManagementService.updateSessionWithTurn(testSessionId, turn);

        // When - Pause session
        SessionTransition pauseTransition = sessionStateManagementService.pauseSession(testSessionId, "User requested pause");
        
        // Then - Verify pause
        assertNotNull(pauseTransition);
        assertEquals(SessionState.SessionStatus.ACTIVE, pauseTransition.getFromStatus());
        assertEquals(SessionState.SessionStatus.PAUSED, pauseTransition.getToStatus());
        
        SessionState pausedState = sessionStateManagementService.getSessionState(testSessionId);
        assertEquals(SessionState.SessionStatus.PAUSED, pausedState.getStatus());
        assertFalse(pausedState.isActive());

        // When - Resume session
        SessionTransition resumeTransition = sessionStateManagementService.resumeSession(testSessionId);
        
        // Then - Verify resume
        assertNotNull(resumeTransition);
        assertEquals(SessionState.SessionStatus.PAUSED, resumeTransition.getFromStatus());
        assertEquals(SessionState.SessionStatus.ACTIVE, resumeTransition.getToStatus());
        
        SessionState resumedState = sessionStateManagementService.getSessionState(testSessionId);
        assertEquals(SessionState.SessionStatus.ACTIVE, resumedState.getStatus());
        assertTrue(resumedState.isActive());
    }

    @Test
    @DisplayName("Should end session with proper cleanup")
    void testSessionEnd() {
        // Given
        SessionState sessionState = sessionStateManagementService.initializeSession(
            testSessionId, testUserId, testAgents);
        
        // Add some conversation activity
        for (int i = 1; i <= 5; i++) {
            ConversationTurn turn = createTestTurn(i, "Message " + i, null);
            sessionStateManagementService.updateSessionWithTurn(testSessionId, turn);
        }

        // When
        SessionTransition endTransition = sessionStateManagementService.endSession(
            testSessionId, "User completed consultation", "Resolved");

        // Then
        assertNotNull(endTransition);
        assertEquals(SessionState.SessionStatus.ACTIVE, endTransition.getFromStatus());
        assertEquals(SessionState.SessionStatus.ENDED, endTransition.getToStatus());
        assertTrue(endTransition.isSuccessful());
        
        SessionState endedState = sessionStateManagementService.getSessionState(testSessionId);
        assertEquals(SessionState.SessionStatus.ENDED, endedState.getStatus());
        assertFalse(endedState.isActive());
        assertNotNull(endedState.getClosureInfo());
        assertEquals("Resolved", endedState.getClosureInfo().getOutcome());
        assertTrue(endedState.getDurationMinutes() > 0);
    }

    @Test
    @DisplayName("Should calculate session quality correctly")
    void testSessionQualityCalculation() {
        // Given
        SessionState sessionState = sessionStateManagementService.initializeSession(
            testSessionId, testUserId, testAgents);

        // Add high-quality turns
        ConversationTurn turn1 = createTestTurn(1, "I have specific symptoms", null);
        turn1.setResponseQuality(0.9);
        ConversationTurn turn2 = createTestTurn(2, null, "Detailed medical response");
        turn2.setResponseQuality(0.8);
        turn2.setRoutedAgent("GENERAL_HEALTH");

        // When
        sessionStateManagementService.updateSessionWithTurn(testSessionId, turn1);
        sessionStateManagementService.updateSessionWithTurn(testSessionId, turn2);

        // Then
        SessionState updatedState = sessionStateManagementService.getSessionState(testSessionId);
        SessionState.SessionQuality quality = updatedState.getSessionQuality();
        
        assertNotNull(quality);
        assertTrue(quality.getOverallQuality() > 0.5);
        assertTrue(quality.getResponseQualityAverage() > 0.7);
        assertTrue(quality.getContextUtilizationScore() >= 0.0);
        assertEquals("GOOD", quality.getQualityLevel());
    }

    @Test
    @DisplayName("Should track cross-session continuity")
    void testCrossSessionContinuity() {
        // Given
        SessionState sessionState1 = sessionStateManagementService.initializeSession(
            testSessionId, testUserId, testAgents);
        
        // Complete first session
        ConversationTurn turn1 = createTestTurn(1, "I have headaches", null);
        sessionStateManagementService.updateSessionWithTurn(testSessionId, turn1);
        sessionStateManagementService.endSession(testSessionId, "Session completed", "Advised to monitor");

        // When - Start new session
        String newSessionId = "test-session-456";
        SessionState sessionState2 = sessionStateManagementService.initializeSession(
            newSessionId, testUserId, testAgents);

        // Then - Check continuity data
        CrossSessionContinuity continuity = sessionStateManagementService.getCrossSessionContinuity(testUserId);
        assertNotNull(continuity);
        assertEquals(testUserId, continuity.getUserId());
        assertTrue(continuity.getRelatedSessions().size() >= 1);
        
        // Check that first session is in related sessions
        boolean foundPreviousSession = continuity.getRelatedSessions().stream()
            .anyMatch(summary -> testSessionId.equals(summary.getSessionId()));
        assertTrue(foundPreviousSession);
    }

    @Test
    @DisplayName("Should handle concurrent session operations")
    void testConcurrentSessionOperations() throws InterruptedException {
        // Given
        SessionState sessionState = sessionStateManagementService.initializeSession(
            testSessionId, testUserId, testAgents);

        // When - Concurrent updates
        Thread thread1 = new Thread(() -> {
            for (int i = 1; i <= 5; i++) {
                ConversationTurn turn = createTestTurn(i, "Thread1 Message " + i, null);
                sessionStateManagementService.updateSessionWithTurn(testSessionId, turn);
            }
        });

        Thread thread2 = new Thread(() -> {
            for (int i = 6; i <= 10; i++) {
                ConversationTurn turn = createTestTurn(i, "Thread2 Message " + i, null);
                sessionStateManagementService.updateSessionWithTurn(testSessionId, turn);
            }
        });

        thread1.start();
        thread2.start();
        thread1.join();
        thread2.join();

        // Then - Verify consistent final state
        SessionState finalState = sessionStateManagementService.getSessionState(testSessionId);
        assertEquals(10, finalState.getTotalTurns());
        assertEquals(SessionState.SessionStatus.ACTIVE, finalState.getStatus());
    }

    @Test
    @DisplayName("Should archive old sessions automatically")
    void testSessionArchival() {
        // Given - Create old session
        SessionState oldSession = sessionStateManagementService.initializeSession(
            testSessionId, testUserId, testAgents);
        
        // Simulate old session with many turns
        for (int i = 1; i <= 100; i++) {
            ConversationTurn turn = createTestTurn(i, "Message " + i, null);
            sessionStateManagementService.updateSessionWithTurn(testSessionId, turn);
        }
        
        sessionStateManagementService.endSession(testSessionId, "Long session completed", "Resolved");

        // When - Trigger archival check
        sessionStateManagementService.performArchivalCheck(testUserId);

        // Then - Session should be marked for archival
        SessionState archivedState = sessionStateManagementService.getSessionState(testSessionId);
        if (archivedState.getTotalTurns() >= 50) { // Assuming archival threshold
            assertEquals(SessionState.SessionStatus.ARCHIVED, archivedState.getStatus());
        }
    }

    @Test
    @DisplayName("Should calculate session effectiveness")
    void testSessionEffectivenessCalculation() {
        // Given
        SessionState sessionState = sessionStateManagementService.initializeSession(
            testSessionId, testUserId, testAgents);

        // Add effective conversation turns
        ConversationTurn turn1 = createTestTurn(1, "Specific medical question", null);
        turn1.setResponseQuality(0.8);
        turn1.setContextUtilization(0.9);
        
        ConversationTurn turn2 = createTestTurn(2, null, "Helpful medical advice");
        turn2.setResponseQuality(0.9);
        turn2.setRoutedAgent("GENERAL_HEALTH");

        // When
        sessionStateManagementService.updateSessionWithTurn(testSessionId, turn1);
        sessionStateManagementService.updateSessionWithTurn(testSessionId, turn2);
        sessionStateManagementService.endSession(testSessionId, "Effective consultation", "Problem resolved");

        // Then
        SessionState finalState = sessionStateManagementService.getSessionState(testSessionId);
        double effectiveness = sessionStateManagementService.calculateSessionEffectiveness(testSessionId);
        
        assertTrue(effectiveness > 0.5);
        assertTrue(effectiveness <= 1.0);
        assertEquals("Problem resolved", finalState.getClosureInfo().getOutcome());
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
} 