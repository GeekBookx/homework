package com.lab.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class User {
    private Long id;
    private String username;
    private String password;
    private String role; // STUDENT, TEACHER, MANAGER, ADMIN
    private String fullName;
    private Integer status; // 0-审核中, 1-正常
    private LocalDateTime createdAt;
}