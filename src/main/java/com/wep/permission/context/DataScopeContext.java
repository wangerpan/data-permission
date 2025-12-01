package com.wep.permission.context;

/**
 * 保存数据范围拼装结果的上下文。
 */
public final class DataScopeContext {

    private static final ThreadLocal<String> DATA_SCOPE = new ThreadLocal<>();

    private DataScopeContext() {
    }

    public static void setScopeCondition(String condition) {
        DATA_SCOPE.set(condition);
    }

    public static String getScopeCondition() {
        return DATA_SCOPE.get();
    }

    public static String clear() {
        String value = DATA_SCOPE.get();
        DATA_SCOPE.remove();
        return value;
    }
}
