package org.example.transportation.service;

import org.example.transportation.dox.user.PasswordUpdateDTO;
import org.example.transportation.dox.visitor.*;
import org.example.transportation.vo.ResultVO;

public interface VisitorService {
    ResultVO register(VisitorRegisterDTO visitorRegisterDTO);

    ResultVO login(VisitorLoginDTO visitorLoginDTO);

    ResultVO logout(String token);

    ResultVO getVisitorInfo();

    ResultVO updateVisitorInfo(VisitorUpdateDTO visitorUpdateDTO);

    ResultVO updatePassword(PasswordUpdateDTO passwordUpdateDTO, String token);

    ResultVO updateAvatar(String avatarUrl);

    ResultVO updateVisitorStatus(Long visitorId, Integer status);
}
