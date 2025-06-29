# Memory Architecture Documentation
## Sub-Plan 1: Memory Architecture Design - Implementation Complete

### 🎯 **Overview**
This document describes the enhanced memory architecture implemented for centralized context management in the Letta health agent system. This implementation represents the completion of Sub-Plan 1 from the Centralized Context Management Plan.

---

## 🏗️ **Architecture Components**

### **1. Enhanced Data Models**

#### **ConversationTurn** (Enhanced)
```java
@Data
@Builder
public class ConversationTurn {
  private int turnNumber;                    // Sequential turn identifier
  private String userMessage;               // Original user message
  private String enrichedMessage;           // Context-enriched message
  private String extractedIntent;           // Classified intent
  private Double intentConfidence;          // Intent confidence score
  private String routedAgent;               // Target agent for response
  private String agentResponse;             // Agent's response
  private LocalDateTime timestamp;          // Turn timestamp
  private String sessionId;                 // Session identifier
  private String contextUsed;               // Context provided for this turn
  private String reasoning;                 // Context selection reasoning
  
  // Metadata for memory management
  private String turnType;                  // USER_MESSAGE, AGENT_RESPONSE, SYSTEM_MESSAGE
  private boolean isArchived;               // Archival status
  private String topicTags;                 // Topic classification tags
  private String emotionalState;            // Emotional context indicators
  private String urgencyLevel;              // Urgency/priority level
}
```

#### **ConversationHistory** (Existing)
```java
@Data
@Builder
public class ConversationHistory {
  private String sessionId;                 // Session identifier
  private String userId;                    // User identifier
  private List<ConversationTurn> turns;     // Complete turn history
  private LocalDateTime createdAt;          // Conversation start time
  private LocalDateTime lastUpdated;        // Last update timestamp
  private String status;                    // Session status
  private int totalTurns;                   // Total turn count
}
```

### **2. Memory Architecture Configuration**

#### **MemoryArchitectureConfig** (New)
```java
@Configuration
@Getter
public class MemoryArchitectureConfig {
    // Memory Block Size Limits (configurable via application.yml)
    private int conversationHistoryLimit = 32000;    // 32KB
    private int activeSessionLimit = 4000;           // 4KB
    private int contextSummaryLimit = 8000;          // 8KB
    private int memoryMetadataLimit = 2000;          // 2KB
    private int agentInstructionsLimit = 16000;      // 16KB
    
    // Memory Management Settings
    private double memoryRotationThreshold = 0.8;   // Rotate at 80% capacity
    private int archivalTriggerTurns = 50;           // Archive after 50 turns
    private int recentTurnsWindow = 5;               // Recent context window
    private int maxRelevantTurns = 10;               // Maximum relevant turns
    
    // Data Format Options
    public enum DataFormat { JSON, STRUCTURED_TEXT }
    private DataFormat dataFormat = STRUCTURED_TEXT;
}
```

### **3. Memory Management Service**

#### **MemoryManagementService** (New)
Core service for memory block operations:

**Key Methods:**
- `formatConversationTurn(ConversationTurn)` - Format turns for memory storage
- `formatConversationHistory(ConversationHistory)` - Format complete history
- `createActiveSessionContent(...)` - Generate session metadata
- `createContextSummary(ConversationHistory)` - Create human-readable summaries
- `createMemoryMetadata(...)` - Generate memory usage metadata
- `isMemoryRotationNeeded(...)` - Check rotation triggers
- `isArchivalNeeded(...)` - Check archival triggers

**Data Format Support:**
- **STRUCTURED_TEXT**: Human-readable format optimized for Letta processing
- **JSON**: Machine-readable format for complex data structures

---

## 📊 **Enhanced Memory Block Structure**

### **Context Extractor Agent Memory Blocks**

#### **1. agent_instructions** (16KB)
- **Purpose**: Enhanced agent instructions for centralized context management
- **Content**: Complete prompt with memory management guidelines
- **Read-Only**: True
- **Update Frequency**: Only during agent creation/updates

