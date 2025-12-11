package com.lab.controller;

import com.lab.entity.Reservation;
import com.lab.mapper.ReservationMapper;
import com.lab.service.ReservationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reservations")
public class ReservationController {

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private ReservationMapper reservationMapper;

    // 1. 提交单次预约
    @PostMapping("/create")
    public String createReservation(@RequestBody Reservation reservation) {
        reservationService.createReservation(reservation);
        return "预约申请提交成功，请等待审核";
    }

    // 2. 提交批量预约
    @PostMapping("/batch")
    public String batchCreateReservation(@RequestBody List<Reservation> reservations) {
        reservationService.batchCreateReservation(reservations);
        return "批量预约申请提交成功";
    }

    // 3. 获取所有预约 (日历用)
    @GetMapping("/list")
    public List<Reservation> getAllReservations() {
        return reservationMapper.findAll();
    }

    // 4. [新增] 获取当前登录用户的预约记录 (学生个人中心)
    @GetMapping("/my")
    public List<Reservation> getMyReservations(@RequestParam Long userId) {
        return reservationMapper.findByUserId(userId);
    }

    // 5. 获取待审核列表
    @GetMapping("/pending")
    public List<Reservation> getPendingReservations() {
        return reservationMapper.findByStatus("PENDING");
    }

    // 6. 审核预约
    @PostMapping("/audit/{id}")
    public String auditReservation(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String status = body.get("status");
        reservationMapper.updateStatus(id, status);
        return "审核操作成功";
    }
}
