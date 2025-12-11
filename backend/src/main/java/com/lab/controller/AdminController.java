package com.lab.controller;

import com.lab.entity.User;
import com.lab.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private UserMapper userMapper;

    // 1. 获取待审核的用户列表 (status = 0 的用户)
    @GetMapping("/users/pending")
    public List<User> getPendingUsers() {
        // 这里的 SQL 需要你在 UserMapper 中补充：
        // SELECT * FROM users WHERE status = 0
        return userMapper.findPendingUsers();
    }

    // 2. 批准用户注册
    @PostMapping("/users/approve/{id}")
    public String approveUser(@PathVariable Long id) {
        // SQL: UPDATE users SET status = 1 WHERE id = #{id}
        userMapper.approveUser(id);
        return "用户审核通过";
    }
    
    // 3. 获取统计数据 (题目要求：导出月度使用报告/查看频率统计图)
    // 这里简单返回 JSON 数据供前端 Chart 渲染
    @GetMapping("/stats")
    public List<Object> getStats() {
        // 需要写一个复杂的聚合 SQL，比如按实验室分组统计预约数量
        // SELECT lab_id, COUNT(*) as count FROM reservations GROUP BY lab_id
        return userMapper.getStats(); 
    }
}
