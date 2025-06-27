package com.health.agents.integration.letta.service;

import com.health.agents.integration.letta.LettaApiClient;
import com.health.agents.integration.letta.model.LettaAgentRequest;
import com.health.agents.integration.letta.model.LettaAgentResponse;
import com.health.agents.integration.letta.model.LettaMessageRequest;
import com.health.agents.integration.letta.model.LettaMessageResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import retrofit2.Response;

import java.io.IOException;
import java.util.List;

@Service
@Slf4j
public class LettaAgentService {
    
    @Autowired
    private LettaApiClient lettaApiClient;
    
    public LettaAgentResponse createAgent(LettaAgentRequest request) {
        try {
            Response<LettaAgentResponse> response = lettaApiClient.createAgent(request).execute();
            if (response.isSuccessful()) {
                log.info("Created agent: {} for identity: {}", response.body().getId(), request.getIdentityIds());
                return response.body();
            } else {
                log.error("Failed to create agent: {}", response.errorBody().string());
                throw new RuntimeException("Failed to create agent: " + response.code());
            }
        } catch (IOException e) {
            log.error("Error creating agent", e);
            throw new RuntimeException("Error creating agent", e);
        }
    }
    
    public LettaAgentResponse getAgent(String agentId) {
        try {
            Response<LettaAgentResponse> response = lettaApiClient.getAgent(agentId).execute();
            if (response.isSuccessful()) {
                return response.body();
            } else {
                log.error("Failed to get agent {}: {}", agentId, response.errorBody().string());
                throw new RuntimeException("Failed to get agent: " + response.code());
            }
        } catch (IOException e) {
            log.error("Error getting agent: {}", agentId, e);
            throw new RuntimeException("Error getting agent", e);
        }
    }
    
    public List<LettaAgentResponse> listAgentsByIdentity(String identityId) {
        try {
            Response<List<LettaAgentResponse>> response = lettaApiClient.listAgents(identityId).execute();
            if (response.isSuccessful()) {
                return response.body();
            } else {
                log.error("Failed to list agents for identity {}: {}", identityId, response.errorBody().string());
                throw new RuntimeException("Failed to list agents: " + response.code());
            }
        } catch (IOException e) {
            log.error("Error listing agents for identity: {}", identityId, e);
            throw new RuntimeException("Error listing agents", e);
        }
    }
    
    public LettaMessageResponse sendMessage(String agentId, LettaMessageRequest request) {
        try {
            log.debug("Sending message to agent {}: {}", agentId, request.getMessages().get(0).getContent());
            Response<LettaMessageResponse> response = lettaApiClient.sendMessage(agentId, request).execute();
            if (response.isSuccessful()) {
                log.debug("Received response from agent {}", agentId);
                return response.body();
            } else {
                log.error("Failed to send message to agent {}: {}", agentId, response.errorBody().string());
                throw new RuntimeException("Failed to send message: " + response.code());
            }
        } catch (IOException e) {
            log.error("Error sending message to agent: {}", agentId, e);
            throw new RuntimeException("Error sending message", e);
        }
    }
    
    public void deleteAgent(String agentId) {
        try {
            Response<Void> response = lettaApiClient.deleteAgent(agentId).execute();
            if (response.isSuccessful()) {
                log.info("Deleted agent: {}", agentId);
            } else {
                log.error("Failed to delete agent {}: {}", agentId, response.errorBody().string());
                throw new RuntimeException("Failed to delete agent: " + response.code());
            }
        } catch (IOException e) {
            log.error("Error deleting agent: {}", agentId, e);
            throw new RuntimeException("Error deleting agent", e);
        }
    }
} 