package com.caoerlin.aicontentcreation.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.caoerlin.aicontentcreation.model.entity.User;
import com.caoerlin.aicontentcreation.service.UserDTOService;
import com.caoerlin.aicontentcreation.mapper.UserDTOMapper;
import org.springframework.stereotype.Service;

/**
* @author Administrator
* @description 针对表【user(用户)】的数据库操作Service实现
* @createDate 2026-05-30 01:30:06
*/
@Service
public class UserDTOServiceImpl extends ServiceImpl<UserDTOMapper, User>
    implements UserDTOService{

}




