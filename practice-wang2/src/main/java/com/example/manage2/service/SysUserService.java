package com.example.manage2.service;

import com.example.manage2.dto.LoginDTO;
import com.example.manage2.entity.SysUser;

import java.util.List;
import java.util.Map;

/**
 * 系统用户业务逻辑接口
 * 负责用户的注册、登录认证以及用户信息查询
 */
public interface SysUserService {

    /**
     * 用户登录认证
     *
     * @param loginDTO 包含用户名和密码的数据传输对象
     * @return 包含 token、userId、realName 的 Map 集合
     */
    Map<String, Object> login(LoginDTO loginDTO);

    /**
     * 新用户注册
     *
     * @param user 用户实体对象 (需包含账号、密码、真实姓名)
     */
    void register(SysUser user);

    /**
     * 获取系统中所有正常状态的用户列表
     *
     * @return 用户列表集合
     */
    List<SysUser> list();
}