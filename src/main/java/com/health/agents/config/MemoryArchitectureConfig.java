package com.health.agents.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Memory Architecture Configuration for Centralized Context Management
 * Sub-Plan 1: Memory Architecture Design (Complete)
 * Sub-Plan 3: Enhanced Context Extraction (Complete)
 * Sub-Plan 4: Bidirectional Memory Updates (Complete)
 */
@Component
@ConfigurationProperties(prefix = "memory")
@Data
public class MemoryArchitectureConfig {
    
    // Memory block identifiers used across the system
    public static final String CONVERSATION_HISTORY = "conversation_history";
    public static final String ACTIVE_SESSION = "active_session";
    public static final String CONTEXT_SUMMARY = "context_summary";
    public static final String MEMORY_METADATA = "memory_metadata";
    public static final String AGENT_INSTRUCTIONS = "agent_instructions";
    
    // Data format enumeration
    public enum DataFormat {
        JSON,
        STRUCTURED_TEXT
    }
    
    // Memory block size limits
    private ConversationHistory conversationHistory = new ConversationHistory();
    private ActiveSession activeSession = new ActiveSession();
    private ContextSummary contextSummary = new ContextSummary();
    private MemoryMetadata memoryMetadata = new MemoryMetadata();
    private AgentInstructions agentInstructions = new AgentInstructions();
    
    // Memory management settings
    private Rotation rotation = new Rotation();
    private Archival archival = new Archival();
    
    // Context selection strategy
    private ContextWindow contextWindow = new ContextWindow();
    
    // Data format configuration
    private String dataFormat = "STRUCTURED_TEXT";
    
    // Sub-Plan 5: Session and State Management Configuration
    private SessionStateConfig sessionState = new SessionStateConfig();
    
    @Data
    public static class ConversationHistory {
        private int limit = 32000;
        private String description = "Stores complete conversation history with enhanced turn tracking";
    }
    
    @Data
    public static class ActiveSession {
        private int limit = 4000;
        private String description = "Current session state and metadata";
    }
    
    @Data
    public static class ContextSummary {
        private int limit = 8000;
        private String description = "Enhanced context summary with pattern analysis";
    }
    
    @Data
    public static class MemoryMetadata {
        private int limit = 2000;
        private String description = "Memory management metadata and bidirectional analytics";
    }
    
    @Data
    public static class AgentInstructions {
        private int limit = 16000;
        private String description = "Agent-specific instructions and configuration";
    }
    
    @Data
    public static class Rotation {
        private double threshold = 0.8;
    }
    
    @Data
    public static class Archival {
        private int triggerTurns = 50;
    }
    
    @Data
    public static class ContextWindow {
        private int recentTurns = 5;
        private int maxRelevant = 10;
    }
    
    // ===== Getter methods for backward compatibility =====
    
    public int getConversationHistoryLimit() {
        return conversationHistory.getLimit();
    }
    
    public String getConversationHistoryDescription() {
        return conversationHistory.getDescription();
    }
    
    public int getActiveSessionLimit() {
        return activeSession.getLimit();
    }
    
    public String getActiveSessionDescription() {
        return activeSession.getDescription();
    }
    
    public int getContextSummaryLimit() {
        return contextSummary.getLimit();
    }
    
    public String getContextSummaryDescription() {
        return contextSummary.getDescription();
    }
    
    public int getMemoryMetadataLimit() {
        return memoryMetadata.getLimit();
    }
    
    public String getMemoryMetadataDescription() {
        return memoryMetadata.getDescription();
    }
    
    public int getAgentInstructionsLimit() {
        return agentInstructions.getLimit();
    }
    
    public String getAgentInstructionsDescription() {
        return agentInstructions.getDescription();
    }
    
    public boolean shouldRotateMemory(int currentSize, int maxSize) {
        double currentRatio = (double) currentSize / maxSize;
        return currentRatio >= rotation.getThreshold();
    }
    
    // ===== SUB-PLAN 3: Enhanced Context Configuration Classes =====
    
