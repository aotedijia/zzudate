package service;

import entity.User;

public interface UserService {
    User getUserByEmail(String email);
    int saveUser(User user);
}
