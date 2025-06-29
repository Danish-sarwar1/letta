package com.health.agents.service;

import com.health.agents.config.MemoryArchitectureConfig;
import com.health.agents.model.dto.ConversationTurn;
import com.health.agents.model.dto.ConversationHistory;
import com.health.agents.model.dto.ContextEnrichmentResult;
import com.health.agents.model.dto.ContextSelectionResult;
import com.health.agents.model.dto.ConversationPatterns;
import com.health.agents.model.dto.ConversationTrends;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Context Enrichment Service for Sub-Plan 3: Enhanced Context Extraction
 * Implements sophisticated context selection algorithms and historical pattern recognition
 */
@Service
@Slf4j
public class ContextEnrichmentService {
    
    @Autowired
    private MemoryArchitectureConfig memoryConfig;
    
    @Autowired
    private ConversationTurnService conversationTurnService;
    
    // Medical keywords for context analysis
    private static final Set<String> MEDICAL_KEYWORDS = Set.of(
        "pain", "headache", "fever", "symptom", "medication", "doctor", "hospital", 
        "treatment", "diagnosis", "prescription", "illness", "disease", "injury",
        "blood", "pressure", "heart", "chest", "stomach", "back", "leg", "arm"
    );
    
    // Emotional keywords for context analysis
    private static final Set<String> EMOTIONAL_KEYWORDS = Set.of(
        "anxious", "worried", "stressed", "depressed", "happy", "sad", "angry",
        "frustrated", "scared", "nervous", "calm", "relaxed", "upset", "mood"
    );
    
    // Follow-up indicators
    private static final Set<String> FOLLOWUP_INDICATORS = Set.of(
        "still", "again", "more", "worse", "better", "continue", "update",
        "follow", "now", "today", "yesterday", "since", "after"
    );
    
    /**
     * Enhanced context enrichment with sophisticated selection algorithms
     */
    public ContextEnrichmentResult enrichContext(String sessionId, String currentMessage, int currentTurnNumber) {
        log.debug("Enriching context for session {} turn {}: {}", sessionId, currentTurnNumber, currentMessage);
        
        try {
            ConversationHistory history = conversationTurnService.getConversationHistory(sessionId);
            if (history == null || history.getTurns().isEmpty()) {
                return createInitialContext(currentMessage, sessionId);
            }
            
            // Apply context selection strategy
            ContextSelectionResult selection = selectRelevantContext(history, currentMessage, currentTurnNumber);
            
            // Generate enriched context using selected turns
            String enrichedContext = generateEnrichedContext(selection, currentMessage, sessionId);
            
            // Analyze conversation patterns
            ConversationPatterns patterns = analyzeConversationPatterns(history, currentMessage);
            
            // Create comprehensive context enrichment result
            return ContextEnrichmentResult.builder()
                .enrichedMessage(enrichedContext)
                .contextUsed(selection.getContextDescription())
                .reasoning(selection.getSelectionReasoning())
                .relevantTurns(selection.getSelectedTurns().size())
                .conversationPatterns(patterns)
                .contextConfidence(selection.getConfidenceScore())
                .build();
            
        } catch (Exception e) {
            log.error("Failed to enrich context for session {}: {}", sessionId, e.getMessage(), e);
            return createFallbackContext(currentMessage, sessionId);
        }
    }
    
