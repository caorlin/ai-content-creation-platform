package com.caoerlin.aicontentcreation.ai.agent;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONUtil;
import com.caoerlin.aicontentcreation.ai.common.enums.SseMessageTypeEnum;
import com.caoerlin.aicontentcreation.ai.constant.PromptConstant;
import com.caoerlin.aicontentcreation.ai.utils.AiModelCallingUtils;
import com.caoerlin.aicontentcreation.ai.utils.AiResponseParseUtils;
import com.caoerlin.aicontentcreation.model.dto.article.ArticleState;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.function.Consumer;

import static com.caoerlin.aicontentcreation.model.enums.ArticleStyleEnum.getArticleStylePrompt;

/**
 * 文章大纲生成
 *
 * @author zyj
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ArticleOutlineAgent {
    private final ChatModel articleContentModel;

    public void generateArticleOutline(ArticleState articleState, Consumer<String> consumer) {
        if (ObjectUtil.isNull(articleState)) {
            log.error("生成文章大纲异常,文章状态为空");
            return;
        }
        //获取文章标题
        ArticleState.TitleResult title = articleState.getTitle();
        //生成文章大纲prompt
        String articleOutlinePrompt = PromptConstant.ARTICLE_OUTLINE_AGENT_PROMPT
                .replace("{mainTitle}", title.getMainTitle())
                .replace("{subTitle}", title.getSubTitle())
                + getArticleStylePrompt(articleState.getStyle());
        String content = AiModelCallingUtils.callModelWithStreaming(articleContentModel, articleOutlinePrompt, consumer, SseMessageTypeEnum.ARTICLE_OUTLINE_AGENT_STREAMING);
        //解析大纲
        ArticleState.OutlineResult outlineResult = AiResponseParseUtils.parseJsonResponse(content, ArticleState.OutlineResult.class, "文章大纲");
        articleState.setOutline(outlineResult);

        log.info("智能体 ArticleOutlineAgent 生成文章大纲成功,sections={}", outlineResult.getSections());
    }

    /**
     * AI 修改大纲
     *
     * @param mainTitle          主标题
     * @param subTitle           副标题
     * @param currentOutlineList 当前大纲
     * @param modifySuggestion   用户的修改建议
     * @return 修改后的大纲
     */
    public List<ArticleState.OutlineSection> aiModifyOutline(String mainTitle, String subTitle, List<ArticleState.OutlineSection> currentOutlineList, String modifySuggestion) {

        String currentOutline = JSONUtil.toJsonStr(currentOutlineList);

        //替换
        String modifyOutlinePrompt = PromptConstant.AI_MODIFY_OUTLINE_PROMPT
                .replace("{mainTitle}", mainTitle)
                .replace("{subTitle}", subTitle)
                .replace("{currentOutline}", currentOutline)
                .replace("modifySuggestion", modifySuggestion);

        String modifyOutline = AiModelCallingUtils.callModel(articleContentModel, modifyOutlinePrompt);

        ArticleState.OutlineResult modifyOutlineResult = AiResponseParseUtils.parseJsonResponse(modifyOutline, ArticleState.OutlineResult.class, "AI 修改大纲");

        log.info("AI 修改大纲成功,modifyOutlineResultSize={}", modifyOutlineResult.getSections().size());
        return modifyOutlineResult.getSections();
    }
}
