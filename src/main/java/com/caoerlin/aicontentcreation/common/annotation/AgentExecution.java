package com.caoerlin.aicontentcreation.common.annotation;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AgentExecution {
    /**
     * 智能体名称
     * 例如：ARTICLE_TITLE_AGENT、ARTICLE_OUTLINE_AGENT
     */
    String value();

    /**
     * 描述
     */
    String description() default "";
}
