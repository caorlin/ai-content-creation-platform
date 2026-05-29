package com.caoerlin.aicontentcreation.model.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

@Data
@Schema(description = "用户登录模型")
public class UserLoginRequest implements Serializable {
    @Schema(description = "用户名", example = "admin")
    private String userAccount;
    @Schema(description = "密码", example = "123456")
    private String userPassword;
}

