package com.caoerlin.aicontentcreation.model.dto.article;

import com.caoerlin.aicontentcreation.common.request.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author zyj
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ArticleQueryRequest extends PageRequest implements Serializable {
    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 状态
     */
    private String status;

    @Serial
    private static final long serialVersionUID = 1L;
}
