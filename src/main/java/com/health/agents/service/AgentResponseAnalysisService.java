package com.health.agents.service;

import com.health.agents.config.MemoryArchitectureConfig;
import com.health.agents.model.dto.AgentResponseMetadata;
import com.health.agents.model.dto.ConversationTurn;
import com.health.agents.model.dto.ConversationHistory;
import com.health.agents.model.dto.ContextEnrichmentResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Agent Response Analysis Service for Sub-Plan 4: Bidirectional Memory Updates
 * Analyzes agent responses to provide feedback for improving context selection and memory updates
 */
@Service
@Slf4j
public class AgentResponseAnalysisService {
    
    @Autowired
    private MemoryArchitectureConfig memoryConfig;
    
    // Keywords for response analysis
    private static final Set<String> MEDICAL_ADVICE_KEYWORDS = Set.of(
        "recommend", "suggest", "should", "prescribe", "treatment", "therapy", 
        "medication", "dose", "consult", "see a doctor", "medical attention"
    );
    
    private static final Set<String> SAFETY_WARNING_KEYWORDS = Set.of(
        "urgent", "emergency", "serious", "danger", "warning", "caution",
        "immediate", "call 911", "seek help", "go to hospital"
    );
    
    private static final Set<String> EMPATHY_KEYWORDS = Set.of(
        "understand", "sorry", "concerned", "support", "help", "care",
        "feel", "worry", "anxiety", "difficult", "challenging"
    );
    
    private static final Set<String> FOLLOW_UP_INDICATORS = Set.of(
        "follow up", "check back", "monitor", "track", "update", "let me know",
        "continue", "progress", "improvement", "changes"
    );
    
    // Response quality patterns
    private static final Pattern VAGUE_RESPONSE_PATTERN = Pattern.compile(
        "\\b(maybe|perhaps|might|could be|not sure|unclear)\\b", Pattern.CASE_INSENSITIVE);
    
    private static final Pattern CONFIDENT_RESPONSE_PATTERN = Pattern.compile(
        "\\b(definitely|certainly|clearly|obviously|exactly|precisely)\\b", Pattern.CASE_INSENSITIVE);
    
    /**
     * Comprehensive analysis of agent response with bidirectional feedback
     */
    public AgentResponseMetadata analyzeAgentResponse(String agentId, String agentType, String agentResponse,
                                                     String enrichedContext, ConversationTurn turn,
                                                     ContextEnrichmentResult contextResult, long responseTimeMs) {
        log.debug("Analyzing agent response for turn {} from agent {}", turn.getTurnNumber(), agentType);
        
        try {
            LocalDateTime analysisStart = LocalDateTime.now();
            
            // Basic response metrics
            int responseLength = agentResponse != null ? agentResponse.length() : 0;
            
            // Quality analysis
            double responseQuality = calculateResponseQuality(agentResponse, turn.getUserMessage());
            double contextRelevance = calculateContextRelevance(agentResponse, enrichedContext);
            double responseConfidence = calculateResponseConfidence(agentResponse);
            
            // Medical analysis
            double medicalAccuracy = calculateMedicalAccuracy(agentResponse, agentType);
            String medicalAdviceLevel = determineMedicalAdviceLevel(agentResponse);
            boolean includedSafetyWarnings = containsSafetyWarnings(agentResponse);
            
            // Emotional analysis
            double emotionalAppropriateness = calculateEmotionalAppropriateness(agentResponse, turn);
            String responseSentiment = analyzeResponseSentiment(agentResponse);
            
            // Context utilization analysis
            String contextUtilization = analyzeContextUtilization(agentResponse, enrichedContext, contextResult);
            String contextFeedback = generateContextFeedback(agentResponse, enrichedContext, contextResult);
            
            // Topic and pattern analysis
            String addressedTopics = extractAddressedTopics(agentResponse);
            String responsePatterns = analyzeResponsePatterns(agentResponse, agentType);
            
            // Effectiveness indicators
            boolean addressedConcern = determineIfConcernAddressed(agentResponse, turn.getUserMessage());
            boolean requiresFollowUp = determineIfFollowUpRequired(agentResponse);
            
            // Error and success analysis
            String errorIndicators = identifyErrorIndicators(agentResponse, turn);
            String successIndicators = identifySuccessIndicators(agentResponse, turn);
            
            // Create comprehensive metadata
            return AgentResponseMetadata.builder()
                .agentId(agentId)
                .agentType(agentType)
                .responseTimestamp(analysisStart)
                .responseTimeMs(responseTimeMs)
                .responseLength(responseLength)
                .responseQuality(responseQuality)
                .contextRelevance(contextRelevance)
                .responseConfidence(responseConfidence)
                .medicalAccuracy(medicalAccuracy)
                .emotionalAppropriateness(emotionalAppropriateness)
                .addressedConcern(addressedConcern)
                .requiresFollowUp(requiresFollowUp)
                .responseSentiment(responseSentiment)
                .addressedTopics(addressedTopics)
                .medicalAdviceLevel(medicalAdviceLevel)
                .includedSafetyWarnings(includedSafetyWarnings)
                .contextUtilization(contextUtilization)
                .contextFeedback(contextFeedback)
                .responsePatterns(responsePatterns)
                .errorIndicators(errorIndicators)
                .successIndicators(successIndicators)
                .analysisMetadata(createAnalysisMetadata(turn, contextResult))
                .build();
                
        } catch (Exception e) {
            log.error("Failed to analyze agent response for turn {}: {}", turn.getTurnNumber(), e.getMessage(), e);
            return createFallbackResponseMetadata(agentId, agentType, agentResponse, responseTimeMs);
        }
    }
    
