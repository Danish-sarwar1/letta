# Multi-Agent Health Consultation System - Complete Implementation Plan

## **Project Overview**
A Java Spring Boot application leveraging Letta's native Identity system and API to create a sophisticated multi-agent health consultation system with user-specific dedicated agent flows and session-based context management.

## **Key Findings from Letta API Research**

### **✅ Confirmed Capabilities**
- **Identity Management**: Full Identity API with properties support (`POST /identities`, `PUT /identities/properties/upsert`)
- **Agent Creation**: Comprehensive agent creation with memory blocks, tools, and identity association (`POST /agents`)
- **Memory Management**: Memory blocks (core memory) and automatic archival memory with self-editing capabilities
- **Session Handling**: While Letta doesn't have traditional "sessions", it supports stateful agents with persistent memory
- **Message API**: Full messaging API (`POST /agents/{agent_id}/messages`) with various message types
- **Model Support**: Model-agnostic with default models available

### **🔄 Architecture Adaptations Required**
Based on Letta's architecture, we need to adapt the session concept:
- **Letta Philosophy**: Agents are stateful by design with persistent memory, not session-based
- **Session Simulation**: We'll use memory blocks and metadata to simulate session boundaries
- **Context Management**: Use Letta's memory blocks for session context and archival memory for historical data

## **Revised Architecture Design**

### **1. User-Centric Agent Ecosystem**
Each user gets:
- **1 Letta Identity** (using Identity API)
- **4 Dedicated Agents** associated with that identity:
  1. **Context Coordinator Agent** - Manages conversation flow and session context
  2. **Intent Classifier Agent** - Determines query intent and routes appropriately  
  3. **General Health Agent** - Handles general health queries with archival memory
  4. **Mental Health Agent** - Handles mental health queries with archival memory

### **2. Session Management via Memory Blocks**
Since Letta doesn't have traditional sessions, we'll implement session-like behavior:
- **Session Context Block**: A dedicated memory block storing current session information
- **Session Metadata**: Store session ID, start time, and status in agent metadata
- **Session Boundaries**: Use memory block updates to mark session start/end
- **Context Persistence**: Automatic migration from session context to archival memory

### **3. API Design**
```
POST /api/v1/chat/start
{
  "userId": "user123",
  "sessionId": "session456"
}

POST /api/v1/chat/message  
{
  "userId": "user123",
  "sessionId": "session456",
  "message": "I have been feeling tired lately"
}

POST /api/v1/chat/end
{
  "userId": "user123", 
  "sessionId": "session456"
}
```

## **Technical Implementation Plan**

### **Phase 1: Project Structure & Dependencies**

#### **1.1 Spring Boot Project Structure**
```
letta-health-agents/
├── src/main/java/com/health/agents/
│   ├── LettaHealthAgentsApplication.java
│   ├── config/
│   │   ├── LettaConfig.java
│   │   └── RetrofitConfig.java
│   ├── controller/
│   │   └── ChatController.java
│   ├── service/
│   │   ├── UserIdentityService.java
│   │   ├── AgentOrchestrationService.java
│   │   ├── SessionManagementService.java
│   │   └── agents/
│   │       ├── ContextCoordinatorService.java
│   │       ├── IntentClassifierService.java
│   │       ├── GeneralHealthAgentService.java
│   │       └── MentalHealthAgentService.java
│   ├── model/
│   │   ├── dto/
│   │   │   ├── ChatRequest.java
│   │   │   ├── ChatResponse.java
│   │   │   ├── StartChatRequest.java
│   │   │   └── SessionContext.java
│   │   └── enums/
│   │       ├── AgentType.java
│   │       ├── IntentType.java
│   │       └── SessionStatus.java
│   └── integration/
│       └── letta/
│           ├── LettaApiClient.java
│           ├── model/
│           │   ├── LettaIdentityRequest.java
│           │   ├── LettaIdentityResponse.java
│           │   ├── LettaIdentityProperty.java
│           │   ├── LettaAgentRequest.java
│           │   ├── LettaAgentResponse.java
│           │   ├── LettaMessageRequest.java
│           │   ├── LettaMessageResponse.java
│           │   └── LettaMemoryBlock.java
│           └── service/
│               ├── LettaIdentityService.java
│               ├── LettaAgentService.java
│               └── LettaMemoryService.java
├── src/main/resources/
│   ├── application.yml
│   └── agent-prompts/
│       ├── context-coordinator-prompt.txt
│       ├── intent-classifier-prompt.txt
│       ├── general-health-prompt.txt
│       └── mental-health-prompt.txt
└── pom.xml
```