    @Component
    @ConfigurationProperties(prefix = "context")
    @Data
    public static class ContextConfig {
        private Selection selection = new Selection();
        private Relevance relevance = new Relevance();
        private Analysis analysis = new Analysis();
        private Patterns patterns = new Patterns();
        private Enrichment enrichment = new Enrichment();
        private Medical medical = new Medical();
        private Emotional emotional = new Emotional();
        private Performance performance = new Performance();
        
        @Data
        public static class Selection {
            private double recentWeight = 1.0;
            private double topicWeight = 0.8;
            private double medicalWeight = 0.9;
            private double emotionalWeight = 0.7;
            private double followupWeight = 0.95;
        }
        
        @Data
        public static class Relevance {
            private double minimumScore = 0.1;
            private double highConfidence = 0.8;
            private double mediumConfidence = 0.5;
        }
        
        @Data
        public static class Analysis {
            private double keywordSimilarityThreshold = 0.3;
            private double topicMatchBoost = 0.3;
            private double recencyDecayFactor = 0.1;
        }
        
        @Data
        public static class Patterns {
            private int phaseTransitionThreshold = 5;
            private int complexityAnalysisWindow = 3;
            private double trendConfidenceThreshold = 0.6;
        }
        
        @Data
        public static class Enrichment {
            private int maxContextSections = 6;
            private int summaryUpdateFrequency = 5;
            private boolean enablePatternAnalysis = true;
            private boolean enableTrendDetection = true;
        }
        
        @Data
        public static class Medical {
            private boolean keywordExpansion = true;
            private boolean symptomTracking = true;
            private boolean medicationTracking = true;
        }
        
        @Data
        public static class Emotional {
            private boolean sentimentAnalysis = true;
            private boolean stateProgression = true;
            private boolean concernEscalationDetection = true;
        }
        
        @Data
        public static class Performance {
            private boolean cacheContextResults = true;
            private int cacheTtlMinutes = 30;
            private boolean parallelAnalysis = true;
        }
    }
    
    // ===== SUB-PLAN 4: Bidirectional Memory Updates Configuration Classes =====
    
    @Component
    @ConfigurationProperties(prefix = "bidirectional")
    @Data
    public static class BidirectionalConfig {
        private ResponseAnalysis responseAnalysis = new ResponseAnalysis();
        private Quality quality = new Quality();
        private ContextUtilization contextUtilization = new ContextUtilization();
        private MemoryUpdates memoryUpdates = new MemoryUpdates();
        private Consistency consistency = new Consistency();
        private Performance performance = new Performance();
        private Feedback feedback = new Feedback();
        private Patterns patterns = new Patterns();
        private Analytics analytics = new Analytics();
        
        @Data
        public static class ResponseAnalysis {
            private boolean enableQualityScoring = true;
            private boolean enableContextCorrelation = true;
            private boolean enableMedicalAccuracy = true;
            private boolean enableEmotionalAppropriateness = true;
            private boolean enablePatternDetection = true;
        }
        
        @Data
        public static class Quality {
            private double excellentThreshold = 0.8;
            private double goodThreshold = 0.6;
            private double fairThreshold = 0.4;
            private double minimumAcceptable = 0.3;
        }
        
        @Data
        public static class ContextUtilization {
            private double highUtilizationThreshold = 0.8;
            private double goodUtilizationThreshold = 0.6;
            private boolean enableFeedbackGeneration = true;
            private boolean trackCorrelationMetrics = true;
        }
        
        @Data
        public static class MemoryUpdates {
            private boolean enableAtomicUpdates = true;
            private boolean enableParallelUpdates = true;
            private int maxRetryAttempts = 3;
            private long retryBackoffMs = 1000;
            private int updateTimeoutSeconds = 30;
            private boolean enableRollback = true;
        }
        
        @Data
        public static class Consistency {
            private boolean enableIntegrityChecks = true;
            private boolean checkTurnConsistency = true;
            private boolean checkSessionConsistency = true;
            private boolean checkMetadataConsistency = true;
            private boolean checkSizeLimits = true;
        }
        
        @Data
        public static class Performance {
            private boolean trackUpdateTimes = true;
            private boolean trackAnalysisMetrics = true;
            private boolean enablePerformanceLogging = true;
            private long alertSlowUpdatesMs = 5000;
        }
        