    /**
     * Calculate overall response quality score
     */
    private double calculateResponseQuality(String response, String userMessage) {
        if (response == null || response.trim().isEmpty()) {
            return 0.0;
        }
        
        double quality = 0.5; // Base score
        
        // Length appropriateness (not too short, not too long)
        int length = response.length();
        if (length > 50 && length < 1000) {
            quality += 0.2;
        } else if (length > 20 && length < 2000) {
            quality += 0.1;
        }
        
        // Specificity (avoid vague responses)
        if (!VAGUE_RESPONSE_PATTERN.matcher(response).find()) {
            quality += 0.15;
        }
        
        // Confidence indicators
        if (CONFIDENT_RESPONSE_PATTERN.matcher(response).find()) {
            quality += 0.1;
        }
        
        // Relevance to user message
        if (hasTopicOverlap(response, userMessage)) {
            quality += 0.15;
        }
        
        return Math.min(1.0, quality);
    }
    
    /**
     * Calculate how well the response uses the provided context
     */
    private double calculateContextRelevance(String response, String enrichedContext) {
        if (response == null || enrichedContext == null) {
            return 0.3; // Default low relevance
        }
        
        double relevance = 0.0;
        
        // Extract key terms from enriched context
        Set<String> contextKeywords = extractKeyTerms(enrichedContext);
        Set<String> responseKeywords = extractKeyTerms(response);
        
        // Calculate keyword overlap
        if (!contextKeywords.isEmpty()) {
            Set<String> intersection = new HashSet<>(contextKeywords);
            intersection.retainAll(responseKeywords);
            
            double keywordOverlap = (double) intersection.size() / contextKeywords.size();
            relevance += keywordOverlap * 0.6;
        }
        
        // Check for explicit context references
        if (response.toLowerCase().contains("based on") || 
            response.toLowerCase().contains("considering") ||
            response.toLowerCase().contains("given that")) {
            relevance += 0.2;
        }
        
        // Check for historical references
        if (response.toLowerCase().contains("previous") || 
            response.toLowerCase().contains("earlier") ||
            response.toLowerCase().contains("before")) {
            relevance += 0.2;
        }
        
        return Math.min(1.0, relevance);
    }
    
    /**
     * Calculate response confidence level
     */
    private double calculateResponseConfidence(String response) {
        if (response == null) return 0.0;
        
        double confidence = 0.5; // Base confidence
        
        // Confident language patterns
        if (CONFIDENT_RESPONSE_PATTERN.matcher(response).find()) {
            confidence += 0.3;
        }
        
        // Avoid uncertain language
        if (VAGUE_RESPONSE_PATTERN.matcher(response).find()) {
            confidence -= 0.2;
        }
        
        // Specific recommendations or actions
        if (containsMedicalAdvice(response)) {
            confidence += 0.1;
        }
        
        // Hedging language reduces confidence
        if (response.toLowerCase().contains("i think") || 
            response.toLowerCase().contains("it seems") ||
            response.toLowerCase().contains("it appears")) {
            confidence -= 0.1;
        }
        
        return Math.max(0.0, Math.min(1.0, confidence));
    }
    
