package com.health.agents.integration.letta;

import com.health.agents.integration.letta.model.*;
import retrofit2.Call;
import retrofit2.http.*;
import java.util.List;

/**
 * Retrofit interface for Letta API integration
 */
public interface LettaApiClient {
    
    // Identity Management
    @GET("/v1/identities")
    Call<List<LettaIdentityResponse>> listIdentities();
    
    @POST("/v1/identities")
    Call<LettaIdentityResponse> createIdentity(@Body LettaIdentityRequest request);
    
    @GET("/v1/identities/{identityId}")
    Call<LettaIdentityResponse> getIdentity(@Path("identityId") String identityId);
    
    @PUT("/v1/identities/{identityId}/properties")
    Call<Void> upsertIdentityProperties(@Path("identityId") String identityId, 
                                       @Body List<LettaIdentityProperty> properties);
    
    // Agent Management
    @POST("/v1/agents")
    Call<LettaAgentResponse> createAgent(@Body LettaAgentRequest request);
    
    @GET("/v1/agents/{agentId}")
    Call<LettaAgentResponse> getAgent(@Path("agentId") String agentId);
    
    @GET("/v1/agents")
    Call<List<LettaAgentResponse>> listAgents(@Query("identity_id") String identityId);
    
    @DELETE("/v1/agents/{agentId}")
    Call<Void> deleteAgent(@Path("agentId") String agentId);
    
    // Message Handling
    @POST("/v1/agents/{agentId}/messages")
    Call<LettaMessageResponse> sendMessage(@Path("agentId") String agentId, 
                                         @Body LettaMessageRequest request);
    
    // Memory Block Management (if available in Letta API)
    @PATCH("/v1/agents/{agentId}/memory")
    Call<Void> updateAgentMemory(@Path("agentId") String agentId,
                                @Body Object memoryUpdate);
} 