#### **1.2 Dependencies (pom.xml)**
```xml
<dependencies>
    <!-- Spring Boot Core -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-validation</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-cache</artifactId>
    </dependency>
    
    <!-- Retrofit for Letta API Integration -->
    <dependency>
        <groupId>com.squareup.retrofit2</groupId>
        <artifactId>retrofit</artifactId>
        <version>2.9.0</version>
    </dependency>
    <dependency>
        <groupId>com.squareup.retrofit2</groupId>
        <artifactId>converter-jackson</artifactId>
        <version>2.9.0</version>
    </dependency>
    <dependency>
        <groupId>com.squareup.okhttp3</groupId>
        <artifactId>logging-interceptor</artifactId>
        <version>4.12.0</version>
    </dependency>
    
    <!-- Utilities -->
    <dependency>
        <groupId>org.apache.commons</groupId>
        <artifactId>commons-lang3</artifactId>
    </dependency>
    <dependency>
        <groupId>com.fasterxml.jackson.datatype</groupId>
        <artifactId>jackson-datatype-jsr310</artifactId>
    </dependency>
    
    <!-- In-Memory Caching -->
    <dependency>
        <groupId>com.github.ben-manes.caffeine</groupId>
        <artifactId>caffeine</artifactId>
    </dependency>
</dependencies>
```

### **Phase 2: Letta API Integration**

#### **2.1 Letta API Client Interface**
```java
public interface LettaApiClient {
    
    // Identity Management
    @GET("/v1/identities")
    Call<List<LettaIdentityResponse>> listIdentities();
    
    @POST("/v1/identities")
    Call<LettaIdentityResponse> createIdentity(@Body LettaIdentityRequest request);
    
    @GET("/v1/identities/{identityId}")
    Call<LettaIdentityResponse> getIdentity(@Path("identityId") String identityId);
    
    @PUT("/v1/identities/{identityId}/properties/upsert")
    Call<Void> upsertIdentityProperties(@Path("identityId") String identityId, 
                                       @Body List<LettaIdentityProperty> properties);
    
    // Agent Management
    @POST("/v1/agents")
    Call<LettaAgentResponse> createAgent(@Body LettaAgentRequest request);
    
    @GET("/v1/agents/{agentId}")
    Call<LettaAgentResponse> getAgent(@Path("agentId") String agentId);
    
    @GET("/v1/agents")
    Call<List<LettaAgentResponse>> listAgents(@Query("identity_id") String identityId);
    
    // Message Handling
    @POST("/v1/agents/{agentId}/messages")
    Call<LettaMessageResponse> sendMessage(@Path("agentId") String agentId, 
                                         @Body LettaMessageRequest request);
    
    // Memory Block Management
    @POST("/v1/blocks")
    Call<LettaBlockResponse> createBlock(@Body LettaBlockRequest request);
    
    @PATCH("/v1/agents/{agentId}/blocks/{blockId}")
    Call<Void> updateAgentBlock(@Path("agentId") String agentId,
                               @Path("blockId") String blockId,
                               @Body LettaBlockUpdateRequest request);
}
```

