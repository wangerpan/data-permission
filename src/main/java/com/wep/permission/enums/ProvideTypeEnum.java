package com.wep.permission.enums;

import lombok.Getter;

/**
 * @author wep
 */
@Getter
public enum ProvideTypeEnum {

    VALUE(1, "值"),
    METHOD(2, "方法");

    private final Integer code;
    private final String name;

    ProvideTypeEnum(Integer code, String name) {
        this.code = code;
        this.name = name;
    }

}
