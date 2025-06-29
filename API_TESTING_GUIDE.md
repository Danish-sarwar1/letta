# 🚀 Letta Health Agents - API Testing Guide

## 📋 Complete API Contract & Testing Commands

### 🏥 Base Configuration
```bash
BASE_URL="http://localhost:8085"
CONTENT_TYPE="Content-Type: application/json"
```

---

## 🎯 Health Chat API Endpoints

### 1. **Health Check**
**Endpoint**: `GET /api/health-chat/health`

```bash
curl -s $BASE_URL/api/health-chat/health
```

**Expected Response**:
```
Health Chat Service is running
```

---

### 2. **Create User Agents**
**Endpoint**: `POST /api/health-chat/debug/create-agent/{userId}`

```bash
# Test with your own user ID
USER_ID="your-test-user-123"
curl -s -X POST $BASE_URL/api/health-chat/debug/create-agent/$USER_ID | jq .
```

**Expected Response**:
```json
{
  "status": "success",
  "user_agents": {
    "userId": "your-test-user-123",
    "identityId": "identity-[uuid]",
    "contextExtractorId": "agent-[uuid]",
    "intentExtractorId": "agent-[uuid]",
    "generalHealthId": "agent-[uuid]",
    "mentalHealthId": "agent-[uuid]"
  }
}
```

---

### 3. **Start Chat Session**
**Endpoint**: `POST /api/health-chat/start`

**Request Body**:
```json
{
  "userId": "string",
  "sessionId": "string"
}
```

```bash
USER_ID="your-test-user-123"
SESSION_ID="session-$(date +%s)"

curl -s -X POST $BASE_URL/api/health-chat/start \
  -H "$CONTENT_TYPE" \
  -d "{\"userId\": \"$USER_ID\", \"sessionId\": \"$SESSION_ID\"}" | jq .
```

**Expected Response**:
```json
{
  "message": "Health consultation session started successfully. How can I help you today?",
  "sessionId": "session-1735467890",
  "userId": "your-test-user-123",
  "sessionActive": true,
  "timestamp": [2025, 6, 29, 14, 30, 45, 123456789]
}
```

---

### 4. **Send Message**
**Endpoint**: `POST /api/health-chat/message`

**Request Body**:
```json
{
  "userId": "string",
  "sessionId": "string", 
  "message": "string"
}
```

#### **General Health Message**:
```bash
curl -s -X POST $BASE_URL/api/health-chat/message \
  -H "$CONTENT_TYPE" \
  -d "{
    \"userId\": \"$USER_ID\",
    \"sessionId\": \"$SESSION_ID\",
    \"message\": \"I have been experiencing persistent headaches for the past week\"
  }" | jq .
```

#### **Mental Health Message**:
```bash
curl -s -X POST $BASE_URL/api/health-chat/message \
  -H "$CONTENT_TYPE" \
  -d "{
    \"userId\": \"$USER_ID\",
    \"sessionId\": \"$SESSION_ID\",
    \"message\": \"I feel stressed and anxious, having trouble sleeping\"
  }" | jq .
```

#### **Combined Health Message**:
```bash
curl -s -X POST $BASE_URL/api/health-chat/message \
  -H "$CONTENT_TYPE" \
  -d "{
    \"userId\": \"$USER_ID\",
    \"sessionId\": \"$SESSION_ID\",
    \"message\": \"Could my stress be causing my headaches? What should I do about both issues?\"
  }" | jq .
```

**Expected Response Structure**:
```json
{
  "message": null,
  "sessionId": "session-1735467890",
  "userId": "your-test-user-123",
  "intent": "GENERAL_HEALTH|MENTAL_HEALTH|EMERGENCY",
  "confidence": 0.7,
  "sessionActive": true,
  "contextConfidence": 0.8,
  "relevantTurns": 4,
  "conversationPhase": "Initial Assessment|Information Gathering|Active Discussion",
  "contextStrategy": "Enhanced Session Management with Quality Score: 0.65",
  "responseQuality": 0.8,
  "responseQualityLevel": "EXCELLENT|GOOD|FAIR|POOR",
  "contextConfidenceLevel": "HIGH|MEDIUM|LOW",
  "bidirectionalUpdateSuccess": true,
  "performanceSummary": "Effectiveness: 80.0%"
}
```

---

### 5. **End Chat Session**
**Endpoint**: `POST /api/health-chat/end`