#### **2.2 Letta Data Models**
```java
@Data
@Builder
public class LettaIdentityRequest {
    private String name;
    private String description;
}

@Data
@Builder
public class LettaIdentityResponse {
    private String id;
    private String name;
    private String description;
    private LocalDateTime createdAt;
    private List<LettaIdentityProperty> properties;
}

@Data
@Builder
public class LettaIdentityProperty {
    private String key;
    private String value;
    private String type; // "string", "number", "boolean"
}

@Data
@Builder
public class LettaAgentRequest {
    private String name;
    private String description;
    private List<String> identityIds;
    private List<LettaMemoryBlock> memoryBlocks;
    private String model;
    private String embedding;
    private List<String> tools;
    private Map<String, Object> metadata;
}

@Data
@Builder
public class LettaMemoryBlock {
    private String label;
    private String value;
    private String description;
    private Integer limit;
    private Boolean readOnly;
}

@Data
@Builder
public class LettaMessageRequest {
    private List<LettaMessage> messages;
    private String senderId;
}

@Data
@Builder
public class LettaMessage {
    private String role; // "user", "assistant", "system"
    private String content;
}
```

### **Phase 3: Core Services Implementation**

#### **3.1 User Identity Service**
```java
@Service
public class UserIdentityService {
    
    @Autowired
    private LettaIdentityService lettaIdentityService;
    
    @Autowired
    private LettaAgentService lettaAgentService;
    
    @Cacheable("userAgentMappings")
    public UserAgentMapping getOrCreateUserAgents(String userId) {
        // 1. Get or create Letta Identity
        LettaIdentityResponse identity = getOrCreateLettaIdentity(userId);
        
        // 2. Check if identity has agent mappings in properties
        UserAgentMapping mapping = getAgentMappingFromIdentity(identity);
        
        // 3. If no agents exist, create them
        if (mapping == null) {
            mapping = createAgentsForUser(userId, identity.getId());
            storeAgentMappingInIdentity(identity.getId(), mapping);
        }
        
        return mapping;
    }
    
    private LettaIdentityResponse getOrCreateLettaIdentity(String userId) {
        // Search for existing identity by user_id property
        List<LettaIdentityResponse> identities = lettaIdentityService.listIdentities();
        Optional<LettaIdentityResponse> existing = identities.stream()
            .filter(identity -> userId.equals(getPropertyValue(identity, "user_id")))
            .findFirst();
            
        if (existing.isPresent()) {
            return existing.get();
        }
        
        // Create new identity with user_id property
        LettaIdentityRequest request = LettaIdentityRequest.builder()
            .name("Health User: " + userId)
            .description("Health consultation user identity")
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
    
    private UserAgentMapping createAgentsForUser(String userId, String identityId) {
        // Create 4 specialized agents for this user
        String contextCoordinatorId = createContextCoordinatorAgent(userId, identityId);
        String intentClassifierId = createIntentClassifierAgent(userId, identityId);
        String generalHealthId = createGeneralHealthAgent(userId, identityId);
        String mentalHealthId = createMentalHealthAgent(userId, identityId);
        
        return UserAgentMapping.builder()
            .userId(userId)
            .identityId(identityId)
            .contextCoordinatorId(contextCoordinatorId)
            .intentClassifierId(intentClassifierId)
            .generalHealthId(generalHealthId)
            .mentalHealthId(mentalHealthId)
            .build();
    }
    
    private String createContextCoordinatorAgent(String userId, String identityId) {
        LettaAgentRequest request = LettaAgentRequest.builder()
            .name("context-coordinator-" + userId)
            .description("Context coordination agent for user " + userId)
            .identityIds(Arrays.asList(identityId))
            .memoryBlocks(Arrays.asList(
                LettaMemoryBlock.builder()
                    .label("session_context")
                    .value("No active session")
                    .description("Current session context and conversation state")
                    .limit(8000)
                    .build(),
                LettaMemoryBlock.builder()
                    .label("user_profile")
                    .value("User ID: " + userId)
                    .description("User profile and preferences")
                    .limit(4000)
                    .build()
            ))
            .model("openai/gpt-4o-mini") // Using available model
            .embedding("openai/text-embedding-3-small")
            .tools(Arrays.asList("core_memory_replace", "core_memory_append"))
            .metadata(Map.of(
                "agent_type", "context_coordinator",
                "user_id", userId
            ))
            .build();
            
        return lettaAgentService.createAgent(request).getId();
    }
    
    private String createIntentClassifierAgent(String userId, String identityId) {
        LettaAgentRequest request = LettaAgentRequest.builder()
            .name("intent-classifier-" + userId)
            .description("Intent classification agent for user " + userId)
            .identityIds(Arrays.asList(identityId))
            .memoryBlocks(Arrays.asList(
                LettaMemoryBlock.builder()
                    .label("classification_patterns")
                    .value("Intent classification patterns for health queries")
                    .description("Learned patterns for intent classification")
                    .limit(4000)
                    .build()
            ))
            .model("openai/gpt-4o-mini")
            .embedding("openai/text-embedding-3-small")
            .tools(Arrays.asList("core_memory_replace", "core_memory_append"))
            .metadata(Map.of(
                "agent_type", "intent_classifier",
                "user_id", userId
            ))
            .build();
            
        return lettaAgentService.createAgent(request).getId();
    }
    
    private String createGeneralHealthAgent(String userId, String identityId) {
        LettaAgentRequest request = LettaAgentRequest.builder()
            .name("general-health-" + userId)
            .description("General health consultation agent for user " + userId)
            .identityIds(Arrays.asList(identityId))
            .memoryBlocks(Arrays.asList(
                LettaMemoryBlock.builder()
                    .label("health_history")
                    .value("User health consultation history")
                    .description("Historical health conversations and patterns")
                    .limit(16000)
                    .build(),
                LettaMemoryBlock.builder()
                    .label("current_context")
                    .value("Current health consultation context")
                    .description("Current session health context")
                    .limit(8000)
                    .build()
            ))
            .model("openai/gpt-4o-mini")
            .embedding("openai/text-embedding-3-small")
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
        LettaAgentRequest request = LettaAgentRequest.builder()
            .name("mental-health-" + userId)
            .description("Mental health support agent for user " + userId)
            .identityIds(Arrays.asList(identityId))
            .memoryBlocks(Arrays.asList(
                LettaMemoryBlock.builder()
                    .label("mental_health_history")
                    .value("User mental health consultation history")
                    .description("Historical mental health conversations and therapeutic patterns")
                    .limit(16000)
                    .build(),
                LettaMemoryBlock.builder()
                    .label("therapeutic_context")
                    .value("Current therapeutic session context")
                    .description("Current session therapeutic context and emotional state")
                    .limit(8000)
                    .build()
            ))
            .model("openai/gpt-4o-mini")
            .embedding("openai/text-embedding-3-small")
            .tools(Arrays.asList("core_memory_replace", "core_memory_append"))
            .metadata(Map.of(
                "agent_type", "mental_health",
                "user_id", userId,
                "enable_archival", "true"
            ))
            .build();
            
        return lettaAgentService.createAgent(request).getId();
    }
}
```

