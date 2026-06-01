package com.caoerlin.aicontentcreation.manager;

import cn.hutool.core.util.ObjectUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.caoerlin.aicontentcreation.constant.ArticleConstant.SSE_RECONNECT_TIME;
import static com.caoerlin.aicontentcreation.constant.ArticleConstant.SSE_TIME_OUT;

@Slf4j
@Component
public class SseEmitterManager {

    /**
     * 记录所有的SseEmitter
     */
    private static final Map<String, SseEmitter> emitterMap = new ConcurrentHashMap<>();

    /**
     * 创建 SSE
     *
     * @param taskId 任务id
     * @return SseEmitter
     */

    public SseEmitter createEmitter(String taskId) {
        SseEmitter emitter = new SseEmitter(SSE_TIME_OUT);

        //设置超时回调
        emitter.onTimeout(() -> {
            log.warn("SSE 连接超时,taskId={}", taskId);
            emitterMap.remove(taskId);
        });

        //sse完成回调
        emitter.onCompletion(() -> {
            log.info("SSE 执行完成,taskId={}", taskId);
            emitterMap.remove(taskId);
        });

        //sse执行错误回调
        emitter.onError(e -> {
            log.error("SSE 执行发生异常,taskId={},e={}", taskId, e.getMessage());
            emitterMap.remove(taskId);
        });

        emitterMap.put(taskId, emitter);
        log.info("创建SSE连接,taskId={}", taskId);

        return emitter;
    }

    /**
     * SSE 发送消息
     *
     * @param taskId  任务id
     * @param message 消息
     */
    public void send(String taskId, String message) {
        //根据taskId获取当前任务的SSE连接
        SseEmitter emitter = emitterMap.get(taskId);
        if (ObjectUtil.isNull(emitter)) {
            log.warn("任务：taskId={},SSE 连接不存在", taskId);
            return;
        }

        try {
            //发送SSE消息
            emitter.send(SseEmitter
                    .event()
                    .data(message)
                    .reconnectTime(SSE_RECONNECT_TIME)
                    .build());
            log.debug("SSE发送消息成功,taskId={},message={}", taskId, message);
        } catch (IOException e) {
            log.error("SSE发送消息失败,taskId={},e={}", taskId, e.getMessage());
            emitterMap.remove(taskId);
        }
    }

    /**
     * SSE 完成
     *
     * @param taskId 任务id
     */
    public void complete(String taskId) {
        //根据taskId获取当前任务的SSE连接
        SseEmitter emitter = emitterMap.get(taskId);
        if (ObjectUtil.isNull(emitter)) {
            log.warn("任务：taskId={},SSE 连接不存在", taskId);
            return;
        }

        try {
            emitter.complete();
            log.info("SSE 连接已完成,taskId={}", taskId);
        } catch (Exception e) {
            log.error("SSE 连接完成失败,taskId={},e={}", taskId, e.getMessage());
        } finally {
            emitterMap.remove(taskId);
        }
    }

    /**
     * 检查SSE是否存在
     *
     * @param taskId 任务id
     * @return
     */
    public boolean exists(String taskId) {
        return emitterMap.containsKey(taskId);
    }
}
