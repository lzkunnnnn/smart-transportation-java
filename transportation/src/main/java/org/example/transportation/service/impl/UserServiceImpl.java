package org.example.transportation.service.impl;

import org.example.transportation.constant.MessageConstant;
import org.example.transportation.dox.user.*;
import org.example.transportation.dox.visitor.Visitor;
import org.example.transportation.enumeration.RoleEnum;
import org.example.transportation.enumeration.UserStatusEnum;
import org.example.transportation.repository.UserRepository;
import org.example.transportation.repository.VisitorRepository;
import org.example.transportation.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
public class UserServiceImpl implements UserService {

    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VisitorRepository visitorRepository;

    private static final Map<String, String> TOKEN_STORE = new ConcurrentHashMap<>();

    @Override
    public ResultVO register(UserRegisterDTO userRegisterDTO) {
        if (userRepository.findByUsername(userRegisterDTO.getUsername()).isPresent()) {
            return ResultVO.error(400, MessageConstant.USERNAME + MessageConstant.ALREADY_EXISTS);
        }

        if (userRegisterDTO.getPhone() != null && !userRegisterDTO.getPhone().isEmpty()) {
            if (userRepository.findByPhone(userRegisterDTO.getPhone()).isPresent()) {
                return ResultVO.error(400, MessageConstant.PHONE + MessageConstant.ALREADY_EXISTS);
            }
        }

        if (userRegisterDTO.getEmail() != null && !userRegisterDTO.getEmail().isEmpty()) {
            if (userRepository.findByEmail(userRegisterDTO.getEmail()).isPresent()) {
                return ResultVO.error(400, MessageConstant.EMAIL + MessageConstant.ALREADY_EXISTS);
            }
        }

        String passwordMD5 = DigestUtils.md5DigestAsHex(userRegisterDTO.getPassword().getBytes());
        User user = User.builder()
                .username(userRegisterDTO.getUsername())
                .password(passwordMD5)
                .phone(userRegisterDTO.getPhone())
                .email(userRegisterDTO.getEmail())
                .status(UserStatusEnum.ENABLE.getId())
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();

        userRepository.save(user);
        return ResultVO.success(MessageConstant.REGISTER + MessageConstant.SUCCESS);
    }

    @Override
    public ResultVO login(UserLoginDTO userLoginDTO) {
        Optional<User> userOptional = userRepository.findByUsername(userLoginDTO.getUsername());
        if (userOptional.isEmpty()) {
            return ResultVO.error(400, MessageConstant.USERNAME + MessageConstant.ERROR);
        }

        User user = userOptional.get();
        if (user.getStatus().equals(UserStatusEnum.DISABLE.getId())) {
            return ResultVO.error(400, MessageConstant.ACCOUNT_LOCKED);
        }

        if (!DigestUtils.md5DigestAsHex(userLoginDTO.getPassword().getBytes()).equals(user.getPassword())) {
            return ResultVO.error(400, MessageConstant.PASSWORD + MessageConstant.ERROR);
        }

        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.USER_ID, user.getId());
        claims.put(JwtClaimsConstant.USERNAME, user.getUsername());
        claims.put(JwtClaimsConstant.ROLE, RoleEnum.USER.getRole());

        String token = JwtUtil.generateToken(claims);
        TOKEN_STORE.put(token, token);

        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", user.getId());
        userInfo.put("username", user.getUsername());
        userInfo.put("role", "USER");
        userInfo.put("roleIds", "0");
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
    public ResultVO getUserInfo() {
        Map<String, Object> claims = ThreadLocalUtil.get();
        Long userId = ((Number) claims.get(JwtClaimsConstant.USER_ID)).longValue();

        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isEmpty()) {
            return ResultVO.error(400, "用户不存在");
        }

