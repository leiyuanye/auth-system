package com.example.auth.dto;

import lombok.Data;
import java.util.List;

@Data
public class LoginUser {
    private Long userId;
    private String username;
    private String realName;
    private List<String> roles;
    private List<String> permissions;
    private String token;
}
