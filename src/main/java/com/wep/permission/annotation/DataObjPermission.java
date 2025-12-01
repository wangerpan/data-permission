package com.wep.permission.annotation;

import java.lang.annotation.*;

/**
 * 数据对象级别的权限控制注解。
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@Documented
public @interface DataObjPermission {

    /**
     * 是否必须校验。
     */
    boolean required() default true;

    /**
     * 允许访问的角色标识列表。
     */
    String[] roleKeys() default {};

    /**
     * 允许访问的用户 id 列表。
     */
    String[] userIds() default {};

    /**
     * 是否按请求方式进行验证（GET/POST/PUT/DELETE 等）。
     */
    boolean isRequestType() default false;

    /**
     * 允许的请求方式列表。
     */
    String[] requestTypes() default {};

    /**
     * 需要进行权限验证的路径列表。
     */
    String[] urls() default {};
}