        @Data
        public static class Feedback {
            private boolean enableContextFeedback = true;
            private boolean enablePatternUpdates = true;
            private boolean enableOptimizationSuggestions = true;
            private double feedbackConfidenceThreshold = 0.7;
        }
        
        @Data
        public static class Patterns {
            private boolean enableMedicalPatternTracking = true;
            private boolean enableEmotionalPatternTracking = true;
            private boolean enableQualityPatternTracking = true;
            private double patternConfidenceThreshold = 0.6;
        }
        
        @Data
        public static class Analytics {
            private boolean enableCorrelationAnalysis = true;
            private boolean enableTrendPrediction = true;
            private boolean enableEffectivenessScoring = true;
            private int analyticsWindowSize = 10;
        }
    }
    
    // Utility methods for memory management
    
    /**
     * Check if memory block is near rotation threshold
     */
    public boolean isNearRotationThreshold(int currentSize, int maxSize) {
        double currentRatio = (double) currentSize / maxSize;
        return currentRatio >= rotation.getThreshold();
    }
    
    /**
     * Get maximum size for a memory block by type
     */
    public int getMaxSizeForBlock(String blockType) {
        switch (blockType) {
            case CONVERSATION_HISTORY:
                return conversationHistory.getLimit();
            case ACTIVE_SESSION:
                return activeSession.getLimit();
            case CONTEXT_SUMMARY:
                return contextSummary.getLimit();
            case MEMORY_METADATA:
                return memoryMetadata.getLimit();
            case AGENT_INSTRUCTIONS:
                return agentInstructions.getLimit();
            default:
                return 4000; // Default limit
        }
    }
    
    /**
     * Check if archival should be triggered based on turn count
     */
    public boolean shouldTriggerArchival(int turnCount) {
        return turnCount >= archival.getTriggerTurns();
    }
    
    /**
     * Get context window size for recent turns
     */
    public int getRecentTurnsWindow() {
        return contextWindow.getRecentTurns();
    }
    
    /**
     * Get maximum relevant turns for context selection
     */
    public int getMaxRelevantTurns() {
        return contextWindow.getMaxRelevant();
    }
    
    /**
     * Check if structured text format is enabled
     */
    public boolean isStructuredTextFormat() {
        return "STRUCTURED_TEXT".equals(dataFormat);
    }
    
    /**
     * Check if JSON format is enabled
     */
    public boolean isJsonFormat() {
        return "JSON".equals(dataFormat);
    }
    
    // ===== SUB-PLAN 4: Bidirectional Memory Utility Methods =====
    
    /**
     * Check if response quality meets minimum threshold
     */
    public boolean isResponseQualityAcceptable(double quality, BidirectionalConfig config) {
        return quality >= config.getQuality().getMinimumAcceptable();
    }
    
    /**
     * Get response quality level
     */
    public String getResponseQualityLevel(double quality, BidirectionalConfig config) {
        if (quality >= config.getQuality().getExcellentThreshold()) {
            return "EXCELLENT";
        } else if (quality >= config.getQuality().getGoodThreshold()) {
            return "GOOD";
        } else if (quality >= config.getQuality().getFairThreshold()) {
            return "FAIR";
        } else {
            return "POOR";
        }
    }
    
    /**
     * Check if context utilization is high
     */
    public boolean isHighContextUtilization(double utilization, BidirectionalConfig config) {
        return utilization >= config.getContextUtilization().getHighUtilizationThreshold();
    }
    
    /**
     * Check if feedback should be generated based on confidence
     */
    public boolean shouldGenerateFeedback(double confidence, BidirectionalConfig config) {
        return confidence >= config.getFeedback().getFeedbackConfidenceThreshold();
    }
    
    /**
     * Check if pattern detection confidence is sufficient
     */
    public boolean isPatternDetectionConfident(double confidence, BidirectionalConfig config) {
        return confidence >= config.getPatterns().getPatternConfidenceThreshold();
    }
    
    /**
     * Check if memory update is considered slow
     */
    public boolean isSlowMemoryUpdate(long updateTimeMs, BidirectionalConfig config) {
        return updateTimeMs >= config.getPerformance().getAlertSlowUpdatesMs();
    }
    
