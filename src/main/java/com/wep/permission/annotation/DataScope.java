package com.wep.permission.annotation;


import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
@Documented
public @interface DataScope {
    /**
     * 是否启用数据范围权限控制.例如:通过获取数据库的权限配置进行sql动态拼接,实现指定数据范围的查询
     * 是否必须
     * 默认 是
     */
    boolean required() default true;

    /**
     * 自定义表达式,如果启用数据范围权限控制,则需要进行补充
     */
    DataScopeExpression[] dataScopeExpressions() default {};
}
