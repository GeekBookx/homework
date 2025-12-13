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

    // 1. 统计某时段某实验室的已预约人数 (用于容量判断)
    @Select("SELECT COUNT(*) FROM reservations " +
            "WHERE lab_id = #{labId} " +
            "AND status != 'REJECTED' " +
            "AND (start_time < #{endTime} AND end_time > #{startTime})")
    int countOverlapping(Long labId, LocalDateTime startTime, LocalDateTime endTime);

    // 2. 创建预约
    @Insert("INSERT INTO reservations(lab_id, user_id, start_time, end_time, status, reason, created_at) " +
            "VALUES(#{labId}, #{userId}, #{startTime}, #{endTime}, #{status}, #{reason}, #{createdAt})")
    void insert(Reservation reservation);

    // 3. 查询所有 (关联实验室名称)
    @Select("SELECT r.*, l.name as labName, u.full_name as username " +
            "FROM reservations r " +
            "LEFT JOIN labs l ON r.lab_id = l.id " +
            "LEFT JOIN users u ON r.user_id = u.id")
    List<Reservation> findAll();

    // 4. [新增] 查询某个用户的预约记录 (学生端用)
    @Select("SELECT r.*, l.name as labName " +
            "FROM reservations r " +
            "LEFT JOIN labs l ON r.lab_id = l.id " +
            "WHERE r.user_id = #{userId} " +
            "ORDER BY r.start_time DESC")
    List<Reservation> findByUserId(Long userId);

    // 5. 待审核列表 (负责人用)
    @Select("SELECT r.*, u.full_name as username, l.name as labName " +
            "FROM reservations r " +
            "LEFT JOIN users u ON r.user_id = u.id " +
            "LEFT JOIN labs l ON r.lab_id = l.id " +
            "WHERE r.status = #{status}")
    List<Reservation> findByStatus(String status);

    // 6. 更新状态
    @Update("UPDATE reservations SET status = #{status} WHERE id = #{id}")
    void updateStatus(Long id, String status);

    // 7. 统计图表数据
    @Select("SELECT l.name, COUNT(r.id) as count " +
            "FROM labs l " +
            "LEFT JOIN reservations r ON l.id = r.lab_id " +
            "GROUP BY l.id, l.name")
    List<Map<String, Object>> getLabStats();
}