    /**
     * Get retry backoff time for failed memory updates
     */
    public long getRetryBackoffTime(int attempt, BidirectionalConfig config) {
        return config.getMemoryUpdates().getRetryBackoffMs() * (long) Math.pow(2, attempt - 1);
    }
    
    // ===== SUB-PLAN 5: Session and State Management Configuration Classes =====
    
    @Component
    @ConfigurationProperties(prefix = "memory.architecture.session-state")
    @Data
    public static class SessionStateConfig {
        private StateTracking stateTracking = new StateTracking();
        private ConversationPhases conversationPhases = new ConversationPhases();
        private BoundaryManagement boundaryManagement = new BoundaryManagement();
        private SessionTimeouts sessionTimeouts = new SessionTimeouts();
        private ArchivalTriggers archivalTriggers = new ArchivalTriggers();
        private CrossSessionContinuity crossSessionContinuity = new CrossSessionContinuity();
        private ContinuityThresholds continuityThresholds = new ContinuityThresholds();
        private PreservationMethods preservationMethods = new PreservationMethods();
        private QualityAssessment qualityAssessment = new QualityAssessment();
        private QualityFactors qualityFactors = new QualityFactors();
        private MemoryIntegration memoryIntegration = new MemoryIntegration();
        private EngagementTracking engagementTracking = new EngagementTracking();
        private EngagementLevels engagementLevels = new EngagementLevels();
        
        @Data
        public static class StateTracking {
            private boolean enabled = true;
            private boolean comprehensiveMetrics = true;
            private boolean realTimeUpdates = true;
            private boolean qualityAssessment = true;
            private boolean complexityScoring = true;
            private boolean engagementMetrics = true;
        }
        
        @Data
        public static class ConversationPhases {
            private int initialAssessmentTurns = 2;
            private int informationGatheringTurns = 5;
            private int activeDiscussionTurns = 10;
            private int extendedConsultationTurns = 20;
            private int deepEngagementThreshold = 20;
        }
        
        @Data
        public static class BoundaryManagement {
            private boolean enabled = true;
            private boolean autoTransitions = true;
            private int longRunningThresholdMinutes = 60;
            private boolean qualityMonitoring = true;
            private boolean attentionAlerts = true;
        }
        
        @Data
        public static class SessionTimeouts {
            private int inactivityTimeoutMinutes = 30;
            private int maxSessionDurationHours = 4;
            private int pauseSuggestionMinutes = 45;
        }
        
        @Data
        public static class ArchivalTriggers {
            private int turnThreshold = 25;
            private int durationThresholdHours = 2;
            private double qualityThreshold = 0.6;
            private double complexityThreshold = 0.8;
        }
        
        @Data
        public static class CrossSessionContinuity {
            private boolean enabled = true;
            private boolean seamlessResumption = true;
            private boolean contextPreservation = true;
            private boolean patternLearning = true;
            private boolean historicalAnalysis = true;
        }
        
        @Data
        public static class ContinuityThresholds {
            private int minimumSessionTurns = 3;
            private double contextRelevanceThreshold = 0.6;
            private double resumptionConfidenceThreshold = 0.7;
            private int patternEstablishmentSessions = 3;
        }
        
        @Data
        public static class PreservationMethods {
            private boolean fullContext = true;
            private boolean summaryContext = true;
            private boolean keyPoints = true;
            private String defaultMethod = "SUMMARY_CONTEXT";
        }
        
        @Data
        public static class QualityAssessment {
            private boolean enabled = true;
            private boolean realTimeScoring = true;
            private boolean multiDimensional = true;
            private boolean userSatisfactionIndicators = true;
        }
        
        @Data
        public static class QualityFactors {
            private double contextContinuityWeight = 0.3;
            private double agentPerformanceWeight = 0.3;
            private double userEngagementWeight = 0.2;
            private double resolutionAchievementWeight = 0.2;
        }
        
        @Data
        public static class MemoryIntegration {
            private boolean sessionMemoryEfficiency = true;
            private boolean rotationAwareness = true;
            private boolean archivalIntegration = true;
            private boolean crossSessionMemoryAccess = true;
        }
        
