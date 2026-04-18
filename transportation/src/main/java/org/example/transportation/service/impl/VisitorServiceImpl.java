package org.example.transportation.service.impl;

import org.example.transportation.constant.MessageConstant;
import org.example.transportation.dox.user.PasswordUpdateDTO;
import org.example.transportation.dox.visitor.*;
import org.example.transportation.enumeration.RoleEnum;
import org.example.transportation.enumeration.UserStatusEnum;
import org.example.transportation.repository.VisitorRepository;
import org.example.transportation.service.VisitorService;
import org.example.transportation.util.JwtClaimsConstant;
import org.example.transportation.util.JwtUtil;
import org.example.transportation.util.ThreadLocalUtil;
import org.example.transportation.vo.ResultVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class VisitorServiceImpl implements VisitorService {

    @Autowired
    private VisitorRepository visitorRepository;

    private static final Map<String, String> TOKEN_STORE = new ConcurrentHashMap<>();

    @Override
    public ResultVO register(VisitorRegisterDTO visitorRegisterDTO) {
        if (visitorRepository.findByUsername(visitorRegisterDTO.getUsername()).isPresent()) {
            return ResultVO.error(400, MessageConstant.USERNAME + MessageConstant.ALREADY_EXISTS);
        }

        if (visitorRegisterDTO.getPhone() != null && !visitorRegisterDTO.getPhone().isEmpty()) {
            if (visitorRepository.findByPhone(visitorRegisterDTO.getPhone()).isPresent()) {
                return ResultVO.error(400, MessageConstant.PHONE + MessageConstant.ALREADY_EXISTS);
            }
        }

        if (visitorRegisterDTO.getEmail() != null && !visitorRegisterDTO.getEmail().isEmpty()) {
            if (visitorRepository.findByEmail(visitorRegisterDTO.getEmail()).isPresent()) {
                return ResultVO.error(400, MessageConstant.EMAIL + MessageConstant.ALREADY_EXISTS);
            }
        }

        String passwordMD5 = DigestUtils.md5DigestAsHex(visitorRegisterDTO.getPassword().getBytes());
        Visitor visitor = Visitor.builder()
                .username(visitorRegisterDTO.getUsername())
                .password(passwordMD5)
                .phone(visitorRegisterDTO.getPhone())
                .email(visitorRegisterDTO.getEmail())
                .status(UserStatusEnum.ENABLE.getId())
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();

        visitorRepository.save(visitor);
        return ResultVO.success(MessageConstant.REGISTER + MessageConstant.SUCCESS);
    }

    @Override
    public ResultVO login(VisitorLoginDTO visitorLoginDTO) {
        Optional<Visitor> visitorOptional = visitorRepository.findByUsername(visitorLoginDTO.getUsername());
        if (visitorOptional.isEmpty()) {
            return ResultVO.error(400, MessageConstant.USERNAME + MessageConstant.ERROR);
        }

        Visitor visitor = visitorOptional.get();
        if (visitor.getStatus().equals(UserStatusEnum.DISABLE.getId())) {
            return ResultVO.error(400, MessageConstant.ACCOUNT_LOCKED);
        }

        if (!DigestUtils.md5DigestAsHex(visitorLoginDTO.getPassword().getBytes()).equals(visitor.getPassword())) {
            return ResultVO.error(400, MessageConstant.PASSWORD + MessageConstant.ERROR);
        }

        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.USER_ID, visitor.getId());
        claims.put(JwtClaimsConstant.USERNAME, visitor.getUsername());
        claims.put(JwtClaimsConstant.ROLE, RoleEnum.VISITOR.getRole());

        String token = JwtUtil.generateToken(claims);
        TOKEN_STORE.put(token, token);

        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        Map<String, Object> visitorInfo = new HashMap<>();
        visitorInfo.put("id", visitor.getId());
        visitorInfo.put("username", visitor.getUsername());
        visitorInfo.put("role", "VISITOR");
        visitorInfo.put("roleIds", "2");
        result.put("visitorInfo", visitorInfo);

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
    public ResultVO getVisitorInfo() {
        Map<String, Object> claims = ThreadLocalUtil.get();
        Long visitorId = ((Number) claims.get(JwtClaimsConstant.USER_ID)).longValue();

        Optional<Visitor> visitorOptional = visitorRepository.findById(visitorId);
        if (visitorOptional.isEmpty()) {
            return ResultVO.error(400, "用户不存在");
        }

        Visitor visitor = visitorOptional.get();
        VisitorVO visitorVO = new VisitorVO();
        BeanUtils.copyProperties(visitor, visitorVO);

        return ResultVO.success(visitorVO);
    }

    @Override
    public ResultVO updateVisitorInfo(VisitorUpdateDTO visitorUpdateDTO) {
        Map<String, Object> claims = ThreadLocalUtil.get();
        Long visitorId = ((Number) claims.get(JwtClaimsConstant.USER_ID)).longValue();

        Optional<Visitor> visitorOptional = visitorRepository.findById(visitorId);
        if (visitorOptional.isEmpty()) {
            return ResultVO.error(400, "用户不存在");
        }

        Visitor visitor = visitorOptional.get();

        if (visitorUpdateDTO.getUsername() != null && !visitorUpdateDTO.getUsername().equals(visitor.getUsername())) {
            if (visitorRepository.findByUsername(visitorUpdateDTO.getUsername()).isPresent()) {
                return ResultVO.error(400, MessageConstant.USERNAME + MessageConstant.ALREADY_EXISTS);
            }
            visitor.setUsername(visitorUpdateDTO.getUsername());
        }

        if (visitorUpdateDTO.getPhone() != null && !visitorUpdateDTO.getPhone().equals(visitor.getPhone())) {
            if (visitorRepository.findByPhone(visitorUpdateDTO.getPhone()).isPresent()) {
                return ResultVO.error(400, MessageConstant.PHONE + MessageConstant.ALREADY_EXISTS);
            }
            visitor.setPhone(visitorUpdateDTO.getPhone());
        }

        if (visitorUpdateDTO.getEmail() != null && !visitorUpdateDTO.getEmail().equals(visitor.getEmail())) {
            if (visitorRepository.findByEmail(visitorUpdateDTO.getEmail()).isPresent()) {
                return ResultVO.error(400, MessageConstant.EMAIL + MessageConstant.ALREADY_EXISTS);
            }
            visitor.setEmail(visitorUpdateDTO.getEmail());
        }

        if (visitorUpdateDTO.getAvatar() != null) {
            visitor.setAvatar(visitorUpdateDTO.getAvatar());
        }

        visitor.setUpdateTime(LocalDateTime.now());
        visitorRepository.save(visitor);

        return ResultVO.success(MessageConstant.UPDATE + MessageConstant.SUCCESS);
    }

    @Override
    public ResultVO updatePassword(PasswordUpdateDTO passwordUpdateDTO, String token) {
        Map<String, Object> claims = ThreadLocalUtil.get();
        Long visitorId = ((Number) claims.get(JwtClaimsConstant.USER_ID)).longValue();

        Optional<Visitor> visitorOptional = visitorRepository.findById(visitorId);
        if (visitorOptional.isEmpty()) {
            return ResultVO.error(400, "用户不存在");
        }

        Visitor visitor = visitorOptional.get();

        if (!DigestUtils.md5DigestAsHex(passwordUpdateDTO.getOldPassword().getBytes()).equals(visitor.getPassword())) {
            return ResultVO.error(400, MessageConstant.OLD_PASSWORD_ERROR);
        }

        if (DigestUtils.md5DigestAsHex(passwordUpdateDTO.getNewPassword().getBytes()).equals(visitor.getPassword())) {
            return ResultVO.error(400, MessageConstant.NEW_PASSWORD_ERROR);
        }

        if (!passwordUpdateDTO.getNewPassword().equals(passwordUpdateDTO.getConfirmPassword())) {
            return ResultVO.error(400, MessageConstant.PASSWORD_NOT_MATCH);
        }

        visitor.setPassword(DigestUtils.md5DigestAsHex(passwordUpdateDTO.getNewPassword().getBytes()));
        visitor.setUpdateTime(LocalDateTime.now());
        visitorRepository.save(visitor);

        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        TOKEN_STORE.remove(token);

        return ResultVO.success(MessageConstant.UPDATE + MessageConstant.SUCCESS);
    }

    @Override
    public ResultVO updateAvatar(String avatarUrl) {
        Map<String, Object> claims = ThreadLocalUtil.get();
        Long visitorId = ((Number) claims.get(JwtClaimsConstant.USER_ID)).longValue();

        Optional<Visitor> visitorOptional = visitorRepository.findById(visitorId);
        if (visitorOptional.isEmpty()) {
            return ResultVO.error(400, "用户不存在");
        }

        Visitor visitor = visitorOptional.get();
        visitor.setAvatar(avatarUrl);
        visitor.setUpdateTime(LocalDateTime.now());
        visitorRepository.save(visitor);

        return ResultVO.success(MessageConstant.UPDATE + MessageConstant.SUCCESS);
    }

    @Override
    public ResultVO updateVisitorStatus(Long visitorId, Integer status) {
        Optional<Visitor> visitorOptional = visitorRepository.findById(visitorId);
        if (visitorOptional.isEmpty()) {
            return ResultVO.error(400, "用户不存在");
        }

        Visitor visitor = visitorOptional.get();
        visitor.setStatus(status);
        visitor.setUpdateTime(LocalDateTime.now());
        visitorRepository.save(visitor);

        return ResultVO.success(MessageConstant.UPDATE + MessageConstant.SUCCESS);
    }

    public static boolean validateToken(String token) {
        return TOKEN_STORE.containsKey(token);
    }
}
