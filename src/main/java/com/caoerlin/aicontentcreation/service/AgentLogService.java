package com.caoerlin.aicontentcreation.service;

import com.caoerlin.aicontentcreation.model.entity.AgentLog;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * @author Administrator
 * @description 针对表【agent_log(智能体执行日志表)】的数据库操作Service
 * @createDate 2026-06-10 14:07:10
 */
public interface AgentLogService extends IService<AgentLog> {

    /**
     * 异步保存智能体执行日志
     *
     * @param agentLog agent日志对象
     */
    void saveLogAsync(AgentLog agentLog);
}
