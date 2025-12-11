package com.lab.controller;

import com.lab.entity.User;
import com.lab.mapper.ReservationMapper; // 引入这个
import com.lab.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map; // 引入 Map

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ReservationMapper reservationMapper; // 注入 ReservationMapper

    // 1. 获取待审核的用户列表 (status = 0 的用户)
    @GetMapping("/users/pending")
    public List<User> getPendingUsers() {
        return userMapper.findPendingUsers();
    }

    // 2. 批准用户注册
    @PostMapping("/users/approve/{id}")
    public String approveUser(@PathVariable Long id) {
        userMapper.approveUser(id);
        return "用户审核通过";
    }
    
    // 3. [修正] 获取真实的实验室使用频率统计
    // 题目要求：查看使用频率统计图
    @GetMapping("/stats")
    public List<Map<String, Object>> getStats() {
        // 调用 ReservationMapper 中写好的 getLabStats 方法
        // 返回格式例如：[{name: "化学实验室", count: 15}, {name: "物理实验室", count: 8}]
        return reservationMapper.getLabStats(); 
    }
}
