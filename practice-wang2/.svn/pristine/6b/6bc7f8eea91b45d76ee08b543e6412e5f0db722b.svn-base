package com.example.manage2.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.example.manage2.common.Result;
import com.example.manage2.utils.JwtUtils;
import io.jsonwebtoken.Claims;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component

public class JwtInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 放行浏览器的预检请求 (解决跨域拦截问题)
        if ("OPTIONS".equals(request.getMethod())) {
            return true;
        }

        // 1. 从请求头中获取 token (约定头部名称为 Authorization)
        String token = request.getHeader("Authorization");

        // 2. 校验 token
        if (token != null && !token.trim().isEmpty()) {
            Claims claims = JwtUtils.getClaimsByToken(token);
            if (claims != null) {
                // Token 有效，为了方便后续 Controller 使用，可以将 userId 存入 request 中
                request.setAttribute("currentUserId", claims.get("userId"));
                return true; // 放行请求
            }
        }

        // 3. 校验失败，拦截请求并返回未授权的 JSON 错误信息
        response.setContentType("application/json;charset=utf-8");
        Result<Object> errorResult = Result.error("未登录或登录已过期，请重新登录");
        errorResult.setCode(401); // 401 状态码代表未授权

        ObjectMapper mapper = new ObjectMapper();
        response.getWriter().write(mapper.writeValueAsString(errorResult));

        return false; // 拦截请求，不再往下执行
    }
}