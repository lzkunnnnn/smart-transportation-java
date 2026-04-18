package org.example.transportation.controller;

import jakarta.validation.Valid;
import org.example.transportation.dox.user.PasswordUpdateDTO;
import org.example.transportation.dox.visitor.*;
import org.example.transportation.service.VisitorService;
import org.example.transportation.vo.ResultVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/visitor")
public class VisitorController {

    @Autowired
    private VisitorService visitorService;

    @PostMapping("/register")
    public ResultVO register(@RequestBody @Valid VisitorRegisterDTO visitorRegisterDTO, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ResultVO.error(400, bindingResult.getFieldError().getDefaultMessage());
        }
        return visitorService.register(visitorRegisterDTO);
    }

    @PostMapping("/login")
    public ResultVO login(@RequestBody @Valid VisitorLoginDTO visitorLoginDTO, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ResultVO.error(400, bindingResult.getFieldError().getDefaultMessage());
        }
        return visitorService.login(visitorLoginDTO);
    }

    @PostMapping("/logout")
    public ResultVO logout(@RequestHeader(value = "Authorization", required = false) String token) {
        return visitorService.logout(token);
    }

    @GetMapping("/info")
    public ResultVO getVisitorInfo() {
        return visitorService.getVisitorInfo();
    }

    @PutMapping("/info")
    public ResultVO updateVisitorInfo(@RequestBody VisitorUpdateDTO visitorUpdateDTO) {
        return visitorService.updateVisitorInfo(visitorUpdateDTO);
    }

    @PatchMapping("/password")
    public ResultVO updatePassword(@RequestBody @Valid PasswordUpdateDTO passwordUpdateDTO,
                                   BindingResult bindingResult,
                                   @RequestHeader("Authorization") String token) {
        if (bindingResult.hasErrors()) {
            return ResultVO.error(400, bindingResult.getFieldError().getDefaultMessage());
        }
        return visitorService.updatePassword(passwordUpdateDTO, token);
    }

    @PatchMapping("/avatar")
    public ResultVO updateAvatar(@RequestParam("avatar") String avatarUrl) {
        return visitorService.updateAvatar(avatarUrl);
    }
}
