# 🚀 Quick Start Testing Guide

## Prerequisites
- Application running on `http://localhost:8085`
- `curl` and `jq` installed
- Terminal access

## 🎯 Option 1: Instant Multi-Agent Test (Recommended)

Run the automated test script:
```bash
./test_multi_agent_flow.sh
```

This will automatically test:
- ✅ Agent creation and routing
- ✅ Intent switching (General Health ↔ Mental Health)  
- ✅ Context preservation and accumulation
- ✅ Session management and quality scoring

## 🎯 Option 2: Manual Step-by-Step Testing

### Quick Setup
```bash
export BASE_URL="http://localhost:8085"
export USER_ID="quicktest-$(date +%s)"
export SESSION_ID="session-$(date +%s)"
```

### 1. Health Check
```bash
curl -s $BASE_URL/api/health-chat/health
```

### 2. Create Agents
```bash
curl -s -X POST $BASE_URL/api/health-chat/debug/create-agent/$USER_ID | jq .
```

### 3. Start Session
```bash
curl -s -X POST $BASE_URL/api/health-chat/start \
  -H "Content-Type: application/json" \
  -d "{\"userId\": \"$USER_ID\", \"sessionId\": \"$SESSION_ID\"}" | jq .
```

### 4. Test General Health Message
```bash
curl -s -X POST $BASE_URL/api/health-chat/message \
  -H "Content-Type: application/json" \
  -d "{\"userId\": \"$USER_ID\", \"sessionId\": \"$SESSION_ID\", \"message\": \"I have back pain\"}" | jq '{intent, confidence, conversationPhase}'
```

### 5. Test Mental Health Switch
```bash
curl -s -X POST $BASE_URL/api/health-chat/message \
  -H "Content-Type: application/json" \
  -d "{\"userId\": \"$USER_ID\", \"sessionId\": \"$SESSION_ID\", \"message\": \"I feel stressed and anxious\"}" | jq '{intent, confidence, relevantTurns}'
```

### 6. Test Combined Question
```bash
curl -s -X POST $BASE_URL/api/health-chat/message \
  -H "Content-Type: application/json" \
  -d "{\"userId\": \"$USER_ID\", \"sessionId\": \"$SESSION_ID\", \"message\": \"Could my stress be causing my back pain?\"}" | jq '{intent, contextConfidence, relevantTurns}'
```

## 🔍 What to Look For

### Successful Intent Classification:
- Physical symptoms → `"intent": "GENERAL_HEALTH"`
- Mental health keywords → `"intent": "MENTAL_HEALTH"`
- Combined questions → Smart routing

### Context Management:
- `relevantTurns` should increase (2 → 4 → 6)
- `contextConfidence` should remain HIGH (0.8)
- `conversationPhase` should progress naturally

### Quality Metrics:
- `responseQuality`: 0.8 (EXCELLENT)
- `responseQualityLevel`: "EXCELLENT"  
- `bidirectionalUpdateSuccess`: true

## 📖 Full Documentation

For complete API contract and advanced testing scenarios, see:
- `API_TESTING_GUIDE.md` - Complete API documentation
- `test_multi_agent_flow.sh` - Automated comprehensive test

## 🎯 Success Criteria

Your system is working correctly if you see:
1. ✅ Different intents for different message types
2. ✅ Increasing `relevantTurns` as conversation continues  
3. ✅ HIGH `contextConfidence` maintained throughout
4. ✅ Natural `conversationPhase` progression
5. ✅ Consistent EXCELLENT `responseQualityLevel` 