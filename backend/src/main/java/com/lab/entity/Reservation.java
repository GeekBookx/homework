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
    private String status; 
    private String reason;
    private LocalDateTime createdAt;

    // --- 非数据库字段 (用于关联查询展示) ---
    private String username; // 申请人姓名
    private String labName;  // 实验室名称
}
