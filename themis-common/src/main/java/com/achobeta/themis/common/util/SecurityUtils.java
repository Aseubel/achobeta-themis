package com.achobeta.themis.common.util;

import com.achobeta.themis.common.exception.AuthenticationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * 安全工具类
 */
public class SecurityUtils {
    
    /**
     * 获取当前用户ID
     * @return 用户ID
     * @throws AuthenticationException 如果用户未登录
     */
    public static Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AuthenticationException("未登录或token无效，禁止访问！");
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof Long) {
            return (Long) principal;
        }
        throw new AuthenticationException("无法获取用户信息");
    }
}

