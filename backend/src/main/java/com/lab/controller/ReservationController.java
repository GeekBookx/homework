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

    // 1. 提交单次预约 (师生)
    @PostMapping("/create")
    public String createReservation(@RequestBody Reservation reservation) {
        reservationService.createReservation(reservation);
        return "预约申请提交成功，请等待审核";
    }

    // 2. 提交批量预约 (师生 - 题目亮点功能)
    @PostMapping("/batch")
    public String batchCreateReservation(@RequestBody List<Reservation> reservations) {
        reservationService.batchCreateReservation(reservations);
        return "批量预约申请提交成功";
    }

    // 3. 获取所有预约 (用于前端日历展示)
    @GetMapping("/list")
    public List<Reservation> getAllReservations() {
        // 注意：你需要去 ReservationMapper.java 补充 findAll() 方法
        // SQL: SELECT * FROM reservations
        return reservationMapper.findAll();
    }

    // 4. 获取待审核列表 (用于负责人审核页面)
    @GetMapping("/pending")
    public List<Reservation> getPendingReservations() {
        // 注意：你需要去 ReservationMapper.java 补充 findByStatus() 方法
        // SQL: SELECT r.*, u.full_name as username FROM reservations r JOIN users u ON r.user_id = u.id WHERE r.status = 'PENDING'
        return reservationMapper.findByStatus("PENDING");
    }

    // 5. 审核预约 (负责人)
    @PostMapping("/audit/{id}")
    public String auditReservation(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String status = body.get("status"); // APPROVED 或 REJECTED
        // 注意：你需要去 ReservationMapper.java 补充 updateStatus() 方法
        // SQL: UPDATE reservations SET status = #{status} WHERE id = #{id}
        reservationMapper.updateStatus(id, status);
        return "审核操作成功";
    }
}
