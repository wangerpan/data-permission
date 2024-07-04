package com.wep.permission.annotation;

import java.lang.annotation.*;

/**
 * @author wep 2024年6月28日
 * 数据对象注解,一般作用于controller层,也可以作用于某个方法上.
 * <p>
 * 注意:
 * // * 作用于方法上的优先级高于作用于类上!!!
 * 如果需要控制数据对象权限,该注解必须添加!!!
 * <p>
 * 作用:
 * 1.用于标记该数据对象的类型
 * 2.可以通过请求方式对接口进行权限控制:VIEW-get请求;ADD-post请求;EDIT-put请求;DEL-delete请求
 * 3.还可以自定义需要权限控制的接口.例如:dept模块下,urls={"/list","/get"}访问这两个接口需要进行权限配置,否则无法访问
 * 4.是否开启数据范围权限控制
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Documented
public @interface DataObjPermission {
    /**
     * 是否必须
     * 默认 是
     */
    boolean required() default true;

    /**
     * 哪些角色可以访问
     */
    String[] roleKeys() default {};


    /**
     * 哪些用户可以访问
     */
    String[] userIds() default {};

    /**
     * 是否通过请求方式进行权限验证
     * 如果是,则优先使用这个方式进行权限验证
     * <p>
     * 默认 否
     */
    boolean isRequestType() default false;

    /**
     * 请求方式
     */
    String[] requestTypes() default {};

    /**
     * 定义需要进行权限验证的路径
     * <p>
     * 例如:dept模块下,urls={"/list","/get"}访问这两个接口需要进行权限配置,否则无法访问
     */
    String[] urls() default {};
}
