package com.tiktok.backend.common.interceptor;

import com.tiktok.backend.common.utils.JwtUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtUtils jwtUtils;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 放行 OPTIONS 请求
        if ("OPTIONS".equals(request.getMethod())) {
            return true;
        }

        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            try {
                Long userId = jwtUtils.getUserIdFromToken(token);
                // 将 userId 存入 request 方便后续使用
                request.setAttribute("userId", userId);
                return true;
            } catch (Exception e) {
                response.setStatus(401);
                response.getWriter().write("Invalid Token");
                return false;
            }
        }

        response.setStatus(401);
        response.getWriter().write("No Token Provided");
        return false;
    }
}