```bash
curl -s -X POST $BASE_URL/api/health-chat/end \
  -H "$CONTENT_TYPE" \
  -d "{\"userId\": \"$USER_ID\", \"sessionId\": \"$SESSION_ID\"}" | jq .
```

---

## 🔍 Conversation Management API

### 6. **Get Conversation History**
**Endpoint**: `GET /api/conversation/history/{sessionId}`

```bash
curl -s $BASE_URL/api/conversation/history/$SESSION_ID | jq .
```

---

### 7. **Get Conversation Statistics**
**Endpoint**: `GET /api/conversation/stats/{sessionId}`

```bash
curl -s $BASE_URL/api/conversation/stats/$SESSION_ID | jq .
```

---

### 8. **Get Recent Turns**
**Endpoint**: `GET /api/conversation/recent/{sessionId}?limit={number}`

```bash
curl -s "$BASE_URL/api/conversation/recent/$SESSION_ID?limit=5" | jq .
```

---

### 9. **Check Memory Status**
**Endpoint**: `GET /api/conversation/memory-status/{sessionId}`

```bash
curl -s $BASE_URL/api/conversation/memory-status/$SESSION_ID | jq .
```

---

## 🧠 System Diagnostic APIs

### 10. **Test Letta Connection**
**Endpoint**: `GET /api/health-chat/test-letta`

```bash
curl -s $BASE_URL/api/health-chat/test-letta | jq .
```

**Expected Response**:
```json
{
  "status": "success",
  "identities_count": 1,
  "first_identity": {
    "id": "identity-[uuid]",
    "agent_ids": ["agent-1", "agent-2", "agent-3", "agent-4"]
  }
}
```

---

### 11. **Get Prompt Statistics**
**Endpoint**: `GET /api/health-chat/prompt-stats`

```bash
curl -s $BASE_URL/api/health-chat/prompt-stats | jq .
```

**Expected Response**:
```json
{
  "status": "success",
  "all_prompts_loaded": true,
  "prompt_statistics": {
    "totalPrompts": 4,
    "allLoaded": true,
    "promptLengths": {
      "MENTAL_HEALTH": 10740,
      "INTENT_CLASSIFIER": 4113,
      "GENERAL_HEALTH": 7857,
      "CONTEXT_COORDINATOR": 15837
    }
  }
}
```

---

## 🎬 Complete Testing Scenarios

### **Scenario 1: Basic Health Consultation**
```bash
#!/bin/bash
BASE_URL="http://localhost:8085"
USER_ID="test-user-$(date +%s)"
SESSION_ID="session-$(date +%s)"

echo "=== Creating User Agents ==="
curl -s -X POST $BASE_URL/api/health-chat/debug/create-agent/$USER_ID | jq .

echo -e "\n=== Starting Session ==="
curl -s -X POST $BASE_URL/api/health-chat/start \
  -H "Content-Type: application/json" \
  -d "{\"userId\": \"$USER_ID\", \"sessionId\": \"$SESSION_ID\"}" | jq .

echo -e "\n=== Sending Health Message ==="
curl -s -X POST $BASE_URL/api/health-chat/message \
  -H "Content-Type: application/json" \
  -d "{\"userId\": \"$USER_ID\", \"sessionId\": \"$SESSION_ID\", \"message\": \"I have a persistent cough for 3 days\"}" | jq .

echo -e "\n=== Ending Session ==="
curl -s -X POST $BASE_URL/api/health-chat/end \
  -H "Content-Type: application/json" \
  -d "{\"userId\": \"$USER_ID\", \"sessionId\": \"$SESSION_ID\"}" | jq .
```

---

