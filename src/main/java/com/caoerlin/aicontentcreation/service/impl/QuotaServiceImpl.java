package com.caoerlin.aicontentcreation.service.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.caoerlin.aicontentcreation.common.exception.BusinessException;
import com.caoerlin.aicontentcreation.common.exception.ErrorCode;
import com.caoerlin.aicontentcreation.mapper.UserMapper;
import com.caoerlin.aicontentcreation.model.entity.User;
import com.caoerlin.aicontentcreation.model.enums.UserRoleEnum;
import com.caoerlin.aicontentcreation.service.QuotaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class QuotaServiceImpl implements QuotaService {
    private final UserMapper userMapper;

    @Override
    public boolean hasQuota(User user) {
        //管理员和vip用户无配额限制
        if (isAdmin(user) || isVip(user)) {
            return true;
        }

        //查询数据库中的数据，保证都是最新数据
        User freshUser = userMapper.selectById(user.getId());
        if (ObjectUtil.isNull(freshUser)) {
            return false;
        }

        Integer quota = freshUser.getQuota();

        return ObjectUtil.isNotEmpty(quota) && quota > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void consumeQuota(User user) {
        //管理员和vip用户无配额限制
        if (isAdmin(user) || isVip(user)) {
            return;
        }

        // 使用原子更新：UPDATE user SET quota = quota - 1 WHERE id = ? AND quota > 0
        // 通过影响行数判断是否成功，避免并发问题
        int affectedRows = userMapper.decrementQuota(user.getId());

        if (affectedRows > 0) {
            log.info("用户配额已消费成功,userId={}", user.getId());
        } else {
            log.warn("用户配额扣减失败（可能配额不足或并发冲突）, userId={}", user.getId());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void checkAndConsumeQuota(User user) {
        //管理员和vip用户无配额限制
        if (isAdmin(user) || isVip(user)) {
            return;
        }

        // 使用原子更新：检查与消费合并为一个原子操作
        // UPDATE user SET quota = quota - 1 WHERE id = ? AND quota > 0
        int affectedRows = userMapper.decrementQuota(user.getId());

        if (affectedRows == 0) {
            // 影响行数为0，说明配额不足（已被其他请求消耗）
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "配额不足，无法创建文章");
        }

        log.info("用户配额检查并消耗成功, userId={}", user.getId());
    }

    private boolean isVip(User user) {
        return StrUtil.equals(user.getUserRole(), UserRoleEnum.VIP.getValue());
    }

    private boolean isAdmin(User user) {
        return StrUtil.equals(user.getUserRole(), UserRoleEnum.ADMIN.getValue());
    }
}
