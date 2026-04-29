package org.example.zzudate.service;

import jakarta.servlet.http.HttpServletRequest;
import org.example.zzudate.dto.UserBaseInfoDto;
import org.example.zzudate.dto.UserSoulInfoDto;
import org.example.zzudate.entity.User;

public interface UserService {
    User getUserByEmail(String email);
    User getUserById(String id);
    int saveUser(User user);
    int saveBaseInfo(UserBaseInfoDto userBaseInfoDto);
    int saveSoulInfo(UserSoulInfoDto userSoulInfoDto);
    Long userCount();
}
