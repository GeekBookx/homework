package com.lab.mapper;

import com.lab.entity.Reservation;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface ReservationMapper {

    // 1. 核心冲突检测 SQL
    @Select("SELECT COUNT(*) FROM reservations " +
            "WHERE lab_id = #{labId} " +
            "AND status != 'REJECTED' " +
            "AND (start_time < #{endTime} AND end_time > #{startTime})")
    int countOverlapping(Long labId, LocalDateTime startTime, LocalDateTime endTime);

    // 2. 创建预约
    @Insert("INSERT INTO reservations(lab_id, user_id, start_time, end_time, status, reason, created_at) " +
            "VALUES(#{labId}, #{userId}, #{startTime}, #{endTime}, #{status}, #{reason}, #{createdAt})")
    void insert(Reservation reservation);

    // 3. 查询所有预约 (供日历显示)
    @Select("SELECT * FROM reservations")
    List<Reservation> findAll();

    // 4. 查询特定状态的预约 (供负责人审核列表用，关联查询出申请人姓名)
    // 注意：这里用了 LEFT JOIN 把 users 表的 full_name 查出来赋给 username 字段
    @Select("SELECT r.*, u.full_name as username " +
            "FROM reservations r " +
            "LEFT JOIN users u ON r.user_id = u.id " +
            "WHERE r.status = #{status}")
    List<Reservation> findByStatus(String status);

    // 5. 更新预约状态 (审核通过/驳回)
    @Update("UPDATE reservations SET status = #{status} WHERE id = #{id}")
    void updateStatus(Long id, String status);
}