    /**
     * Advanced context selection using multiple strategies
     */
    private ContextSelectionResult selectRelevantContext(ConversationHistory history, 
                                                        String currentMessage, int currentTurnNumber) {
        List<ConversationTurn> allTurns = history.getTurns();
        Map<ConversationTurn, Double> turnRelevanceScores = new HashMap<>();
        
        // Strategy 1: Recent context (highest priority)
        List<ConversationTurn> recentTurns = getRecentTurns(allTurns, currentTurnNumber);
        for (ConversationTurn turn : recentTurns) {
            double score = calculateRecentContextScore(turn, currentTurnNumber);
            turnRelevanceScores.put(turn, score * 1.0); // Base weight
        }
        
        // Strategy 2: Topic-based context selection
        List<ConversationTurn> topicRelevantTurns = getTopicRelevantTurns(allTurns, currentMessage);
        for (ConversationTurn turn : topicRelevantTurns) {
            double score = calculateTopicRelevanceScore(turn, currentMessage);
            turnRelevanceScores.merge(turn, score * 0.8, Double::max); // Topic weight
        }
        
        // Strategy 3: Medical context prioritization
        List<ConversationTurn> medicalTurns = getMedicalRelevantTurns(allTurns, currentMessage);
        for (ConversationTurn turn : medicalTurns) {
            double score = calculateMedicalRelevanceScore(turn, currentMessage);
            turnRelevanceScores.merge(turn, score * 0.9, Double::max); // Medical weight
        }
        
        // Strategy 4: Emotional context continuity
        List<ConversationTurn> emotionalTurns = getEmotionallyRelevantTurns(allTurns, currentMessage);
        for (ConversationTurn turn : emotionalTurns) {
            double score = calculateEmotionalRelevanceScore(turn, currentMessage);
            turnRelevanceScores.merge(turn, score * 0.7, Double::max); // Emotional weight
        }
        
        // Strategy 5: Follow-up context detection
        List<ConversationTurn> followUpTurns = getFollowUpRelevantTurns(allTurns, currentMessage);
        for (ConversationTurn turn : followUpTurns) {
            double score = calculateFollowUpRelevanceScore(turn, currentMessage);
            turnRelevanceScores.merge(turn, score * 0.95, Double::max); // Follow-up weight
        }
        
        // Select top relevant turns based on scores
        List<ConversationTurn> selectedTurns = turnRelevanceScores.entrySet().stream()
            .sorted(Map.Entry.<ConversationTurn, Double>comparingByValue().reversed())
            .limit(getMaxRelevantTurns())
            .map(Map.Entry::getKey)
            .sorted(Comparator.comparing(ConversationTurn::getTurnNumber)) // Maintain chronological order
            .collect(Collectors.toList());
        
        // Calculate overall confidence score
        double averageScore = turnRelevanceScores.values().stream()
            .mapToDouble(Double::doubleValue)
            .average()
            .orElse(0.5);
        
        // Generate selection reasoning
        String reasoning = generateSelectionReasoning(selectedTurns, turnRelevanceScores, currentMessage);
        
        return ContextSelectionResult.builder()
            .selectedTurns(selectedTurns)
            .confidenceScore(Math.min(averageScore, 1.0))
            .selectionReasoning(reasoning)
            .contextDescription(formatContextDescription(selectedTurns))
            .build();
    }
    
    /**
     * Generate enriched context message using selected turns
     */
    private String generateEnrichedContext(ContextSelectionResult selection, String currentMessage, String sessionId) {
        StringBuilder enrichedContext = new StringBuilder();
        
        // Current message
        enrichedContext.append("CURRENT_MESSAGE: ").append(currentMessage).append("\n\n");
        
        // Relevant context from selected turns
        if (!selection.getSelectedTurns().isEmpty()) {
            enrichedContext.append("RELEVANT_CONTEXT: ");
            for (ConversationTurn turn : selection.getSelectedTurns()) {
                enrichedContext.append(formatTurnForContext(turn)).append(" ");
            }
            enrichedContext.append("\n\n");
        }
        
        // Topic context
        String topicContext = extractTopicContext(selection.getSelectedTurns(), currentMessage);
        enrichedContext.append("TOPIC_CONTEXT: ").append(topicContext).append("\n\n");
        
        // Medical context
        String medicalContext = extractMedicalContext(selection.getSelectedTurns(), currentMessage);
        enrichedContext.append("MEDICAL_CONTEXT: ").append(medicalContext).append("\n\n");
        
        // Emotional context
        String emotionalContext = extractEmotionalContext(selection.getSelectedTurns(), currentMessage);
        enrichedContext.append("EMOTIONAL_CONTEXT: ").append(emotionalContext).append("\n\n");
        
        // Conversation flow context
        String flowContext = extractConversationFlowContext(selection.getSelectedTurns(), currentMessage);
        enrichedContext.append("CONVERSATION_FLOW: ").append(flowContext).append("\n\n");
        
        // Context confidence and reasoning
        enrichedContext.append("CONTEXT_CONFIDENCE: ").append(String.format("%.2f", selection.getConfidenceScore()))
                      .append("\nCONTEXT_REASONING: ").append(selection.getSelectionReasoning());
        
        return enrichedContext.toString();
    }
    
