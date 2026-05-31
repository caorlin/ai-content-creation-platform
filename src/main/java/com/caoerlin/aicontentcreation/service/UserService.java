package com.caoerlin.aicontentcreation.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.caoerlin.aicontentcreation.model.dto.user.UserQueryRequest;
import com.caoerlin.aicontentcreation.model.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;
import com.caoerlin.aicontentcreation.model.vo.user.LoginUserVO;
import com.caoerlin.aicontentcreation.model.vo.user.UserVO;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

/**
 * @author zyj
 * @description 针对表【user(用户)】的数据库操作Service
 */
public interface UserService extends IService<User> {

    /**
     * 用户注册接口
     *
     * @param userAccount   账户名称
     * @param userPassword  密码
     * @param checkPassword 确认密码
     * @return 用户id
     */
    Long userRegister(String userAccount, String userPassword, String checkPassword);

    /**
     * 密码加密
     *
     * @param userPassword 密码
     * @return 加密后的密码
     */
    String getEncryptPassword(String userPassword);

    /**
     * 用户登录
     *
     * @param userAccount  账户名称
     * @param userPassword 密码
     * @param request
     * @return 登录信息
     */
    LoginUserVO userLogin(String userAccount, String userPassword, HttpServletRequest request);

    /**
     * 获取当前登录对象
     *
     * @param request
     * @return 当前登录对象
     */
    LoginUserVO getLoginUser(HttpServletRequest request);

    /**
     * 退出登录
     *
     * @param request
     * @return
     */
    Boolean userLogout(HttpServletRequest request);

    /**
     * 获取脱敏的用户信息
     *
     * @param user 未脱敏用户信息
     * @return 脱敏的用户信息
     */
    UserVO getUserVO(User user);

    /**
     * 获取脱敏的用户信息列表
     *
     * @param userList 未脱敏的用户信息列表
     * @return 脱敏的用户信息列表
     */
    List<UserVO> getUserVOList(List<User> userList);

    /**
     * 获取用户查询页
     *
     * @param pageNum          页号
     * @param pageSize         页数
     * @param userQueryRequest 查询条件
     * @return
     */
    Page<User> getUserPage(long pageNum, long pageSize, UserQueryRequest userQueryRequest);

    /**
     * 查询对象转换为 QueryWrapper对象
     *
     * @param userQueryRequest
     * @return
     */
    QueryWrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest);
}
