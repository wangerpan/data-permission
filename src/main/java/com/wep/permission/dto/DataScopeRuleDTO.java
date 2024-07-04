package com.wep.permission.dto;

import lombok.Data;

@Data
public class DataScopeRuleDTO {
    /**
     * 表别名
     */
    private String tableAlias;

    /**
     * 数据库字段名
     */
    private String columnName;

    /**
     * 拼接类型:SpliceTypeEnum
     */
    private String spliceType;

    /**
     * 条件表达式:EQ NQ LIKE 等
     */
    private String expression;

    /**
     * 值类型: 1-值，2-方法
     */
    private Integer provideType;

    /**
     * 具体值,也可能是 全限定类名#方法名
     */
    private String value;

    /**
     * 如果值类型是方法,且有参数,则记录参数类型,形参，分号隔开.例如:java.math.BigDecimal;java.math.BigDecimal
     */
    private String formalParam;

    /**
     * 如果值类型是方法,且有参数,则记录参数类型,实参，分号隔开.例如:100;500
     */
    private String actualParam;

    /**
     * 执行结果
     */
    private Object result;
}
