package com.health.agents.service;

import com.health.agents.integration.letta.model.*;
import com.health.agents.integration.letta.service.LettaAgentService;
import com.health.agents.integration.letta.service.LettaIdentityService;
import com.health.agents.model.UserAgentMapping;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.health.agents.service.AgentPromptService.AgentType;

@Service
@Slf4j
public class UserIdentityService {

    @Autowired
    private LettaIdentityService lettaIdentityService;

    @Autowired
    private LettaAgentService lettaAgentService;

    @Autowired
    private AgentPromptService agentPromptService;

    @Value("${letta.api.default-model}")
    private String defaultModel;

    @Value("${letta.api.default-embedding}")
    private String defaultEmbedding;

    @Cacheable("userAgentMappings")
    public UserAgentMapping getOrCreateUserAgents(String userId) {
        log.info("Getting or creating agents for user: {}", userId);

        // 1. Get or create Letta Identity
        LettaIdentityResponse identity = getOrCreateLettaIdentity(userId);

        // 2. Check if identity has agent mappings in properties
        UserAgentMapping mapping = getAgentMappingFromIdentity(identity);

        // 3. If no agents exist, create them
        if (mapping == null) {
            log.info("No existing agents found for user {}, creating new agent ecosystem", userId);
            mapping = createAgentsForUser(userId, identity.getId());
            storeAgentMappingInIdentity(identity.getId(), mapping);
        } else {
            log.info("Found existing agents for user {}: {}", userId, mapping);
        }

        return mapping;
    }

    private LettaIdentityResponse getOrCreateLettaIdentity(String userId) {
        // Search for existing identity by identifier_key
        List<LettaIdentityResponse> identities = lettaIdentityService.listIdentities();
        Optional<LettaIdentityResponse> existing = identities.stream()
            .filter(identity -> ("user_id_" + userId).equals(identity.getIdentifierKey()))
            .findFirst();

        if (existing.isPresent()) {
            log.info("Found existing identity for user {}: {}", userId, existing.get().getId());
            return existing.get();
        }

        // Create new identity with user_id property
        log.info("Creating new identity for user: {}", userId);
        LettaIdentityRequest request = LettaIdentityRequest.builder()
            .name("Health User: " + userId)
            .identifierKey("user_id_" + userId)
            .identityType("user")
            .build();

        LettaIdentityResponse identity = lettaIdentityService.createIdentity(request);

        // Set user_id property
        List<LettaIdentityProperty> properties = Arrays.asList(
            LettaIdentityProperty.builder()
                .key("user_id")
                .value(userId)
                .type("string")
                .build()
        );

        lettaIdentityService.upsertIdentityProperties(identity.getId(), properties);
        return identity;
    }

    private UserAgentMapping getAgentMappingFromIdentity(LettaIdentityResponse identity) {
        if (identity.getProperties() == null) {
            return null;
        }

        String contextExtractorId = getPropertyValue(identity, "context_extractor_id");
        String intentExtractorId = getPropertyValue(identity, "intent_extractor_id");
        String generalHealthId = getPropertyValue(identity, "general_health_id");
        String mentalHealthId = getPropertyValue(identity, "mental_health_id");

        if (contextExtractorId != null && intentExtractorId != null && 
            generalHealthId != null && mentalHealthId != null) {
            return UserAgentMapping.builder()
                .userId(getPropertyValue(identity, "user_id"))
                .identityId(identity.getId())
                .contextExtractorId(contextExtractorId)
                .intentExtractorId(intentExtractorId)
                .generalHealthId(generalHealthId)
                .mentalHealthId(mentalHealthId)
                .build();
        }

        return null;
    }

    private void storeAgentMappingInIdentity(String identityId, UserAgentMapping mapping) {
        List<LettaIdentityProperty> properties = Arrays.asList(
            LettaIdentityProperty.builder()
                .key("context_extractor_id")
                .value(mapping.getContextExtractorId())
                .type("string")
                .build(),
            LettaIdentityProperty.builder()
                .key("intent_extractor_id")
                .value(mapping.getIntentExtractorId())
                .type("string")
                .build(),
            LettaIdentityProperty.builder()
                .key("general_health_id")
                .value(mapping.getGeneralHealthId())
                .type("string")
                .build(),
            LettaIdentityProperty.builder()
                .key("mental_health_id")
                .value(mapping.getMentalHealthId())
                .type("string")
                .build()
        );

        lettaIdentityService.upsertIdentityProperties(identityId, properties);
        log.info("Stored agent mapping for identity: {}", identityId);
    }

