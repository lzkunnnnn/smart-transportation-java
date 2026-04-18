package org.example.transportation.controller;

import jakarta.validation.Valid;
import org.example.transportation.dox.user.AdminLoginDTO;
import org.example.transportation.dox.user.UserUpdateDTO;
import org.example.transportation.service.AdminService;
import org.example.transportation.service.UserService;
import org.example.transportation.service.VisitorService;
import org.example.transportation.vo.ResultVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private AdminService adminService;

    @Autowired
    private UserService userService;

    @Autowired
    private VisitorService visitorService;

    @PostMapping("/login")
    public ResultVO login(@RequestBody @Valid AdminLoginDTO adminLoginDTO, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ResultVO.error(400, bindingResult.getFieldError().getDefaultMessage());
        }
        return adminService.login(adminLoginDTO);
    }

    @PostMapping("/logout")
    public ResultVO logout(@RequestHeader(value = "Authorization", required = false) String token) {
        return adminService.logout(token);
    }

    @GetMapping("/info")
    public ResultVO getAdminInfo() {
        return adminService.getAdminInfo();
    }

    @PutMapping("/info")
    public ResultVO updateAdminInfo(@RequestBody UserUpdateDTO userUpdateDTO) {
        return adminService.updateAdminInfo(userUpdateDTO);
    }

    @PatchMapping("/password")
    public ResultVO updatePassword(@RequestParam("oldPassword") String oldPassword,
            @RequestParam("newPassword") String newPassword) {
        return adminService.updatePassword(oldPassword, newPassword);
    }

    @GetMapping("/users")
    public ResultVO getAllUsers() {
        return userService.getAllUsers();
    }

    @PatchMapping("/user/status/{id}/{status}")
    public ResultVO updateUserStatus(@PathVariable("id") Long userId, @PathVariable("status") Integer status) {
        return userService.updateUserStatus(userId, status);
    }

    @PatchMapping("/visitor/status/{id}/{status}")
    public ResultVO updateVisitorStatus(@PathVariable("id") Long visitorId, @PathVariable("status") Integer status) {
        return visitorService.updateVisitorStatus(visitorId, status);
    }

    @DeleteMapping("/user/{id}")
    public ResultVO deleteUser(@PathVariable("id") Long userId) {
        return userService.deleteUser(userId);
    }

    @PutMapping("/user/info/{id}")
    public ResultVO updateUserInfo(@PathVariable("id") Long userId, @RequestBody UserUpdateDTO userUpdateDTO) {
        return userService.updateUserInfo(userId, userUpdateDTO);
    }
}