    /**
     * Calculate medical accuracy for health-related responses
     */
    private double calculateMedicalAccuracy(String response, String agentType) {
        if (response == null) return 0.5;
        
        double accuracy = 0.7; // Default moderate accuracy
        
        // Health agents should have higher medical accuracy expectations
        if ("General Health".equals(agentType) || "Mental Health".equals(agentType)) {
            // Check for appropriate medical disclaimers
            if (response.toLowerCase().contains("consult") && 
                response.toLowerCase().contains("doctor")) {
                accuracy += 0.15;
            }
            
            // Check for safety warnings when appropriate
            if (containsSafetyWarnings(response)) {
                accuracy += 0.1;
            }
            
            // Penalize overly specific medical advice without proper disclaimers
            if (containsSpecificMedicalAdvice(response) && !containsMedicalDisclaimers(response)) {
                accuracy -= 0.2;
            }
        }
        
        return Math.max(0.0, Math.min(1.0, accuracy));
    }
    
    /**
     * Calculate emotional appropriateness of the response
     */
    private double calculateEmotionalAppropriateness(String response, ConversationTurn turn) {
        if (response == null) return 0.5;
        
        double appropriateness = 0.6; // Base score
        
        // Check for empathy keywords
        long empathyCount = EMPATHY_KEYWORDS.stream()
            .mapToLong(keyword -> response.toLowerCase().contains(keyword) ? 1 : 0)
            .sum();
        
        if (empathyCount > 0) {
            appropriateness += Math.min(0.3, empathyCount * 0.1);
        }
        
        // Check for emotional tone matching
        if (turn.getEmotionalState() != null) {
            if (turn.getEmotionalState().toLowerCase().contains("anxious") && 
                response.toLowerCase().contains("understand")) {
                appropriateness += 0.1;
            }
        }
        
        // Avoid overly clinical responses to emotional concerns
        if (response.toLowerCase().contains("symptom") && 
            !response.toLowerCase().contains("feel") &&
            turn.getUserMessage().toLowerCase().contains("worried")) {
            appropriateness -= 0.1;
        }
        
        return Math.max(0.0, Math.min(1.0, appropriateness));
    }
    
    /**
     * Analyze how well the agent utilized the provided context
     */
    private String analyzeContextUtilization(String response, String enrichedContext, ContextEnrichmentResult contextResult) {
        if (response == null || enrichedContext == null) {
            return "Unable to analyze context utilization";
        }
        
        StringBuilder analysis = new StringBuilder();
        
        // Check context confidence utilization
        if (contextResult != null && contextResult.getContextConfidence() > 0.8) {
            if (calculateContextRelevance(response, enrichedContext) > 0.7) {
                analysis.append("Excellent use of high-confidence context. ");
            } else {
                analysis.append("Missed opportunity to use high-confidence context. ");
            }
        }
        
        // Check historical context usage
        if (enrichedContext.contains("RELEVANT_CONTEXT:") && 
            hasHistoricalReferences(response)) {
            analysis.append("Good utilization of historical context. ");
        }
        
        // Check medical context usage
        if (enrichedContext.contains("MEDICAL_CONTEXT:") && 
            containsMedicalReferences(response)) {
            analysis.append("Appropriate use of medical context. ");
        }
        
        if (analysis.length() == 0) {
            analysis.append("Limited context utilization detected.");
        }
        
        return analysis.toString().trim();
    }
    
