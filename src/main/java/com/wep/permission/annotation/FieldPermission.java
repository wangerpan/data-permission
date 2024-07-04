package com.wep.permission.annotation;

import java.lang.annotation.*;

/**
 * @author wep
 * 定义数据对象字段的字段权限
 * <p>
 * 作用于方法上,对该方法的入参和出参进行控制
 * <p>
 * viewFields和maskFields 一般用于方法出参的属性值
 * editFields 用于方法入参的属性值
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
@Documented
public @interface FieldPermission {
    boolean required() default true;

    /**
     * 定义被查看的字段列表
     */
    String[] viewFields() default {};

    /**
     * 定义脱敏的字段列表
     * 一般用于的字段:手机号,身份照,邮箱,地址,合同编号的等
     */
    String[] maskFields() default {};

    /**
     * 定义可编辑的字段列表
     */
    String[] editFields() default {};
}
