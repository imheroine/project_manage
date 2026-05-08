package com.example.manage2.controller;

import com.example.manage2.entity.SysUser;
import com.example.manage2.service.SysUserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest; // 核心修复：使用 WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;


import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// 1. 专门用于测试 Controller，自动装配 MockMvc 和 ObjectMapper
@WebMvcTest(SysUserController.class)
class SysUserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean // 模拟 Service
    private SysUserService sysUserService;

    @Test
    void testRegisterApi() throws Exception {
        SysUser user = new SysUser();
        user.setUsername("test_user");
        user.setPassword("123456");
        user.setRealName("测试账号");

        // 模拟 Service 的 register 方法
        doNothing().when(sysUserService).register(any(SysUser.class));

        mockMvc.perform(post("/api/user/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("注册成功"));
    }
}

