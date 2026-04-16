package service;

import dto.UserBaseInfoDto;
import dto.UserSoulInfoDto;
import entity.User;

public interface UserService {
    User getUserByEmail(String email);
    int saveUser(User user);
    int saveBaseInfo(UserBaseInfoDto userBaseInfoDto);
    int saveSoulInfo(UserSoulInfoDto userSoulInfoDto);
}
