package com.wep.permission.aspect;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import com.wep.permission.annotation.FieldPermission;
import com.wep.permission.exception.PermissionException;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 字段级权限控制切面。
 */
@Aspect
@Component
public class FieldPermissionAspect {

    @Around("@annotation(fieldPermission)")
    public Object around(ProceedingJoinPoint pjp, FieldPermission fieldPermission) throws Throwable {
        Object result = pjp.proceed();
        if (fieldPermission == null || !fieldPermission.required() || result == null) {
            return result;
        }
        Map<String, Object> source = toMap(result);
        Map<String, Object> filtered = new HashMap<>();

        // 控制可见字段
        if (ArrayUtil.isNotEmpty(fieldPermission.viewFields())) {
            Set<String> viewable = CollUtil.newHashSet(fieldPermission.viewFields());
            filtered.putAll(source.entrySet().stream()
                    .filter(e -> viewable.contains(e.getKey()))
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
        } else {
            filtered.putAll(source);
        }

        // 脱敏字段
        for (String maskField : fieldPermission.maskFields()) {
            if (!filtered.containsKey(maskField)) {
                continue;
            }
            Object value = filtered.get(maskField);
            filtered.put(maskField, maskValue(value));
        }

        // 记录可编辑字段，便于前端或上游消费
        if (ArrayUtil.isNotEmpty(fieldPermission.editFields())) {
            filtered.putIfAbsent("__editable__", CollUtil.newArrayList(fieldPermission.editFields()));
        }

        if (MapUtil.isEmpty(filtered)) {
            throw new PermissionException("字段权限校验后无可见字段，拒绝访问");
        }
        return BeanUtil.copyProperties(filtered, result.getClass());
    }

    private Map<String, Object> toMap(Object result) {
        if (result instanceof Map<?, ?> map) {
            Map<String, Object> target = new HashMap<>();
            map.forEach((key, value) -> target.put(String.valueOf(key), value));
            return target;
        }
        return BeanUtil.beanToMap(result, false, true);
    }

    private Object maskValue(Object value) {
        if (!(value instanceof String str) || StrUtil.isBlank(str)) {
            return value;
        }
        if (str.length() <= 2) {
            return "*".repeat(str.length());
        }
        int visible = Math.max(1, str.length() / 4);
        String prefix = str.substring(0, visible);
        String suffix = str.substring(str.length() - visible);
        return prefix + "*".repeat(str.length() - visible * 2) + suffix;
    }
}