    /**
     * Analyze conversation patterns for better context understanding
     */
    private ConversationPatterns analyzeConversationPatterns(ConversationHistory history, String currentMessage) {
        List<ConversationTurn> turns = history.getTurns();
        
        // Analyze topic patterns
        Map<String, Integer> topicFrequency = new HashMap<>();
        List<String> topicProgression = new ArrayList<>();
        
        // Analyze agent routing patterns
        Map<String, Integer> agentRouting = new HashMap<>();
        
        // Analyze temporal patterns
        List<LocalDateTime> timestamps = new ArrayList<>();
        
        // Analyze emotional patterns
        List<String> emotionalProgression = new ArrayList<>();
        
        for (ConversationTurn turn : turns) {
            // Topic analysis
            if (turn.getTopicTags() != null && !turn.getTopicTags().isEmpty()) {
                topicFrequency.merge(turn.getTopicTags(), 1, Integer::sum);
                topicProgression.add(turn.getTopicTags());
            }
            
            // Agent routing analysis
            if (turn.getRoutedAgent() != null) {
                agentRouting.merge(turn.getRoutedAgent(), 1, Integer::sum);
            }
            
            // Temporal analysis
            if (turn.getTimestamp() != null) {
                timestamps.add(turn.getTimestamp());
            }
            
            // Emotional analysis
            if (turn.getEmotionalState() != null && !turn.getEmotionalState().isEmpty()) {
                emotionalProgression.add(turn.getEmotionalState());
            }
        }
        
        // Identify current conversation phase
        String conversationPhase = identifyConversationPhase(turns, currentMessage);
        
        // Detect conversation trends
        ConversationTrends trends = detectConversationTrends(turns, currentMessage);
        
        return ConversationPatterns.builder()
            .topicFrequency(topicFrequency)
            .topicProgression(topicProgression)
            .agentRoutingPatterns(agentRouting)
            .emotionalProgression(emotionalProgression)
            .conversationPhase(conversationPhase)
            .conversationTrends(trends)
            .totalTurns(turns.size())
            .sessionDuration(calculateSessionDuration(timestamps))
            .build();
    }
    
    /**
     * Get recent turns based on configured window
     */
    private List<ConversationTurn> getRecentTurns(List<ConversationTurn> allTurns, int currentTurnNumber) {
        int windowSize = getRecentTurnsWindow();
        int startIndex = Math.max(0, allTurns.size() - windowSize);
        return allTurns.subList(startIndex, allTurns.size());
    }
    
    /**
     * Calculate relevance score for recent context
     */
    private double calculateRecentContextScore(ConversationTurn turn, int currentTurnNumber) {
        int turnDistance = currentTurnNumber - turn.getTurnNumber();
        return Math.max(0.1, 1.0 - (turnDistance * 0.1)); // Decay with distance
    }
    
    /**
     * Get topic-relevant turns using keyword matching and semantic similarity
     */
    private List<ConversationTurn> getTopicRelevantTurns(List<ConversationTurn> allTurns, String currentMessage) {
        String messageLower = currentMessage.toLowerCase();
        Set<String> messageKeywords = extractKeywords(messageLower);
        
        return allTurns.stream()
            .filter(turn -> isTopicRelevant(turn, messageLower, messageKeywords))
            .collect(Collectors.toList());
    }
    
