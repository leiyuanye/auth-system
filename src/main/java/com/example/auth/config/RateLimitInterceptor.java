package com.example.auth.config;

import com.example.auth.annotation.RateLimit;
import com.example.auth.common.Result;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class RateLimitInterceptor implements HandlerInterceptor {

    private final ConcurrentHashMap<String, RateLimitInfo> rateLimitMap = new ConcurrentHashMap<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        HandlerMethod handlerMethod = (HandlerMethod) handler;
        RateLimit rateLimit = handlerMethod.getMethodAnnotation(RateLimit.class);

        if (rateLimit == null) {
            return true;
        }

        String key = getKey(request);
        RateLimitInfo info = rateLimitMap.computeIfAbsent(key, k -> new RateLimitInfo());

        synchronized (info) {
            long now = System.currentTimeMillis();
            if (now - info.lastRequestTime > rateLimit.timeWindow() * 1000) {
                info.count.set(1);
                info.lastRequestTime = now;
            } else {
                if (info.count.get() >= rateLimit.max()) {
                    sendErrorResponse(response, "请求过于频繁，请稍后再试");
                    return false;
                }
                info.count.incrementAndGet();
                info.lastRequestTime = now;
            }
        }

        return true;
    }

    private String getKey(HttpServletRequest request) {
        String ip = request.getRemoteAddr();
        String uri = request.getRequestURI();
        return ip + ":" + uri;
    }

    private void sendErrorResponse(HttpServletResponse response, String message) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(429);
        try (PrintWriter writer = response.getWriter()) {
            ObjectMapper mapper = new ObjectMapper();
            writer.write(mapper.writeValueAsString(Result.fail(message)));
        }
    }

    private static class RateLimitInfo {
        AtomicInteger count = new AtomicInteger(0);
        long lastRequestTime = 0;
    }
}