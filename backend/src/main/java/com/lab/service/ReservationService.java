package com.lab.service;

import com.lab.entity.Lab;
import com.lab.entity.Reservation;
import com.lab.mapper.LabMapper;
import com.lab.mapper.ReservationMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ReservationService {

    @Autowired
    private ReservationMapper reservationMapper;
    
    @Autowired
    private LabMapper labMapper;

    // 核心逻辑：检查实验室容量与状态
    private void checkLabAvailability(Long labId, LocalDateTime start, LocalDateTime end) {
        Lab lab = labMapper.findById(labId);
        
        // 1. 基础检查
        if (lab == null) {
            throw new RuntimeException("实验室不存在");
        }
        if (!lab.getIsActive()) {
            throw new RuntimeException("该实验室处于维护期，暂停预约！");
        }

        // 2. 容量检查 (关键修复)
        // 查询该时间段已有的有效预约数量
        int currentBookings = reservationMapper.countOverlapping(labId, start, end);
        
        // 如果已预约人数 >= 实验室容量，则禁止预约
        if (currentBookings >= lab.getCapacity()) {
            throw new RuntimeException("预约失败：该时段实验室已满 (容量: " + lab.getCapacity() + ", 已约: " + currentBookings + ")");
        }
    }

    @Transactional
    public void createReservation(Reservation res) {
        // 执行检查
        checkLabAvailability(res.getLabId(), res.getStartTime(), res.getEndTime());
        
        res.setStatus("PENDING");
        res.setCreatedAt(LocalDateTime.now());
        reservationMapper.insert(res);
    }

    @Transactional
    public void batchCreateReservation(List<Reservation> reservationList) {
        if (reservationList.isEmpty()) return;
        
        for (Reservation res : reservationList) {
            // 对每个时间段都独立检查容量
            checkLabAvailability(res.getLabId(), res.getStartTime(), res.getEndTime());
            
            res.setStatus("PENDING");
            res.setCreatedAt(LocalDateTime.now());
            reservationMapper.insert(res);
        }
    }
}
