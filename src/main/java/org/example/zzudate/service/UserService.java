package org.example.zzudate.service;

import org.example.zzudate.dto.UserBaseInfoDto;
import org.example.zzudate.dto.UserSoulInfoDto;
import org.example.zzudate.entity.User;

public interface UserService {
    User getUserByEmail(String email);
    int saveUser(User user);
    int saveBaseInfo(UserBaseInfoDto userBaseInfoDto);
    int saveSoulInfo(UserSoulInfoDto userSoulInfoDto);
}
