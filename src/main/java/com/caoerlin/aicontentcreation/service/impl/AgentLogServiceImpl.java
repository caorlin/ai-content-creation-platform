package com.caoerlin.aicontentcreation.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.caoerlin.aicontentcreation.model.entity.AgentLog;
import com.caoerlin.aicontentcreation.service.AgentLogService;
import com.caoerlin.aicontentcreation.mapper.AgentLogMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
* @author Administrator
* @description 针对表【agent_log(智能体执行日志表)】的数据库操作Service实现
* @createDate 2026-06-10 14:07:10
*/
@Slf4j
@Service
public class AgentLogServiceImpl extends ServiceImpl<AgentLogMapper, AgentLog>
    implements AgentLogService{

    @Override
    @Async
    public void saveLogAsync(AgentLog agentLog) {
        try {
            save(agentLog);
            log.info("智能体执行日志保存成功,taskId={},agentName={},status={},durationMs={}",
                    agentLog.getTaskId(),
                    agentLog.getAgentName(),
                    agentLog.getStatus(),
                    agentLog.getDurationMs()
            );
        } catch (Exception e) {
            log.info("智能体执行日志保存失败,taskId={},agentName={}}",
                    agentLog.getTaskId(),
                    agentLog.getAgentName(),
                    e
            );
        }
    }
}




