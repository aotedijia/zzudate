package org.example.zzudate.controller;

import org.example.zzudate.entity.User;
import org.example.zzudate.EmailService;
import org.example.zzudate.GenerateEmailCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.example.zzudate.Result;
import org.example.zzudate.service.UserService;
import org.example.zzudate.vo.TokenVo;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/auth")
public class AuthController {
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private EmailService emailService;
    @Autowired
    private UserService userService;

    @PostMapping("/getemailcode")
    public Result getEmailCode(String email) {
        if (email == null) {
            throw new IllegalArgumentException("邮箱不能为空");
        }
        String limitKey = "login:emailcode:limit:" + email;
        Boolean success = stringRedisTemplate.opsForValue()
                .setIfAbsent(limitKey, "1", 60, TimeUnit.SECONDS);
        if (!success) {
            Long ttl = stringRedisTemplate.getExpire(limitKey);
            return Result.error("发送过于频繁，请60秒后再试");
        }
        String emailcode = GenerateEmailCode.generateEmailcode();
        emailService.sendCode(email, emailcode);
        String key = "login:code:" + email;
        stringRedisTemplate.opsForValue().set(key, emailcode, 5, TimeUnit.MINUTES);
        return Result.success("验证码已经发送");
    }
    @PostMapping("/emailloginautoregister")
    public Result emailloginautoregister(String email, String emailCode) {
        if (email == null || emailCode == null) {
            throw new IllegalArgumentException("邮箱或者验证码不能为空");
        }
        String key = "login:code:" + email;
        String emailcode = stringRedisTemplate.opsForValue().get(key);
        if (emailcode == null || !emailcode.equals(emailCode)) {
            return Result.error("验证码过期或验证码不正确");
        }
        stringRedisTemplate.delete(key);
        User user = userService.getUserByEmail(email);
        //如果用户不存在则自动创建用户
        if (user == null) {
            user = new User();
            user.setEmail(email);
            user.setCreateTime(LocalDateTime.now());
            userService.saveUser(user);
        }
        String accessToken = UUID.randomUUID().toString();
        stringRedisTemplate.opsForValue().set("login:accessToken:" + accessToken, user.getId().toString(), 48, TimeUnit.HOURS);
        TokenVo tokenVo = new TokenVo();
        tokenVo.setAccessToken(accessToken);
        tokenVo.setUserId(user.getId());
        tokenVo.setEmail(user.getEmail());
        return Result.success("登录成功", tokenVo);
    }
}