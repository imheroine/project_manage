package com.example.manage2.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private JwtInterceptor jwtInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtInterceptor)
                .addPathPatterns("/api/**")
                // 白名单范围，保证注册接口绝对不被拦截
                .excludePathPatterns(
                        "/api/user/login",
                        "/api/user/register",
                        "/api/user/login/**",
                        "/api/user/register/**"
                );
    }

    // 添加静态资源映射，让 SpringBoot 能读取到硬盘上的文件
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 获取项目根目录下的 uploads 文件夹的绝对路径
        String path = System.getProperty("user.dir") + File.separator + "uploads" + File.separator;

        // 将浏览器的 /uploads/** 请求，映射到本地的 file:路径下
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + path);
    }
}