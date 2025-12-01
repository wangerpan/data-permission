package com.wep.permission.annotation;

import java.lang.annotation.*;

/**
 * 字段级权限控制注解。
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Documented
public @interface FieldPermission {

    /**
     * 是否必须校验。
     */
    boolean required() default true;

    /**
     * 可见字段列表。
     */
    String[] viewFields() default {};

    /**
     * 需要脱敏的字段列表。
     */
    String[] maskFields() default {};

    /**
     * 允许编辑的字段列表。
     */
    String[] editFields() default {};
}