    /**
     * Calculate topic relevance score
     */
    private double calculateTopicRelevanceScore(ConversationTurn turn, String currentMessage) {
        Set<String> currentKeywords = extractKeywords(currentMessage.toLowerCase());
        Set<String> turnKeywords = extractKeywords(getTurnText(turn).toLowerCase());
        
        // Calculate keyword overlap
        Set<String> intersection = new HashSet<>(currentKeywords);
        intersection.retainAll(turnKeywords);
        
        if (currentKeywords.isEmpty() || turnKeywords.isEmpty()) {
            return 0.1;
        }
        
        double jaccardSimilarity = (double) intersection.size() / 
            (currentKeywords.size() + turnKeywords.size() - intersection.size());
        
        // Boost score for topic tags match
        if (turn.getTopicTags() != null && containsRelevantTopicTags(turn.getTopicTags(), currentMessage)) {
            jaccardSimilarity += 0.3;
        }
        
        return Math.min(1.0, jaccardSimilarity);
    }
    
    /**
     * Get medically relevant turns
     */
    private List<ConversationTurn> getMedicalRelevantTurns(List<ConversationTurn> allTurns, String currentMessage) {
        Set<String> medicalKeywords = getMedicalKeywords(currentMessage);
        
        return allTurns.stream()
            .filter(turn -> containsMedicalContent(turn, medicalKeywords))
            .collect(Collectors.toList());
    }
    
    /**
     * Calculate medical relevance score
     */
    private double calculateMedicalRelevanceScore(ConversationTurn turn, String currentMessage) {
        Set<String> currentMedicalKeywords = getMedicalKeywords(currentMessage);
        Set<String> turnMedicalKeywords = getMedicalKeywords(getTurnText(turn));
        
        if (currentMedicalKeywords.isEmpty()) {
            return 0.1;
        }
        
        Set<String> intersection = new HashSet<>(currentMedicalKeywords);
        intersection.retainAll(turnMedicalKeywords);
        
        return Math.min(1.0, (double) intersection.size() / currentMedicalKeywords.size());
    }
    
    /**
     * Get emotionally relevant turns
     */
    private List<ConversationTurn> getEmotionallyRelevantTurns(List<ConversationTurn> allTurns, String currentMessage) {
        Set<String> emotionalKeywords = getEmotionalKeywords(currentMessage);
        
        return allTurns.stream()
            .filter(turn -> containsEmotionalContent(turn, emotionalKeywords))
            .collect(Collectors.toList());
    }
    
    /**
     * Calculate emotional relevance score
     */
    private double calculateEmotionalRelevanceScore(ConversationTurn turn, String currentMessage) {
        Set<String> currentEmotionalKeywords = getEmotionalKeywords(currentMessage);
        Set<String> turnEmotionalKeywords = getEmotionalKeywords(getTurnText(turn));
        
        if (currentEmotionalKeywords.isEmpty()) {
            return 0.1;
        }
        
        Set<String> intersection = new HashSet<>(currentEmotionalKeywords);
        intersection.retainAll(turnEmotionalKeywords);
        
        return Math.min(1.0, (double) intersection.size() / currentEmotionalKeywords.size());
    }
    
    /**
     * Get follow-up relevant turns
     */
    private List<ConversationTurn> getFollowUpRelevantTurns(List<ConversationTurn> allTurns, String currentMessage) {
        return allTurns.stream()
            .filter(turn -> isFollowUpRelevant(turn, currentMessage))
            .collect(Collectors.toList());
    }
    
    /**
     * Calculate follow-up relevance score
     */
    private double calculateFollowUpRelevanceScore(ConversationTurn turn, String currentMessage) {
        String messageLower = currentMessage.toLowerCase();
        
        // Check for explicit follow-up indicators
        long followUpCount = FOLLOWUP_INDICATORS.stream()
            .mapToLong(indicator -> messageLower.contains(indicator) ? 1 : 0)
            .sum();
        
        if (followUpCount == 0) {
            return 0.1;
        }
        
        // Simple recency score based on turn number (higher turn numbers are more recent)
        double recencyScore = 1.0 / (Math.max(1, turn.getTurnNumber()));
        
        return Math.min(1.0, (followUpCount * 0.3) + recencyScore);
    }
    
