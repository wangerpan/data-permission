package com.wep.permission.config;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wep.permission.annotation.DataObjPermission;
import com.wep.permission.annotation.FieldPermission;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * 字段编辑权限控制
 */
@Slf4j
public class EditPermissionInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }
        HandlerMethod handlerMethod = (HandlerMethod) handler;

        // 获取执行方法所在的类
        Class<?> containingClass = handlerMethod.getBeanType();
        DataObjPermission dataObjPermission = containingClass.getAnnotation(DataObjPermission.class);
        if (dataObjPermission == null || !dataObjPermission.required()) {
            return true;
        }

        FieldPermission fieldPermission = handlerMethod.getMethodAnnotation(FieldPermission.class);
        if (fieldPermission == null || !fieldPermission.required()) {
            log.info("fieldPermission is null or fieldPermission is not required");
            return true;
        }


        String[] editFields = fieldPermission.editFields();
        if (ArrayUtil.isEmpty(editFields)) {
            log.info("editFields is empty");
            return true;
        }
        List<String> editFieldList = CollUtil.toList(editFields);

        ObjectMapper objectMapper = new ObjectMapper();
        // 处理POST请求的JSON数据
        String jsonBody = getRequestBody(request);
        if (StrUtil.isEmpty(jsonBody)) {
            return true;
        }
        Map<String, Object> parameterMap = objectMapper.readValue(jsonBody, Map.class);
        for (String key : parameterMap.keySet()) {
            if (!editFieldList.contains(key)) {
                throw new IllegalAccessException("Field " + key + " is not allowed to be edited.");
            }
        }
        return true;
    }

    private String getRequestBody(HttpServletRequest request) throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        String line;
        while ((line = request.getReader().readLine()) != null) {
            stringBuilder.append(line);
        }
        return stringBuilder.toString();
    }
}
