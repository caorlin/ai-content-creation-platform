package com.caoerlin.aicontentcreation.common.aop;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.caoerlin.aicontentcreation.common.annotation.AgentExecution;
import com.caoerlin.aicontentcreation.common.exception.BusinessException;
import com.caoerlin.aicontentcreation.common.exception.ErrorCode;
import com.caoerlin.aicontentcreation.model.dto.article.ArticleState;
import com.caoerlin.aicontentcreation.model.entity.AgentLog;
import com.caoerlin.aicontentcreation.model.enums.AgentStatusEnum;
import com.caoerlin.aicontentcreation.service.AgentLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class AgentIntercept {
    private final AgentLogService agentLogService;

    @Around("@annotation(agentExecution)")
    public Object aroundAgentExecution(ProceedingJoinPoint pjp, AgentExecution agentExecution) {
        //agent执行起始时间
        long startTime = System.currentTimeMillis();
        LocalDateTime startDateTime = LocalDateTime.now();

        //提取taskId和输入参数
        String taskId = extractTaskId(pjp);
        String inputData = extractInputData(pjp);
        String prompt = extractPrompt(pjp);

        AgentLog agentLog = new AgentLog();
        agentLog.setTaskId(taskId);
        agentLog.setAgentName(agentExecution.value());
        agentLog.setInputData(inputData);
        agentLog.setPrompt(prompt);
        agentLog.setStartTime(startDateTime);
        agentLog.setStatus(AgentStatusEnum.RUNNING.getStatus());

        Object result;
        try {
            //执行方法目标
            result = pjp.proceed();

            //记录成功状态
            agentLog.setStatus(AgentStatusEnum.SUCCESS.getStatus());
            agentLog.setEndTime(LocalDateTime.now());
            agentLog.setDurationMs((int) (System.currentTimeMillis() - startTime));
            agentLog.setOutputData(extractOutputData(result));

            log.info("智能体执行成功,{},taskId={},耗时={}ms", agentLog.getAgentName(), taskId, agentLog.getDurationMs());
        } catch (Throwable e) {
            //记录执行失败的信息
            agentLog.setStatus(AgentStatusEnum.FAILED.getStatus());
            agentLog.setEndTime(LocalDateTime.now());
            agentLog.setDurationMs((int) (System.currentTimeMillis() - startTime));
            agentLog.setErrorMessage(StrUtil.isNotBlank(e.getMessage()) ? e.getMessage() : e.getClass().getName());

            log.error("智能体执行失败：{},taskId={}", agentLog.getAgentName(), taskId, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "智能体执行失败");
        } finally {
            agentLogService.saveLogAsync(agentLog);
        }
        return result;
    }

    /**
     * 提取输出参数
     */
    private String extractOutputData(Object result) {
        try {
            if (ObjectUtil.isNull(result)) {
                return null;
            }

            //只记录基本类型和简单类型
            if (result instanceof String || result instanceof Number || result instanceof Boolean) {
                return String.valueOf(result);
            }

            if (result instanceof List) {
                return "{\"listSize\":" + ((List<?>) result).size() + "}";
            }

            return "{\"type\":" + result.getClass().getSimpleName() + "}";
        } catch (Exception e) {
            log.error("提取输出数据失败", e);
            return null;
        }
    }

    /**
     * 提取提示词
     */
    private String extractPrompt(ProceedingJoinPoint pjp) {
        try {
            //根据方法名判断使用的提示词
            MethodSignature methodSignature = (MethodSignature) pjp.getSignature();
            Method method = methodSignature.getMethod();
            return method.getDeclaringClass().getSimpleName() + "." + method.getName();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 提取输入参数
     *
     */
    private String extractInputData(ProceedingJoinPoint pjp) {
        try {
            //获取输入参数
            Object[] args = pjp.getArgs();
            if (ArrayUtil.isEmpty(args)) {
                return null;
            }

            //获取方法签名
            MethodSignature methodSignature = (MethodSignature) pjp.getSignature();
            //获取参数名称
            String[] parameterNames = methodSignature.getParameterNames();

            //开始提取参数
            Map<String, Object> inputMap = new HashMap<>();
            for (int i = 0; i < args.length && i < parameterNames.length; i++) {
                Object arg = args[i];
                //只记录基本类型和简单类型
                if (arg instanceof String || arg instanceof Number || arg instanceof Boolean) {
                    inputMap.put(parameterNames[i], arg);
                } else if (arg instanceof ArticleState) {
                    ArticleState articleState = (ArticleState) arg;
                    inputMap.put("taskId", articleState.getTaskId());
                    if (ObjectUtil.isNotNull(articleState.getTitle())) {
                        inputMap.put("mainTitle", articleState.getTitle().getMainTitle());
                    }
                }
            }

            return inputMap.isEmpty() ? null : JSONUtil.toJsonStr(inputMap);
        } catch (Exception e) {
            log.error("提取输入参数失败,e={}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * 提取任务id
     */
    private String extractTaskId(ProceedingJoinPoint pjp) {
        Object[] args = pjp.getArgs();
        if (ArrayUtil.isEmpty(args)) {
            return "unknown";
        }

        //优先从ArticleState只能提取
        for (Object arg : args) {
            if (arg instanceof ArticleState) {
                return ((ArticleState) arg).getTaskId();
            }
        }

        //尝试从第一个参数提取,第一个可能是taskId
        for (Object arg : args) {
            if (arg instanceof String) {
                return (String) arg;
            }
        }
        return "unknown";
    }
}
