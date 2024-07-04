package com.wep.permission.utils;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginUser {
    private String userId;

    private String userName;

    private String userPhone;
    // 角色Id列表
    private List<String> roleIds;

    // 角色key列表
    private List<String> roleKeys;

    // 部门Id列表
    private List<String> deptIds;

    // 下级部门列表
    private List<String> subDeptIds;

    // 登录时间
    private String loginTime;
}
