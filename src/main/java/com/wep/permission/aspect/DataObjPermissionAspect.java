package com.wep.permission.aspect;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import com.wep.permission.annotation.DataObjPermission;
import com.wep.permission.exception.PermissionException;
import com.wep.permission.utils.TokenUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Arrays;
import java.util.Optional;

/**
 * 对 {@link DataObjPermission} 进行校验的切面。
 */
@Aspect
@Component
public class DataObjPermissionAspect {

    @Pointcut("@within(com.wep.permission.annotation.DataObjPermission) || @annotation(com.wep.permission.annotation.DataObjPermission)")
    public void pointcut() {
    }

    @Before("pointcut() && @annotation(permission)")
    public void beforeWithAnnotation(JoinPoint joinPoint, DataObjPermission permission) {
        handlePermission(permission);
    }

    @Before("pointcut() && !@annotation(com.wep.permission.annotation.DataObjPermission) && @within(permission)")
    public void beforeWithClass(JoinPoint joinPoint, DataObjPermission permission) {
        handlePermission(permission);
    }

    private void handlePermission(DataObjPermission permission) {
        if (permission == null || !permission.required()) {
            return;
        }
        if (!validateUser(permission) || !validateRole(permission)) {
            throw new PermissionException("当前登录人不在允许的角色或用户列表中，拒绝访问");
        }
        if (!validateRequest(permission)) {
            throw new PermissionException("请求方式或路径不在授权范围内，拒绝访问");
        }
    }

    private boolean validateUser(DataObjPermission permission) {
        String[] userIds = permission.userIds();
        if (ArrayUtil.isEmpty(userIds)) {
            return true;
        }
        String currentUserId = TokenUtils.getCurrentUserId();
        return StrUtil.isNotBlank(currentUserId) && Arrays.asList(userIds).contains(currentUserId);
    }

    private boolean validateRole(DataObjPermission permission) {
        String[] roleKeys = permission.roleKeys();
        if (ArrayUtil.isEmpty(roleKeys)) {
            return true;
        }
        return CollUtil.containsAny(Arrays.asList(roleKeys), Optional.ofNullable(TokenUtils.getCurrentRoles()).orElseGet(CollUtil::newHashSet));
    }

    private boolean validateRequest(DataObjPermission permission) {
        if (!permission.isRequestType() && ArrayUtil.isEmpty(permission.urls())) {
            return true;
        }
        HttpServletRequest request = currentRequest();
        if (request == null) {
            return true;
        }
        if (permission.isRequestType()) {
            String method = request.getMethod();
            if (!Arrays.asList(permission.requestTypes()).contains(method)) {
                return false;
            }
        }
        if (ArrayUtil.isNotEmpty(permission.urls())) {
            String path = request.getRequestURI();
            return Arrays.stream(permission.urls()).anyMatch(path::contains);
        }
        return true;
    }

    private HttpServletRequest currentRequest() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attributes == null ? null : attributes.getRequest();
    }
}
