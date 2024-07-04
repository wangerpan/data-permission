package com.wep.permission.utils;

import cn.hutool.core.util.StrUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.ArrayList;
import java.util.List;

public class TokenUtils {
    private final static String USER_ID = "login_user_id";
    private final static String USER_NAME = "login_user_name";
    private final static String USER_PHONE = "login_user_phone";
    private final static String USER_ROLE_IDS = "login_user_role_ids";
    private final static String USER_ROLE_KEYS = "login_user_role_keys";
    private final static String USER_DEPT_IDS = "login_user_dept_ids";
    private final static String USER_SUB_DEPT_IDS = "login_user_sub_dept_ids";

    /**
     * 获取当前登录人信息
     *
     * @return
     */
    public static LoginUser getLoginUser() {
        // 从header获取token标识
        return LoginUser.builder()
                .userId(getCurrentUserId())
                .userName(getCurrentUserName())
                .userPhone(getCurrentUserPhone())
                .roleKeys(getCurrentUserRoleKeys())
                .roleIds(getCurrentUserRoleIds())
                .deptIds(getCurrentUserDeptIds())
                .subDeptIds(getCurrentUserSubDeptIds())
                .build();
    }

    /**
     * 查询当前用户的手机号
     *
     * @return
     */
    public static String getCurrentUserPhone() {
        HttpServletRequest request = getCurrentRequest();
        // 从header获取token标识
        return request.getHeader(USER_PHONE);
    }

    /**
     * 查询当前用户的姓名
     *
     * @return
     */
    public static String getCurrentUserName() {
        HttpServletRequest request = getCurrentRequest();
        // 从header获取token标识
        return request.getHeader(USER_NAME);
    }

    /**
     * 查询当前用户的id
     *
     * @return
     */
    public static String getCurrentUserId() {
        HttpServletRequest request = getCurrentRequest();
        // 从header获取token标识
        return request.getHeader(USER_ID);
    }

    /**
     * 查询当前用户的角色id列表
     *
     * @return
     */
    public static List<String> getCurrentUserRoleIds() {
        HttpServletRequest request = getCurrentRequest();
        String roleIds = request.getHeader(USER_ROLE_IDS);
        if (StrUtil.isEmpty(roleIds)) {
            return new ArrayList<>();
        }
        // 从header获取角色id列表
        return StrUtil.split(roleIds, StrUtil.COMMA);
    }

    /**
     * 查询当前用户的角色key列表
     *
     * @return
     */
    public static List<String> getCurrentUserRoleKeys() {
        HttpServletRequest request = getCurrentRequest();
        String roleIds = request.getHeader(USER_ROLE_KEYS);
        if (StrUtil.isEmpty(roleIds)) {
            return new ArrayList<>();
        }
        // 从header获取角色key列表
        return StrUtil.split(roleIds, StrUtil.COMMA);
    }


    /**
     * 获取当前登录人所在部门ids
     *
     * @return
     */
    public static List<String> getCurrentUserDeptIds() {
        HttpServletRequest request = getCurrentRequest();
        String deptIds = request.getHeader(USER_DEPT_IDS);
        if (StrUtil.isEmpty(deptIds)) {
            return new ArrayList<>();
        }
        // 从header获取部门d列表
        return StrUtil.split(deptIds, StrUtil.COMMA);
    }

    /**
     * 获取当前登录人的下级部门ids
     *
     * @return
     */
    public static List<String> getCurrentUserSubDeptIds() {
        HttpServletRequest request = getCurrentRequest();
        String deptIds = request.getHeader(USER_SUB_DEPT_IDS);
        if (StrUtil.isEmpty(deptIds)) {
            return new ArrayList<>();
        }
        // 从header获取部门d列表
        return StrUtil.split(deptIds, StrUtil.COMMA);
    }


    /**
     * 获取当前登录的request
     *
     * @return
     */
    private static HttpServletRequest getCurrentRequest() {
        RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();
        return ((ServletRequestAttributes) requestAttributes).getRequest();
    }
}