#### **2. conversation_history** (32KB)
- **Purpose**: Complete conversation with turn-by-turn tracking
- **Format**: `TURN_N: USER: [message] | ENRICHED: [context] | INTENT: [classification] | AGENT: [response] | META: [metadata]`
- **Management**: Append-only during session, rotate when approaching limits
- **Update Frequency**: Every user message and agent response

#### **3. active_session** (4KB)
- **Purpose**: Current session state and metadata
- **Format**: `SESSION_ID: [id] | STATUS: [status] | TOPIC: [topic] | TURNS: [count] | UPDATED: [timestamp]`
- **Management**: Update with each conversation turn
- **Update Frequency**: Every message processing cycle

#### **4. context_summary** (8KB)
- **Purpose**: Human-readable conversation summary
- **Format**: Natural language summary with key topics, medical history, emotional context
- **Management**: Update every 5 turns or significant topic changes
- **Update Frequency**: Periodic (every 5 turns) or topic-driven

#### **5. memory_metadata** (2KB)
- **Purpose**: Memory management metadata and statistics
- **Format**: Memory usage percentages, rotation flags, archival status
- **Management**: Update with every memory operation
- **Update Frequency**: Every memory block update

---

## ⚙️ **Configuration Properties**

### **application.yml** (Enhanced)
```yaml
# Memory Architecture Configuration for Centralized Context Management (Sub-Plan 1)
memory:
  # Memory Block Size Limits (in characters)
  conversation-history:
    limit: 32000
  active-session:
    limit: 4000
  context-summary:
    limit: 8000
  memory-metadata:
    limit: 2000
  agent-instructions:
    limit: 16000
  
  # Memory Management Settings
  rotation:
    threshold: 0.8  # Rotate when 80% full
  archival:
    trigger-turns: 50  # Archive after 50 turns
  
  # Context Selection Strategy
  context-window:
    recent-turns: 5     # Recent turns to prioritize
    max-relevant: 10    # Maximum relevant turns to include
    
  # Data Format (JSON or STRUCTURED_TEXT)
  data-format: STRUCTURED_TEXT
```

---

## 🔄 **Memory Management Operations**

### **1. Memory Rotation Strategy**
- **Trigger Condition**: When conversation_history reaches 80% of 32KB limit (25,600 characters)
- **Rotation Action**: Move oldest 25% of conversation to archival, maintain recent 75%
- **Preservation Rules**: Always preserve last 10 turns and any emergency/critical information
- **Implementation**: Automatic via memory usage monitoring

### **2. Archival Triggers**
- **Turn Count**: After 50 conversation turns
- **Memory Pressure**: When approaching memory limits across multiple blocks
- **Session End**: When session is explicitly ended
- **Topic Completion**: When major health topics are resolved

### **3. Context Selection Algorithm**

#### **Priority Levels:**
1. **Recent Messages** (last 3-5 messages) - Immediate context
2. **Topic-Related Messages** - Same health concern or related topics
3. **Medical History** - Previous symptoms, treatments, medications
4. **Emotional Patterns** - Mood, stress levels, psychological state
5. **Follow-up References** - Questions referring to previous topics
6. **Emergency Indicators** - Concerning symptoms or crisis mentions

#### **Context Enrichment Rules:**
- **Follow-up questions**: Include original topic context and agent responses
- **New topics**: Provide transition context and maintain continuity
- **Clarifications**: Include specific context being clarified
- **Emotional expressions**: Include related emotional history

---

## 🎯 **Implementation Details**

