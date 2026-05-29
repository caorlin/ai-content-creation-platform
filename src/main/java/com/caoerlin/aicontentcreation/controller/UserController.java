package com.caoerlin.aicontentcreation.controller;

import cn.hutool.core.util.ObjectUtil;
import com.caoerlin.aicontentcreation.common.exception.ErrorCode;
import com.caoerlin.aicontentcreation.common.exception.ThrowUtils;
import com.caoerlin.aicontentcreation.common.response.BaseResponse;
import com.caoerlin.aicontentcreation.common.response.ResultUtils;
import com.caoerlin.aicontentcreation.model.dto.user.UserRegisterRequest;
import com.caoerlin.aicontentcreation.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户控制层
 *
 * @author zyj
 */
@RestController
@Tag(name = "用户模块")
@RequestMapping("user")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PostMapping("register")
    @Operation(summary = "用户注册接口")
    public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest request) {
        ThrowUtils.throwIf(ObjectUtil.isNull(request), ErrorCode.PARAMS_ERROR);
        String userAccount = request.getUserAccount();
        String userPassword = request.getUserPassword();
        String checkPassword = request.getCheckPassword();
        return ResultUtils.success(userService.userRegister(userAccount, userPassword, checkPassword));
    }
}
