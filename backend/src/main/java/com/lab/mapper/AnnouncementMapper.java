package com.lab.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import java.util.Map;

@Mapper
public interface AnnouncementMapper {
    // 获取当前公告
    @Select("SELECT content FROM announcements WHERE is_current = 1 ORDER BY created_at DESC LIMIT 1")
    Map<String, Object> findCurrent();

    // 把旧公告都设为非当前
    @Update("UPDATE announcements SET is_current = 0 WHERE is_current = 1")
    void deactivateAll();

    // 插入新公告
    @Insert("INSERT INTO announcements(content, is_current) VALUES(#{content}, 1)")
    void insert(String content);
}
