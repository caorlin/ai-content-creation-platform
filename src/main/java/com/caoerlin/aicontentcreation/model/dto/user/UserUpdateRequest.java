package com.caoerlin.aicontentcreation.model.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author zyj
 */
@Data
@Schema(description = "用户更新请求实体")
public class UserUpdateRequest implements Serializable {

    /**
     * id
     */
    @Schema(name = "id",title = "id")
    private Long id;

    /**
     * 用户昵称
     */
    @Schema(name = "userName",title = "用户昵称")
    private String userName;

    /**
     * 用户头像
     */
    @Schema(name = "userAvatar",title = "用户头像")
    private String userAvatar;

    /**
     * 简介
     */
    @Schema(name = "userProfile",title = "简介")
    private String userProfile;

    /**
     * 用户角色：user/admin
     */
    @Schema(name = "userRole",title = "用户角色：user/admin")
    private String userRole;

    @Serial
    private static final long serialVersionUID = 1L;
}