package com.achobeta.themis.interceptor;
/**
 * <p>
 * 描述：具体登录检查切面类
 * </p>
 *
 * @Author: ZGjie20
 * @version: 1.0.0
 */
import com.achobeta.themis.common.annotation.LoginRequired;
import com.achobeta.themis.common.exception.UnauthorizedException;
import com.achobeta.themis.common.util.JwtUtil;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Aspect
@Component
public class LoginCheckAspect {
    @Resource
    private JwtUtil jwtUtil;

    @Around("@annotation(com.achobeta.themis.common.annotation.LoginRequired) || @within(com.achobeta.themis.common.annotation.LoginRequired)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attrs != null ? attrs.getRequest() : null;
        HttpServletResponse response = attrs != null ? attrs.getResponse() : null;
        if (request == null) {
            throw new UnauthorizedException("无法获取请求对象，禁止访问！");
        }
        String accessToken = request.getHeader("Authorization");
        if (accessToken != null && accessToken.startsWith("Bearer ")) {
            accessToken = accessToken.substring(7);
        }
        boolean accessTokenValid = accessToken != null && jwtUtil.validateToken(accessToken) && jwtUtil.isAccessToken(accessToken);
        if (accessTokenValid) {
            return joinPoint.proceed();
        }
        // accessToken过期/无效，检测refreshToken
        String refreshToken = request.getHeader("refresh-token");
        if (refreshToken == null || refreshToken.isEmpty()) {
            throw new UnauthorizedException("登录凭证已失效，请重新登录！");
        }
        if (!jwtUtil.validateToken(refreshToken) || !jwtUtil.isRefreshToken(refreshToken)) {
            throw new UnauthorizedException("刷新凭证已失效，请重新登录！");
        }
        // 生成新accessToken并下发
        Long userId = jwtUtil.getUserIdFromToken(refreshToken);
        String username = jwtUtil.getUsernameFromToken(refreshToken);
        String newAccessToken = jwtUtil.generateAccessToken(userId, username);
        if (response != null) {
            response.setHeader("access-token", newAccessToken);
        }

        return joinPoint.proceed();
    }
}

