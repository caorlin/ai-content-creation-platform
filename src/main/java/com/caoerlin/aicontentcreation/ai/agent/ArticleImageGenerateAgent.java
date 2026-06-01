package com.caoerlin.aicontentcreation.ai.agent;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.caoerlin.aicontentcreation.ai.common.enums.SseMessageTypeEnum;
import com.caoerlin.aicontentcreation.manager.CosManager;
import com.caoerlin.aicontentcreation.model.dto.article.ArticleState;
import com.caoerlin.aicontentcreation.model.enums.ImageMethodEnum;
import com.caoerlin.aicontentcreation.service.ImageSearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@Slf4j
@Service
@RequiredArgsConstructor
public class ArticleImageGenerateAgent {
    private final ImageSearchService imageSearchService;
    private final CosManager cosManager;

    public void generateArticleImage(ArticleState state, Consumer<String> streamHandler) {
        if (ObjectUtil.isNull(state) && CollectionUtil.isEmpty(state.getImageRequirements())) {
            log.warn("开始执行 ArticleImageGenerateAgent,参数异常: taskId={},ArticleState={}", state.getTaskId(), JSONUtil.toJsonStr(state));
            return;
        }
        //生成图片结果列表
        ArrayList<ArticleState.ImageResult> imageResultList = new ArrayList<>();
        //获取文章需要的图片搜索信息列表
        List<ArticleState.ImageRequirement> imageRequirementList = state.getImageRequirements();
        //逐条生成图片
        for (ArticleState.ImageRequirement imageRequirement : imageRequirementList) {
            log.info("开始执行 ArticleImageGenerateAgent,开始检索图片 Position={},keywords={}", imageRequirement.getPosition(), imageRequirement.getKeywords());

            //开始搜索
            String imageUrl = imageSearchService.searchImage(imageRequirement.getKeywords());

            ImageMethodEnum method = imageSearchService.getMethod();

            if (StrUtil.isBlank(imageUrl)) {
                //使用降级策略
                imageUrl = imageSearchService.getFallbackImage(imageRequirement.getPosition());
                method = ImageMethodEnum.PICSUM;
                log.warn("ArticleImageGenerateAgent智能体检索图片失败,开始服务降级处理,随机生成url,Position={}", imageRequirement.getPosition());
            }

            //将图片上传到cos，现阶段先不存入cos
            log.info("开始执行 ArticleImageGenerateAgent,检索图片成功,imageUrl={}", imageUrl);
            String cosImageUrl = cosManager.useDirectUrl(imageUrl);
            log.info("开始执行 ArticleImageGenerateAgent,图片上传cos成功,cosImageUrl={}", cosImageUrl);

            //配图结果
            ArticleState.ImageResult imageResult = getImageResult(imageRequirement, cosImageUrl, method);
            imageResultList.add(imageResult);

            //推送单个图片结果
            String imageResultStr = SseMessageTypeEnum.ARTICLE_IMAGE_GENERATE_AGENT_COMPLETE.getStreamingPrefix() + JSONUtil.toJsonStr(imageResult);
            log.info("开始执行 ArticleImageGenerateAgent,开始推送结果, imageResult={}", imageResultStr);
            streamHandler.accept(imageResultStr);

            log.info("开始执行 ArticleImageGenerateAgent,文章配图检索成功,Position={},keywords={}", imageRequirement.getPosition(), method);
        }
        state.setImages(imageResultList);

        log.info("ArticleImageGenerateAgent智能体,检索所有图片成功,imageSize={}", imageResultList.size());
    }

    /**
     * 获取配图结果
     *
     * @param imageRequirement
     * @param cosImageUrl
     * @param method
     * @return
     */
    private ArticleState.ImageResult getImageResult(ArticleState.ImageRequirement imageRequirement, String cosImageUrl, ImageMethodEnum method) {
        ArticleState.ImageResult imageResult = new ArticleState.ImageResult();
        imageResult.setUrl(cosImageUrl);
        imageResult.setKeywords(imageRequirement.getKeywords());
        imageResult.setDescription(imageRequirement.getType());
        imageResult.setMethod(method.getName());
        imageResult.setPosition(imageRequirement.getPosition());
        imageResult.setSectionTitle(imageRequirement.getSectionTitle());
        return imageResult;
    }
}