    /**
     * Generate feedback for improving future context selection
     */
    private String generateContextFeedback(String response, String enrichedContext, ContextEnrichmentResult contextResult) {
        StringBuilder feedback = new StringBuilder();
        
        // Analyze what context was missing
        if (response.toLowerCase().contains("more information") || 
            response.toLowerCase().contains("tell me more")) {
            feedback.append("Response indicates insufficient context provided. ");
        }
        
        // Check for historical context needs
        if (response.toLowerCase().contains("when did") || 
            response.toLowerCase().contains("how long")) {
            feedback.append("Timeline context would be beneficial. ");
        }
        
        // Check for symptom progression needs
        if (response.toLowerCase().contains("getting worse") || 
            response.toLowerCase().contains("improving")) {
            feedback.append("Symptom progression tracking would improve responses. ");
        }
        
        // Context confidence feedback
        if (contextResult != null && contextResult.getContextConfidence() < 0.6) {
            feedback.append("Low context confidence may have impacted response quality. ");
        }
        
        if (feedback.length() == 0) {
            feedback.append("Context selection appears appropriate for this response.");
        }
        
        return feedback.toString().trim();
    }
    
    /**
     * Extract topics addressed in the response
     */
    private String extractAddressedTopics(String response) {
        Set<String> topics = new HashSet<>();
        
        // Medical topics
        if (response.toLowerCase().contains("headache") || response.toLowerCase().contains("pain")) {
            topics.add("Pain Management");
        }
        if (response.toLowerCase().contains("anxiety") || response.toLowerCase().contains("stress")) {
            topics.add("Mental Health");
        }
        if (response.toLowerCase().contains("medication") || response.toLowerCase().contains("treatment")) {
            topics.add("Treatment");
        }
        if (response.toLowerCase().contains("sleep") || response.toLowerCase().contains("rest")) {
            topics.add("Sleep");
        }
        
        return topics.isEmpty() ? "General Health Discussion" : String.join(", ", topics);
    }
    
    /**
     * Analyze response patterns for the agent type
     */
    private String analyzeResponsePatterns(String response, String agentType) {
        StringBuilder patterns = new StringBuilder();
        
        // Pattern analysis based on agent type
        switch (agentType) {
            case "General Health":
                if (containsMedicalAdvice(response)) {
                    patterns.append("Medical advice pattern. ");
                }
                if (containsMedicalDisclaimers(response)) {
                    patterns.append("Appropriate disclaimers. ");
                }
                break;
                
            case "Mental Health":
                if (containsEmpathy(response)) {
                    patterns.append("Empathetic response pattern. ");
                }
                if (containsCopingStrategies(response)) {
                    patterns.append("Coping strategies provided. ");
                }
                break;
        }
        
        return patterns.length() > 0 ? patterns.toString().trim() : "Standard response pattern";
    }
    
    // Helper methods
    
    private boolean hasTopicOverlap(String response, String userMessage) {
        Set<String> responseWords = extractKeyTerms(response);
        Set<String> messageWords = extractKeyTerms(userMessage);
        
        responseWords.retainAll(messageWords);
        return !responseWords.isEmpty();
    }
    
    private Set<String> extractKeyTerms(String text) {
        if (text == null) return new HashSet<>();
        
        return new HashSet<>(Arrays.asList(
            text.toLowerCase()
                .replaceAll("[^a-zA-Z\\s]", "")
                .split("\\s+")
        ));
    }
    
    private boolean containsMedicalAdvice(String response) {
        return MEDICAL_ADVICE_KEYWORDS.stream()
            .anyMatch(keyword -> response.toLowerCase().contains(keyword));
    }
    
    private boolean containsSafetyWarnings(String response) {
        return SAFETY_WARNING_KEYWORDS.stream()
            .anyMatch(keyword -> response.toLowerCase().contains(keyword));
    }
    
    private boolean containsEmpathy(String response) {
        return EMPATHY_KEYWORDS.stream()
            .anyMatch(keyword -> response.toLowerCase().contains(keyword));
    }
    
    private boolean containsSpecificMedicalAdvice(String response) {
        return response.toLowerCase().contains("take") && 
               (response.toLowerCase().contains("mg") || response.toLowerCase().contains("pill"));
    }
    
    private boolean containsMedicalDisclaimers(String response) {
        return response.toLowerCase().contains("consult") || 
               response.toLowerCase().contains("see a doctor") ||
               response.toLowerCase().contains("medical professional");
    }
    
