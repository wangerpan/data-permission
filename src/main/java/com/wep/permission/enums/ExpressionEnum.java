package com.wep.permission.enums;

/**
 * 支持的数据范围表达式枚举。
 */
public enum ExpressionEnum {
    EQ("="),
    NE("<>"),
    GT(">"),
    GE(">="),
    LT("<"),
    LE("<="),
    LIKE("LIKE"),
    IN("IN");

    private final String symbol;

    ExpressionEnum(String symbol) {
        this.symbol = symbol;
    }

    public String getSymbol() {
        return symbol;
    }
}