    /**
     * Extract topic context from selected turns
     */
    private String extractTopicContext(List<ConversationTurn> selectedTurns, String currentMessage) {
        if (selectedTurns.isEmpty()) {
            return "New conversation topic";
        }
        
        // Identify main topics from selected turns
        Map<String, Integer> topicCounts = new HashMap<>();
        for (ConversationTurn turn : selectedTurns) {
            if (turn.getTopicTags() != null && !turn.getTopicTags().isEmpty()) {
                topicCounts.merge(turn.getTopicTags(), 1, Integer::sum);
            }
        }
        
        if (topicCounts.isEmpty()) {
            return "General health discussion";
        }
        
        String mainTopic = topicCounts.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse("General health discussion");
        
        return String.format("Continuing discussion about %s (referenced in %d previous turns)", 
            mainTopic, topicCounts.get(mainTopic));
    }
    
    /**
     * Extract medical context from selected turns
     */
    private String extractMedicalContext(List<ConversationTurn> selectedTurns, String currentMessage) {
        Set<String> medicalTerms = new HashSet<>();
        
        // Extract medical terms from current message
        medicalTerms.addAll(getMedicalKeywords(currentMessage));
        
        // Extract medical terms from selected turns
        for (ConversationTurn turn : selectedTurns) {
            medicalTerms.addAll(getMedicalKeywords(getTurnText(turn)));
        }
        
        if (medicalTerms.isEmpty()) {
            return "No specific medical context";
        }
        
        return "Medical context includes: " + String.join(", ", medicalTerms);
    }
    
    /**
     * Extract emotional context from selected turns
     */
    private String extractEmotionalContext(List<ConversationTurn> selectedTurns, String currentMessage) {
        Set<String> emotionalTerms = new HashSet<>();
        
        // Extract emotional terms from current message
        emotionalTerms.addAll(getEmotionalKeywords(currentMessage));
        
        // Extract emotional terms from selected turns
        for (ConversationTurn turn : selectedTurns) {
            emotionalTerms.addAll(getEmotionalKeywords(getTurnText(turn)));
        }
        
        if (emotionalTerms.isEmpty()) {
            return "Neutral emotional state";
        }
        
        return "Emotional context includes: " + String.join(", ", emotionalTerms);
    }
    
    /**
     * Extract conversation flow context
     */
    private String extractConversationFlowContext(List<ConversationTurn> selectedTurns, String currentMessage) {
        if (selectedTurns.isEmpty()) {
            return "Starting new conversation";
        }
        
        // Analyze flow patterns
        boolean hasFollowUp = FOLLOWUP_INDICATORS.stream()
            .anyMatch(indicator -> currentMessage.toLowerCase().contains(indicator));
        
        if (hasFollowUp) {
            return String.format("Following up on previous discussion (referencing %d recent turns)", 
                selectedTurns.size());
        }
        
        // Check for topic continuity
        ConversationTurn lastTurn = selectedTurns.get(selectedTurns.size() - 1);
        Set<String> lastTurnKeywords = extractKeywords(getTurnText(lastTurn));
        Set<String> currentKeywords = extractKeywords(currentMessage);
        
        lastTurnKeywords.retainAll(currentKeywords);
        if (!lastTurnKeywords.isEmpty()) {
            return "Continuing related topic from previous discussion";
        }
        
        return "New topic in ongoing conversation";
    }
    
    /**
     * Extract keywords from text
     */
    private Set<String> extractKeywords(String text) {
        return Arrays.stream(text.toLowerCase().split("\\W+"))
            .filter(word -> word.length() > 2)
            .filter(word -> !isStopWord(word))
            .collect(Collectors.toSet());
    }
    
