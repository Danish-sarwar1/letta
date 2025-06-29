package com.health.agents.service;

import com.health.agents.config.MemoryArchitectureConfig;
import com.health.agents.model.dto.ConversationHistory;
import com.health.agents.model.dto.ConversationTurn;
import com.health.agents.model.dto.ContextEnrichmentResult;
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
 * Sub-Plan 6: Unit Tests for Context Enrichment
 * Tests enhanced context extraction strategies, pattern recognition, and relevance scoring
 */
@SpringBootTest
@DisplayName("Context Enrichment Tests")
class ContextEnrichmentTest {

    @Mock
    private LettaAgentService lettaAgentService;

    @Mock
    private ConversationTurnService conversationTurnService;

    @Mock
    private MemoryArchitectureConfig memoryConfig;

    @InjectMocks
    private ContextEnrichmentService contextEnrichmentService;

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
        when(memoryConfig.getRecentTurnsWindow()).thenReturn(5);
        when(memoryConfig.getMaxRelevantTurns()).thenReturn(10);
        
        MemoryArchitectureConfig.ContextWindow contextWindow = new MemoryArchitectureConfig.ContextWindow();
        contextWindow.setRecentTurns(5);
        contextWindow.setMaxRelevant(10);
        when(memoryConfig.getContextWindow()).thenReturn(contextWindow);
    }

    @Test
    @DisplayName("Should enrich context for simple user message")
    void testBasicContextEnrichment() throws Exception {
        // Given
        String userMessage = "I have been experiencing headaches for the past few days";
        ConversationHistory history = createTestHistory(Arrays.asList(
            createTestTurn(1, "I feel tired lately", null)
        ));
        
        when(conversationTurnService.getConversationHistory(testSessionId)).thenReturn(history);

        // When
        ContextEnrichmentResult result = contextEnrichmentService.enrichContext(
            testSessionId, userMessage, 2);

        // Then
        assertNotNull(result);
        assertNotNull(result.getEnrichedContext());
        assertTrue(result.getContextConfidence() >= 0.0);
        assertTrue(result.getContextConfidence() <= 1.0);
        assertTrue(result.getRelevantTurns() >= 0);
        assertNotNull(result.getContextStrategy());
        assertNotNull(result.getTimestamp());
        assertTrue(result.getEnrichedContext().contains("headaches"));
    }

    @Test
    @DisplayName("Should handle context enrichment with no conversation history")
    void testContextEnrichmentWithNoHistory() throws Exception {
        // Given
        String userMessage = "Hello, I need help with my health";
        when(conversationTurnService.getConversationHistory(testSessionId)).thenReturn(null);

        // When
        ContextEnrichmentResult result = contextEnrichmentService.enrichContext(
            testSessionId, userMessage, 1);

        // Then
        assertNotNull(result);
        assertNotNull(result.getEnrichedContext());
        assertTrue(result.getContextConfidence() >= 0.0);
        assertEquals(0, result.getRelevantTurns());
        assertEquals("NO_HISTORY", result.getContextStrategy());
        assertTrue(result.getEnrichedContext().contains("health"));
    }

    @Test
    @DisplayName("Should identify patterns in conversation history")
    void testPatternRecognition() throws Exception {
        // Given
        String userMessage = "My headache is getting worse";
        ConversationHistory history = createTestHistory(Arrays.asList(
            createTestTurn(1, "I have a headache", null),
            createTestTurn(2, null, "Tell me more about your headache symptoms"),
            createTestTurn(3, "The headache started this morning", null),
            createTestTurn(4, null, "Have you tried any pain relief?"),
            createTestTurn(5, "I took aspirin but it didn't help", null)
        ));
        history.getTurns().forEach(turn -> turn.setTopicTags("headache,pain,symptoms"));
        
        when(conversationTurnService.getConversationHistory(testSessionId)).thenReturn(history);

        // When
        ContextEnrichmentResult result = contextEnrichmentService.enrichContext(
            testSessionId, userMessage, 6);

        // Then
        assertNotNull(result);
        assertTrue(result.getContextConfidence() > 0.5);
        assertTrue(result.getRelevantTurns() > 0);
        assertEquals("PATTERN_BASED", result.getContextStrategy());
        assertTrue(result.getEnrichedContext().contains("pattern"));
        assertTrue(result.isPatternDetected());
    }

    @Test
    @DisplayName("Should calculate relevance scores correctly")
    void testRelevanceScoring() throws Exception {
        // Given
        String userMessage = "I'm feeling anxious about my symptoms";
        ConversationHistory history = createTestHistory(Arrays.asList(
            createTestTurn(1, "I have physical symptoms", null),
            createTestTurn(2, null, "Can you describe the symptoms?"),
            createTestTurn(3, "I'm worried about my health", null),
            createTestTurn(4, null, "It's normal to feel concerned"),
            createTestTurn(5, "I can't sleep well", null)
        ));
        
        when(conversationTurnService.getConversationHistory(testSessionId)).thenReturn(history);

        // When
        ContextEnrichmentResult result = contextEnrichmentService.enrichContext(
            testSessionId, userMessage, 6);

        // Then
        assertNotNull(result);
        assertTrue(result.getContextConfidence() > 0.3);
        assertTrue(result.getRelevantTurns() > 0);
        assertNotNull(result.getRelevanceScores());
        assertTrue(result.getRelevanceScores().size() > 0);
        
        // Check that relevance scores are properly calculated
        for (Double score : result.getRelevanceScores()) {
            assertTrue(score >= 0.0 && score <= 1.0);
        }
    }

    @Test
    @DisplayName("Should select appropriate context window size")
    void testContextWindowSelection() throws Exception {
        // Given
        String userMessage = "How can I manage my condition?";
        ConversationHistory longHistory = createTestHistory(createManyTurns(20));
        
        when(conversationTurnService.getConversationHistory(testSessionId)).thenReturn(longHistory);

        // When
        ContextEnrichmentResult result = contextEnrichmentService.enrichContext(
            testSessionId, userMessage, 21);

        // Then
        assertNotNull(result);
        assertTrue(result.getRelevantTurns() <= memoryConfig.getMaxRelevantTurns());
        assertNotNull(result.getContextStrategy());
        assertTrue(result.getContextStrategy().contains("WINDOW") || 
                  result.getContextStrategy().contains("RECENT"));
    }

    @Test
    @DisplayName("Should handle medical terminology appropriately")
    void testMedicalTerminologyHandling() throws Exception {
        // Given
        String userMessage = "I've been experiencing tachycardia and dyspnea";
        ConversationHistory history = createTestHistory(Arrays.asList(
            createTestTurn(1, "I have chest pain", null),
            createTestTurn(2, null, "Please describe your chest discomfort"),
            createTestTurn(3, "My heart feels like it's racing", null)
        ));
        
        when(conversationTurnService.getConversationHistory(testSessionId)).thenReturn(history);

        // When
        ContextEnrichmentResult result = contextEnrichmentService.enrichContext(
            testSessionId, userMessage, 4);

        // Then
        assertNotNull(result);
        assertTrue(result.getContextConfidence() > 0.0);
        assertTrue(result.getEnrichedContext().contains("tachycardia") || 
                  result.getEnrichedContext().contains("heart") ||
                  result.getEnrichedContext().contains("cardiac"));
    }

    @Test
    @DisplayName("Should prioritize recent context appropriately")
    void testRecentContextPrioritization() throws Exception {
        // Given
        String userMessage = "The medication isn't working";
        ConversationHistory history = createTestHistory(Arrays.asList(
            createTestTurn(1, "I had a headache last week", null), // Old, less relevant
            createTestTurn(2, null, "How are you feeling now?"),
            createTestTurn(3, "I started taking the prescribed medication", null), // Recent, more relevant
            createTestTurn(4, null, "Good, take it as directed"),
            createTestTurn(5, "I've been taking it for 3 days", null) // Most recent, most relevant
        ));
        
        // Set timestamps to make recency clear
        history.getTurns().get(0).setTimestamp(LocalDateTime.now().minusDays(7));
        history.getTurns().get(2).setTimestamp(LocalDateTime.now().minusDays(1));
        history.getTurns().get(4).setTimestamp(LocalDateTime.now().minusHours(1));
        
        when(conversationTurnService.getConversationHistory(testSessionId)).thenReturn(history);

        // When
        ContextEnrichmentResult result = contextEnrichmentService.enrichContext(
            testSessionId, userMessage, 6);

        // Then
        assertNotNull(result);
        assertTrue(result.getContextConfidence() > 0.4);
        assertTrue(result.getEnrichedContext().contains("medication"));
        assertEquals("RECENCY_WEIGHTED", result.getContextStrategy());
    }

    @Test
    @DisplayName("Should handle context enrichment errors gracefully")
    void testErrorHandling() throws Exception {
        // Given
        String userMessage = "Help me understand my condition";
        when(conversationTurnService.getConversationHistory(testSessionId))
            .thenThrow(new RuntimeException("Database connection failed"));

        // When
        ContextEnrichmentResult result = contextEnrichmentService.enrichContext(
            testSessionId, userMessage, 1);

        // Then
        assertNotNull(result);
        assertNotNull(result.getEnrichedContext());
        assertEquals("ERROR_FALLBACK", result.getContextStrategy());
        assertEquals(0, result.getRelevantTurns());
        assertTrue(result.getContextConfidence() >= 0.0);
        assertTrue(result.getEnrichedContext().contains("condition"));
    }

    @Test
    @DisplayName("Should detect emotional context correctly")
    void testEmotionalContextDetection() throws Exception {
        // Given
        String userMessage = "I'm really scared about what this could mean";
        ConversationHistory history = createTestHistory(Arrays.asList(
            createTestTurn(1, "I found a lump", null),
            createTestTurn(2, null, "I understand your concern"),
            createTestTurn(3, "I'm worried it could be serious", null),
            createTestTurn(4, null, "Let's focus on getting proper evaluation")
        ));
        
        when(conversationTurnService.getConversationHistory(testSessionId)).thenReturn(history);

        // When
        ContextEnrichmentResult result = contextEnrichmentService.enrichContext(
            testSessionId, userMessage, 5);

        // Then
        assertNotNull(result);
        assertTrue(result.getContextConfidence() > 0.3);
        assertTrue(result.isEmotionalContextDetected());
        assertNotNull(result.getEmotionalState());
        assertTrue(result.getEmotionalState().contains("ANXIETY") || 
                  result.getEmotionalState().contains("FEAR"));
    }

    @Test
    @DisplayName("Should optimize context for different agent types")
    void testAgentSpecificContextOptimization() throws Exception {
        // Given
        String userMessage = "I need help with my depression and sleep issues";
        ConversationHistory history = createTestHistory(Arrays.asList(
            createTestTurn(1, "I've been feeling down lately", null),
            createTestTurn(2, null, "How long have you been feeling this way?"),
            createTestTurn(3, "About two weeks, and I can't sleep", null)
        ));
        
        when(conversationTurnService.getConversationHistory(testSessionId)).thenReturn(history);

        // When - Enrich context for mental health agent
        ContextEnrichmentResult result = contextEnrichmentService.enrichContextForAgent(
            testSessionId, userMessage, 4, "MENTAL_HEALTH");

        // Then
        assertNotNull(result);
        assertTrue(result.getContextConfidence() > 0.4);
        assertEquals("AGENT_OPTIMIZED", result.getContextStrategy());
        assertTrue(result.getEnrichedContext().contains("depression") || 
                  result.getEnrichedContext().contains("mental health"));
    }

    @Test
    @DisplayName("Should maintain context consistency across turns")
    void testContextConsistency() throws Exception {
        // Given
        String[] userMessages = {
            "I have been having chest pain",
            "The pain happens when I exercise",
            "It's been going on for a week"
        };
        
        ConversationHistory history = createTestHistory(Arrays.asList());
        when(conversationTurnService.getConversationHistory(testSessionId)).thenReturn(history);

        // When - Enrich context for multiple turns in sequence
        ContextEnrichmentResult result1 = contextEnrichmentService.enrichContext(
            testSessionId, userMessages[0], 1);
        
        // Add the turn to history for next enrichment
        history.getTurns().add(createTestTurn(1, userMessages[0], null));
        
        ContextEnrichmentResult result2 = contextEnrichmentService.enrichContext(
            testSessionId, userMessages[1], 2);
        
        history.getTurns().add(createTestTurn(2, userMessages[1], null));
        
        ContextEnrichmentResult result3 = contextEnrichmentService.enrichContext(
            testSessionId, userMessages[2], 3);

        // Then - Verify increasing context confidence and consistency
        assertTrue(result2.getContextConfidence() > result1.getContextConfidence());
        assertTrue(result3.getContextConfidence() > result2.getContextConfidence());
        
        assertTrue(result2.getRelevantTurns() > 0);
        assertTrue(result3.getRelevantTurns() > result2.getRelevantTurns());
        
        // All should contain chest pain context
        assertTrue(result1.getEnrichedContext().contains("chest"));
        assertTrue(result2.getEnrichedContext().contains("chest"));
        assertTrue(result3.getEnrichedContext().contains("chest"));
    }

    // Helper methods

    private ConversationHistory createTestHistory(List<ConversationTurn> turns) {
        return ConversationHistory.builder()
            .sessionId(testSessionId)
            .userId(testUserId)
            .turns(turns)
            .totalTurns(turns.size())
            .status("ACTIVE")
            .createdAt(LocalDateTime.now().minusHours(1))
            .lastUpdated(LocalDateTime.now())
            .build();
    }

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