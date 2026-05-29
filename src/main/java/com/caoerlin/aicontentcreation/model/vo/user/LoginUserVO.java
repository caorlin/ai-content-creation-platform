package com.caoerlin.aicontentcreation.model.vo.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Schema(description = "登录视图")
public class LoginUserVO implements Serializable {
    @Schema(description = "用户id")
    private Long id;
    @Schema(description = "账号名称")
    private String userAccount;
    @Schema(description = "用户名")
    private String username;
    @Schema(description = "用户头像")
    private String userAvatar;
    @Schema(description = "用户简介")
    private String userProfile;
    @Schema(description = "用户角色：user/admin")
    private String userRole;
    @Schema(description = "创建时间")
    private LocalDateTime createTime;
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
