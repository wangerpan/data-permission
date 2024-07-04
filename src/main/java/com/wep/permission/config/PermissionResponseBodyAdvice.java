package com.wep.permission.config;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Validator;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.DesensitizedUtil;
import cn.hutool.core.util.IdcardUtil;
import cn.hutool.core.util.PhoneUtil;
import com.github.pagehelper.PageInfo;
import com.wep.permission.annotation.DataObjPermission;
import com.wep.permission.annotation.FieldPermission;
import com.wep.permission.common.PermissionConstant;
import com.wep.permission.exception.PermissionException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.*;

/**
 * 字段的查看权限控制
 */
@Slf4j
@ControllerAdvice
public class PermissionResponseBodyAdvice implements ResponseBodyAdvice<Object> {
    @Override
    public boolean supports(MethodParameter methodParameter, Class<? extends HttpMessageConverter<?>> aClass) {
        return true;
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter methodParameter, MediaType mediaType,
                                  Class<? extends HttpMessageConverter<?>> aClass, ServerHttpRequest request,
                                  ServerHttpResponse response) {
        // 先获取方法上的注解
        DataObjPermission dataObjPermission = methodParameter.getMethodAnnotation(DataObjPermission.class);
        if (dataObjPermission == null) {
            // 获取不到在获取类上注解
            Class<?> containingClass = methodParameter.getContainingClass();
            dataObjPermission = containingClass.getAnnotation(DataObjPermission.class);
        }
        if (dataObjPermission == null || !dataObjPermission.required()) {
            return body;
        }
        if (body == null) {
            return null;
        }
        Map<String, List<String>> fieldPermissionMap = new HashMap<>();
        FieldPermission fieldPermission = methodParameter.getMethodAnnotation(FieldPermission.class);
        if (fieldPermission != null) {
            String[] viewFields = fieldPermission.viewFields();
            if (ArrayUtil.isNotEmpty(viewFields)) {
                for (String viewField : viewFields) {
                    fieldPermissionMap.put(viewField, CollUtil.toList(PermissionConstant.PERMISSION_VIEW));

                }
            }
            String[] maskFields = fieldPermission.maskFields();
            if (ArrayUtil.isNotEmpty(maskFields)) {
                for (String maskField : maskFields) {
                    fieldPermissionMap.put(maskField, CollUtil.toList(PermissionConstant.PERMISSION_MASK));
                }
            }
        }
        log.debug("fieldPermissionMap = {}", fieldPermissionMap);

        if (MapUtil.isEmpty(fieldPermissionMap)) {
            return body;
        }

        // 检查返回对象的类型并进行处理
        if (body instanceof Map) {
            Map<String, Object> responseBody = (Map<String, Object>) body;
            if (PermissionConstant.RESULT_SUCCESS.equals(responseBody.get(PermissionConstant.CODE))) {
                Object data = responseBody.get(PermissionConstant.DATA);
                if (data != null && !(data instanceof Boolean)) {
                    // 创建新的对象，包含有权限的字段
                    Object filteredData = preHandleDataObject(data, fieldPermissionMap);
                    responseBody.put(PermissionConstant.DATA, filteredData);
                }
            }
        }
        return body;
    }

    /**
     * 结果预处理
     * 把复杂的数据结构,拆解成简单数据结构,然后再对简单数据结构进行返回值的处理
     *
     * @param data               返回的结果数据
     * @param fieldPermissionMap 权限合集
     * @return
     */
    private Object preHandleDataObject(Object data, Map<String, List<String>> fieldPermissionMap) {
        try {
            // 如果结果类型是集合类
            if (data instanceof Collection) {
                // 如果是集合，递归处理集合中的元素
                Collection<?> collection = (Collection<?>) data;
                Collection<Object> filteredCollection = collection.getClass().newInstance();
                for (Object item : collection) {
                    filteredCollection.add(filterDataObject(item, fieldPermissionMap));
                }
                return filteredCollection;

            } else if (data instanceof Map) {
                // 如果是Map类型，递归处理Map中的值
                Map<?, ?> map = (Map<?, ?>) data;
                Map<Object, Object> filteredMap = new HashMap<>();
                for (Map.Entry<?, ?> entry : map.entrySet()) {
                    Object key = entry.getKey();
                    Object mapValue = entry.getValue();
                    List<String> permissions = hasPermission(key + "", fieldPermissionMap);
                    if (CollUtil.isEmpty(permissions)) {
                        log.info("没有该字段的权限:field.getName = {}", key);
                        continue;
                    }
                    mapValue = handleFieldValue(permissions, mapValue);
                    if (mapValue != null && isBasicClass(mapValue.getClass())) {
                        filteredMap.put(key, mapValue);
                    } else {
                        filteredMap.put(key, filterDataObject(mapValue, fieldPermissionMap));
                    }
                }
                return filteredMap;
            } else if (data instanceof PageInfo) {
                // 如果是分页，处理分页的数据
                PageInfo<Object> pageInfo = (PageInfo<Object>) data;
                List<Object> newInstanceList = ((PageInfo<Object>) data).getList();
                PageInfo<Object> newInstancePage = pageInfo.getClass().newInstance();
                newInstancePage.setList((List<Object>) preHandleDataObject(newInstanceList, fieldPermissionMap));
                newInstancePage.setTotal(pageInfo.getTotal());
                newInstancePage.setPageNum(((PageInfo<?>) data).getPageNum());
                newInstancePage.setPageSize(((PageInfo<?>) data).getPageSize());
                return newInstancePage;
            } else {
                return filterDataObject(data, fieldPermissionMap);
            }
        } catch (InstantiationException | IllegalAccessException e) {
            throw new PermissionException("结果预处理失败:" + e.getMessage());
        }
    }

