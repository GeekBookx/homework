package com.lab.controller;

import com.lab.entity.User;
import com.lab.mapper.ReservationMapper;
import com.lab.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ReservationMapper reservationMapper;

    // 1. 获取待审核用户
    @GetMapping("/users/pending")
    public List<User> getPendingUsers() {
        return userMapper.findPendingUsers();
    }

    // 2. 批准用户
    @PostMapping("/users/approve/{id}")
    public String approveUser(@PathVariable Long id) {
        userMapper.approveUser(id);
        return "用户审核通过";
    }

    // 🔥 新增：拒绝用户
    @PostMapping("/users/reject/{id}")
    public String rejectUser(@PathVariable Long id) {
        userMapper.rejectUser(id);
        return "用户申请已拒绝";
    }
    
    // 3. 统计数据
    @GetMapping("/stats")
    public List<Map<String, Object>> getStats() {
        return reservationMapper.getLabStats(); 
    }
}
