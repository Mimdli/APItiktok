package com.tiktok.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tiktok.common.BusinessException;
import com.tiktok.dto.LoginRequest;
import com.tiktok.dto.LoginResponse;
import com.tiktok.dto.RegisterRequest;
import com.tiktok.entity.User;
import com.tiktok.mapper.UserMapper;
import com.tiktok.utils.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private JwtUtils jwtUtils;

    public LoginResponse register(RegisterRequest request) {
        validateCredentials(request.getUsername(), request.getPassword());

        Long count = userMapper.selectCount(
                new LambdaQueryWrapper<User>().eq(User::getUsername, request.getUsername())
        );
        if (count > 0) {
            throw new BusinessException("用户名已存在");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(request.getPassword());
        userMapper.insert(user);

        return buildLoginResponse(user);
    }

    public LoginResponse login(LoginRequest request) {
        if (!StringUtils.hasText(request.getUsername()) || !StringUtils.hasText(request.getPassword())) {
            throw new BusinessException("用户名和密码不能为空");
        }

        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, request.getUsername())
        );
        if (user == null || !request.getPassword().equals(user.getPassword())) {
            throw new BusinessException(401, "用户名或密码错误");
        }

        return buildLoginResponse(user);
    }

    private LoginResponse buildLoginResponse(User user) {
        String token = jwtUtils.generateToken(user.getId());
        return new LoginResponse(token, user.getId(), user.getUsername());
    }

    private void validateCredentials(String username, String password) {
        if (!StringUtils.hasText(username) || !StringUtils.hasText(password)) {
            throw new BusinessException("用户名和密码不能为空");
        }
        if (username.length() > 32) {
            throw new BusinessException("用户名长度不能超过32个字符");
        }
        if (password.length() > 100) {
            throw new BusinessException("密码长度不能超过100个字符");
        }
    }
}
