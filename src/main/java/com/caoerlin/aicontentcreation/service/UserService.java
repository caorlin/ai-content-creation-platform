package com.caoerlin.aicontentcreation.service;

import com.caoerlin.aicontentcreation.model.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;

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
}
