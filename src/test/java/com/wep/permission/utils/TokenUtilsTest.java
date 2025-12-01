package com.wep.permission.utils;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import static org.assertj.core.api.Assertions.assertThat;

class TokenUtilsTest {

    @AfterEach
    void tearDown() {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void shouldParseLoginUserFromHeaders() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("login_user_id", "42");
        request.addHeader("login_user_name", "tester");
        request.addHeader("login_user_phone", "1234567890");
        request.addHeader("login_user_role_ids", "1,2");
        request.addHeader("login_user_role_keys", "admin,user");
        request.addHeader("login_user_dept_ids", "A,B");
        request.addHeader("login_user_sub_dept_ids", "C");

        bindRequest(request);

        LoginUser loginUser = TokenUtils.getLoginUser();

        assertThat(loginUser.getUserId()).isEqualTo("42");
        assertThat(loginUser.getUserName()).isEqualTo("tester");
        assertThat(loginUser.getUserPhone()).isEqualTo("1234567890");
        assertThat(loginUser.getRoleIds()).containsExactly("1", "2");
        assertThat(loginUser.getRoleKeys()).containsExactly("admin", "user");
        assertThat(loginUser.getDeptIds()).containsExactly("A", "B");
        assertThat(loginUser.getSubDeptIds()).containsExactly("C");
    }

    @Test
    void shouldReturnEmptyListsWhenHeadersMissing() {
        bindRequest(new MockHttpServletRequest());

        assertThat(TokenUtils.getCurrentUserRoleIds()).isEmpty();
        assertThat(TokenUtils.getCurrentUserRoleKeys()).isEmpty();
        assertThat(TokenUtils.getCurrentUserDeptIds()).isEmpty();
        assertThat(TokenUtils.getCurrentUserSubDeptIds()).isEmpty();
    }

    private void bindRequest(HttpServletRequest request) {
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }
}
