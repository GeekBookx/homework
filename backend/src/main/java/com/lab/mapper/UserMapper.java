package com.lab.mapper;

import com.lab.entity.User;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import java.util.List;
import java.util.Map;

@Mapper
public interface UserMapper {
    @Select("SELECT * FROM users WHERE username = #{username}")
    User findByUsername(String username);

    @Insert("INSERT INTO users(username, password, role, full_name, status) " +
            "VALUES(#{username}, #{password}, #{role}, #{fullName}, #{status})")
    void insert(User user);

    // --- 新增 Admin 方法 ---
    
    @Select("SELECT * FROM users WHERE status = 0")
    List<User> findPendingUsers();

    @Update("UPDATE users SET status = 1 WHERE id = #{id}")
    void approveUser(Long id);

    // 简单的统计数据：按角色统计人数 (示例)
    @Select("SELECT role, COUNT(*) as count FROM users GROUP BY role")
    List<Object> getStats(); 
}