    /**
     * Check if word is a stop word
     */
    private boolean isStopWord(String word) {
        Set<String> stopWords = Set.of("the", "and", "but", "for", "are", "you", "have", "that", "with");
        return stopWords.contains(word);
    }
    
    /**
     * Get medical keywords from text
     */
    private Set<String> getMedicalKeywords(String text) {
        return MEDICAL_KEYWORDS.stream()
            .filter(keyword -> text.toLowerCase().contains(keyword))
            .collect(Collectors.toSet());
    }
    
    /**
     * Get emotional keywords from text  
     */
    private Set<String> getEmotionalKeywords(String text) {
        return EMOTIONAL_KEYWORDS.stream()
            .filter(keyword -> text.toLowerCase().contains(keyword))
            .collect(Collectors.toSet());
    }
    
    /**
     * Get combined text from a turn
     */
    private String getTurnText(ConversationTurn turn) {
        StringBuilder text = new StringBuilder();
        if (turn.getUserMessage() != null) {
            text.append(turn.getUserMessage()).append(" ");
        }
        if (turn.getAgentResponse() != null) {
            text.append(turn.getAgentResponse()).append(" ");
        }
        return text.toString();
    }
    
    /**
     * Check if turn is topic relevant
     */
    private boolean isTopicRelevant(ConversationTurn turn, String messageLower, Set<String> messageKeywords) {
        String turnText = getTurnText(turn).toLowerCase();
        Set<String> turnKeywords = extractKeywords(turnText);
        
        // Check keyword overlap
        Set<String> intersection = new HashSet<>(messageKeywords);
        intersection.retainAll(turnKeywords);
        
        return !intersection.isEmpty() || containsRelevantTopicTags(turn.getTopicTags(), messageLower);
    }
    
    /**
     * Check if turn contains relevant topic tags
     */
    private boolean containsRelevantTopicTags(String topicTags, String message) {
        if (topicTags == null || topicTags.isEmpty()) {
            return false;
        }
        return message.toLowerCase().contains(topicTags.toLowerCase());
    }
    
    /**
     * Check if turn contains medical content
     */
    private boolean containsMedicalContent(ConversationTurn turn, Set<String> medicalKeywords) {
        if (medicalKeywords.isEmpty()) {
            return false;
        }
        
        String turnText = getTurnText(turn).toLowerCase();
        return medicalKeywords.stream().anyMatch(turnText::contains);
    }
    
    /**
     * Check if turn contains emotional content
     */
    private boolean containsEmotionalContent(ConversationTurn turn, Set<String> emotionalKeywords) {
        if (emotionalKeywords.isEmpty()) {
            return false;
        }
        
        String turnText = getTurnText(turn).toLowerCase();
        return emotionalKeywords.stream().anyMatch(turnText::contains);
    }
    
    /**
     * Check if turn is follow-up relevant
     */
    private boolean isFollowUpRelevant(ConversationTurn turn, String currentMessage) {
        String messageLower = currentMessage.toLowerCase();
        return FOLLOWUP_INDICATORS.stream().anyMatch(messageLower::contains);
    }
    
    /**
     * Identify current conversation phase
     */
    private String identifyConversationPhase(List<ConversationTurn> turns, String currentMessage) {
        if (turns.size() <= 2) {
            return "Initial Assessment";
        } else if (turns.size() <= 5) {
            return "Information Gathering";  
        } else if (turns.size() <= 10) {
            return "Active Discussion";
        } else {
            return "Extended Consultation";
        }
    }
    
    /**
     * Detect conversation trends
     */
    private ConversationTrends detectConversationTrends(List<ConversationTurn> turns, String currentMessage) {
        // Simple trend analysis - could be enhanced with ML
        boolean increasingComplexity = turns.size() > 3 && 
            turns.get(turns.size()-1).getUserMessage().length() > turns.get(0).getUserMessage().length();
        
        boolean topicShift = turns.size() > 1 && 
            !extractKeywords(currentMessage).isEmpty();
        
        return ConversationTrends.builder()
            .increasingComplexity(increasingComplexity)
            .topicShift(topicShift)
            .conversationDepth(turns.size())
            .build();
    }
    