    private UserAgentMapping createAgentsForUser(String userId, String identityId) {
        log.info("Creating 4 specialized agents for user: {}", userId);

        // Create 4 specialized agents for this user
        String contextExtractorId = createContextCoordinatorAgent(userId, identityId);
        String intentExtractorId = createIntentClassifierAgent(userId, identityId);
        String generalHealthId = createGeneralHealthAgent(userId, identityId);
        String mentalHealthId = createMentalHealthAgent(userId, identityId);

        return UserAgentMapping.builder()
            .userId(userId)
            .identityId(identityId)
            .contextExtractorId(contextExtractorId)
            .intentExtractorId(intentExtractorId)
            .generalHealthId(generalHealthId)
            .mentalHealthId(mentalHealthId)
            .build();
    }

    private String createContextCoordinatorAgent(String userId, String identityId) {
        log.info("Creating Context Extractor agent for user: {}", userId);

        // Get the specialized prompt for this agent type - now focused on context extraction
        String agentPrompt = agentPromptService.getAgentPrompt(AgentPromptService.AgentType.CONTEXT_COORDINATOR);

        LettaAgentRequest request = LettaAgentRequest.builder()
            .name("context-extractor-" + userId)
            .description("Context extraction agent for user " + userId + " - maintains complete conversation history for context")
            .identityIds(Arrays.asList(identityId))
            .memoryBlocks(Arrays.asList(
                LettaMemoryBlock.builder()
                    .label("agent_instructions")
                    .value(agentPrompt)
                    .description("Core agent instructions for context extraction")
                    .limit(8000)
                    .readOnly(true)
                    .build(),
                LettaMemoryBlock.builder()
                    .label("conversation_history")
                    .value("")
                    .description("Shared conversation memory accessible by all agents for context")
                    .limit(16000)
                    .readOnly(false)
                    .build(),
                LettaMemoryBlock.builder()
                    .label("session_context")
                    .value("No active session")
                    .description("Current session metadata and status")
                    .limit(2000)
                    .readOnly(false)
                    .build(),
                LettaMemoryBlock.builder()
                    .label("user_profile")
                    .value("User ID: " + userId)
                    .description("User profile and preferences")
                    .limit(2000)
                    .readOnly(false)
                    .build()
            ))
            .model(defaultModel)
            .embedding(defaultEmbedding)
            .tools(Arrays.asList("core_memory_replace", "core_memory_append"))
            .metadata(Map.of(
                "agent_type", "context_extractor",
                "user_id", userId,
                "role", "context_management"
            ))
            .build();

        return lettaAgentService.createAgent(request).getId();
    }

    private String createIntentClassifierAgent(String userId, String identityId) {
        log.info("Creating Intent Extractor agent for user: {}", userId);

        // Get the specialized prompt for this agent type - now focused purely on intent extraction
        String agentPrompt = agentPromptService.getAgentPrompt(AgentPromptService.AgentType.INTENT_CLASSIFIER);

        LettaAgentRequest request = LettaAgentRequest.builder()
            .name("intent-extractor-" + userId)
            .description("Intent extraction agent for user " + userId + " - pure intent classification")
            .identityIds(Arrays.asList(identityId))
            .memoryBlocks(Arrays.asList(
                LettaMemoryBlock.builder()
                    .label("agent_instructions")
                    .value(agentPrompt)
                    .description("Core agent instructions for intent extraction and classification")
                    .limit(8000)
                    .readOnly(true)
                    .build(),
                LettaMemoryBlock.builder()
                    .label("classification_patterns")
                    .value("Intent classification patterns for health queries: GENERAL_HEALTH, MENTAL_HEALTH, EMERGENCY, UNCLEAR")
                    .description("Learned patterns for intent classification")
                    .limit(2000)
                    .readOnly(false)
                    .build(),
                LettaMemoryBlock.builder()
                    .label("conversation_history")
                    .value("")
                    .description("Shared conversation memory accessible by all agents for context")
                    .limit(16000)
                    .readOnly(false)
                    .build()
            ))
            .model(defaultModel)
            .embedding(defaultEmbedding)
            .tools(Arrays.asList("core_memory_replace", "core_memory_append"))
            .metadata(Map.of(
                "agent_type", "intent_extractor",
                "user_id", userId,
                "role", "intent_classification"
            ))
            .build();

        return lettaAgentService.createAgent(request).getId();
    }

