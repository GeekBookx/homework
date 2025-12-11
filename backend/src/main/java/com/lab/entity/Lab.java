package com.lab.entity;

import lombok.Data;

@Data
public class Lab {
    private Long id;
    private String name;
    private Integer capacity;
    private String equipmentList;
    private Boolean isActive; // 对应数据库 is_active
    private String description;
}