#### **3.2 Session Management Service**
```java
@Service
public class SessionManagementService {
    
    @Autowired
    private LettaAgentService lettaAgentService;
    
    @Autowired
    private LettaMemoryService lettaMemoryService;
    
    public void startSession(String userId, String sessionId, UserAgentMapping agents) {
        // Update context coordinator with new session
        String sessionContext = String.format(
            "Session ID: %s\\nUser ID: %s\\nSession Start: %s\\nStatus: ACTIVE", 
            sessionId, userId, LocalDateTime.now()
        );
        
        lettaMemoryService.updateAgentMemoryBlock(
            agents.getContextCoordinatorId(),
            "session_context",
            sessionContext
        );
        
        // Load relevant historical context from archival memory
        loadHistoricalContext(agents, userId, sessionId);
    }
    
    public void endSession(String userId, String sessionId, UserAgentMapping agents) {
        // Archive current session context
        archiveSessionContext(agents, sessionId);
        
        // Clear session context
        String clearedContext = String.format(
            "Previous Session: %s\\nSession End: %s\\nStatus: ENDED", 
            sessionId, LocalDateTime.now()
        );
        
        lettaMemoryService.updateAgentMemoryBlock(
            agents.getContextCoordinatorId(),
            "session_context", 
            clearedContext
        );
    }
    
    private void loadHistoricalContext(UserAgentMapping agents, String userId, String sessionId) {
        // Query archival memory for relevant historical context
        // This leverages Letta's automatic archival memory system
        String contextQuery = String.format("User %s recent health conversations", userId);
        
        // Send context loading message to health agents
        LettaMessageRequest contextRequest = LettaMessageRequest.builder()
            .messages(Arrays.asList(
                LettaMessage.builder()
                    .role("system")
                    .content("Loading historical context for session: " + sessionId)
                    .build()
            ))
            .senderId(agents.getIdentityId())
            .build();
            
        // Both health agents load their respective historical contexts
        lettaAgentService.sendMessage(agents.getGeneralHealthId(), contextRequest);
        lettaAgentService.sendMessage(agents.getMentalHealthId(), contextRequest);
    }
    
    private void archiveSessionContext(UserAgentMapping agents, String sessionId) {
        // Get current session context from context coordinator
        String sessionSummary = String.format("Archiving session %s context", sessionId);
        
        LettaMessageRequest archiveRequest = LettaMessageRequest.builder()
            .messages(Arrays.asList(
                LettaMessage.builder()
                    .role("system")
                    .content("Archive session context: " + sessionSummary)
                    .build()
            ))
            .senderId(agents.getIdentityId())
            .build();
            
        // Send to health agents to archive relevant conversation context
        lettaAgentService.sendMessage(agents.getGeneralHealthId(), archiveRequest);
        lettaAgentService.sendMessage(agents.getMentalHealthId(), archiveRequest);
    }
}
```