### **1. Enhanced Agent Creation**
```java
private String createContextCoordinatorAgent(String userId, String identityId) {
    // Enhanced memory blocks with Sub-Plan 1 architecture
    .memoryBlocks(Arrays.asList(
        // Agent Instructions (16KB)
        LettaMemoryBlock.builder()
            .label(MemoryArchitectureConfig.AGENT_INSTRUCTIONS)
            .value(enhancedPrompt)
            .limit(memoryArchitectureConfig.getAgentInstructionsLimit())
            .build(),
            
        // Conversation History (32KB)  
        LettaMemoryBlock.builder()
            .label(MemoryArchitectureConfig.CONVERSATION_HISTORY)
            .value("No messages yet")
            .limit(memoryArchitectureConfig.getConversationHistoryLimit())
            .build(),
            
        // Active Session (4KB)
        LettaMemoryBlock.builder()
            .label(MemoryArchitectureConfig.ACTIVE_SESSION)
            .value(initialSessionContent)
            .limit(memoryArchitectureConfig.getActiveSessionLimit())
            .build(),
            
        // Context Summary (8KB)
        LettaMemoryBlock.builder()
            .label(MemoryArchitectureConfig.CONTEXT_SUMMARY)
            .value("No context available")
            .limit(memoryArchitectureConfig.getContextSummaryLimit())
            .build(),
            
        // Memory Metadata (2KB)
        LettaMemoryBlock.builder()
            .label(MemoryArchitectureConfig.MEMORY_METADATA)
            .value(initialMetadata)
            .limit(memoryArchitectureConfig.getMemoryMetadataLimit())
            .build()
    ))
}
```

### **2. Enhanced Context Enrichment**
The enhanced context extractor now provides:

```
CURRENT_MESSAGE: [current user message]
RELEVANT_CONTEXT: [most relevant context from conversation history]
TOPIC_CONTEXT: [current topic and related previous discussions]
EMOTIONAL_CONTEXT: [emotional state and psychological indicators]
MEDICAL_CONTEXT: [symptoms, treatments, medical history mentioned]
CONVERSATION_FLOW: [conversation progression and follow-up patterns]
```

### **3. Memory Block Updates**
Each message processing cycle updates:
1. **conversation_history**: Append new conversation turn
2. **active_session**: Update session state and metadata
3. **context_summary**: Refresh if topic changes or every 5 turns
4. **memory_metadata**: Update usage statistics and rotation flags

---

## 📈 **Performance Considerations**

### **Memory Usage Optimization**
- **Structured Text Format**: Optimized for Letta processing and human readability
- **Efficient Context Selection**: Prioritized algorithm reduces unnecessary context
- **Rotation Strategy**: Prevents memory bloat while preserving critical information

### **Monitoring and Alerts**
- **Usage Tracking**: Real-time memory usage monitoring via memory_metadata
- **Rotation Triggers**: Automatic rotation when approaching limits
- **Archival Readiness**: Proactive archival trigger detection

---

## ✅ **Sub-Plan 1 Deliverables - Complete**

### **✓ Updated Memory Block Structure**
- Enhanced memory blocks implemented in `UserIdentityService.createContextCoordinatorAgent()`
- New memory block architecture: conversation_history (32KB), active_session (4KB), context_summary (8KB), memory_metadata (2KB)

### **✓ New Memory Management Prompt** 
- Enhanced context coordinator prompt with advanced memory management capabilities
- Support for multi-block memory operations and sophisticated context enrichment

### **✓ Memory Architecture Documentation**
- This comprehensive documentation describing the complete implementation
- Configuration examples, usage patterns, and operational guidelines

---

## 🔗 **Integration Points**

### **Ready for Sub-Plan 2: Conversation Turn Management**
The memory architecture provides foundation for:
- Complete conversation turn tracking (data models ready)
- Memory block update mechanisms (service layer implemented)
- Context enrichment based on full history (algorithms in place)

### **Configuration-Driven Design**
All memory settings are externally configurable via `application.yml`, allowing for:
- Environment-specific tuning
- A/B testing of memory parameters
- Production scaling adjustments

---

## 📝 **Next Steps**
With Sub-Plan 1 complete, the system is ready for:
1. **Sub-Plan 2**: Enhanced conversation turn management and memory update integration
2. **Sub-Plan 3**: Advanced context extraction with centralized memory
3. **Sub-Plan 4**: Bidirectional memory updates for agent response capture

The foundation is now in place for full centralized context management implementation. 