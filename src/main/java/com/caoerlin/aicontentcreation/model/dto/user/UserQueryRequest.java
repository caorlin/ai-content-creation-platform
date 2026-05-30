package com.caoerlin.aicontentcreation.model.dto.user;

import com.caoerlin.aicontentcreation.common.request.PageRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * @author zyj
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "用户查询请求")
public class UserQueryRequest extends PageRequest implements Serializable {

    /**
     * id
     */
    @Schema(name = "id", title = "id")
    private Long id;

    /**
     * 用户昵称
     */
    @Schema(name = "userName", title = "用户昵称")
    private String userName;

    /**
     * 账号
     */
    @Schema(name = "userAccount", title = "账号")
    private String userAccount;

    /**
     * 简介
     */
    @Schema(name = "userProfile", title = "简介")
    private String userProfile;

    /**
     * 用户角色：user/admin/ban
     */
    @Schema(name = "userRole", title = "用户角色：user/admin/ban")
    private String userRole;

    private static final long serialVersionUID = 1L;
}
