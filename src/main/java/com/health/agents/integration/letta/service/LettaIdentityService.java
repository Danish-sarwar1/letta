package com.health.agents.integration.letta.service;

import com.health.agents.integration.letta.LettaApiClient;
import com.health.agents.integration.letta.model.LettaIdentityProperty;
import com.health.agents.integration.letta.model.LettaIdentityRequest;
import com.health.agents.integration.letta.model.LettaIdentityResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import retrofit2.Response;

import java.io.IOException;
import java.util.List;

@Service
@Slf4j
public class LettaIdentityService {
    
    @Autowired
    private LettaApiClient lettaApiClient;
    
    public List<LettaIdentityResponse> listIdentities() {
        try {
            Response<List<LettaIdentityResponse>> response = lettaApiClient.listIdentities().execute();
            if (response.isSuccessful()) {
                return response.body();
            } else {
                log.error("Failed to list identities: {}", response.errorBody().string());
                throw new RuntimeException("Failed to list identities: " + response.code());
            }
        } catch (IOException e) {
            log.error("Error listing identities", e);
            throw new RuntimeException("Error listing identities", e);
        }
    }
    
    public LettaIdentityResponse createIdentity(LettaIdentityRequest request) {
        try {
            Response<LettaIdentityResponse> response = lettaApiClient.createIdentity(request).execute();
            if (response.isSuccessful()) {
                log.info("Created identity: {}", response.body().getId());
                return response.body();
            } else {
                log.error("Failed to create identity: {}", response.errorBody().string());
                throw new RuntimeException("Failed to create identity: " + response.code());
            }
        } catch (IOException e) {
            log.error("Error creating identity", e);
            throw new RuntimeException("Error creating identity", e);
        }
    }
    
    public LettaIdentityResponse getIdentity(String identityId) {
        try {
            Response<LettaIdentityResponse> response = lettaApiClient.getIdentity(identityId).execute();
            if (response.isSuccessful()) {
                return response.body();
            } else {
                log.error("Failed to get identity {}: {}", identityId, response.errorBody().string());
                throw new RuntimeException("Failed to get identity: " + response.code());
            }
        } catch (IOException e) {
            log.error("Error getting identity: {}", identityId, e);
            throw new RuntimeException("Error getting identity", e);
        }
    }
    
    public void upsertIdentityProperties(String identityId, List<LettaIdentityProperty> properties) {
        try {
            Response<Void> response = lettaApiClient.upsertIdentityProperties(identityId, properties).execute();
            if (response.isSuccessful()) {
                log.info("Updated identity properties for: {}", identityId);
            } else {
                log.error("Failed to update identity properties for {}: {}", identityId, response.errorBody().string());
                throw new RuntimeException("Failed to update identity properties: " + response.code());
            }
        } catch (IOException e) {
            log.error("Error updating identity properties for: {}", identityId, e);
            throw new RuntimeException("Error updating identity properties", e);
        }
    }
} 