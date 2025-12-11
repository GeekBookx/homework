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
}