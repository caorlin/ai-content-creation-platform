package com.caoerlin.aicontentcreation.constant;

public interface ArticleConstant {
    /**
     * sse超时时间(ms)  30分钟
     */
    Long SSE_TIME_OUT = 30 * 60 * 1000L;

    /**
     * ss重连时间(ms)
     */
    Long SSE_RECONNECT_TIME = 3000L;

    /**
     * 文章标题生成完成
     */
    String ARTICLE_TITLE_AGENT_COMPLETE = "ARTICLE_TITLE_AGENT_COMPLETE";

    /**
     * 文章大纲生成完成
     */
    String ARTICLE_OUTLINE_AGENT_AGENT_COMPLETE = "ARTICLE_OUTLINE_AGENT_AGENT_COMPLETE";

    /**
     * 文章内容生成完成
     */
    String ARTICLE_CONTENT_AGENT_COMPLETE = "ARTICLE_CONTENT_AGENT_COMPLETE";

    /**
     * 配图需求生成完成
     */
    String ARTICLE_IMAGE_REQUIREMENTS_AGENT_COMPLETE = "ARTICLE_IMAGE_REQUIREMENTS_AGENT_COMPLETE";

    /**
     * 图片检索生成完成
     */
    String ARTICLE_IMAGE_GENERATE_AGENT_COMPLETE = "ARTICLE_IMAGE_GENERATE_AGENT_COMPLETE";

    /**
     * 图文合并完成
     */
    String MERGE_COMPLETE = "MERGE_COMPLETE";
}
