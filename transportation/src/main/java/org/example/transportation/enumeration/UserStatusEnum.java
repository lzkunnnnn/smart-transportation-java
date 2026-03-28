package org.example.transportation.enumeration;

import lombok.Getter;

@Getter
public enum UserStatusEnum {
    ENABLE(0, "启用"),
    DISABLE(1, "禁用");

    private final Integer id;
    private final String description;

    UserStatusEnum(Integer id, String description) {
        this.id = id;
        this.description = description;
    }
}
