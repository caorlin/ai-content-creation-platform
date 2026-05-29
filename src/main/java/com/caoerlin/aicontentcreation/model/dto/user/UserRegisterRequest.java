package com.caoerlin.aicontentcreation.model.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

@Data
@Schema(description = "用户注册模型")
public class UserRegisterRequest implements Serializable {
    @Schema(description = "用户名", example = "zhansan")
    private String userAccount;
    @Schema(description = "密码", example = "123456")
    private String userPassword;
    @Schema(description = "确认密码", example = "123456")
    private String checkPassword;
}
