package com.wep.permission.annotation;


import com.wep.permission.enums.ExpressionEnum;
import com.wep.permission.enums.ProvideTypeEnum;
import com.wep.permission.enums.SpliceTypeEnum;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({})
public @interface DataScopeExpression {
    /**
     * 条件拼接类型:AND / OR
     */
    SpliceTypeEnum spliceType() default SpliceTypeEnum.AND;

    /**
     * 表别名
     */
    String tableAlias();

    /**
     * 字段名
     */
    String columnName();

    /**
     * 条件表达式:EQ NQ LIKE 等
     */
    ExpressionEnum expression() default ExpressionEnum.EQ;

    /**
     * 值类型: 1-值，2-方法
     */
    ProvideTypeEnum provideTypeEnum() default ProvideTypeEnum.VALUE;

    /**
     * 具体值,也可能是 全限定类名#方法名
     */
    String value();

    /**
     * 如果值类型是方法,且有参数,则记录参数类型,形参，分号隔开.例如:java.math.BigDecimal;java.math.BigDecimal
     */
    String formalParam() default "";

    /**
     * 如果值类型是方法,且有参数,则记录参数类型,实参，分号隔开.例如:100;500
     */
    String actualParam() default "";
}
