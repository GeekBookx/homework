package com.lab.controller;

import com.lab.mapper.AnnouncementMapper; // 需要创建
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/announcements")
public class AnnouncementController {

    @Autowired
    private AnnouncementMapper announcementMapper;

    // 1. 获取当前生效的公告 (登录页使用)
    @GetMapping("/current")
    public Map<String, Object> getCurrentAnnouncement() {
        // 题目要求：展示固定的公告
        // SQL: SELECT * FROM announcements WHERE is_current = 1 LIMIT 1
        return announcementMapper.findCurrent();
    }

    // 2. 发布新公告 (管理员)
    @PostMapping("/publish")
    public String publishAnnouncement(@RequestBody Map<String, String> body) {
        String content = body.get("content");
        // 逻辑：先把所有公告设为历史状态，再插入新的当前公告
        announcementMapper.deactivateAll();
        announcementMapper.insert(content);
        return "公告发布成功";
    }
}