    private String createGeneralHealthAgent(String userId, String identityId) {
        log.info("Creating General Health agent for user: {}", userId);

        // Get the specialized prompt for this agent type
        String agentPrompt = agentPromptService.getAgentPrompt(AgentPromptService.AgentType.GENERAL_HEALTH);

        LettaAgentRequest request = LettaAgentRequest.builder()
            .name("general-health-" + userId)
            .description("General health consultation agent for user " + userId)
            .identityIds(Arrays.asList(identityId))
            .memoryBlocks(Arrays.asList(
                LettaMemoryBlock.builder()
                    .label("agent_instructions")
                    .value(agentPrompt)
                    .description("Core agent instructions and behavior guidelines")
                    .limit(16000)
                    .readOnly(true)
                    .build(),
                LettaMemoryBlock.builder()
                    .label("health_history")
                    .value("User health consultation history")
                    .description("Historical health conversations and patterns")
                    .limit(16000)
                    .readOnly(false)
                    .build(),
                LettaMemoryBlock.builder()
                    .label("current_context")
                    .value("Current health consultation context")
                    .description("Current session health context")
                    .limit(8000)
                    .readOnly(false)
                    .build(),
                LettaMemoryBlock.builder()
                    .label("conversation_history")
                    .value("")
                    .description("Shared conversation memory accessible by all agents for context")
                    .limit(16000)
                    .readOnly(false)
                    .build()
            ))
            .model(defaultModel)
            .embedding(defaultEmbedding)
            .tools(Arrays.asList("core_memory_replace", "core_memory_append"))
            .metadata(Map.of(
                "agent_type", "general_health",
                "user_id", userId,
                "enable_archival", "true"
            ))
            .build();

        return lettaAgentService.createAgent(request).getId();
    }

    private String createMentalHealthAgent(String userId, String identityId) {
        log.info("Creating Mental Health agent for user: {}", userId);

        // Get the specialized prompt for this agent type
        String agentPrompt = agentPromptService.getAgentPrompt(AgentPromptService.AgentType.MENTAL_HEALTH);

        LettaAgentRequest request = LettaAgentRequest.builder()
            .name("mental-health-" + userId)
            .description("Mental health support agent for user " + userId)
            .identityIds(Arrays.asList(identityId))
            .memoryBlocks(Arrays.asList(
                LettaMemoryBlock.builder()
                    .label("agent_instructions")
                    .value(agentPrompt)
                    .description("Core agent instructions and behavior guidelines")
                    .limit(16000)
                    .readOnly(true)
                    .build(),
                LettaMemoryBlock.builder()
                    .label("mental_health_history")
                    .value("User mental health consultation history")
                    .description("Historical mental health conversations and therapeutic patterns")
                    .limit(16000)
                    .readOnly(false)
                    .build(),
                LettaMemoryBlock.builder()
                    .label("therapeutic_context")
                    .value("Current therapeutic session context")
                    .description("Current session therapeutic context and emotional state")
                    .limit(8000)
                    .readOnly(false)
                    .build(),
                LettaMemoryBlock.builder()
                    .label("conversation_history")
                    .value("")
                    .description("Shared conversation memory accessible by all agents for context")
                    .limit(16000)
                    .readOnly(false)
                    .build()
            ))
            .model(defaultModel)
            .embedding(defaultEmbedding)
            .tools(Arrays.asList("core_memory_replace", "core_memory_append"))
            .metadata(Map.of(
                "agent_type", "mental_health",
                "user_id", userId,
                "enable_archival", "true"
            ))
            .build();

        return lettaAgentService.createAgent(request).getId();
    }

    private String getPropertyValue(LettaIdentityResponse identity, String key) {
        if (identity.getProperties() == null) {
            return null;
        }

        return identity.getProperties().stream()
            .filter(prop -> key.equals(prop.getKey()))
            .map(LettaIdentityProperty::getValue)
            .findFirst()
            .orElse(null);
    }

    public List<LettaIdentityResponse> testLettaConnection() {
        return lettaIdentityService.listIdentities();
    }
}
