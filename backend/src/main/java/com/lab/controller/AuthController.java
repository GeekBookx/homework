package com.lab.controller;

import com.lab.entity.User;
import com.lab.mapper.UserMapper; // 需要你自己创建对应的Mapper接口
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class AuthController {

    @Autowired
    private UserMapper userMapper;

    @PostMapping("/login")
    public User login(@RequestBody User loginReq) {
        User user = userMapper.findByUsername(loginReq.getUsername());
        
        if (user == null || !user.getPassword().equals(loginReq.getPassword())) {
            throw new RuntimeException("用户名或密码错误");
        }
        
        // 核心题目要求：状态校验
        if (user.getStatus() == 0) {
            throw new RuntimeException("账号正在审核中，请联系管理员");
        }
        
        if (user.getStatus() == 2) {
            throw new RuntimeException("账号已被禁用");
        }

        return user; // 返回用户信息给前端保存
    }

    @PostMapping("/register")
    public String register(@RequestBody User user) {
        // 题目要求：负责人(MANAGER)注册默认待审核(0)，其他人默认激活(1)
        if ("MANAGER".equals(user.getRole())) {
            user.setStatus(0);
        } else {
            user.setStatus(1);
        }
        
        userMapper.insert(user);
        
        return "MANAGER".equals(user.getRole()) ? "注册成功，等待管理员审核" : "注册成功";
    }
}