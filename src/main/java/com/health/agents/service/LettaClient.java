package com.health.agents.service;

import java.util.Arrays;
import com.health.agents.integration.letta.model.LettaMessage;
import com.health.agents.integration.letta.model.LettaMessageRequest;
import com.health.agents.integration.letta.model.LettaMessageResponse;
import com.health.agents.integration.letta.service.LettaAgentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class LettaClient {

  private static final Logger log = LoggerFactory.getLogger(LettaClient.class);

  @Autowired
  private LettaAgentService lettaAgentService;

  /**
   * Send a message to a Letta agent and return the response content
   *
   * @param agentId The ID of the agent to send the message to
   * @param identityId The identity ID for the request (will be mapped to senderId)
   * @param content The message content to send
   * @return The agent's response content as a string
   */
  public String sendMessage(String agentId, String identityId, String content) {
    try {
      log.debug("Sending message to agent {}: {}", agentId, content);

      // Create the message request using your actual model structure
      LettaMessageRequest request = LettaMessageRequest.builder()
          .messages(Arrays.asList(
              LettaMessage.builder()
                  .role("user")
                  .content(content)
                  .build()
          ))
          .senderId(identityId)  // Use senderId instead of identityId
          .build();

      // Send the message using the existing LettaAgentService
      LettaMessageResponse response = lettaAgentService.sendMessage(agentId, request);

      // Extract the response content
      if (response != null && response.getMessages() != null && !response.getMessages().isEmpty()) {
        // Get the last message from the agent (should be the response)
        String responseContent = response.getMessages().get(response.getMessages().size() - 1).getContent();
        log.debug("Received response from agent {}: {}", agentId, responseContent);
        return responseContent;
      } else {
        log.warn("Empty response from agent {}", agentId);
        return "Agent response was empty";
      }

    } catch (Exception e) {
      log.error("Error sending message to agent {}: {}", agentId, e.getMessage(), e);
      throw new RuntimeException("Failed to communicate with agent: " + agentId, e);
    }
  }

  /**
   * Send a message to a Letta agent with additional options
   *
   * @param agentId The ID of the agent to send the message to
   * @param identityId The identity ID for the request
   * @param content The message content to send
   * @param includeHistory Whether to include conversation history
   * @return The agent's response content as a string
   */
  public String sendMessage(String agentId, String identityId, String content, boolean includeHistory) {
    // For now, this delegates to the main sendMessage method
    // You can extend this later to handle history inclusion differently
    return sendMessage(agentId, identityId, content);
  }

  /**
   * Send a structured message to a Letta agent
   *
   * @param agentId The ID of the agent to send the message to
   * @param request The complete message request
   * @return The agent's response content as a string
   */
  public String sendStructuredMessage(String agentId, LettaMessageRequest request) {
    try {
      log.debug("Sending structured message to agent {}", agentId);

      LettaMessageResponse response = lettaAgentService.sendMessage(agentId, request);

      if (response != null && response.getMessages() != null && !response.getMessages().isEmpty()) {
        String responseContent = response.getMessages().get(response.getMessages().size() - 1).getContent();
        log.debug("Received structured response from agent {}", agentId);
        return responseContent;
      } else {
        log.warn("Empty structured response from agent {}", agentId);
        return "Agent response was empty";
      }

    } catch (Exception e) {
      log.error("Error sending structured message to agent {}: {}", agentId, e.getMessage(), e);
      throw new RuntimeException("Failed to communicate with agent: " + agentId, e);
    }
  }

  /**
   * Send a message with retry logic
   *
   * @param agentId The ID of the agent to send the message to
   * @param identityId The identity ID for the request
   * @param content The message content to send
   * @param maxRetries Maximum number of retry attempts
   * @return The agent's response content as a string
   */
  public String sendMessageWithRetry(String agentId, String identityId, String content, int maxRetries) {
    Exception lastException = null;

    for (int attempt = 1; attempt <= maxRetries; attempt++) {
      try {
        log.debug("Attempt {} to send message to agent {}", attempt, agentId);
        return sendMessage(agentId, identityId, content);
      } catch (Exception e) {
        lastException = e;
        log.warn("Attempt {} failed for agent {}: {}", attempt, agentId, e.getMessage());

        if (attempt < maxRetries) {
          try {
            // Wait before retry (exponential backoff)
            Thread.sleep(1000 * attempt);
          } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted during retry", ie);
          }
        }
      }
    }

    throw new RuntimeException("Failed to send message after " + maxRetries + " attempts", lastException);
  }

  /**
   * Send a message and get the full response object
   *
   * @param agentId The ID of the agent to send the message to
   * @param identityId The identity ID for the request
   * @param content The message content to send
   * @return The complete LettaMessageResponse object
   */
  public LettaMessageResponse sendMessageGetFullResponse(String agentId, String identityId, String content) {
    try {
      log.debug("Sending message to agent {} for full response", agentId);

      LettaMessageRequest request = LettaMessageRequest.builder()
          .messages(Arrays.asList(
              LettaMessage.builder()
                  .role("user")
                  .content(content)
                  .build()
          ))
          .senderId(identityId)  // Use senderId instead of identityId
          .build();

      return lettaAgentService.sendMessage(agentId, request);

    } catch (Exception e) {
      log.error("Error getting full response from agent {}: {}", agentId, e.getMessage(), e);
      throw new RuntimeException("Failed to get full response from agent: " + agentId, e);
    }
  }
}

