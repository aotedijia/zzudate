package org.example.zzudate.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.example.zzudate.entity.User;
import org.example.zzudate.EmailService;
import org.example.zzudate.GenerateEmailCode;
import org.example.zzudate.utils.CurrentUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.example.zzudate.Result;
import org.example.zzudate.service.UserService;
import org.example.zzudate.vo.TokenVo;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
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

    private static final List<String> ALLOWED_DOMAINS = List.of(
            "gs.zzu.edu.cn",
            "zzu.edu.cn",
            "stu.zzu.edu.cn"
    );
    @PostMapping("logout")
    public Result logout(HttpServletRequest request) {
        String accessToken=request.getHeader("Authorization");
        if(accessToken!=null){
            String key="login:accessToken:"+accessToken;
            stringRedisTemplate.delete(key);
        }
        CurrentUser.remove();
        return Result.success("已安全退出");
    }

    @PostMapping("/getemailcode")
    public Result getEmailCode(String email,HttpServletRequest request) {
        if (email==null){
            throw new IllegalArgumentException("邮箱不能为空");
        }
        String domain=email.substring(email.indexOf('@')+1);
        if(!ALLOWED_DOMAINS.contains(domain)){
            return Result.error("仅限郑州大学邮箱注册");
        }
        String ip=getClientIp(request);
        String date=LocalDate.now().toString();
        String ipDailyKey="login:emailcode:daily_limit:"+date+":"+ip;
        Long currentCount=stringRedisTemplate.opsForValue().increment(ipDailyKey);
        if(currentCount!=null&&currentCount==1){
            stringRedisTemplate.expire(ipDailyKey,24,TimeUnit.HOURS);
        }
        if(currentCount!=null&&currentCount>15){
            return Result.error("该设备今日发送次数已达上限，请明天再试");
        }
        String limitKey = "login:emailcode:limit:" + email;
        Boolean success = stringRedisTemplate.opsForValue()
                .setIfAbsent(limitKey, "1", 60, TimeUnit.SECONDS);
        if (!success) {
            return Result.error("发送过于频繁，请60秒后再试");
        }
        String emailcode = GenerateEmailCode.generateEmailcode();
        emailService.sendCode(email,emailcode);
        String key = "login:code:" + email;
        stringRedisTemplate.opsForValue().set(key, emailcode, 5, TimeUnit.MINUTES);
        return Result.success("验证码已经发送");
    }
    @PostMapping("/emailloginautoregister")
    public Result emailloginautoregister(String email, String emailCode) {
        if (email==null||emailCode==null){
            throw new IllegalArgumentException("邮箱或者验证码不能为空");
        }
        String key="login:code:"+email;
        String emailcode=stringRedisTemplate.opsForValue().get(key);
        if(emailcode==null||!emailcode.equals(emailCode)){
            return Result.error("验证码过期或验证码不正确");
        }
        stringRedisTemplate.delete(key);
        User user=userService.getUserByEmail(email);
        //如果用户不存在则自动创建用户
        if(user==null) {
            user=new User();
            user.setEmail(email);
            user.setCreateTime(LocalDateTime.now());
            userService.saveUser(user);
        }
        String accessToken = UUID.randomUUID().toString();
        stringRedisTemplate.opsForValue().set("login:accessToken:" + accessToken, user.getId(), 48, TimeUnit.HOURS);
        TokenVo tokenVo = new TokenVo();
        tokenVo.setAccessToken(accessToken);
        tokenVo.setUserId(user.getId());
        tokenVo.setEmail(user.getEmail());
        tokenVo.setName(user.getName());
        return Result.success("登录成功", tokenVo);
    }
    private String getClientIp(HttpServletRequest request) {
        String ip=request.getHeader("X-Forwarded-For");
        if(ip==null||ip.isEmpty()||"unknown".equalsIgnoreCase(ip)){
            ip=request.getHeader("Proxy-Client-IP");
        }
        if(ip==null||ip.isEmpty()||"unknown".equalsIgnoreCase(ip)){
            ip=request.getHeader("WL-Proxy-Client-IP");
        }
        if(ip==null||ip.isEmpty()||"unknown".equalsIgnoreCase(ip)){
            ip=request.getRemoteAddr();
        }
        //多级代理时取第一个IP
        if(ip!=null&&ip.contains(",")){
            ip=ip.split(",")[0].trim();
        }
        return ip;
    }
}