package com.caoerlin.aicontentcreation.service.impl;

import com.caoerlin.aicontentcreation.ai.agent.ArcticTitleAgent;
import com.caoerlin.aicontentcreation.ai.common.enums.SseMessageTypeEnum;
import com.caoerlin.aicontentcreation.common.exception.BusinessException;
import com.caoerlin.aicontentcreation.common.exception.ErrorCode;
import com.caoerlin.aicontentcreation.model.dto.article.ArticleState;
import com.caoerlin.aicontentcreation.service.ArticleAgentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.function.Consumer;

@Slf4j
@Service
@RequiredArgsConstructor
public class ArticleAgentServiceImpl implements ArticleAgentService {
    private final ArcticTitleAgent arcticTitleAgent;

    @Override
    public void executeArticleTitleGeneratePhage(ArticleState state, Consumer<String> streamHandler) {
        try {
            log.info("文章标题方案生成阶段,开始生成文章可选标题列表,taskId={},topic={},style={}", state.getTaskId(), state.getTopic(), state.getStyle());
            //开始生成文章标题
            arcticTitleAgent.generateArticleTitle(state);
            streamHandler.accept(SseMessageTypeEnum.ARTICLE_TITLE_AGENT_COMPLETE.getValue());
            log.info("文章可选标题列表生成完成,taskId={},titleOptionSize={}", state.getTaskId(), state.getTitleOptions().size());
        } catch (Exception e) {
            log.error("文章可选标题列表生成失败,taskId={},e={}", state.getTaskId(), e.getMessage());
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "文章标题方案生成失败");
        }
    }
}