#### **3.3 Agent Orchestration Service**
```java
@Service
public class AgentOrchestrationService {
    
    @Autowired
    private UserIdentityService userIdentityService;
    
    @Autowired
    private SessionManagementService sessionManagementService;
    
    @Autowired
    private LettaAgentService lettaAgentService;
    
    public ChatResponse startChat(String userId, String sessionId) {
        // 1. Get or create user's agents
        UserAgentMapping agents = userIdentityService.getOrCreateUserAgents(userId);
        
        // 2. Initialize session
        sessionManagementService.startSession(userId, sessionId, agents);
        
        return ChatResponse.builder()
            .message("Health consultation session started successfully")
            .sessionId(sessionId)
            .userId(userId)
            .sessionActive(true)
            .timestamp(LocalDateTime.now())
            .build();
    }
    
    public ChatResponse processMessage(String userId, String sessionId, String message) {
        // 1. Get user's agents
        UserAgentMapping agents = userIdentityService.getOrCreateUserAgents(userId);
        
        // 2. Send to Context Coordinator first
        String contextualMessage = processWithContextCoordinator(
            agents.getContextCoordinatorId(), 
            agents.getIdentityId(),
            message, 
            sessionId
        );
        
        // 3. Classify intent
        IntentResult intent = classifyIntent(
            agents.getIntentClassifierId(),
            agents.getIdentityId(), 
            contextualMessage
        );
        
        // 4. Route to appropriate health agent
        String response = routeToHealthAgent(agents, intent, contextualMessage);
        
        return ChatResponse.builder()
            .message(response)
            .sessionId(sessionId)
            .userId(userId)
            .intent(intent.getIntent().toString())
            .confidence(intent.getConfidence())
            .sessionActive(true)
            .timestamp(LocalDateTime.now())
            .build();
    }
    
    public void endChat(String userId, String sessionId) {
        UserAgentMapping agents = userIdentityService.getOrCreateUserAgents(userId);
        sessionManagementService.endSession(userId, sessionId, agents);
    }
    
    private String processWithContextCoordinator(String agentId, String identityId, 
                                               String message, String sessionId) {
        LettaMessageRequest request = LettaMessageRequest.builder()
            .messages(Arrays.asList(
                LettaMessage.builder()
                    .role("user")
                    .content(String.format("Session: %s\\nMessage: %s", sessionId, message))
                    .build()
            ))
            .senderId(identityId)
            .build();
            
        LettaMessageResponse response = lettaAgentService.sendMessage(agentId, request);
        
        // Extract contextual message from response
        return extractAssistantMessage(response);
    }
    
    private IntentResult classifyIntent(String agentId, String identityId, String contextualMessage) {
        LettaMessageRequest request = LettaMessageRequest.builder()
            .messages(Arrays.asList(
                LettaMessage.builder()
                    .role("user")
                    .content("Classify intent: " + contextualMessage)
                    .build()
            ))
            .senderId(identityId)
            .build();
            
        LettaMessageResponse response = lettaAgentService.sendMessage(agentId, request);
        
        // Parse intent classification from response
        return parseIntentFromResponse(response);
    }
    
    private String routeToHealthAgent(UserAgentMapping agents, IntentResult intent, 
                                    String contextualMessage) {
        String targetAgentId;
        
        switch (intent.getIntent()) {
            case GENERAL_HEALTH:
                targetAgentId = agents.getGeneralHealthId();
                break;
            case MENTAL_HEALTH:
                targetAgentId = agents.getMentalHealthId();
                break;
            default:
                targetAgentId = agents.getGeneralHealthId(); // Default to general health
        }
        
        LettaMessageRequest request = LettaMessageRequest.builder()
            .messages(Arrays.asList(
                LettaMessage.builder()
                    .role("user")
                    .content(contextualMessage)
                    .build()
            ))
            .senderId(agents.getIdentityId())
            .build();
            
        LettaMessageResponse response = lettaAgentService.sendMessage(targetAgentId, request);
        
        return extractAssistantMessage(response);
    }
}
```

