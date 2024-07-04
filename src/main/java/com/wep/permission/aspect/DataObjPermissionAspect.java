package com.wep.permission.aspect;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ArrayUtil;
import com.alibaba.fastjson2.JSON;
import com.wep.permission.annotation.DataObjPermission;
import com.wep.permission.exception.PermissionException;
import com.wep.permission.utils.TokenUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.List;

/**
 * @author wep
 */
@Aspect
@Slf4j
@Component
public class DataObjPermissionAspect {

    @Resource
    private HttpServletRequest request;

    // 方法切点
    @Pointcut("@within(com.wep.permission.annotation.DataObjPermission)")
    public void dataScopePointCut() {
    }

    @After("dataScopePointCut()")
    public void clearThreadLocal() {

    }

    @Before("dataScopePointCut()")
    public void doBefore(JoinPoint point) {
        Signature signature = point.getSignature();
        MethodSignature methodSignature = (MethodSignature) signature;
        Method method = methodSignature.getMethod();

        // 获得作用于方法上的注解
        DataObjPermission dataObjPermission = method.getAnnotation(DataObjPermission.class);
        // 获取不到 在获取目标类上的注解
        if (dataObjPermission == null) {
            Class<?> targetClass = point.getTarget().getClass();
            dataObjPermission = targetClass.getAnnotation(DataObjPermission.class);

            // 如果类上也没有注解，再获取接口上的注解
            if (dataObjPermission == null) {
                Class<?>[] interfaces = targetClass.getInterfaces();
                for (Class<?> interfaceClass : interfaces) {
                    dataObjPermission = interfaceClass.getAnnotation(DataObjPermission.class);
                    if (dataObjPermission != null) {
                        break;
                    }
                }
            }
        }
        // 如果接口上也获取不到,则添加的该注解无效 或者把注解成非必须
        if (dataObjPermission == null || !dataObjPermission.required()) {
            return;
        }
        checkControllerPermission(dataObjPermission);

    }

    /**
     * 定义数据对象的权限控制
     *
     * @param dataObjPermission
     */
    private void checkControllerPermission(DataObjPermission dataObjPermission) {
        // 获取当前用户ID
        log.debug("request URI = {}", request.getRequestURI());
        String userId = TokenUtils.getCurrentUserId();
        // 用户的角色key列表
        List<String> roleKeyList = TokenUtils.getCurrentUserRoleKeys();

        // 注解配置的角色key列表
        String[] roleKeys = dataObjPermission.roleKeys();

        // 如果不包含任意一个,则没有权限
        if (!CollUtil.containsAny(roleKeyList, CollUtil.toList(roleKeys))) {
            String[] userIds = dataObjPermission.userIds();
            if (ArrayUtil.isNotEmpty(userIds)) {
                if (!ArrayUtil.contains(roleKeys, userId)) {
                    throw new PermissionException("The user role can't access this resource. " +
                            " roleKeyList = " + JSON.toJSONString(roleKeyList));
                }
            }
            throw new PermissionException("The user role can't access this resource. " +
                    " roleKeyList = " + JSON.toJSONString(roleKeyList));
        }

        // 通过请求方式进行权限验证
        // 根据不同的请求方式 控制数据对象的接口是否有权限请求
        if (dataObjPermission.isRequestType()) {
            log.debug("getMethod = {}", request.getMethod());
            String requestType = request.getMethod();
            String[] requestTypes = dataObjPermission.requestTypes();
            if (ArrayUtil.isNotEmpty(requestTypes)) {
                if (!ArrayUtil.contains(requestTypes, requestType)) {
                    throw new PermissionException("User does not have access to this resource" + requestType + " is not support");
                }
            }
        } else {
            if (ArrayUtil.isEmpty(dataObjPermission.urls())) {
                return;
            }
            String url = request.getRequestURI();
            String[] permissionUrls = dataObjPermission.urls();
            boolean hasPermission = false;
            for (String permissionUrl : permissionUrls) {
                if (url.contains(permissionUrl)) {
                    hasPermission = true;
                    break;
                }
            }
            if (!hasPermission) {
                throw new PermissionException("User does not have access to this resource");
            }
        }
    }
}
