package com.wep.permission.exception;

import java.io.Serializable;

public class PermissionException extends RuntimeException implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 错误码
     */
    private Integer code = 500;

    /**
     * 错误提示
     */
    private String message;

    /**
     * 空构造方法，避免反序列化问题
     */
    public PermissionException() {
    }

    public PermissionException(String message) {
        this.message = message;
    }

    public PermissionException(String message, Integer code) {
        this.message = message;
        this.code = code;
    }

    public PermissionException(String message, Throwable cause) {
        super(message, cause);
    }


    @Override
    public String getMessage() {
        return message;
    }

    public PermissionException setMessage(String message) {
        this.message = message;
        return this;
    }

    public Integer getCode() {
        return code;
    }
}
