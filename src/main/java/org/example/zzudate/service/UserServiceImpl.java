package org.example.zzudate.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.example.zzudate.dto.UserBaseInfoDto;
import org.example.zzudate.dto.UserSoulInfoDto;
import org.example.zzudate.entity.User;
import org.example.zzudate.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
@Service
public class UserServiceImpl {
    @Autowired
    private UserMapper userMapper;

    public User getUserByEmail(String email) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getEmail,email);
        return userMapper.selectOne(wrapper);}

    public int saveUser(User user) {
        return userMapper.insert(user);}

    public int saveBaseInfo(UserBaseInfoDto userBaseInfoDto) {
        User user=new User();
        user.setId(userBaseInfoDto.getId());
        user.setNumber(userBaseInfoDto.getNumber());
        user.setGender(userBaseInfoDto.getGender());
        user.setHeight(userBaseInfoDto.getHeight());
        user.setCollege(userBaseInfoDto.getCollege());
        user.setCampus(userBaseInfoDto.getCampus());
        user.setGrade(userBaseInfoDto.getGrade());

        int result=userMapper.updateById(user);
        if(result>0) {
            System.out.println("基础信息同步成功，用户ID:"+user.getId());
        }
        return result;
    }

    public int saveSoulInfo(UserSoulInfoDto userSoulInfoDto) {
        User user=new User();
        user.setId(userSoulInfoDto.getUserId());
        user.setAnswers(userSoulInfoDto.getAnswers());
        int result = userMapper.updateById(user);
        if(result>0) {
            System.out.println("深度信息同步成功，用户ID:"+user.getId());
        }
        return result;
    }
}
