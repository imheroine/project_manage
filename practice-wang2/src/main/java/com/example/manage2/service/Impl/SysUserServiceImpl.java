package com.example.manage2.service.Impl;

import com.example.manage2.dto.LoginDTO;
import com.example.manage2.entity.SysUser;
import com.example.manage2.mapper.SysUserMapper;
import com.example.manage2.service.SysUserService;
import com.example.manage2.utils.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SysUserServiceImpl implements SysUserService {

    @Autowired
    private SysUserMapper sysUserMapper;

    // ================== 认证与授权 ==================

    @Override
    public Map<String, Object> login(LoginDTO loginDTO) {
        // 1. 根据用户名查询数据库
        SysUser user = sysUserMapper.findByUsername(loginDTO.getUsername());

        // 2. 账号状态与存在性校验
        if (user == null) {
            throw new RuntimeException("账号不存在");
        }
        if (user.getStatus() == 0) {
            throw new RuntimeException("该账号已被停用，请联系管理员");
        }

        // 3. 密码比对
        if (!user.getPassword().equals(loginDTO.getPassword())) {
            throw new RuntimeException("密码错误");
        }

        // 4. 认证成功，签发 JWT Token
        String token = JwtUtils.generateToken(user.getId(), user.getUsername());

        // 5. 组装返回给前端的用户上下文信息
        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("userId", user.getId());
        result.put("realName", user.getRealName());

        return result;
    }

    @Override
    public void register(SysUser user) {
        // 1. 校验账号唯一性
        SysUser existUser = sysUserMapper.findByUsername(user.getUsername());
        if (existUser != null) {
            throw new RuntimeException("该账号已被注册，请更换账号名");
        }

        // 2. 初始化新用户默认属性
        user.setStatus(1); // 1: 正常启用状态
        user.setCreateTime(new Date());

        // 3. 插入数据库
        sysUserMapper.insertUser(user);
    }

    // ================== 信息查询 ==================

    @Override
    public List<SysUser> list() {
        // 仅查询状态为 1（正常）的用户，防止将已停用的员工分配给项目
        return sysUserMapper.findAllNormalUsers();
    }
}