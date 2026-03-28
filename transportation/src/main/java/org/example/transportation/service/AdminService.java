package org.example.transportation.service;

import org.example.transportation.dox.user.AdminLoginDTO;
import org.example.transportation.dox.user.Admin;
import org.example.transportation.dox.user.UserUpdateDTO;
import org.example.transportation.vo.ResultVO;

public interface AdminService {
    ResultVO login(AdminLoginDTO adminLoginDTO);

    ResultVO logout(String token);

    ResultVO getAdminInfo();

    ResultVO updatePassword(String oldPassword, String newPassword);

    ResultVO updateAdminInfo(UserUpdateDTO userUpdateDTO);
}
