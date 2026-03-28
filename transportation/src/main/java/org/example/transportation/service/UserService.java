package org.example.transportation.service;

import org.example.transportation.dox.user.*;
import org.example.transportation.vo.ResultVO;

import java.util.List;

public interface UserService {
    ResultVO register(UserRegisterDTO userRegisterDTO);

    ResultVO login(UserLoginDTO userLoginDTO);

    ResultVO logout(String token);

    ResultVO getUserInfo();

    ResultVO updateUserInfo(UserUpdateDTO userUpdateDTO);

    ResultVO updatePassword(PasswordUpdateDTO passwordUpdateDTO, String token);

    ResultVO updateAvatar(String avatarUrl);

    ResultVO getAllUsers();

    ResultVO updateUserStatus(Long userId, Integer status);

    ResultVO deleteUser(Long userId);

    ResultVO updateUserInfo(Long userId, UserUpdateDTO userUpdateDTO);
}
