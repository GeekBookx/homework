package com.lab.mapper;

import com.lab.entity.User;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserMapper {
    @Select("SELECT * FROM users WHERE username = #{username}")
    User findByUsername(String username);

    @Insert("INSERT INTO users(username, password, role, full_name, status) " +
            "VALUES(#{username}, #{password}, #{role}, #{fullName}, #{status})")
    void insert(User user);
}