package com.caoerlin.aicontentcreation.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.caoerlin.aicontentcreation.common.exception.BusinessException;
import com.caoerlin.aicontentcreation.common.exception.ErrorCode;
import com.caoerlin.aicontentcreation.constant.AgentStatusConstant;
import com.caoerlin.aicontentcreation.constant.ArticleConstant;
import com.caoerlin.aicontentcreation.model.entity.AgentLog;
import com.caoerlin.aicontentcreation.model.enums.AgentStatusEnum;
import com.caoerlin.aicontentcreation.model.vo.agentlog.AgentExecutionStats;
import com.caoerlin.aicontentcreation.service.AgentLogService;
import com.caoerlin.aicontentcreation.mapper.AgentLogMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Administrator
 * @description 针对表【agent_log(智能体执行日志表)】的数据库操作Service实现
 * @createDate 2026-06-10 14:07:10
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AgentLogServiceImpl extends ServiceImpl<AgentLogMapper, AgentLog>
        implements AgentLogService {
    private final AgentLogMapper agentLogMapper;

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

    @Override
    public AgentExecutionStats getExecutionStats(String taskId) {
        if (StrUtil.isBlank(taskId)) {
            log.error("任务id不能为空");
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "任务id不能为空");
        }

        List<AgentLog> agentLogList = getAgentLogByTaskId(taskId);
        if (CollUtil.isEmpty(agentLogList)) {
            return AgentExecutionStats.builder()
                    .taskId(taskId)
                    .agentCount(0)
                    .totalDurationMs(0)
                    .overallStatus("NOT_FOUND")
                    .build();
        }

        //计算统计
        int totalDuration = 0;
        Map<String, Integer> agentDurations = new HashMap<>();
        String overallStatus = AgentStatusEnum.SUCCESS.getStatus();

        for (AgentLog agentLog : agentLogList) {
            //累加总数
            if (ObjectUtil.isNotNull(agentLog.getDurationMs())) {
                Integer durationMs = agentLog.getDurationMs();
                String agentName = agentLog.getAgentName();
                totalDuration += durationMs;
                agentDurations.put(agentName, durationMs);
            }

            //判断总体状态
            if (StrUtil.equals(AgentStatusConstant.FAILED, agentLog.getStatus())) {
                overallStatus = AgentStatusConstant.FAILED;
            } else if (StrUtil.equals(AgentStatusConstant.RUNNING, agentLog.getStatus())
                    && !StrUtil.equals(AgentStatusConstant.FAILED, agentLog.getStatus())) {
                overallStatus = AgentStatusConstant.RUNNING;
            }
        }
        return AgentExecutionStats.builder()
                .taskId(taskId)
                .agentCount(agentLogList.size())
                .totalDurationMs(totalDuration)
                .overallStatus(overallStatus)
                .agentDurations(agentDurations)
                .logs(agentLogList)
                .build();
    }

    private List<AgentLog> getAgentLogByTaskId(String taskId) {
        LambdaQueryWrapper<AgentLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AgentLog::getTaskId, taskId);
        return agentLogMapper.selectList(wrapper);
    }
}




