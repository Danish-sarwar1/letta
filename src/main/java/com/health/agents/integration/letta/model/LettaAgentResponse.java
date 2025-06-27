package com.health.agents.integration.letta.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class LettaAgentResponse {
    private String id;
    private String name;
    private String system;
    private String description;
    
    @JsonProperty("agent_type")
    private String agentType;
    
    @JsonProperty("llm_config")
    private LettaLlmConfig llmConfig;
    
    @JsonProperty("embedding_config")
    private LettaEmbeddingConfig embeddingConfig;
    
    private LettaMemory memory;
    
    // Tools in response are complex objects, not just strings
    private List<LettaTool> tools;
    
    // Sources, tags, and other response fields
    private List<Object> sources; // Simplified as Object for now
    private List<String> tags;
    
    @JsonProperty("created_by_id")
    private String createdById;
    
    @JsonProperty("last_updated_by_id")
    private String lastUpdatedById;
    
    @JsonProperty("created_at")
    private String createdAt;
    
    @JsonProperty("updated_at")
    private String updatedAt;
    
    @JsonProperty("tool_rules")
    private List<Map<String, Object>> toolRules;
    
    @JsonProperty("message_ids")
    private List<String> messageIds;
    
    @JsonProperty("response_format")
    private Map<String, Object> responseFormat;
    
    private Map<String, Object> metadata;
    
    @JsonProperty("tool_exec_environment_variables")
    private List<Map<String, Object>> toolExecEnvironmentVariables;
    
    @JsonProperty("project_id")
    private String projectId;
    
    @JsonProperty("template_id")
    private String templateId;
    
    @JsonProperty("base_template_id")
    private String baseTemplateId;
    
    @JsonProperty("identity_ids")
    private List<String> identityIds;
    
    @JsonProperty("message_buffer_autoclear")
    private Boolean messageBufferAutoclear;
    
    @JsonProperty("enable_sleeptime")
    private Boolean enableSleeptime;
    
    @JsonProperty("multi_agent_group")
    private Map<String, Object> multiAgentGroup;
    
    @JsonProperty("last_run_completion")
    private String lastRunCompletion;
    
    @JsonProperty("last_run_duration_ms")
    private Long lastRunDurationMs;
    
    private String timezone;
}

