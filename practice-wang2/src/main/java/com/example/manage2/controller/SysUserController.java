package com.example.manage2.controller;

import com.example.manage2.common.Result;
import com.example.manage2.dto.LoginDTO;
import com.example.manage2.entity.SysUser;
import com.example.manage2.service.SysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 系统用户接口控制器
 */
@RestController
@RequestMapping("/api/user")
@CrossOrigin // 允许跨域请求
public class SysUserController {

    @Autowired
    private SysUserService sysUserService;

    // ================== 登录与注册 ==================

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody LoginDTO loginDTO) {
        // 基础参数校验
        if (loginDTO.getUsername() == null || loginDTO.getPassword() == null) {
            return Result.error("用户名或密码不能为空");
        }

        try {
            Map<String, Object> tokenInfo = sysUserService.login(loginDTO);
            return Result.success(tokenInfo, "登录成功");
        } catch (RuntimeException e) {
            // 捕获 Service 层抛出的业务异常（如密码错误、账号停用）
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/register")
    public Result<String> register(@RequestBody SysUser user) {
        // 基础参数校验
        if (user.getUsername() == null || user.getPassword() == null || user.getRealName() == null) {
            return Result.error("账号、密码和真实姓名均不能为空");
        }

        try {
            sysUserService.register(user);
            return Result.success(null, "注册成功");
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }


    // ================== 用户信息查询 ==================

    /**
     * 获取用户列表 (通常用于用户管理界面)
     */
    @GetMapping("/list")
    public Result<List<SysUser>> getUserList() {
        return Result.success(sysUserService.list());
    }

    /**
     * 获取系统中所有可用的用户列表
     * (通常供前端项目的“选择负责人”、“选择参与人”下拉框使用)
     */
    @GetMapping("/all")
    public Result<List<SysUser>> getAllUsers() {
        // 这里的逻辑与 list() 一致，分设两个接口是为了在日后权限细分时做到互不影响
        return Result.success(sysUserService.list());
    }
}