        User user = userOptional.get();
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);

        return ResultVO.success(userVO);
    }

    @Override
    public ResultVO updateUserInfo(UserUpdateDTO userUpdateDTO) {
        Map<String, Object> claims = ThreadLocalUtil.get();
        Long userId = ((Number) claims.get(JwtClaimsConstant.USER_ID)).longValue();

        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isEmpty()) {
            return ResultVO.error(400, "用户不存在");
        }

        User user = userOptional.get();

        if (userUpdateDTO.getUsername() != null && !userUpdateDTO.getUsername().equals(user.getUsername())) {
            if (userRepository.findByUsername(userUpdateDTO.getUsername()).isPresent()) {
                return ResultVO.error(400, MessageConstant.USERNAME + MessageConstant.ALREADY_EXISTS);
            }
            user.setUsername(userUpdateDTO.getUsername());
        }

        if (userUpdateDTO.getPhone() != null && !userUpdateDTO.getPhone().equals(user.getPhone())) {
            if (userRepository.findByPhone(userUpdateDTO.getPhone()).isPresent()) {
                return ResultVO.error(400, MessageConstant.PHONE + MessageConstant.ALREADY_EXISTS);
            }
            user.setPhone(userUpdateDTO.getPhone());
        }

        if (userUpdateDTO.getEmail() != null && !userUpdateDTO.getEmail().equals(user.getEmail())) {
            if (userRepository.findByEmail(userUpdateDTO.getEmail()).isPresent()) {
                return ResultVO.error(400, MessageConstant.EMAIL + MessageConstant.ALREADY_EXISTS);
            }
            user.setEmail(userUpdateDTO.getEmail());
        }

        if (userUpdateDTO.getAvatar() != null) {
            user.setAvatar(userUpdateDTO.getAvatar());
        }

        user.setUpdateTime(LocalDateTime.now());
        userRepository.save(user);

        return ResultVO.success(MessageConstant.UPDATE + MessageConstant.SUCCESS);
    }

    @Override
    public ResultVO updatePassword(PasswordUpdateDTO passwordUpdateDTO, String token) {
        Map<String, Object> claims = ThreadLocalUtil.get();
        Long userId = ((Number) claims.get(JwtClaimsConstant.USER_ID)).longValue();

        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isEmpty()) {
            return ResultVO.error(400, "用户不存在");
        }

        User user = userOptional.get();

        if (!DigestUtils.md5DigestAsHex(passwordUpdateDTO.getOldPassword().getBytes()).equals(user.getPassword())) {
            return ResultVO.error(400, MessageConstant.OLD_PASSWORD_ERROR);
        }

        if (DigestUtils.md5DigestAsHex(passwordUpdateDTO.getNewPassword().getBytes()).equals(user.getPassword())) {
            return ResultVO.error(400, MessageConstant.NEW_PASSWORD_ERROR);
        }

        if (!passwordUpdateDTO.getNewPassword().equals(passwordUpdateDTO.getConfirmPassword())) {
            return ResultVO.error(400, MessageConstant.PASSWORD_NOT_MATCH);
        }

        user.setPassword(DigestUtils.md5DigestAsHex(passwordUpdateDTO.getNewPassword().getBytes()));
        user.setUpdateTime(LocalDateTime.now());
        userRepository.save(user);

        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        TOKEN_STORE.remove(token);

        return ResultVO.success(MessageConstant.UPDATE + MessageConstant.SUCCESS);
    }

    @Override
    public ResultVO updateAvatar(String avatarUrl) {
        Map<String, Object> claims = ThreadLocalUtil.get();
        Long userId = ((Number) claims.get(JwtClaimsConstant.USER_ID)).longValue();

        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isEmpty()) {
            return ResultVO.error(400, "用户不存在");
        }

        User user = userOptional.get();
        user.setAvatar(avatarUrl);
        user.setUpdateTime(LocalDateTime.now());
        userRepository.save(user);

        return ResultVO.success(MessageConstant.UPDATE + MessageConstant.SUCCESS);
    }

    @Override
    public ResultVO getAllUsers() {
        Iterable<User> users = userRepository.findAll();
        List<UserVO> userVOList = new ArrayList<>();

        // 添加普通用户
        for (User user : users) {
            UserVO userVO = new UserVO();
            BeanUtils.copyProperties(user, userVO);
            userVO.setUserType("user");
            userVOList.add(userVO);
        }

        // 添加群众用户
        Iterable<Visitor> visitors = visitorRepository.findAll();
        int visitorCount = 0;
        for (Visitor visitor : visitors) {
            UserVO userVO = new UserVO();
            userVO.setId(visitor.getId());
            userVO.setUsername(visitor.getUsername());
            userVO.setPhone(visitor.getPhone());
            userVO.setEmail(visitor.getEmail());
            userVO.setAvatar(visitor.getAvatar());
            userVO.setStatus(visitor.getStatus());
            userVO.setCreateTime(visitor.getCreateTime());
            userVO.setUpdateTime(visitor.getUpdateTime());
            userVO.setUserType("visitor");
            userVOList.add(userVO);
            visitorCount++;
        }
        log.info("获取用户列表：普通管理员 {} 人，群众 {} 人，总计 {} 人",
                ((List) users).size(), visitorCount, userVOList.size());

        return ResultVO.success(userVOList);
    }

    @Override
    public ResultVO updateUserStatus(Long userId, Integer status) {
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isEmpty()) {
            return ResultVO.error(400, "用户不存在");
        }

        User user = userOptional.get();
        user.setStatus(status);
        user.setUpdateTime(LocalDateTime.now());
        userRepository.save(user);

        return ResultVO.success(MessageConstant.UPDATE + MessageConstant.SUCCESS);
    }

    @Override
    public ResultVO deleteUser(Long userId) {
        userRepository.deleteById(userId);
        return ResultVO.success(MessageConstant.DELETE + MessageConstant.SUCCESS);
    }

    @Override
    public ResultVO updateUserInfo(Long userId, UserUpdateDTO userUpdateDTO) {
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isEmpty()) {
            return ResultVO.error(400, "用户不存在");
        }

        User user = userOptional.get();

        if (userUpdateDTO.getUsername() != null && !userUpdateDTO.getUsername().equals(user.getUsername())) {
            if (userRepository.findByUsername(userUpdateDTO.getUsername()).isPresent()) {
                return ResultVO.error(400, MessageConstant.USERNAME + MessageConstant.ALREADY_EXISTS);
            }
            user.setUsername(userUpdateDTO.getUsername());
        }

        if (userUpdateDTO.getPhone() != null && !userUpdateDTO.getPhone().equals(user.getPhone())) {
            if (userRepository.findByPhone(userUpdateDTO.getPhone()).isPresent()) {
                return ResultVO.error(400, MessageConstant.PHONE + MessageConstant.ALREADY_EXISTS);
            }
            user.setPhone(userUpdateDTO.getPhone());
        }

        if (userUpdateDTO.getEmail() != null && !userUpdateDTO.getEmail().equals(user.getEmail())) {
            if (userRepository.findByEmail(userUpdateDTO.getEmail()).isPresent()) {
                return ResultVO.error(400, MessageConstant.EMAIL + MessageConstant.ALREADY_EXISTS);
            }
            user.setEmail(userUpdateDTO.getEmail());
        }

        if (userUpdateDTO.getAvatar() != null) {
            user.setAvatar(userUpdateDTO.getAvatar());
        }

        user.setUpdateTime(LocalDateTime.now());
        userRepository.save(user);

        return ResultVO.success(MessageConstant.UPDATE + MessageConstant.SUCCESS);
    }

    public static boolean validateToken(String token) {
        return TOKEN_STORE.containsKey(token);
    }
}
