package com.caoerlin.aicontentcreation.controller;

import cn.hutool.core.util.ObjectUtil;
import com.caoerlin.aicontentcreation.common.exception.ErrorCode;
import com.caoerlin.aicontentcreation.common.exception.ThrowUtils;
import com.caoerlin.aicontentcreation.common.response.BaseResponse;
import com.caoerlin.aicontentcreation.common.response.ResultUtils;
import com.caoerlin.aicontentcreation.model.dto.user.UserLoginRequest;
import com.caoerlin.aicontentcreation.model.dto.user.UserRegisterRequest;
import com.caoerlin.aicontentcreation.model.vo.user.LoginUserVO;
import com.caoerlin.aicontentcreation.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

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

    @PostMapping("login")
    @Operation(summary = "用户登录接口")
    public BaseResponse<LoginUserVO> userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(ObjectUtil.isNull(userLoginRequest), ErrorCode.PARAMS_ERROR);
        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();
        return ResultUtils.success(userService.userLogin(userAccount, userPassword, request));
    }


    @PostMapping("logout")
    @Operation(summary = "退出登录接口")
    public BaseResponse<Boolean> userLogout(HttpServletRequest request){
        return ResultUtils.success(userService.userLogout(request));
    }
}