    /**
     * 对返回的结果进行二次处理:
     *
     * @param data
     * @param fieldPermissionMap
     * @return
     */
    private Object filterDataObject(Object data, Map<String, List<String>> fieldPermissionMap) {
        try {
            if (MapUtil.isEmpty(fieldPermissionMap)) {
                log.info("未配置权限注解");
                return data;
            }
            // 创建新的实例
            Object filteredObject = data.getClass().newInstance();
            // 已知数据对象类型:获取该数据对象的权限:

            // 获取所有字段并处理
            Field[] fields = data.getClass().getDeclaredFields();
            for (Field field : fields) {
                // 设置private字段可以被访问
                field.setAccessible(true);
                Object value = field.get(data);
                if (value == null) {
                    continue;
                }
                List<String> permissions = hasPermission(field.getName(), fieldPermissionMap);
                if (CollUtil.isEmpty(permissions)) {
                    log.debug("没有该字段的权限:field.getName = {}", field.getName());
                    continue;
                }

                if (value instanceof Collection) {
                    // 如果是集合，递归处理集合中的元素
                    Collection<?> collection = (Collection<?>) value;
                    Collection<Object> filteredCollection = collection.getClass().newInstance();
                    for (Object item : collection) {
                        filteredCollection.add(filterDataObject(item, fieldPermissionMap));
                    }
                    field.set(filteredObject, filteredCollection);
                } else if (value instanceof Map) {
                    // 如果是Map类型，递归处理Map中的值
                    Map<?, ?> map = (Map<?, ?>) value;
                    Map<Object, Object> filteredMap = new HashMap<>();
                    for (Map.Entry<?, ?> entry : map.entrySet()) {
                        Object key = entry.getKey();
                        Object mapValue = entry.getValue();

                        List<String> keyPermissions = hasPermission(key + "", fieldPermissionMap);
                        if (CollUtil.isEmpty(keyPermissions)) {
                            log.info("没有该字段的权限:field.getName = {}", key);
                            continue;
                        }
                        mapValue = handleFieldValue(keyPermissions, mapValue);
                        if (mapValue != null && isBasicClass(mapValue.getClass())) {
                            filteredMap.put(key, mapValue);
                        } else {
                            filteredMap.put(key, filterDataObject(mapValue, fieldPermissionMap));
                        }
                    }
                    field.set(filteredObject, filteredMap);
                } else if (!isBasicClass(value.getClass()) && !(value instanceof Date) && !(value instanceof LocalDateTime)) {
                    // 如果是复杂对象，递归处理
                    field.set(filteredObject, filterDataObject(value, fieldPermissionMap));
                } else {
                    // 直接复制值
                    field.set(filteredObject, handleFieldValue(permissions, value));
                }
            }
            return filteredObject;
        } catch (InstantiationException | IllegalAccessException e) {
            log.error(e.getMessage());
            throw new PermissionException("生成新对象失败");
        }
    }

    /**
     * 是否 基本类
     *
     * @param clazz
     * @return
     */
    private boolean isBasicClass(Class<?> clazz) {
        return clazz.isPrimitive() || //判断是否是原始类型 int, char 等
                clazz.equals(String.class) ||
                clazz.equals(Boolean.class) ||
                clazz.equals(Byte.class) ||
                clazz.equals(Character.class) ||
                clazz.equals(Double.class) ||
                clazz.equals(Float.class) ||
                clazz.equals(Integer.class) ||
                clazz.equals(Long.class) ||
                clazz.equals(Short.class);
    }

    public List<String> hasPermission(String fieldName, Map<String, List<String>> fieldPermissionMap) {
        List<String> permissions = fieldPermissionMap.get(fieldName);
        if (CollUtil.isEmpty(permissions)) {
            return new ArrayList<>();
        }
        return permissions;
    }

    /**
     * 脱敏操作
     *
     * @param value
     * @return
     */
    private Object handleFieldValue(List<String> permissions, Object value) {
        if (value == null) {
            return value;
        }

        if (!isBasicClass(value.getClass())) {
            return value;
        }

        if (CollUtil.contains(permissions, PermissionConstant.PERMISSION_MASK)) {
            return maskHandle(value);
        }
        return value;
    }

    /**
     * 脱敏逻辑
     *
     * @param value
     * @return
     */
    private Object maskHandle(Object value) {
        if (value == null) {
            return null;
        }
        // 如果为空
        if ("".equals(value)) {
            return null;
        }
        // 手机号脱敏
        if (PhoneUtil.isPhone(value.toString())) {
            return PhoneUtil.hideBetween(value.toString());
        }
        // 身份证脱敏
        if (IdcardUtil.isValidCard(value.toString())) {
            return IdcardUtil.hide(value.toString(), 10, 16);
        }
        // 邮箱脱敏
        if (Validator.isEmail(value.toString())) {
            return DesensitizedUtil.email(value.toString());
        }
        // 姓名脱敏
        if (Validator.isChineseName(value.toString())) {
            return DesensitizedUtil.chineseName(value.toString());
        }
        if (value.toString().length() > 8) {
            int index1 = value.toString().length() / 2 - 2;
            int index2 = value.toString().length() / 2 + 2;
            return value.toString().substring(0, index1) + "****" + value.toString().substring(index2);
        }
        // 默认脱敏规则
        return "*" + value.toString().substring(1);
    }
}
