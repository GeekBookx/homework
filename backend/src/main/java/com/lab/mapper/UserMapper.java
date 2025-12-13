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

    // --- Admin 方法 ---
    
    @Select("SELECT * FROM users WHERE status = 0")
    List<User> findPendingUsers();

    @Update("UPDATE users SET status = 1 WHERE id = #{id}")
    void approveUser(Long id);

    // 🔥 新增：拒绝用户（状态设为2）
    @Update("UPDATE users SET status = 2 WHERE id = #{id}")
    void rejectUser(Long id);

    @Select("SELECT role, COUNT(*) as count FROM users GROUP BY role")
    List<Object> getStats(); 
}
