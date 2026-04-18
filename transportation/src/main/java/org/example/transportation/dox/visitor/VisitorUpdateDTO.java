package org.example.transportation.dox.visitor;

import jakarta.validation.constraints.Email;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class VisitorUpdateDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String username;

    private String phone;

    @Email(message = "邮箱格式不正确")
    private String email;

    private String avatar;
}