    private boolean containsCopingStrategies(String response) {
        return response.toLowerCase().contains("try") || 
               response.toLowerCase().contains("practice") ||
               response.toLowerCase().contains("technique");
    }
    
    private boolean hasHistoricalReferences(String response) {
        return response.toLowerCase().contains("previous") || 
               response.toLowerCase().contains("earlier") ||
               response.toLowerCase().contains("before") ||
               response.toLowerCase().contains("last time");
    }
    
    private boolean containsMedicalReferences(String response) {
        return response.toLowerCase().contains("symptom") || 
               response.toLowerCase().contains("condition") ||
               response.toLowerCase().contains("treatment") ||
               response.toLowerCase().contains("medication");
    }
    
    private String determineMedicalAdviceLevel(String response) {
        if (containsSpecificMedicalAdvice(response)) {
            return "Specific";
        } else if (containsMedicalAdvice(response)) {
            return "General";
        } else if (response.toLowerCase().contains("consult") || 
                   response.toLowerCase().contains("see a doctor")) {
            return "Referral";
        } else {
            return "None";
        }
    }
    
    private String analyzeResponseSentiment(String response) {
        if (containsEmpathy(response)) {
            return "Empathetic";
        } else if (containsMedicalAdvice(response)) {
            return "Professional";
        } else if (containsSafetyWarnings(response)) {
            return "Cautionary";
        } else {
            return "Neutral";
        }
    }
    
    private boolean determineIfConcernAddressed(String response, String userMessage) {
        Set<String> userKeywords = extractKeyTerms(userMessage);
        Set<String> responseKeywords = extractKeyTerms(response);
        
        userKeywords.retainAll(responseKeywords);
        return !userKeywords.isEmpty() && response.length() > 50;
    }
    
    private boolean determineIfFollowUpRequired(String response) {
        return FOLLOW_UP_INDICATORS.stream()
            .anyMatch(indicator -> response.toLowerCase().contains(indicator));
    }
    
    private String identifyErrorIndicators(String response, ConversationTurn turn) {
        StringBuilder errors = new StringBuilder();
        
        if (response == null || response.trim().isEmpty()) {
            errors.append("Empty response. ");
        }
        
        if (response != null && response.length() < 20) {
            errors.append("Response too short. ");
        }
        
        if (response != null && VAGUE_RESPONSE_PATTERN.matcher(response).find() && 
            response.length() < 100) {
            errors.append("Vague response with limited detail. ");
        }
        
        return errors.length() > 0 ? errors.toString().trim() : "No significant issues detected";
    }
    
    private String identifySuccessIndicators(String response, ConversationTurn turn) {
        StringBuilder success = new StringBuilder();
        
        if (response != null && response.length() > 100 && response.length() < 800) {
            success.append("Appropriate response length. ");
        }
        
        if (containsEmpathy(response)) {
            success.append("Empathetic tone. ");
        }
        
        if (containsMedicalDisclaimers(response)) {
            success.append("Proper medical disclaimers. ");
        }
        
        return success.length() > 0 ? success.toString().trim() : "Standard response quality";
    }
    
    private Map<String, Object> createAnalysisMetadata(ConversationTurn turn, ContextEnrichmentResult contextResult) {
        Map<String, Object> metadata = new HashMap<>();
        
        metadata.put("turnNumber", turn.getTurnNumber());
        metadata.put("analysisTimestamp", LocalDateTime.now());
        
        if (contextResult != null) {
            metadata.put("contextConfidence", contextResult.getContextConfidence());
            metadata.put("relevantTurns", contextResult.getRelevantTurns());
        }
        
        return metadata;
    }
    
    private AgentResponseMetadata createFallbackResponseMetadata(String agentId, String agentType, 
                                                               String response, long responseTimeMs) {
        return AgentResponseMetadata.builder()
            .agentId(agentId)
            .agentType(agentType)
            .responseTimestamp(LocalDateTime.now())
            .responseTimeMs(responseTimeMs)
            .responseLength(response != null ? response.length() : 0)
            .responseQuality(0.5)
            .contextRelevance(0.5)
            .responseConfidence(0.5)
            .contextUtilization("Analysis failed")
            .contextFeedback("Unable to generate feedback")
            .errorIndicators("Response analysis failed")
            .build();
    }
} 