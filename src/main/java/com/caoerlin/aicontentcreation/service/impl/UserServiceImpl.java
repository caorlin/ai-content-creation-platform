package com.caoerlin.aicontentcreation.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.caoerlin.aicontentcreation.common.exception.BusinessException;
import com.caoerlin.aicontentcreation.common.exception.ErrorCode;
import com.caoerlin.aicontentcreation.constant.UserConstant;
import com.caoerlin.aicontentcreation.model.entity.User;
import com.caoerlin.aicontentcreation.model.enums.UserRoleEnum;
import com.caoerlin.aicontentcreation.model.vo.user.LoginUserVO;
import com.caoerlin.aicontentcreation.service.UserService;
import com.caoerlin.aicontentcreation.mapper.UserMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static com.caoerlin.aicontentcreation.constant.UserConstant.USER_LOGIN_STATE;

/**
 * @author zyj
 * @description 针对表【user(用户)】的数据库操作Service实现
 */
@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
        implements UserService {

    @Override
    public Long userRegister(String userAccount, String userPassword, String checkPassword) {
        log.info("进入用户注册接口,开始进入用户注册");

        if (StrUtil.hasBlank(userAccount, userPassword, checkPassword)) {
            log.error("请求参数为空");
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }

        if (userAccount.length() < UserConstant.USER_ACCOUNT_LENGTH) {
            log.error("账户名称长度小于4");
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账户名称格式不规范");
        }

        if (userPassword.length() < UserConstant.USER_PASSWORD_LENGTH || checkPassword.length() < UserConstant.USER_PASSWORD_LENGTH) {
            log.error("账户密码长度小于4");
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账户密码格式不规范");
        }

        if (!StrUtil.equals(userPassword, checkPassword)) {
            log.error("两次输入密码不一致,password:{},check:{}", userPassword, checkPassword);
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "两次输入密码不一致,请确认后输入");
        }

        //判断账户是否存在
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUserAccount, userAccount);
        long count = count(wrapper);
        if (count > 0) {
            log.error("该账户已存在,userAccount:{}", userAccount);
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账户已存在");
        }

        try {
            //加密密码
            log.info("进入用户注册接口,开始加密密码");
            String encryptPassword = getEncryptPassword(userPassword);
            //创建用户并且插入数据库
            log.info("进入用户注册接口,开始保存用户信息");
            User user = new User();
            user.setUserAccount(userAccount);
            user.setUserPassword(encryptPassword);
            //处理用户名称
            String name = UUID.randomUUID().toString().replaceAll("-", "").substring(0, 10);
            user.setUsername("创作者" + name);
            user.setUserRole(UserRoleEnum.USER.getValue());
            //插入数据
            boolean result = save(user);
            if (!result) {
                log.error("注册账户失败,数据库异常");
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "注册账户失败");
            }
            log.info("进入用户注册接口,用户注册完成");
            return user.getId();
        } catch (BusinessException e) {
            log.error("注册账户失败,原因:{}", e.getMessage());
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "系统异常");
        }
    }

    @Override
    public String getEncryptPassword(String userPassword) {
        // 盐值，混淆密码
        final String SALT = UserConstant.USER_PASSWORD_SALT;
        return DigestUtils.md5DigestAsHex((userPassword + SALT).getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public LoginUserVO userLogin(String userAccount, String userPassword, HttpServletRequest request) {
        log.info("开始进入用户登录接口");
        if (StrUtil.hasBlank(userAccount, userPassword)) {
            log.error("登录参数为空");
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请输入账户名或密码");
        }
        if (userAccount.length() < UserConstant.USER_ACCOUNT_LENGTH || userPassword.length() < UserConstant.USER_PASSWORD_LENGTH) {
            log.error("账户名或密码格式错误,账户名:{}", userAccount);
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账户名或密码格式错误");
        }

        //加密密码
        String encryptPassword = getEncryptPassword(userPassword);
        //查询用户
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUserAccount, userAccount)
                .eq(User::getUserPassword, encryptPassword);
        User user = getOne(wrapper);
        if (ObjectUtil.isNull(user)) {
            log.error("账户不存在或密码错误,账户名:{}", userAccount);
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账户不存在或密码错误");
        }
        //记录用户登录状态
        request.getSession().setAttribute(USER_LOGIN_STATE, user);
        //用户信息脱敏
        return getLoginUserVO(user);
    }

    /**
     * 获取用户信息脱敏后信息
     *
     * @param user 用户信息
     * @return 脱敏后的用户信息
     */
    public LoginUserVO getLoginUserVO(User user) {
        if (ObjectUtil.isNull(user)) {
            return null;
        }
        LoginUserVO loginUserVO = new LoginUserVO();
        BeanUtil.copyProperties(user, loginUserVO);
        return loginUserVO;
    }

}




