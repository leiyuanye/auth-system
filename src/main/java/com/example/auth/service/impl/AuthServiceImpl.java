package com.example.auth.service.impl;

import com.example.auth.dto.LoginRequest;
import com.example.auth.dto.LoginUser;
import com.example.auth.entity.SysUser;
import com.example.auth.mapper.SysMenuMapper;
import com.example.auth.mapper.SysRoleMapper;
import com.example.auth.mapper.SysUserMapper;
import com.example.auth.service.AuthService;
import com.example.auth.utils.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.List;

@Service
public class AuthServiceImpl implements AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);

    @Autowired
    private SysUserMapper userMapper;

    @Autowired
    private SysRoleMapper roleMapper;

    @Autowired
    private SysMenuMapper menuMapper;

    @Autowired
    private JwtUtil jwtUtil;

    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public LoginUser login(LoginRequest request) {
        log.info("========== 登录请求开始 ==========");
        log.info("登录请求 - IP来源记录中...");

        // 参数校验
        if (request == null) {
            log.warn("登录失败：请求对象为空");
            throw new RuntimeException("请求参数无效");
        }

        String username = request.getUsername();
        String password = request.getPassword();

        if (username == null || username.trim().isEmpty()) {
            log.warn("登录失败：用户名为空");
            throw new RuntimeException("用户名不能为空");
        }

        if (password == null || password.isEmpty()) {
            log.warn("登录失败：密码为空，用户名={}", username);
            throw new RuntimeException("密码不能为空");
        }

        username = username.trim();
        log.info("登录尝试 - 用户名: {}", username);

        // 查询用户
        SysUser user = userMapper.selectByUsername(username);
        if (user == null) {
            log.warn("登录失败：用户不存在，用户名={}", username);
            throw new RuntimeException("用户不存在或已禁用");
        }

        log.info("用户存在 - userId={}, username={}, status={}", user.getId(), user.getUsername(), user.getStatus());

        // BCrypt密码比对
        boolean passwordMatch = passwordEncoder.matches(password, user.getPassword());

        log.info("密码校验 - 匹配结果={}", passwordMatch);

        if (!passwordMatch) {
            log.warn("登录失败：密码错误，用户名={}", username);
            throw new RuntimeException("密码错误");
        }

        // 检查用户状态
        if (user.getStatus() != 1) {
            log.warn("登录失败：用户状态异常，用户名={}, status={}", username, user.getStatus());
            throw new RuntimeException("用户已被禁用，请联系管理员");
        }

        // 查询角色编码列表
        List<String> roles = roleMapper.selectRoleCodesByUserId(user.getId());
        log.info("用户角色 - userId={}, roles={}", user.getId(), roles);

        // 查询权限编码列表
        List<String> permissions = menuMapper.selectPermCodesByUserId(user.getId());
        log.info("用户权限 - userId={}, permissions数量={}", user.getId(), permissions.size());

        // 生成token
        String token = jwtUtil.generateToken(user.getId(), user.getUsername());
        log.info("Token生成 - userId={}, token前缀={}...", user.getId(), token.substring(0, Math.min(20, token.length())));

        LoginUser loginUser = new LoginUser();
        loginUser.setUserId(user.getId());
        loginUser.setUsername(user.getUsername());
        loginUser.setRealName(user.getRealName());
        loginUser.setRoles(roles);
        loginUser.setPermissions(permissions);
        loginUser.setToken(token);

        log.info("========== 登录成功 ==========");
        log.info("userId={}, username={}, realName={}", user.getId(), user.getUsername(), user.getRealName());

        return loginUser;
    }
}
