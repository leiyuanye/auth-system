package com.example.auth.service;

import com.example.auth.dto.LoginRequest;
import com.example.auth.dto.LoginUser;

public interface AuthService {
    LoginUser login(LoginRequest request);
}
