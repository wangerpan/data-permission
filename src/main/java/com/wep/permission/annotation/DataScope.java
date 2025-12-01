package com.wep.permission.annotation;

import java.lang.annotation.*;

/**
 * 数据范围注解，可组合多个 {@link DataScopeExpression} 片段。
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
@Documented
public @interface DataScope {

    /**
     * 需要拼装的数据范围表达式。
     */
    DataScopeExpression[] dataScopeExpressions();
}