### **Phase 4: Main Controller**

#### **4.1 Chat Controller**
```java
@RestController
@RequestMapping("/api/v1/chat")
@CrossOrigin(origins = "*")
@Validated
public class ChatController {
    
    @Autowired
    private AgentOrchestrationService orchestrationService;
    
    @PostMapping("/start")
    public ResponseEntity<ChatResponse> startChat(@Valid @RequestBody StartChatRequest request) {
        ChatResponse response = orchestrationService.startChat(
            request.getUserId(), 
            request.getSessionId()
        );
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/message")
    public ResponseEntity<ChatResponse> sendMessage(@Valid @RequestBody ChatRequest request) {
        ChatResponse response = orchestrationService.processMessage(
            request.getUserId(),
            request.getSessionId(), 
            request.getMessage()
        );
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/end")
    public ResponseEntity<Void> endChat(@Valid @RequestBody EndChatRequest request) {
        orchestrationService.endChat(request.getUserId(), request.getSessionId());
        return ResponseEntity.ok().build();
    }
}
```

### **Phase 5: Configuration**

#### **5.1 Application Configuration**
```yaml
# application.yml
spring:
  application:
    name: letta-health-agents
  cache:
    type: caffeine
    caffeine:
      spec: maximumSize=1000,expireAfterWrite=1h

letta:
  api:
    base-url: ${LETTA_API_URL:http://localhost:8283}
    api-key: ${LETTA_API_KEY:}
    timeout: 60s
    default-model: "openai/gpt-4o-mini"
    default-embedding: "openai/text-embedding-3-small"

logging:
  level:
    com.health.agents: DEBUG
    retrofit2: DEBUG
```

