package com.lab.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Reservation {
    private Long id;
    private Long labId;
    private Long userId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String status; // PENDING, APPROVED, REJECTED
    private String reason;
    private LocalDateTime createdAt;

    // --- 新增字段 ---
    // 这个字段在数据库 reservations 表里不存在
    // 但 MyBatis 执行关联查询时，会自动把 users 表的 full_name 映射到这里
    private String username; 
}