        @Data
        public static class EngagementTracking {
            private boolean responseRateMonitoring = true;
            private boolean messageLengthAnalysis = true;
            private boolean questionFrequencyTracking = true;
            private boolean participationLevelAssessment = true;
        }
        
        @Data
        public static class EngagementLevels {
            private HighThreshold highThreshold = new HighThreshold();
            private MediumThreshold mediumThreshold = new MediumThreshold();
            private LowThreshold lowThreshold = new LowThreshold();
            
            @Data
            public static class HighThreshold {
                private int minAvgLength = 100;
                private int minQuestions = 2;
            }
            
            @Data
            public static class MediumThreshold {
                private int minAvgLength = 50;
                private int minQuestions = 1;
            }
            
            @Data
            public static class LowThreshold {
                private int maxAvgLength = 50;
                private int maxQuestions = 0;
            }
        }
        
        // Helper methods for Sub-Plan 5
        public boolean isSessionStateManagementEnabled() {
            return stateTracking.enabled;
        }
        
        public boolean isCrossSessionContinuityEnabled() {
            return crossSessionContinuity.enabled;
        }
        
        public boolean isQualityAssessmentEnabled() {
            return qualityAssessment.enabled;
        }
        
        public boolean shouldTriggerArchival(int turns, long durationHours, double quality, double complexity) {
            return turns >= archivalTriggers.turnThreshold ||
                   durationHours >= archivalTriggers.durationThresholdHours ||
                   quality <= archivalTriggers.qualityThreshold ||
                   complexity >= archivalTriggers.complexityThreshold;
        }
        
        public boolean isLongRunningSession(long durationMinutes) {
            return durationMinutes >= boundaryManagement.longRunningThresholdMinutes;
        }
        
        public boolean shouldSuggestPause(long durationMinutes) {
            return durationMinutes >= sessionTimeouts.pauseSuggestionMinutes;
        }
        
        public String determineConversationPhase(int turns) {
            if (turns <= conversationPhases.initialAssessmentTurns) {
                return "Initial Assessment";
            } else if (turns <= conversationPhases.informationGatheringTurns) {
                return "Information Gathering";
            } else if (turns <= conversationPhases.activeDiscussionTurns) {
                return "Active Discussion";
            } else if (turns <= conversationPhases.extendedConsultationTurns) {
                return "Extended Consultation";
            } else {
                return "Deep Engagement";
            }
        }
        
        public String determineEngagementLevel(double avgLength, int questionsAsked) {
            if (avgLength >= engagementLevels.highThreshold.minAvgLength && 
                questionsAsked >= engagementLevels.highThreshold.minQuestions) {
                return "HIGH";
            } else if (avgLength >= engagementLevels.mediumThreshold.minAvgLength && 
                       questionsAsked >= engagementLevels.mediumThreshold.minQuestions) {
                return "MEDIUM";
            } else {
                return "LOW";
            }
        }
        
        public double calculateQualityScore(double contextContinuity, double agentPerformance, 
                                           double userEngagement, double resolutionAchievement) {
            return (contextContinuity * qualityFactors.contextContinuityWeight) +
                   (agentPerformance * qualityFactors.agentPerformanceWeight) +
                   (userEngagement * qualityFactors.userEngagementWeight) +
                   (resolutionAchievement * qualityFactors.resolutionAchievementWeight);
        }
        
        public boolean canResumeSeamlessly(double resumptionConfidence) {
            return resumptionConfidence >= continuityThresholds.resumptionConfidenceThreshold;
        }
        
        public boolean hasRelevantContext(double contextRelevance) {
            return contextRelevance >= continuityThresholds.contextRelevanceThreshold;
        }
        
        public boolean hasEstablishedPatterns(int totalSessions) {
            return totalSessions >= continuityThresholds.patternEstablishmentSessions;
        }
        
        public boolean isMeaningfulSession(int turns) {
            return turns >= continuityThresholds.minimumSessionTurns;
        }
    }
    
    // Add getter method for sessionState
    public SessionStateConfig getSessionState() {
        return sessionState;
    }
} 