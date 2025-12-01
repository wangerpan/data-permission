package com.wep.permission.utils;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * 简单的 Token 工具类，示例实现可根据实际登录框架替换。
 */
public final class TokenUtils {

    private static final ThreadLocal<String> CURRENT_USER = new ThreadLocal<>();
    private static final ThreadLocal<Set<String>> CURRENT_ROLES = new ThreadLocal<>();

    private TokenUtils() {
    }

    public static void mockLogin(String userId, List<String> roles) {
        CURRENT_USER.set(userId);
        CURRENT_ROLES.set(new CopyOnWriteArraySet<>(CollUtil.defaultIfEmpty(roles, Collections.emptyList())));
    }

    public static void clear() {
        CURRENT_USER.remove();
        CURRENT_ROLES.remove();
    }

    public static String getCurrentUserId() {
        return CURRENT_USER.get();
    }

    public static Set<String> getCurrentRoles() {
        return CURRENT_ROLES.get();
    }

    public static boolean hasRole(String roleKey) {
        if (StrUtil.isBlank(roleKey)) {
            return false;
        }
        Set<String> roles = CURRENT_ROLES.get();
        return roles != null && roles.contains(roleKey);
    }
}
