package com.caoerlin.aicontentcreation.mapper;

import com.caoerlin.aicontentcreation.model.entity.User;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

/**
 * @author zyj
 * @description 针对表【user(用户)】的数据库操作Mapper
 * @Entity com.caoerlin.aicontentcreation.dto.UserDTO
 */
public interface UserMapper extends BaseMapper<User> {

    /**
     * 原子扣减用户配额
     * 使用 quota > 0 条件确保并发安全，避免超扣
     *
     * @param userId 用户ID
     * @return 影响行数，1表示成功，0表示配额不足
     */
    int decrementQuota(@Param("userId") Long userId);

}




