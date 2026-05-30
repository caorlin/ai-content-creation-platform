package com.caoerlin.aicontentcreation.service;

import com.caoerlin.aicontentcreation.model.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;
import com.caoerlin.aicontentcreation.model.vo.user.LoginUserVO;
import jakarta.servlet.http.HttpServletRequest;

/**
 * @author zyj
 * @description 针对表【user(用户)】的数据库操作Service
 */
public interface UserService extends IService<User> {

    /**
     * 用户注册接口
     *
     * @param userAccount   账户名称
     * @param userPassword  密码
     * @param checkPassword 确认密码
     * @return 用户id
     */
    Long userRegister(String userAccount, String userPassword, String checkPassword);

    /**
     * 密码加密
     *
     * @param userPassword 密码
     * @return 加密后的密码
     */
    String getEncryptPassword(String userPassword);

    /**
     * 用户登录
     *
     * @param userAccount  账户名称
     * @param userPassword 密码
     * @param request
     * @return 登录信息
     */
    LoginUserVO userLogin(String userAccount, String userPassword, HttpServletRequest request);

    /**
     * 获取当前登录对象
     *
     * @param request
     * @return 当前登录对象
     */
    User getLoginUser(HttpServletRequest request);

    /**
     * 退出登录
     *
     * @param request
     * @return
     */
    Boolean userLogout(HttpServletRequest request);
}
