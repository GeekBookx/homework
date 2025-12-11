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
    private LabMapper labMapper; // 注入 LabMapper

    // 检查实验室是否可用
    private void checkLabAvailability(Long labId) {
        Lab lab = labMapper.findById(labId);
        if (lab == null) {
            throw new RuntimeException("实验室不存在");
        }
        if (!lab.getIsActive()) {
            throw new RuntimeException("该实验室处于维护期，暂停预约！");
        }
    }

    public boolean checkConflict(Long labId, LocalDateTime startTime, LocalDateTime endTime) {
        return reservationMapper.countOverlapping(labId, startTime, endTime) > 0;
    }

    @Transactional
    public void createReservation(Reservation res) {
        // 1. 检查维护期
        checkLabAvailability(res.getLabId());

        // 2. 冲突检测
        if (checkConflict(res.getLabId(), res.getStartTime(), res.getEndTime())) {
            throw new RuntimeException("该时间段已被预约，请选择其他时间！");
        }
        
        res.setStatus("PENDING");
        res.setCreatedAt(LocalDateTime.now());
        reservationMapper.insert(res);
    }

    @Transactional
    public void batchCreateReservation(List<Reservation> reservationList) {
        if (reservationList.isEmpty()) return;
        
        // 批量预约前检查一次维护期即可
        checkLabAvailability(reservationList.get(0).getLabId());

        for (Reservation res : reservationList) {
            if (checkConflict(res.getLabId(), res.getStartTime(), res.getEndTime())) {
                throw new RuntimeException("批量预约失败：时间 " + res.getStartTime() + " 存在冲突");
            }
            res.setStatus("PENDING");
            res.setCreatedAt(LocalDateTime.now());
            reservationMapper.insert(res);
        }
    }
}
