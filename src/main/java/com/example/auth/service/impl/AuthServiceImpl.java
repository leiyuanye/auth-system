package com.example.auth.service.impl;

import com.example.auth.dto.LoginRequest;
import com.example.auth.dto.LoginUser;
import com.example.auth.entity.SysUser;
import com.example.auth.mapper.SysMenuMapper;
import com.example.auth.mapper.SysRoleMapper;
import com.example.auth.mapper.SysUserMapper;
import com.example.auth.service.AuthService;
import com.example.auth.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private SysUserMapper userMapper;

    @Autowired
    private SysRoleMapper roleMapper;

    @Autowired
    private SysMenuMapper menuMapper;

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public LoginUser login(LoginRequest request) {
        if (request == null || request.getUsername() == null || request.getPassword() == null) {
            throw new RuntimeException("用户名或密码不能为空");
        }

        SysUser user = userMapper.selectByUsername(request.getUsername().trim());
        if (user == null) {
            throw new RuntimeException("用户不存在或已禁用");
        }

        // MD5加密比对
        String md5Password = DigestUtils.md5DigestAsHex(request.getPassword().getBytes(StandardCharsets.UTF_8));
        if (!md5Password.equals(user.getPassword())) {
            throw new RuntimeException("密码错误");
        }

        // 查询角色编码列表
        List<String> roles = roleMapper.selectRoleCodesByUserId(user.getId());
        // 查询权限编码列表
        List<String> permissions = menuMapper.selectPermCodesByUserId(user.getId());

        // 生成token
        String token = jwtUtil.generateToken(user.getId(), user.getUsername());

        LoginUser loginUser = new LoginUser();
        loginUser.setUserId(user.getId());
        loginUser.setUsername(user.getUsername());
        loginUser.setRealName(user.getRealName());
        loginUser.setRoles(roles);
        loginUser.setPermissions(permissions);
        loginUser.setToken(token);

        return loginUser;
    }
}
