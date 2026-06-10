package com.caoerlin.aicontentcreation.constant;

import com.caoerlin.aicontentcreation.model.enums.AgentStatusEnum;

public interface AgentStatusConstant {
    String SUCCESS = AgentStatusEnum.SUCCESS.getStatus();
    String RUNNING = AgentStatusEnum.RUNNING.getStatus();
    String FAILED = AgentStatusEnum.FAILED.getStatus();
}
