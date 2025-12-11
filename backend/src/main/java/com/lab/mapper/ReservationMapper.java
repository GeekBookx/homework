package com.lab.mapper;

import com.lab.entity.Reservation;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface ReservationMapper {

    // 1. 核心冲突检测
    @Select("SELECT COUNT(*) FROM reservations " +
            "WHERE lab_id = #{labId} " +
            "AND status != 'REJECTED' " +
            "AND (start_time < #{endTime} AND end_time > #{startTime})")
    int countOverlapping(Long labId, LocalDateTime startTime, LocalDateTime endTime);

    // 2. 创建预约
    @Insert("INSERT INTO reservations(lab_id, user_id, start_time, end_time, status, reason, created_at) " +
            "VALUES(#{labId}, #{userId}, #{startTime}, #{endTime}, #{status}, #{reason}, #{createdAt})")
    void insert(Reservation reservation);

    // 3. 查询所有 (关联实验室名称，供日历显示详细信息)
    @Select("SELECT r.*, l.name as labName " +
            "FROM reservations r " +
            "LEFT JOIN labs l ON r.lab_id = l.id")
    List<Reservation> findAll();

    // 4. 待审核列表 (关联用户和实验室)
    @Select("SELECT r.*, u.full_name as username, l.name as labName " +
            "FROM reservations r " +
            "LEFT JOIN users u ON r.user_id = u.id " +
            "LEFT JOIN labs l ON r.lab_id = l.id " +
            "WHERE r.status = #{status}")
    List<Reservation> findByStatus(String status);

    // 5. 更新状态
    @Update("UPDATE reservations SET status = #{status} WHERE id = #{id}")
    void updateStatus(Long id, String status);

    // 6. [统计图表] 统计每个实验室的预约数量
    @Select("SELECT l.name, COUNT(r.id) as count " +
            "FROM labs l " +
            "LEFT JOIN reservations r ON l.id = r.lab_id " +
            "GROUP BY l.id, l.name")
    List<Map<String, Object>> getLabStats();
}
