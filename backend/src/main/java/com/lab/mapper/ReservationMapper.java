package com.lab.mapper;
import com.lab.entity.Reservation;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import java.time.LocalDateTime;

@Mapper
public interface ReservationMapper {
    // 核心冲突检测 SQL
    @Select("SELECT COUNT(*) FROM reservations " +
            "WHERE lab_id = #{labId} " +
            "AND status != 'REJECTED' " +
            "AND (start_time < #{endTime} AND end_time > #{startTime})")
    int countOverlapping(Long labId, LocalDateTime startTime, LocalDateTime endTime);

    @Insert("INSERT INTO reservations(lab_id, user_id, start_time, end_time, status, reason, created_at) " +
            "VALUES(#{labId}, #{userId}, #{startTime}, #{endTime}, #{status}, #{reason}, #{createdAt})")
    void insert(Reservation reservation);
}