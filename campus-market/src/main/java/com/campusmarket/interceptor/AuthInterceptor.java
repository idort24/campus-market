package com.campusmarket.interceptor;

import com.campusmarket.common.R;
import com.campusmarket.service.RbacService;
import com.campusmarket.utils.JwtUtil;
import com.campusmarket.utils.UserContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private RbacService rbacService;

    // 管理端接口要求的管理类权限，命中任一即放行
    private static final Set<String> ADMIN_PERMS = new HashSet<>(Arrays.asList(
            "user:manage", "user:status", "goods:audit", "report:handle", "category:manage", "dict:manage"
    ));

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 1. 校验登录 token
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            writeJson(response, 401, "登录失效，请重新登录");
            return false;
        }
        String token = authHeader.substring(7);
        if (JwtUtil.isTokenInvalid(token)) {
            writeJson(response, 401, "登录失效，请重新登录");
            return false;
        }
        Long userId = JwtUtil.getUserIdByToken(token);
        UserContext.setUserId(userId);

        // 2. 查询当前用户权限并缓存（供 /user/permissions 与前端按钮显隐）
        Set<String> perms = rbacService.getUserPermCodes(userId);
        UserContext.setPerms(perms);

        // 3. 管理端接口按权限鉴权（后台角色/权限/用户管理类接口）
        String uri = request.getRequestURI();
        boolean adminApi = uri.contains("/api/v1/admin/")
                || uri.startsWith("/api/v1/roles")
                || uri.startsWith("/api/v1/permissions")
                || uri.matches("/api/v1/users/\\d+/roles");
        if (adminApi) {
            boolean hasAdminPerm = perms.stream().anyMatch(ADMIN_PERMS::contains);
            if (!hasAdminPerm) {
                writeJson(response, 403, "无权限访问");
                return false;
            }
        }
        return true;
    }

    private void writeJson(HttpServletResponse response, int code, String msg) throws Exception {
        response.setContentType("application/json;charset=utf-8");
        response.getWriter().write(objectMapper.writeValueAsString(R.error(code, msg)));
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        UserContext.remove();
    }
}