    /**
     * Calculate session duration
     */
    private String calculateSessionDuration(List<LocalDateTime> timestamps) {
        if (timestamps.size() < 2) {
            return "Less than 1 minute";
        }
        
        timestamps.sort(LocalDateTime::compareTo);
        long minutes = ChronoUnit.MINUTES.between(timestamps.get(0), timestamps.get(timestamps.size()-1));
        
        if (minutes < 1) {
            return "Less than 1 minute";
        } else if (minutes < 60) {
            return minutes + " minutes";
        } else {
            return (minutes / 60) + " hours " + (minutes % 60) + " minutes";
        }
    }
    
    /**
     * Format turn for context display
     */
    private String formatTurnForContext(ConversationTurn turn) {
        return String.format("Turn %d: %s", turn.getTurnNumber(), 
            turn.getUserMessage() != null ? turn.getUserMessage() : "");
    }
    
    /**
     * Format context description
     */
    private String formatContextDescription(List<ConversationTurn> selectedTurns) {
        if (selectedTurns.isEmpty()) {
            return "No previous context";
        }
        
        return String.format("Selected %d relevant turns for context enrichment", selectedTurns.size());
    }
    
    /**
     * Generate selection reasoning
     */
    private String generateSelectionReasoning(List<ConversationTurn> selectedTurns, 
                                            Map<ConversationTurn, Double> scores, String currentMessage) {
        if (selectedTurns.isEmpty()) {
            return "No relevant context found";
        }
        
        double avgScore = selectedTurns.stream()
            .mapToDouble(turn -> scores.getOrDefault(turn, 0.0))
            .average()
            .orElse(0.0);
        
        return String.format("Selected %d turns with average relevance score %.2f based on topic, medical, and temporal relevance", 
            selectedTurns.size(), avgScore);
    }
    
    /**
     * Create initial context for new conversations
     */
    private ContextEnrichmentResult createInitialContext(String currentMessage, String sessionId) {
        return ContextEnrichmentResult.builder()
            .enrichedMessage(String.format("CURRENT_MESSAGE: %s\n\nRELEVANT_CONTEXT: Starting new health consultation session\n\nTOPIC_CONTEXT: Initial health inquiry\n\nMEDICAL_CONTEXT: %s\n\nEMOTIONAL_CONTEXT: Beginning of conversation\n\nCONVERSATION_FLOW: New session initialization", 
                currentMessage, extractMedicalContext(Collections.emptyList(), currentMessage)))
            .contextUsed("Initial session context")
            .reasoning("New conversation - no previous context available")
            .relevantTurns(0)
            .contextConfidence(0.8)
            .build();
    }
    
    /**
     * Create fallback context when enrichment fails
     */
    private ContextEnrichmentResult createFallbackContext(String currentMessage, String sessionId) {
        return ContextEnrichmentResult.builder()
            .enrichedMessage(String.format("CURRENT_MESSAGE: %s\n\nRELEVANT_CONTEXT: Context enrichment temporarily unavailable\n\nTOPIC_CONTEXT: General health discussion\n\nMEDICAL_CONTEXT: Basic processing\n\nEMOTIONAL_CONTEXT: Neutral\n\nCONVERSATION_FLOW: Continuing conversation", currentMessage))
            .contextUsed("Fallback context due to processing error")
            .reasoning("Context enrichment failed - using basic processing")
            .relevantTurns(0)
            .contextConfidence(0.3)
            .build();
    }
    
    /**
     * Get max relevant turns
     */
    private int getMaxRelevantTurns() {
        return memoryConfig != null ? memoryConfig.getMaxRelevantTurns() : 10;
    }
    
    /**
     * Get recent turns window
     */
    private int getRecentTurnsWindow() {
        return memoryConfig != null ? memoryConfig.getRecentTurnsWindow() : 5;
    }
} 