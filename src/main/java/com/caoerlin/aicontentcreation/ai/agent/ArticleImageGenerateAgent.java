package com.caoerlin.aicontentcreation.ai.agent;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONUtil;
import com.caoerlin.aicontentcreation.ai.common.enums.SseMessageTypeEnum;
import com.caoerlin.aicontentcreation.common.annotation.AgentExecution;
import com.caoerlin.aicontentcreation.manager.CosManager;
import com.caoerlin.aicontentcreation.model.dto.article.ArticleState;
import com.caoerlin.aicontentcreation.model.dto.image.ImageRequest;
import com.caoerlin.aicontentcreation.model.enums.ImageMethodEnum;
import com.caoerlin.aicontentcreation.service.ImageSearchService;
import com.caoerlin.aicontentcreation.service.strategy.ImageServiceStrategy;
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
    private final List<ImageSearchService> imageSearchService;
    private final CosManager cosManager;
    private final ImageServiceStrategy imageServiceStrategy;

    @AgentExecution(value = "ARTICLE_IMAGE_GENERATE_AGENT", description = "文章配图检索Agent")
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

            //构建图片请求
            ImageRequest request = ImageRequest.builder()
                    .keywords(imageRequirement.getKeywords())
                    .position(imageRequirement.getPosition())
                    .prompt(imageRequirement.getPrompt())
                    .type(imageRequirement.getType())
                    .build();

            //检索生成图片，并且上传到cos
            ImageServiceStrategy.ImageResult result = imageServiceStrategy.getImageAndUpload(imageRequirement.getImageSource(), request);

            String cosUrl = result.getUrl();
            ImageMethodEnum method = result.getMethod();

            //构建图片结果
            ArticleState.ImageResult imageResult = buildImageResult(imageRequirement, cosUrl, method);
            imageResultList.add(imageResult);

            //推送单个图片结果
            String imageResultStr = SseMessageTypeEnum.ARTICLE_IMAGE_GENERATE_AGENT_COMPLETE.getStreamingPrefix() + JSONUtil.toJsonStr(imageResult);
            log.info("开始执行 ArticleImageGenerateAgent,开始推送结果, imageResult={}", imageResultStr);
            streamHandler.accept(imageResultStr);

            log.info("执行 ArticleImageGenerateAgent,文章配图检索成功,Position={},method={}", imageRequirement.getPosition(), imageResult.getMethod());
        }
        state.setImages(imageResultList);

        log.info("ArticleImageGenerateAgent智能体,检索所有图片成功,imageSize={}", imageResultList.size());
    }

    private ArticleState.ImageResult buildImageResult(ArticleState.ImageRequirement imageRequirement, String cosUrl, ImageMethodEnum method) {
        return ArticleState.ImageResult.builder()
                .position(imageRequirement.getPosition())
                .url(cosUrl)
                .method(method.getName())
                .keywords(imageRequirement.getKeywords())
                .sectionTitle(imageRequirement.getSectionTitle())
                .placeholderId(imageRequirement.getPlaceholderId())
                .description(imageRequirement.getType())
                .build();
    }

    /**
     * 获取配图结果
     *
     * @param imageRequirement
     * @param cosImageUrl
     * @param method
     * @return
     */
//    private ArticleState.ImageResult getImageResult(ArticleState.ImageRequirement imageRequirement, String cosImageUrl, ImageMethodEnum method) {
//        ArticleState.ImageResult imageResult = new ArticleState.ImageResult();
//        imageResult.setUrl(cosImageUrl);
//        imageResult.setKeywords(imageRequirement.getKeywords());
//        imageResult.setDescription(imageRequirement.getType());
//        imageResult.setMethod(method.getName());
//        imageResult.setPosition(imageRequirement.getPosition());
//        imageResult.setSectionTitle(imageRequirement.getSectionTitle());
//        return imageResult;
//    }
}
