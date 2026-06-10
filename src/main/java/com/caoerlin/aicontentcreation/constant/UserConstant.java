package com.caoerlin.aicontentcreation.constant;

public interface UserConstant {
    /**
     * 账户名称默认最短长度
     */
    Integer USER_ACCOUNT_LENGTH = 4;

    /**
     * 用户密码最短长度
     */
    Integer USER_PASSWORD_LENGTH = 8;

    /**
     * 密码盐值
     */
    String USER_PASSWORD_SALT = "manbo";

    /**
     * 用户登录态键
     */
    String USER_LOGIN_STATE = "user_login";

    /**
     * 默认角色
     */
    String DEFAULT_ROLE = "user";

    /**
     * 管理员角色
     */
    String ADMIN_ROLE = "admin";

    /**
     * 默认密码
     */
    String DEFAULT_PASSWORD = "12345678";

    /**
     * 用户 vip 角色标识
     */
    String VIP_ROLE = "vip";

    Integer DEFAULT_QUOTA = 5;
}
