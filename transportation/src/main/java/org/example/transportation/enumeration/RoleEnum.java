package org.example.transportation.enumeration;

import lombok.Getter;

@Getter
public enum RoleEnum {
    USER("USER"),
    ADMIN("ADMIN"),
    VISITOR("VISITOR");

    private final String role;

    RoleEnum(String role) {
        this.role = role;
    }
}