### **Scenario 2: Multi-Agent Intent Switching**
```bash
#!/bin/bash
BASE_URL="http://localhost:8085"
USER_ID="multi-agent-user-$(date +%s)"
SESSION_ID="multi-session-$(date +%s)"

# Setup
curl -s -X POST $BASE_URL/api/health-chat/debug/create-agent/$USER_ID | jq .
curl -s -X POST $BASE_URL/api/health-chat/start \
  -H "Content-Type: application/json" \
  -d "{\"userId\": \"$USER_ID\", \"sessionId\": \"$SESSION_ID\"}" | jq .

echo "=== Step 1: General Health ==="
curl -s -X POST $BASE_URL/api/health-chat/message \
  -H "Content-Type: application/json" \
  -d "{\"userId\": \"$USER_ID\", \"sessionId\": \"$SESSION_ID\", \"message\": \"I have back pain from sitting too long\"}" | jq .intent,.confidence,.conversationPhase

echo -e "\n=== Step 2: Mental Health Switch ==="
curl -s -X POST $BASE_URL/api/health-chat/message \
  -H "Content-Type: application/json" \
  -d "{\"userId\": \"$USER_ID\", \"sessionId\": \"$SESSION_ID\", \"message\": \"I also feel very stressed and anxious lately\"}" | jq .intent,.confidence,.conversationPhase

echo -e "\n=== Step 3: Combined Question ==="
curl -s -X POST $BASE_URL/api/health-chat/message \
  -H "Content-Type: application/json" \
  -d "{\"userId\": \"$USER_ID\", \"sessionId\": \"$SESSION_ID\", \"message\": \"Could my stress be causing my back pain?\"}" | jq .intent,.confidence,.relevantTurns

echo -e "\n=== Session Stats ==="
curl -s $BASE_URL/api/conversation/stats/$SESSION_ID | jq .
```

---

### **Scenario 3: Context Memory Testing**
```bash
#!/bin/bash
BASE_URL="http://localhost:8085"
USER_ID="memory-test-$(date +%s)"
SESSION_ID="memory-session-$(date +%s)"

# Setup
curl -s -X POST $BASE_URL/api/health-chat/debug/create-agent/$USER_ID | jq .
curl -s -X POST $BASE_URL/api/health-chat/start \
  -H "Content-Type: application/json" \
  -d "{\"userId\": \"$USER_ID\", \"sessionId\": \"$SESSION_ID\"}" | jq .

# Send multiple messages to build context
for i in {1..5}; do
  echo "=== Message $i ==="
  curl -s -X POST $BASE_URL/api/health-chat/message \
    -H "Content-Type: application/json" \
    -d "{\"userId\": \"$USER_ID\", \"sessionId\": \"$SESSION_ID\", \"message\": \"Message $i: Building conversation context\"}" | jq .relevantTurns,.contextConfidence
done

echo -e "\n=== Final Context Test ==="
curl -s -X POST $BASE_URL/api/health-chat/message \
  -H "Content-Type: application/json" \
  -d "{\"userId\": \"$USER_ID\", \"sessionId\": \"$SESSION_ID\", \"message\": \"Summarize everything we discussed\"}" | jq .relevantTurns,.contextConfidence,.contextSummary
```

---

## 🔑 Key Response Fields to Monitor

| Field | Description | Values |
|-------|-------------|---------|
| `intent` | Detected user intent | `GENERAL_HEALTH`, `MENTAL_HEALTH`, `EMERGENCY` |
| `confidence` | Intent classification confidence | `0.0 - 1.0` |
| `contextConfidence` | Context quality score | `0.0 - 1.0` |
| `relevantTurns` | Number of conversation turns used | Integer |
| `conversationPhase` | Current conversation stage | `Initial Assessment`, `Information Gathering`, `Active Discussion` |
| `responseQuality` | Agent response quality | `0.0 - 1.0` |
| `responseQualityLevel` | Quality classification | `EXCELLENT`, `GOOD`, `FAIR`, `POOR` |
| `bidirectionalUpdateSuccess` | Memory sync status | `true/false` |

---

## 🚀 Quick Start Commands

**Copy and paste these commands to start testing immediately:**

```bash
# Set variables
export BASE_URL="http://localhost:8085"
export USER_ID="quicktest-$(date +%s)"
export SESSION_ID="session-$(date +%s)"

# 1. Check system health
curl -s $BASE_URL/api/health-chat/health

# 2. Create agents
curl -s -X POST $BASE_URL/api/health-chat/debug/create-agent/$USER_ID | jq .

# 3. Start session
curl -s -X POST $BASE_URL/api/health-chat/start \
  -H "Content-Type: application/json" \
  -d "{\"userId\": \"$USER_ID\", \"sessionId\": \"$SESSION_ID\"}" | jq .

# 4. Send test message
curl -s -X POST $BASE_URL/api/health-chat/message \
  -H "Content-Type: application/json" \
  -d "{\"userId\": \"$USER_ID\", \"sessionId\": \"$SESSION_ID\", \"message\": \"I have a headache\"}" | jq .

# 5. Check system stats
curl -s $BASE_URL/api/health-chat/prompt-stats | jq .
```

Save this guide and use these commands to thoroughly test the Centralized Context Management system! 🎯 