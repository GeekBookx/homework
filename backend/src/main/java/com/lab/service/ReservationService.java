package com.lab.service;

import com.lab.entity.Reservation;
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

    /**
     * 核心算法：检测时间冲突
     * 逻辑：如果 (新开始时间 < 现有结束时间) 且 (新结束时间 > 现有开始时间)，则视为冲突。
     * 排除状态为 'REJECTED' (已驳回) 的记录。
     */
    public boolean checkConflict(Long labId, LocalDateTime startTime, LocalDateTime endTime) {
        int count = reservationMapper.countOverlapping(labId, startTime, endTime);
        return count > 0;
    }

    /**
     * 创建单次预约
     */
    @Transactional
    public void createReservation(Reservation res) {
        // 1. 冲突检测
        if (checkConflict(res.getLabId(), res.getStartTime(), res.getEndTime())) {
            throw new RuntimeException("该时间段已被预约，请选择其他时间！");
        }
        
        // 2. 设置初始状态
        res.setStatus("PENDING"); // 默认待审核
        res.setCreatedAt(LocalDateTime.now());
        
        // 3. 写入数据库
        reservationMapper.insert(res);
    }

    /**
     * 批量预约 (题目亮点要求)
     * 场景：老师预约接下来5周的同一个时间段
     */
    @Transactional
    public void batchCreateReservation(List<Reservation> reservationList) {
        for (Reservation res : reservationList) {
            // 对每一条记录都进行冲突检测
            if (checkConflict(res.getLabId(), res.getStartTime(), res.getEndTime())) {
                throw new RuntimeException("批量预约失败：时间 " + res.getStartTime() + " 存在冲突，所有操作已回滚。");
            }
            
            res.setStatus("PENDING");
            res.setCreatedAt(LocalDateTime.now());
            reservationMapper.insert(res);
        }
    }
}