package com.example.auth.config;

import cn.hutool.json.JSONUtil;
import com.example.auth.common.Result;
import com.example.auth.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;

@Component
public class JwtInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || authHeader.isEmpty()) {
            writeUnauthorized(response, "未提供token");
            return false;
        }
        String token = authHeader.startsWith("Bearer ") ? authHeader.substring(7) : authHeader;
        Claims claims = jwtUtil.parseToken(token);
        if (claims == null) {
            writeUnauthorized(response, "token无效或已过期");
            return false;
        }
        Object userId = claims.get("userId");
        if (userId instanceof Number) {
            request.setAttribute("userId", ((Number) userId).longValue());
        }
        Object username = claims.get("username");
        if (username instanceof String) {
            request.setAttribute("username", username);
        }
        return true;
    }

    private void writeUnauthorized(HttpServletResponse response, String message) throws Exception {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter writer = response.getWriter();
        writer.write(JSONUtil.toJsonStr(Result.fail(401, message)));
        writer.flush();
        writer.close();
    }
}
