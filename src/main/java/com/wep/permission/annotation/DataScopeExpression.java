package com.wep.permission.annotation;

import com.wep.permission.enums.ExpressionEnum;
import com.wep.permission.enums.SpliceTypeEnum;

import java.lang.annotation.*;

/**
 * 组成数据范围条件的表达式片段。
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.ANNOTATION_TYPE})
@Documented
public @interface DataScopeExpression {

    /**
     * 表别名。
     */
    String tableAlias();

    /**
     * 列名。
     */
    String columnName();

    /**
     * 比较值或 SpEL 表达式。
     */
    String value();

    /**
     * 比较运算符。
     */
    ExpressionEnum expression() default ExpressionEnum.EQ;

    /**
     * 条件拼接方式。
     */
    SpliceTypeEnum spliceType() default SpliceTypeEnum.AND;
}
