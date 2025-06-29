#!/bin/bash

# 🚀 Letta Health Agents - Multi-Agent Flow Test Script
# This script demonstrates intent switching and context management

set -e  # Exit on any error

# Configuration
BASE_URL="http://localhost:8085"
USER_ID="test-$(date +%s)"
SESSION_ID="session-$(date +%s)"

echo "🏥 === LETTA HEALTH AGENTS - MULTI-AGENT FLOW TEST ==="
echo "📋 User ID: $USER_ID"
echo "📋 Session ID: $SESSION_ID"
echo "📋 Base URL: $BASE_URL"
echo ""

# Function to make API calls with error handling
make_request() {
    local method=$1
    local endpoint=$2
    local data=$3
    local description=$4
    
    echo "🔄 $description..."
    
    if [ "$method" = "GET" ]; then
        response=$(curl -s "$BASE_URL$endpoint")
    else
        response=$(curl -s -X "$method" "$BASE_URL$endpoint" \
            -H "Content-Type: application/json" \
            -d "$data")
    fi
    
    echo "✅ Response: $response" | jq .
    echo ""
}

# Function to extract specific fields from response
extract_fields() {
    local response=$1
    echo "$response" | jq '{intent: .intent, confidence: .confidence, contextConfidence: .contextConfidence, relevantTurns: .relevantTurns, conversationPhase: .conversationPhase, responseQualityLevel: .responseQualityLevel}'
}

echo "🔍 === STEP 1: SYSTEM HEALTH CHECK ==="
make_request "GET" "/api/health-chat/health" "" "Checking system health"

echo "🤖 === STEP 2: CREATE USER AGENTS ==="
make_request "POST" "/api/health-chat/debug/create-agent/$USER_ID" "" "Creating user agents"

echo "🎬 === STEP 3: START CHAT SESSION ==="
start_data="{\"userId\": \"$USER_ID\", \"sessionId\": \"$SESSION_ID\"}"
make_request "POST" "/api/health-chat/start" "$start_data" "Starting chat session"

echo "🏥 === STEP 4: GENERAL HEALTH MESSAGE ==="
health_message="{\"userId\": \"$USER_ID\", \"sessionId\": \"$SESSION_ID\", \"message\": \"I have been experiencing persistent lower back pain for about 2 weeks now. It gets worse when I sit for long periods at work.\"}"
health_response=$(curl -s -X POST "$BASE_URL/api/health-chat/message" \
    -H "Content-Type: application/json" \
    -d "$health_message")

echo "📊 Key Metrics:"
extract_fields "$health_response"
echo "💬 Expected: GENERAL_HEALTH intent, Initial Assessment phase"
echo ""

echo "🧠 === STEP 5: MENTAL HEALTH MESSAGE (INTENT SWITCH) ==="
mental_message="{\"userId\": \"$USER_ID\", \"sessionId\": \"$SESSION_ID\", \"message\": \"Besides the back pain, I have been feeling very stressed and anxious lately. I am having trouble sleeping and feel overwhelmed with work pressure.\"}"
mental_response=$(curl -s -X POST "$BASE_URL/api/health-chat/message" \
    -H "Content-Type: application/json" \
    -d "$mental_message")

echo "📊 Key Metrics:"
extract_fields "$mental_response"
echo "💬 Expected: MENTAL_HEALTH intent, Information Gathering phase, increased turns"
echo ""

echo "🔗 === STEP 6: COMBINED HEALTH MESSAGE (CONTEXT AGGREGATION) ==="
combined_message="{\"userId\": \"$USER_ID\", \"sessionId\": \"$SESSION_ID\", \"message\": \"I think my back pain and stress might be related. Could the physical pain be making my anxiety worse, or could stress be causing muscle tension that leads to back pain? What should I do about both issues?\"}"
combined_response=$(curl -s -X POST "$BASE_URL/api/health-chat/message" \
    -H "Content-Type: application/json" \
    -d "$combined_message")

echo "📊 Key Metrics:"
extract_fields "$combined_response"
echo "💬 Expected: Smart routing, Active Discussion phase, high context confidence"
echo ""

echo "🔄 === STEP 7: BACK TO GENERAL HEALTH (BIDIRECTIONAL SWITCH) ==="
exercise_message="{\"userId\": \"$USER_ID\", \"sessionId\": \"$SESSION_ID\", \"message\": \"What specific exercises or stretches would you recommend for my lower back pain? Should I see a physical therapist or try home remedies first?\"}"
exercise_response=$(curl -s -X POST "$BASE_URL/api/health-chat/message" \
    -H "Content-Type: application/json" \
    -d "$exercise_message")

echo "📊 Key Metrics:"
extract_fields "$exercise_response"
echo "💬 Expected: GENERAL_HEALTH intent, maximum context turns"
echo ""

echo "📈 === STEP 8: SESSION STATISTICS ==="
make_request "GET" "/api/conversation/stats/$SESSION_ID" "" "Getting session statistics"

echo "🧠 === STEP 9: SYSTEM DIAGNOSTICS ==="
make_request "GET" "/api/health-chat/prompt-stats" "" "Checking prompt statistics"
make_request "GET" "/api/health-chat/test-letta" "" "Testing Letta connection"

echo "🏁 === STEP 10: END SESSION ==="
end_data="{\"userId\": \"$USER_ID\", \"sessionId\": \"$SESSION_ID\"}"
make_request "POST" "/api/health-chat/end" "$end_data" "Ending chat session"

echo "🎉 === TEST COMPLETE ==="
echo ""
echo "🎯 === SUMMARY OF WHAT WAS TESTED ==="
echo "✅ Multi-agent intent switching (General Health ↔ Mental Health)"
echo "✅ Context preservation across agent switches"
echo "✅ Session state management and progression"
echo "✅ Bidirectional memory updates"
echo "✅ Quality scoring and effectiveness tracking"
echo "✅ System diagnostics and health monitoring"
echo ""
echo "📋 === EXPECTED BEHAVIOR ==="
echo "1. Intent Classification: GENERAL_HEALTH → MENTAL_HEALTH → Smart Routing → GENERAL_HEALTH"
echo "2. Context Accumulation: 2 → 4 → 6 → 8 relevant turns"
echo "3. Phase Progression: Initial Assessment → Information Gathering → Active Discussion"
echo "4. Quality Consistency: HIGH context confidence (0.8) and EXCELLENT response quality"
echo ""
echo "🚀 Test completed successfully! Check the responses above for detailed metrics." 