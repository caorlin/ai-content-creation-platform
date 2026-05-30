package com.caoerlin.aicontentcreation.common.aop;

import cn.hutool.core.util.ObjectUtil;
import com.caoerlin.aicontentcreation.common.annotation.AuthCheck;
import com.caoerlin.aicontentcreation.common.exception.BusinessException;
import com.caoerlin.aicontentcreation.common.exception.ErrorCode;
import com.caoerlin.aicontentcreation.model.entity.User;
import com.caoerlin.aicontentcreation.model.enums.UserRoleEnum;
import com.caoerlin.aicontentcreation.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import static com.caoerlin.aicontentcreation.constant.UserConstant.USER_LOGIN_STATE;

@Aspect
@Component
@RequiredArgsConstructor
public class AuthInterceptor {
    private final UserService userService;


    @Around("@annotation(authCheck)")
    public Object doInterceptor(ProceedingJoinPoint joinPoint, AuthCheck authCheck) throws Throwable {
        String mustRole = authCheck.mustRole();

        //获取当前请求对象
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
        HttpServletRequest request = attributes.getRequest();
        //获取当前登录对象信息
        User user = (User) request.getSession().getAttribute(USER_LOGIN_STATE);
        //根据注解填入角色获取对应的角色enum
        UserRoleEnum mustRoleEnum = UserRoleEnum.getEnumByValue(mustRole);
        if (ObjectUtil.isNull(mustRoleEnum)) {
            //不需要权限直接放行
            return joinPoint.proceed();
        }
        //获取当前角色权限
        UserRoleEnum userRoleEnum = UserRoleEnum.getEnumByValue(user.getUserRole());
        if (ObjectUtil.isNull(userRoleEnum)) {
            //用户没有设置对应role
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "用户无权限");
        }

        //必须是管理员，当前用户不是
        if (UserRoleEnum.ADMIN.getValue().equals(mustRoleEnum.getValue()) && !UserRoleEnum.ADMIN.getValue().equals(userRoleEnum.getValue())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "用户无权限");
        }
        //放行
        return joinPoint.proceed();
    }
}
