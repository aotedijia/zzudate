package service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import entity.User;
import mapper.UserMapper;
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
}
