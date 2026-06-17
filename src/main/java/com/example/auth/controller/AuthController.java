package com.example.auth.controller;

import com.example.auth.annotation.RateLimit;
import com.example.auth.common.Result;
import com.example.auth.dto.LoginRequest;
import com.example.auth.dto.LoginUser;
import com.example.auth.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    @RateLimit(max = 10, timeWindow = 60)
    public Result<LoginUser> login(@RequestBody LoginRequest request) {
        try {
            LoginUser user = authService.login(request);
            return Result.ok(user);
        } catch (Exception e) {
            // 登录失败返回400，避免被前端401拦截器误判为token过期
            return Result.fail(400, e.getMessage());
        }
    }
}
