package org.example.zzudate.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.example.zzudate.utils.LoginInterceptor;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Autowired
    private LoginInterceptor loginInterceptor; // 改成你定义的拦截器类名
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loginInterceptor) // 使用刚才注入的变量
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/auth/**",
                        "/error",
                        "/static/**"
                );
    }
}
