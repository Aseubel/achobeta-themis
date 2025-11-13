package com.achobeta.themis.common.util;

import com.achobeta.themis.common.exception.AuthenticationException;
import com.achobeta.themis.common.exception.BusinessException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 安全工具类
 */

public class SecurityUtils {
    
    /**
     * 获取当前用户ID
     * @return 用户ID
     * @throws AuthenticationException 如果用户未登录
     */
    public static String  getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        // 校验是否是当前用户在操作
        String userId = (String) ((Map<String, Object>) authentication.getPrincipal()).get("userId");

        return  userId;

    }
}

