package com.caoerlin.aicontentcreation.controller;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.caoerlin.aicontentcreation.common.annotation.AuthCheck;
import com.caoerlin.aicontentcreation.common.exception.ErrorCode;
import com.caoerlin.aicontentcreation.common.exception.ThrowUtils;
import com.caoerlin.aicontentcreation.common.request.DeleteRequest;
import com.caoerlin.aicontentcreation.common.response.BaseResponse;
import com.caoerlin.aicontentcreation.common.response.ResultUtils;
import com.caoerlin.aicontentcreation.manager.SseEmitterManager;
import com.caoerlin.aicontentcreation.model.dto.article.ArticleCreateRequest;
import com.caoerlin.aicontentcreation.model.dto.article.ArticleQueryRequest;
import com.caoerlin.aicontentcreation.model.enums.ArticleStyleEnum;
import com.caoerlin.aicontentcreation.model.vo.article.ArticleVO;
import com.caoerlin.aicontentcreation.model.vo.user.LoginUserVO;
import com.caoerlin.aicontentcreation.service.ArticleAsyncService;
import com.caoerlin.aicontentcreation.service.ArticleService;
import com.caoerlin.aicontentcreation.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * @author zyj
 */
@Slf4j
@RestController
@Tag(name = "文章管理接口")
@RequestMapping("article")
@RequiredArgsConstructor
public class ArticleController {
    private final UserService userService;
    private final ArticleService articleService;
    private final ArticleAsyncService articleAsyncService;
    private final SseEmitterManager sseEmitterManager;

    @PostMapping("create")
    @Operation(summary = "文章创建接口")
    public BaseResponse<String> createArticle(@RequestBody ArticleCreateRequest request,
                                              HttpServletRequest httpServletRequest) {
        String topic = request.getTopic();
        String style = request.getStyle();
        ThrowUtils.throwIf(ObjectUtil.isNull(request), ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(StrUtil.isBlank(topic), ErrorCode.PARAMS_ERROR, "选题不能为空");
        ThrowUtils.throwIf(!ArticleStyleEnum.hasStyle(style), ErrorCode.PARAMS_ERROR, "无效的文章风格");

        //获取当前登录对象
        LoginUserVO loginUser = userService.getLoginUser(httpServletRequest);

        //执行文章任务创建
        String taskId = articleService.createArticleTask(topic, style, loginUser);

        //异步创建文章
        articleAsyncService.executeArticleGeneration(taskId, style, topic);
        return ResultUtils.success(taskId);
    }

    /**
     * SSE 进度推送
     */
    @GetMapping("/progress/{taskId}")
    @Operation(summary = "获取文章生成进度(SSE)")
    public SseEmitter getProgress(@PathVariable String taskId, HttpServletRequest httpServletRequest) {
        ThrowUtils.throwIf(taskId == null || taskId.trim().isEmpty(),
                ErrorCode.PARAMS_ERROR, "任务ID不能为空");

        // 校验权限（内部会检查任务是否存在以及用户是否有权限访问）
        LoginUserVO loginUser = userService.getLoginUser(httpServletRequest);
        articleService.getArticleDetail(taskId, loginUser);

        // 创建 SSE Emitter
        SseEmitter emitter = sseEmitterManager.createEmitter(taskId);

        log.info("SSE 连接已建立, taskId={}", taskId);
        return emitter;
    }

    /**
     * 获取文章详情
     */
    @GetMapping("/{taskId}")
    @Operation(summary = "获取文章详情")
    @AuthCheck(mustRole = "user")
    public BaseResponse<ArticleVO> getArticle(@PathVariable String taskId,
                                              HttpServletRequest httpServletRequest) {
        ThrowUtils.throwIf(taskId == null || taskId.trim().isEmpty(),
                ErrorCode.PARAMS_ERROR, "任务ID不能为空");

        LoginUserVO loginUser = userService.getLoginUser(httpServletRequest);
        ArticleVO articleVO = articleService.getArticleDetail(taskId, loginUser);

        return ResultUtils.success(articleVO);
    }

    /**
     * 分页查询文章列表
     */
    @PostMapping("/list")
    @Operation(summary = "分页查询文章列表")
    @AuthCheck(mustRole = "user")
    public BaseResponse<Page<ArticleVO>> listArticle(@RequestBody ArticleQueryRequest request,
                                                     HttpServletRequest httpServletRequest) {
        LoginUserVO loginUser = userService.getLoginUser(httpServletRequest);
        Page<ArticleVO> articleVOPage = articleService.listArticleByPage(request, loginUser);

        return ResultUtils.success(articleVOPage);
    }

    /**
     * 删除文章
     */
    @PostMapping("/delete")
    @Operation(summary = "删除文章")
    @AuthCheck(mustRole = "user")
    public BaseResponse<Boolean> deleteArticle(@RequestBody DeleteRequest deleteRequest,
                                               HttpServletRequest httpServletRequest) {
        ThrowUtils.throwIf(deleteRequest == null || deleteRequest.getId() == null,
                ErrorCode.PARAMS_ERROR);

        LoginUserVO loginUser = userService.getLoginUser(httpServletRequest);
        boolean result = articleService.deleteArticle(deleteRequest.getId(), loginUser);

        return ResultUtils.success(result);
    }

}