#### **5.2 Letta Configuration**
```java
@Configuration
public class LettaConfig {
    
    @Value("${letta.api.base-url}")
    private String lettaBaseUrl;
    
    @Value("${letta.api.api-key}")
    private String lettaApiKey;
    
    @Value("${letta.api.timeout}")
    private Duration timeout;
    
    @Bean
    public OkHttpClient okHttpClient() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder()
            .connectTimeout(timeout)
            .readTimeout(timeout)
            .writeTimeout(timeout);
            
        if (!lettaApiKey.isEmpty()) {
            builder.addInterceptor(chain -> {
                Request original = chain.request();
                Request request = original.newBuilder()
                    .header("Authorization", "Bearer " + lettaApiKey)
                    .build();
                return chain.proceed(request);
            });
        }
        
        // Add logging
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);
        builder.addInterceptor(logging);
        
        return builder.build();
    }
    
    @Bean
    public Retrofit retrofit(OkHttpClient okHttpClient) {
        return new Retrofit.Builder()
            .baseUrl(lettaBaseUrl)
            .client(okHttpClient)
            .addConverterFactory(JacksonConverterFactory.create())
            .build();
    }
    
    @Bean
    public LettaApiClient lettaApiClient(Retrofit retrofit) {
        return retrofit.create(LettaApiClient.class);
    }
}
```

## **Key Features & Benefits**

### **✅ Complete Letta Integration**
1. **Identity Management** → Native Letta Identity API with properties
2. **Agent Storage** → Agent IDs stored in Identity properties  
3. **Session Context** → Simulated via memory blocks and metadata
4. **Historical Data** → Automatic Letta archival memory
5. **User Persistence** → Handled by Letta Identity system

### **✅ Zero External Database Requirements**
- **No PostgreSQL/H2**: Everything managed by Letta
- **No Redis**: Minimal caching for performance only
- **No Complex State Management**: Letta handles persistence
- **No Manual Memory Migration**: Letta's automatic lifecycle

### **✅ Scalable Architecture**
1. **User ID** → Letta Identity → Agent Properties → Dedicated Agents
2. **Session Simulation** → Memory blocks + metadata
3. **Historical Context** → Letta archival memory
4. **Agent Orchestration** → Direct API calls

## **API Usage Examples**

### **Start Chat Session**
```bash
POST /api/v1/chat/start
{
    "userId": "user123",
    "sessionId": "session456"
}

# Behind the scenes:
# 1. Find/Create Letta Identity for "user123"
# 2. Check Identity properties for agent mappings
# 3. Create 4 agents if needed, store IDs in Identity properties
# 4. Initialize session context in Context Coordinator memory blocks
```

### **Send Message**
```bash
POST /api/v1/chat/message
{
    "userId": "user123",
    "sessionId": "session456",
    "message": "I have been feeling tired lately"
}

# Behind the scenes:
# 1. Get Letta Identity for "user123"
# 2. Get agent IDs from Identity properties
# 3. Route: Context Coordinator → Intent Classifier → Health Agent
# 4. All context managed in Letta memory blocks and archival memory
```

### **End Chat Session**
```bash
POST /api/v1/chat/end
{
    "userId": "user123", 
    "sessionId": "session456"
}

# Behind the scenes:
# 1. Archive session context to Letta archival memory
# 2. Clear session context in memory blocks
# 3. Update session status in agent metadata
```

## **Implementation Steps**

### **Step 1: Environment Setup**
1. Set up Letta server (local or cloud)
2. Create Spring Boot project with dependencies
3. Configure Retrofit for Letta API integration

### **Step 2: Core Integration**
1. Implement Letta API client and models
2. Build Identity and Agent services
3. Create session management using memory blocks

### **Step 3: Agent Orchestration**
1. Implement the 4 specialized agents
2. Build routing and orchestration logic
3. Create chat controller and API endpoints

### **Step 4: Testing & Deployment**
1. Test complete user flow with multiple sessions
2. Verify memory persistence and archival
3. Deploy and monitor system performance

This architecture fully leverages Letta's capabilities while meeting all your requirements for multi-agent health consultation with session management and persistent memory! 