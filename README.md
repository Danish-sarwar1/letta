# Letta Health Agents - Multi-Agent Health Consultation System

A sophisticated Java Spring Boot application that leverages Letta API to implement a multi-agent health consultation system with user-specific dedicated agent flows and session-based context management.

## 🏗️ Architecture Overview

### Core Components

1. **User Identity Management**: Leverages Letta's native Identity system
2. **Multi-Agent Ecosystem**: 4 specialized agents per user
3. **Session Management**: Context preservation and archival
4. **Intent Classification**: Smart routing to appropriate health agents
5. **Memory Management**: Core memory for sessions, archival for persistence

### Agent Architecture

Each user gets a dedicated ecosystem of 4 specialized agents:

- **Context Coordinator**: Manages conversation flow and session context
- **Intent Classifier**: Determines query intent and routes appropriately  
- **General Health Agent**: Handles physical health consultations
- **Mental Health Agent**: Provides mental health support

## 🚀 Quick Start

### Prerequisites

- Java 17+
- Maven 3.6+
- Letta server running (default: http://localhost:8283)
- Letta API key (optional, for authentication)

### Environment Setup

```bash
# Set Letta API configuration
export LETTA_API_URL=http://localhost:8283
export LETTA_API_KEY=your_api_key_here  # Optional
```

### Running the Application

```bash
# Clone and build
git clone <repository-url>
cd letta-health-agents
mvn clean install

# Run the application
mvn spring-boot:run

# The service will start on http://localhost:8080
```

## 📡 API Endpoints

### Start Health Consultation

```http
POST /api/health-chat/start
Content-Type: application/json

{
  "userId": "user123",
  "sessionId": "session456"
}
```

### Send Message

```http
POST /api/health-chat/message
Content-Type: application/json

{
  "userId": "user123",
  "sessionId": "session456",
  "message": "I've been having headaches for the past week"
}
```

### End Session

```http
POST /api/health-chat/end
Content-Type: application/json

{
  "userId": "user123",
  "sessionId": "session456"
}
```

### Health Check

```http
GET /api/health-chat/health
```

## 🔄 System Flow

1. **User Request** → Identity Check → Agent Creation (if needed)
2. **Session Start** → Context Loading from archival memory
3. **Message Processing**:
   - Context Coordinator enriches message with session context
   - Intent Classifier determines health domain (General/Mental)
   - Appropriate Health Agent provides specialized response
4. **Session End** → Context archived for future sessions

## 🧠 Agent Capabilities

### Context Coordinator
- Session state management
- Conversation flow coordination
- Context enrichment from user history
- Memory block management

### Intent Classifier
- High-accuracy intent classification
- Confidence scoring (0.0-1.0)
- Emergency detection and routing
- Secondary domain identification

### General Health Agent
- Physical health consultations
- Medical information (with disclaimers)
- Wellness and lifestyle guidance
- Emergency protocol awareness

### Mental Health Agent
- Emotional support and validation
- Evidence-based coping strategies
- Crisis intervention protocols
- Trauma-informed care approach

## 💾 Memory Management

### Core Memory (Session-scoped)
- **Context Coordinator**: `session_context`, `user_profile`
- **Intent Classifier**: `classification_patterns`
- **Health Agents**: `current_context`, `health_history`/`therapeutic_context`

### Archival Memory (Persistent)
- Automatic archival of session contexts
- Historical conversation retrieval
- User pattern recognition
- Cross-session continuity

## 🔧 Configuration

### Application Properties (`application.yml`)

```yaml
letta:
  api:
    base-url: ${LETTA_API_URL:http://localhost:8283}
    api-key: ${LETTA_API_KEY:}
    timeout: 60s
    default-model: "openai/gpt-4o-mini"
    default-embedding: "openai/text-embedding-3-small"
    max-retries: 3
    retry-delay: 1s
```

### Caching Configuration

- **Caffeine Cache**: User agent mappings cached for 1 hour
- **Maximum Size**: 1000 entries
- **Automatic Eviction**: Based on write time

## 🏥 Medical Ethics & Safety

### Built-in Safety Protocols

- **Medical Disclaimers**: All health advice includes appropriate disclaimers
- **Emergency Detection**: Automatic crisis intervention protocols
- **Professional Referrals**: Encourages professional medical consultation
- **Boundary Management**: Clear limitations on AI capabilities

### Crisis Intervention

The system automatically detects and responds to:
- Suicidal ideation or self-harm
- Medical emergencies
- Severe mental health crises
- Abuse or safety concerns

## 🔒 Security Features

- **Input Validation**: Comprehensive request validation
- **Error Handling**: Graceful error responses without sensitive data exposure
- **Authentication**: Letta API key support
- **Rate Limiting**: Built-in Spring Boot protections

## 📊 Monitoring & Observability

### Built-in Endpoints

- **Health Check**: `/api/health-chat/health`
- **Actuator**: `/actuator/health`, `/actuator/metrics`
- **Prometheus**: `/actuator/prometheus`

### Logging

- **Structured Logging**: JSON format with correlation IDs
- **Log Levels**: Configurable per package
- **Audit Trail**: User interactions and system events

## 🧪 Testing

### Unit Tests

```bash
mvn test
```

### Integration Tests

```bash
mvn integration-test
```

### Manual Testing with cURL

```bash
# Start session
curl -X POST http://localhost:8080/api/health-chat/start \
  -H "Content-Type: application/json" \
  -d '{"userId":"test123","sessionId":"sess456"}'

# Send message
curl -X POST http://localhost:8080/api/health-chat/message \
  -H "Content-Type: application/json" \
  -d '{"userId":"test123","sessionId":"sess456","message":"I have a headache"}'

# End session
curl -X POST http://localhost:8080/api/health-chat/end \
  -H "Content-Type: application/json" \
  -d '{"userId":"test123","sessionId":"sess456"}'
```

## 🔄 Development Workflow

### Adding New Agent Types

1. Add enum to `AgentType`
2. Create prompt file in `src/main/resources/prompts/`
3. Update `UserIdentityService` to create new agent
4. Modify routing logic in `AgentOrchestrationService`

### Customizing Prompts

Agent prompts are stored in `src/main/resources/prompts/`:
- `context-coordinator.txt`
- `intent-classifier.txt`
- `general-health.txt`
- `mental-health.txt`

## 📈 Performance Considerations

- **Agent Reuse**: Each user's agents are created once and reused
- **Memory Efficiency**: Letta handles memory management automatically
- **Caching**: User agent mappings cached to reduce API calls
- **Async Processing**: Non-blocking I/O with Retrofit

## 🐛 Troubleshooting

### Common Issues

1. **Letta Connection Failed**
   - Check `LETTA_API_URL` environment variable
   - Verify Letta server is running
   - Check network connectivity

2. **Agent Creation Errors**
   - Verify Letta API key if authentication is enabled
   - Check Letta server logs for detailed errors
   - Ensure sufficient resources on Letta server

3. **Memory Issues**
   - Monitor JVM heap usage
   - Adjust cache settings if needed
   - Check Letta server memory usage

### Debug Mode

Enable debug logging:

```yaml
logging:
  level:
    com.health.agents: DEBUG
    retrofit2: DEBUG
```

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Implement changes with tests
4. Submit a pull request
5. Ensure all CI checks pass

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.

## 🙏 Acknowledgments

- **Letta Team**: For the powerful agent orchestration platform
- **Spring Boot**: For the robust application framework
- **Retrofit**: For elegant HTTP client integration

---

**Note**: This system is designed for educational and support purposes. It does not replace professional medical or mental health care. Users should always consult with qualified healthcare providers for medical advice and treatment. 