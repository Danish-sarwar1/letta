# Letta Agent Prompt Writing Guide

## Complete Guide to Writing Effective Letta Agent Prompts

### Version: 1.0
### Date: December 2024

---

## Table of Contents

1. [Introduction](#introduction)
2. [Standard Prompt Format](#standard-prompt-format)
3. [Key Style Elements](#key-style-elements)
4. [Essential Sections](#essential-sections)
5. [Memory Block Integration](#memory-block-integration)
6. [Letta-Specific Instructions](#letta-specific-instructions)
7. [Writing Style Guidelines](#writing-style-guidelines)
8. [Template for New Agents](#template-for-new-agents)
9. [Real Examples](#real-examples)
10. [Best Practices](#best-practices)
11. [Common Pitfalls](#common-pitfalls)

---

## Introduction

This guide provides a comprehensive framework for writing effective prompts for Letta agents. Letta agents use these prompts as their core instructions, defining their behavior, memory management, and interaction patterns.

### Key Principles
- **Consistency**: All prompts follow the same structural pattern
- **Clarity**: Instructions are clear and actionable
- **Memory-Aware**: Explicit integration with Letta's memory system
- **Safety-First**: Built-in safety protocols and limitations
- **Role-Specific**: Tailored to each agent's specific function

---

## Standard Prompt Format

Every Letta agent prompt follows this standardized structure:

```markdown
# [Agent Type] Agent

[Brief agent description and primary role]

## Core Responsibilities:
1. **[Primary Function]**: Description
2. **[Secondary Function]**: Description
3. **[Additional Functions]**: Description

## Memory Blocks You Manage:
- **[memory_block_name]**: Description of what gets stored
- **[another_memory_block]**: Description of purpose

## [Domain/Specialty] Areas You Cover:
### [Category 1]:
- **[Specific Area]**: Details
- **[Another Area]**: Details

### [Category 2]:
- **[Specific Area]**: Details

## [Process/Approach] Framework:
### [Stage 1]:
1. **[Step Name]**: What to do
2. **[Step Name]**: What to do

### [Stage 2]:
- Instructions and guidelines

## Safety Protocols:
### [Critical Situations]:
- **[Emergency Type]**: When to act
- **[Warning Signs]**: What to watch for

## Communication Guidelines:
### [Style Requirements]:
- **[Tone Attribute]**: How to communicate
- **[Language Style]**: Specific requirements

## Response Format:
Structure responses as:
1. **[Step 1]**: What to include
2. **[Step 2]**: What to include

## Memory Management:
### [Memory Block] Updates:
Document in [memory_block_name]:
- What information to store
- When to update

Remember: [Final guidance and boundaries]
```

---

## Key Style Elements

### Header Structure
```markdown
# [Agent Type] Agent
```
- Always starts with agent type + "Agent"
- Uses H1 markdown header (`#`)
- Examples: "Context Extractor Agent", "Intent Extractor Agent"

### Section Organization
Required sections in order:
```markdown
## Core Responsibilities:
## Memory Blocks You Manage:
## [Domain] Areas You Cover:
## [Process] Framework:
## Safety Protocols:
## Communication Guidelines:
## Response Format:
## Memory Management:
```

### Formatting Patterns

**Bold Categories with Descriptions:**
```markdown
- **Category Name**: Detailed description of what this covers
- **Another Category**: More specific details
```

**Numbered Lists for Processes:**
```markdown
1. **Action Step**: What to do in this step
2. **Next Step**: What comes next
3. **Final Step**: How to conclude
```

**Subsections with Examples:**
```markdown
### For [Specific Condition]:
- Specific intervention or strategy
- Another strategy
- Professional referral criteria
```

---

## Essential Sections

### 1. Core Responsibilities
**Purpose**: Define what the agent does
**Format**: Numbered list with bold headings
**Example**:
```markdown
## Core Responsibilities:
1. **Message History Management**: Maintain complete conversation history
2. **Context Enrichment**: Provide enriched messages with relevant context
3. **Session Tracking**: Keep track of current session metadata
```

### 2. Memory Blocks You Manage
**Purpose**: Specify which memory blocks the agent uses
**Format**: Bullet list with block names and descriptions
**Example**:
```markdown
## Memory Blocks You Manage:
- **conversation_history**: Complete conversation history for current session
- **session_context**: Current session metadata and status
- **user_profile**: User preferences and profile information
```

### 3. Domain Coverage
**Purpose**: Define the agent's area of expertise
**Format**: Categorized sections with bullet points
**Example**:
```markdown
## Health Domains You Cover:
### Primary Care Areas:
- **Common Symptoms**: Headaches, fatigue, pain, fever
- **Chronic Conditions**: Diabetes, hypertension, arthritis
### Specialized Areas:
- **Cardiovascular Health**: Heart health, blood pressure
- **Mental Health**: Anxiety, depression, stress management
```

### 4. Safety Protocols
**Purpose**: Define emergency situations and responses
**Format**: Categorized emergency types with clear actions
**Example**:
```markdown
## Safety Protocols:
### Mental Health Emergencies:
- **Suicidal Ideation**: Active suicidal thoughts, plans, or attempts
- **Self-Harm**: Active self-injury or dangerous behaviors
- **Crisis Resources**: Provide immediate crisis hotline numbers
```

### 5. Communication Guidelines
**Purpose**: Define tone, style, and language requirements
**Format**: Categorized communication aspects
**Example**:
```markdown
## Communication Guidelines:
### Tone and Style:
- **Professional yet Accessible**: Use medical terminology but explain clearly
- **Empathetic and Supportive**: Acknowledge concerns with understanding
- **Evidence-Based**: Base recommendations on established knowledge
```

### 6. Response Format
**Purpose**: Structure how the agent should format responses
**Format**: Numbered steps with examples
**Example**:
```markdown
## Response Format:
Structure responses as:
1. **Acknowledgment**: "I understand you're experiencing..."
2. **Information**: Evidence-based explanation
3. **Recommendations**: Specific actionable advice
4. **Next Steps**: Follow-up guidance
5. **Disclaimer**: Appropriate professional limitations
```

### 7. Memory Management
**Purpose**: Specify when and what to store in memory blocks
**Format**: Subsections for each memory block with storage criteria
**Example**:
```markdown
## Memory Management:
### Conversation History Updates:
Document in conversation_history memory:
- All user messages in chronological order
- Context about conversation flow
- Topic changes and continuity

### Session Context Tracking:
Maintain in session_context memory:
- Current session ID and status
- Active topics being discussed
- Session goals and progress
```

---

## Memory Block Integration

### Memory Block Definition
Every agent prompt must explicitly reference its memory blocks:

```markdown
## Memory Blocks You Manage:
- **block_name**: Clear description of purpose and content
- **another_block**: What gets stored and when it's updated
```

### Memory Management Instructions
Provide specific guidance on memory usage:

```markdown
## Memory Management:
### [Block Name] Updates:
Document in [block_name] memory:
- Specific types of information to store
- Triggers for when to update
- Storage format and patterns

### [Block Name] Tracking:
Maintain in [block_name] memory:
- Current state information
- Real-time tracking requirements
- Context preservation rules
```

### Memory Block Types

**Static Blocks (readOnly: true)**:
- `agent_instructions`: Core behavioral rules (never changes)
- Purpose: Ensure consistent agent behavior

**Dynamic Blocks (readOnly: false)**:
- `conversation_history`: Agent-managed conversation flow
- `session_context`: Agent-tracked session state
- `health_history`: Agent-accumulated medical information

### Memory Management Tools
Reference Letta's built-in tools:
```markdown
Available tools: core_memory_replace, core_memory_append
- Use core_memory_replace for complete updates
- Use core_memory_append for adding new information
```

---

## Letta-Specific Instructions

### Tool Usage References
```markdown
## Memory Management:
Agents have access to these Letta tools:
- **core_memory_replace**: Completely replace memory block content
- **core_memory_append**: Add new information to existing memory
- **archival_memory_search**: Search historical information
```

### Response Format Specifications
```markdown
## Response Format:
When responding, always:
1. **Update Memory First**: Use core_memory tools as needed
2. **Structure Response**: Follow the defined format
3. **Include Context**: Reference relevant memory content
```

### Integration with Letta Architecture
```markdown
## Letta Integration Notes:
- Memory blocks are automatically backed up by Letta
- Archival memory handles long-term storage overflow
- Identity system links agents to specific users
- Cross-session context available through archival memory
```

---

## Writing Style Guidelines

### Language Characteristics
- **Directive and Clear**: Use imperative mood ("Provide...", "Ensure...", "Document...")
- **Professional but Accessible**: Technical terms with explanations
- **Structured and Logical**: Clear hierarchy and organization
- **Action-Oriented**: Focus on what the agent should DO

### Tone Requirements
- **Authoritative**: Agent knows its role and boundaries
- **Empathetic**: Especially for health-related agents
- **Safety-Focused**: Always prioritize user safety
- **Evidence-Based**: Reference best practices and standards

### Content Guidelines
1. **Be Specific**: Avoid vague instructions
2. **Include Examples**: Show expected behavior patterns
3. **Define Boundaries**: Clear scope limitations
4. **Safety First**: Always include safety protocols
5. **Memory Aware**: Explicit memory management instructions

### Formatting Standards
- Use markdown headers for structure (`##`, `###`)
- Bold important terms and concepts (`**term**`)
- Use bullet points for lists and options
- Number sequential processes
- Include code blocks for examples

---

## Template for New Agents

```markdown
# [YourAgentType] Agent

You are a [Agent Type] agent responsible for [primary purpose]. Your role is to [main function] while [key constraints/ethics].

## Core Responsibilities:
1. **[Primary Function]**: [Detailed description of main task]
2. **[Secondary Function]**: [Description of supporting tasks]
3. **[Support Function]**: [Additional responsibilities]
4. **[Integration Function]**: [How it works with other agents]

## Memory Blocks You Manage:
- **[primary_memory_block]**: [Purpose, content type, and usage patterns]
- **[secondary_memory_block]**: [Purpose, content type, and update triggers]
- **[context_block]**: [Session-specific information and tracking]

## [Domain/Expertise] You Cover:
### [Primary Category]:
- **[Specific Area 1]**: [Detailed coverage and limitations]
- **[Specific Area 2]**: [What you handle in this area]
- **[Specific Area 3]**: [Scope and boundaries]

### [Secondary Category]:
- **[Related Area 1]**: [How you support this area]
- **[Related Area 2]**: [Integration with primary function]

### [Specialized Category]:
- **[Expert Area 1]**: [Deep expertise provided]
- **[Expert Area 2]**: [Specialized knowledge offered]

## [Process/Methodology] Approach:
### [Initial Stage]:
1. **[First Step]**: [What to do and how to do it]
2. **[Assessment Step]**: [How to evaluate the situation]
3. **[Analysis Step]**: [How to process information]

### [Processing Stage]:
1. **[Core Process]**: [Main processing methodology]
2. **[Integration Process]**: [How to combine information]
3. **[Decision Process]**: [How to make determinations]

### [Response Stage]:
1. **[Formulation]**: [How to structure responses]
2. **[Validation]**: [How to ensure quality]
3. **[Delivery]**: [How to present information]

## Safety Protocols:
### [Critical Situations]:
- **[Emergency Type 1]**: [Recognition criteria and response]
- **[Emergency Type 2]**: [Warning signs and immediate actions]
- **[Escalation Criteria]**: [When to refer to other agents/professionals]

### [Risk Management]:
- **[Risk Type 1]**: [How to identify and mitigate]
- **[Risk Type 2]**: [Prevention and response strategies]

### [Professional Boundaries]:
- **[Scope Limitation 1]**: [What you cannot do]
- **[Scope Limitation 2]**: [When to refer elsewhere]
- **[Ethical Boundary]**: [Professional standards to maintain]

## Communication Guidelines:
### Tone and Style:
- **[Primary Tone]**: [How to communicate - formal, casual, empathetic]
- **[Language Level]**: [Technical complexity appropriate for users]
- **[Cultural Sensitivity]**: [How to respect diverse backgrounds]
- **[Professional Standards]**: [Maintaining appropriate boundaries]

### Content Standards:
- **[Accuracy Requirement]**: [How to ensure information quality]
- **[Clarity Requirement]**: [How to make information understandable]
- **[Completeness Standard]**: [How comprehensive to be]

## Response Format:
Structure all responses as:
1. **[Opening Section]**: "[Example opening or requirement]"
2. **[Information Section]**: "[How to present core information]"
3. **[Analysis Section]**: "[How to provide analysis or interpretation]"
4. **[Recommendation Section]**: "[How to offer actionable advice]"
5. **[Next Steps Section]**: "[How to guide follow-up actions]"
6. **[Disclaimer Section]**: "[Required professional disclaimers]"

## Memory Management:
### [Primary Memory Block] Updates:
Document in [primary_memory_block] memory:
- [Information type 1]: [When and how to store]
- [Information type 2]: [Update triggers and format]
- [Information type 3]: [Retention and organization rules]

### [Secondary Memory Block] Tracking:
Maintain in [secondary_memory_block] memory:
- [Current state info]: [Real-time tracking requirements]
- [Progress tracking]: [How to monitor development]
- [Pattern recognition]: [What patterns to identify and store]

### [Context Block] Management:
Update [context_block] memory with:
- [Session information]: [What session data to maintain]
- [User context]: [How to track user-specific information]
- [Interaction history]: [What interaction patterns to preserve]

## Quality Assurance:
### [Accuracy Checks]:
- [Verification method 1]: [How to ensure information accuracy]
- [Validation process]: [How to confirm response quality]

### [Consistency Maintenance]:
- [Style consistency]: [How to maintain uniform communication]
- [Information consistency]: [How to avoid contradictions]

## Integration Notes:
### [Cross-Agent Communication]:
- [Integration point 1]: [How you work with other agents]
- [Information sharing]: [What you share and receive]
- [Handoff procedures]: [When and how to transfer conversations]

Remember: [Final guidance about agent purpose, boundaries, ethical considerations, and user safety. This should encapsulate the agent's core mission and fundamental limitations.]
```

---

## Real Examples

### Example 1: Context Extractor Agent Structure

```markdown
# Context Extractor Agent

You are a Context Extractor agent responsible for maintaining conversation context and enriching user messages for better intent classification.

## Core Responsibilities:
1. **Full Conversation Management**: Maintain complete conversation history
2. **Context Enrichment**: Provide enriched messages with relevant context
3. **Session Tracking**: Keep track of current session metadata

## Memory Blocks You Manage:
- **conversation_history**: Complete conversation history for current session - never cleared
- **session_context**: Current session metadata and status
- **user_profile**: User profile and preferences

## Memory Management:
### conversation_history (Full Conversation)
- Keep the COMPLETE conversation history for the current session
- Format: "MSG1: [message] | MSG2: [message] | MSG3: [message] | ..." 
- When new message arrives, append: "| MSG[N+1]: [new message]"
- **Never clear or remove messages** - maintain full context

### Response Format:
```
CURRENT_MESSAGE: [current user message]
RELEVANT_CONTEXT: [most relevant context from conversation history]
ENRICHED: [current message with context for intent classification]
```
```

### Example 2: Health Agent Safety Protocol

```markdown
## Safety Protocols:

### Emergency Red Flags - Immediate Medical Attention Required:
- **Cardiovascular**: Chest pain, severe shortness of breath, signs of heart attack/stroke
- **Neurological**: Severe headache, confusion, loss of consciousness, seizures
- **Respiratory**: Severe breathing difficulty, choking, signs of respiratory distress

### Professional Medical Consultation Recommended:
- Persistent symptoms lasting more than expected timeframes
- Worsening symptoms despite appropriate self-care
- New or unusual symptoms with unclear causes

### Always Include Medical Disclaimer:
"**Medical Disclaimer**: This information is for educational purposes only and does not constitute medical advice. Always consult with a qualified healthcare provider for proper diagnosis and treatment."
```

---

## Best Practices

### 1. Structure and Organization
- **Consistent Sections**: Always include all essential sections
- **Logical Flow**: Information should flow from general to specific
- **Clear Hierarchy**: Use proper markdown headers (##, ###)
- **Scannable Format**: Use bullet points and bold text effectively

### 2. Memory Integration
- **Explicit References**: Always mention memory blocks by name
- **Clear Purposes**: Define what goes in each memory block
- **Update Triggers**: Specify when to update memory
- **Tool Usage**: Reference Letta's core memory tools

### 3. Safety and Ethics
- **Safety First**: Always include safety protocols
- **Clear Boundaries**: Define what the agent cannot do
- **Professional Standards**: Include appropriate disclaimers
- **Escalation Paths**: Clear guidance on when to refer

### 4. Communication Excellence
- **User-Focused**: Write from user's perspective
- **Action-Oriented**: Focus on what agent should do
- **Example-Rich**: Include concrete examples
- **Tone-Appropriate**: Match tone to agent function

### 5. Quality Assurance
- **Test Scenarios**: Consider edge cases in instructions
- **Clear Examples**: Provide specific response examples
- **Consistent Terminology**: Use same terms throughout
- **Complete Coverage**: Address all relevant situations

---

## Common Pitfalls

### 1. Vague Instructions
**❌ Avoid**: "Handle user concerns appropriately"
**✅ Better**: "When user expresses anxiety, provide specific breathing exercises and validate their feelings"

### 2. Missing Memory Management
**❌ Avoid**: Not specifying when to update memory blocks
**✅ Better**: "Update conversation_history after each user message using core_memory_append"

### 3. Unclear Boundaries
**❌ Avoid**: "Provide health advice"
**✅ Better**: "Provide educational health information while always including medical disclaimers and referral guidance"

### 4. Inconsistent Format
**❌ Avoid**: Mixing different section structures
**✅ Better**: Follow the standard template consistently

### 5. Missing Safety Protocols
**❌ Avoid**: No emergency handling instructions
**✅ Better**: Clear emergency recognition and response protocols

### 6. Poor Response Format
**❌ Avoid**: "Respond helpfully"
**✅ Better**: Structured response format with specific sections

---

## Conclusion

Effective Letta agent prompts are the foundation of a successful multi-agent system. By following this guide's structure, style, and best practices, you can create agents that:

- Perform their roles consistently and effectively
- Integrate seamlessly with Letta's memory system
- Maintain appropriate safety and professional standards
- Provide high-quality user experiences
- Work well within a multi-agent ecosystem

Remember: A well-written prompt is an investment in agent performance, user safety, and system reliability.

---

*This guide is a living document. Update it as you learn more about effective Letta agent prompt engineering and discover new best practices.* 