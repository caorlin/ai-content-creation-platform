package com.caoerlin.aicontentcreation.model.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/***
 * @author zyj
 */
@Data
@Schema(description = "用户添加请求详情")
public class UserAddRequest implements Serializable {
    /**
     * 用户昵称
     */
    @Schema(name = "username",title = "用户昵称")
    private String username;

    /**
     * 账号
     */
    @Schema(name = "userAccount",title = "账号")
    private String userAccount;

    /**
     * 用户头像
     */
    @Schema(name = "userAvatar",title = "用户头像")
    private String userAvatar;

    /**
     * 用户简介
     */
    @Schema(name = "userProfile",title = "用户简介")
    private String userProfile;

    /**
     * 用户角色: user, admin
     */
    @Schema(name = "userRole",title = "用户角色: user, admin")
    private String userRole;

    @Serial
    private static final long serialVersionUID = 1L;

}
