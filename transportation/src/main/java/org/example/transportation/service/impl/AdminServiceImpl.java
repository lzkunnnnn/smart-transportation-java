package org.example.transportation.service.impl;

import org.example.transportation.constant.MessageConstant;
import org.example.transportation.dox.user.Admin;
import org.example.transportation.dox.user.AdminLoginDTO;
import org.example.transportation.dox.user.UserUpdateDTO;
import org.example.transportation.repository.AdminRepository;
import org.example.transportation.service.AdminService;
import org.example.transportation.util.JwtClaimsConstant;
import org.example.transportation.util.JwtUtil;
import org.example.transportation.util.ThreadLocalUtil;
import org.example.transportation.vo.ResultVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AdminServiceImpl implements AdminService {

    @Autowired
    private AdminRepository adminRepository;

    private static final Map<String, String> TOKEN_STORE = new ConcurrentHashMap<>();

    @Override
    public ResultVO login(AdminLoginDTO adminLoginDTO) {
        Optional<Admin> adminOptional = adminRepository.findByUsername(adminLoginDTO.getUsername());
        if (adminOptional.isEmpty()) {
            return ResultVO.error(400, MessageConstant.USERNAME + MessageConstant.ERROR);
        }

        Admin admin = adminOptional.get();

        if (!DigestUtils.md5DigestAsHex(adminLoginDTO.getPassword().getBytes()).equals(admin.getPassword())) {
            return ResultVO.error(400, MessageConstant.PASSWORD + MessageConstant.ERROR);
        }

        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.ADMIN_ID, admin.getId());
        claims.put(JwtClaimsConstant.USERNAME, admin.getUsername());
        claims.put(JwtClaimsConstant.ROLE, "ADMIN");

        String token = JwtUtil.generateToken(claims);
        TOKEN_STORE.put(token, token);

        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", admin.getId());
        userInfo.put("username", admin.getUsername());
        userInfo.put("role", "ADMIN");
        userInfo.put("roleIds", "1");
        result.put("userInfo", userInfo);

        return ResultVO.success(result);
    }

    @Override
    public ResultVO logout(String token) {
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        TOKEN_STORE.remove(token);
        return ResultVO.success(MessageConstant.LOGOUT + MessageConstant.SUCCESS);
    }

    @Override
    public ResultVO getAdminInfo() {
        Map<String, Object> claims = ThreadLocalUtil.get();
        Long adminId = ((Number) claims.get(JwtClaimsConstant.ADMIN_ID)).longValue();

        Optional<Admin> adminOptional = adminRepository.findById(adminId);
        if (adminOptional.isEmpty()) {
            return ResultVO.error(400, "管理员不存在");
        }

        return ResultVO.success(adminOptional.get());
    }

    @Override
    public ResultVO updatePassword(String oldPassword, String newPassword) {
        Map<String, Object> claims = ThreadLocalUtil.get();
        Long adminId = ((Number) claims.get(JwtClaimsConstant.ADMIN_ID)).longValue();

        Optional<Admin> adminOptional = adminRepository.findById(adminId);
        if (adminOptional.isEmpty()) {
            return ResultVO.error(400, "管理员不存在");
        }

        Admin admin = adminOptional.get();

        if (!DigestUtils.md5DigestAsHex(oldPassword.getBytes()).equals(admin.getPassword())) {
            return ResultVO.error(400, MessageConstant.OLD_PASSWORD_ERROR);
        }

        if (DigestUtils.md5DigestAsHex(newPassword.getBytes()).equals(admin.getPassword())) {
            return ResultVO.error(400, MessageConstant.NEW_PASSWORD_ERROR);
        }

        admin.setPassword(DigestUtils.md5DigestAsHex(newPassword.getBytes()));
        admin.setUpdateTime(LocalDateTime.now());
        adminRepository.save(admin);

        return ResultVO.success(MessageConstant.UPDATE + MessageConstant.SUCCESS);
    }

    @Override
    public ResultVO updateAdminInfo(UserUpdateDTO userUpdateDTO) {
        Map<String, Object> claims = ThreadLocalUtil.get();
        Long adminId = ((Number) claims.get(JwtClaimsConstant.ADMIN_ID)).longValue();

        Optional<Admin> adminOptional = adminRepository.findById(adminId);
        if (adminOptional.isEmpty()) {
            return ResultVO.error(400, "管理员不存在");
        }

        Admin admin = adminOptional.get();

        if (userUpdateDTO.getUsername() != null && !userUpdateDTO.getUsername().equals(admin.getUsername())) {
            if (adminRepository.findByUsername(userUpdateDTO.getUsername()).isPresent()) {
                return ResultVO.error(400, MessageConstant.USERNAME + MessageConstant.ALREADY_EXISTS);
            }
            admin.setUsername(userUpdateDTO.getUsername());
        }

        if (userUpdateDTO.getPhone() != null && !userUpdateDTO.getPhone().equals(admin.getPhone())) {
            admin.setPhone(userUpdateDTO.getPhone());
        }

        if (userUpdateDTO.getEmail() != null && !userUpdateDTO.getEmail().equals(admin.getEmail())) {
            admin.setEmail(userUpdateDTO.getEmail());
        }

        if (userUpdateDTO.getAvatar() != null) {
            admin.setAvatar(userUpdateDTO.getAvatar());
        }

        admin.setUpdateTime(LocalDateTime.now());
        adminRepository.save(admin);

        return ResultVO.success(MessageConstant.UPDATE + MessageConstant.SUCCESS);
    }

    public static boolean validateToken(String token) {
        return TOKEN_STORE.containsKey(token);
    }
}
