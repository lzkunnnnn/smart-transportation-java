package org.example.transportation.controller;

import jakarta.validation.Valid;
import org.example.transportation.dox.user.*;
import org.example.transportation.service.UserService;
import org.example.transportation.vo.ResultVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public ResultVO register(@RequestBody @Valid UserRegisterDTO userRegisterDTO, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ResultVO.error(400, bindingResult.getFieldError().getDefaultMessage());
        }
        return userService.register(userRegisterDTO);
    }

    @PostMapping("/login")
    public ResultVO login(@RequestBody @Valid UserLoginDTO userLoginDTO, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ResultVO.error(400, bindingResult.getFieldError().getDefaultMessage());
        }
        return userService.login(userLoginDTO);
    }

    @PostMapping("/logout")
    public ResultVO logout(@RequestHeader(value = "Authorization", required = false) String token) {
        return userService.logout(token);
    }

    @GetMapping("/info")
    public ResultVO getUserInfo() {
        return userService.getUserInfo();
    }

    @PutMapping("/info")
    public ResultVO updateUserInfo(@RequestBody UserUpdateDTO userUpdateDTO) {
        return userService.updateUserInfo(userUpdateDTO);
    }

    @PatchMapping("/password")
    public ResultVO updatePassword(@RequestBody @Valid PasswordUpdateDTO passwordUpdateDTO,
                                   BindingResult bindingResult,
                                   @RequestHeader("Authorization") String token) {
        if (bindingResult.hasErrors()) {
            return ResultVO.error(400, bindingResult.getFieldError().getDefaultMessage());
        }
        return userService.updatePassword(passwordUpdateDTO, token);
    }

    @PatchMapping("/avatar")
    public ResultVO updateAvatar(@RequestParam("avatar") String avatarUrl) {
        return userService.updateAvatar(avatarUrl);
    }
}
