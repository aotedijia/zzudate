package org.example.zzudate.utils;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.concurrent.TimeUnit;

@Component
public class LoginInterceptor implements HandlerInterceptor {
    private final StringRedisTemplate stringRedisTemplate;
    public LoginInterceptor(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler){
        String accessToken = request.getHeader("Authorization");
        if(accessToken==null){
            return false;
        }
        String key="login:accessToken:"+accessToken;
        String userId=stringRedisTemplate.opsForValue().get(key);
        if(userId==null){
            return false;
        }
        stringRedisTemplate.expire(key, 48, TimeUnit.HOURS);
        CurrentUser.setUserId(Long.valueOf(userId));
        return true;
    }
    @Override
    public void afterCompletion(HttpServletRequest request,HttpServletResponse response,Object handler,Exception ex){
        CurrentUser.remove();
    }
}
