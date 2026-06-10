package com.caoerlin.aicontentcreation.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@RequiredArgsConstructor
public enum AgentStatusEnum {
    SUCCESS("SUCCESS", "执行成功"),
    RUNNING("RUNNING", "执行中"),
    FAILED("FAILED", "执行失败");

    private final String status;
    private final String desc;
}
