package com.health.agents.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserAgentMapping {
    private String userId;
    private String identityId;
    private String contextCoordinatorId;
    private String intentClassifierId;
    private String generalHealthId;
    private String mentalHealthId;
} 