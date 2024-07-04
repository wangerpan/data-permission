package com.wep.permission.aspect;

import com.wep.permission.annotation.FieldPermission;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.Arrays;

/**
 * 用于数据对象的权限控制
 */
@Aspect
@Component
@Slf4j
public class FieldPermissionAspect {

    @Before("@annotation(fieldPermission)")
    public void checkFieldPermission(JoinPoint joinPoint, FieldPermission fieldPermission) throws IllegalAccessException {
        Object[] args = joinPoint.getArgs();
        if (args.length == 0) {
            return;
        }
        Object target = args[0];
        if (fieldPermission.required()) {
            // 检查编辑权限
            if (fieldPermission.editFields().length > 0) {
                applyEditPermissions(target, fieldPermission.editFields());
            }
        }
    }

    private void applyEditPermissions(Object target, String[] editFields) throws IllegalAccessException {
        for (Field field : target.getClass().getDeclaredFields()) {
            field.setAccessible(true);
            if (!Arrays.asList(editFields).contains(field.getName())) {
                field.set(target, null); // 或者抛出异常表示不能编辑
            }
        }
    }
}
