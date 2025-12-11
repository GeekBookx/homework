package com.lab.mapper;

import com.lab.entity.Lab;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface LabMapper {
    @Select("SELECT * FROM labs")
    List<Lab> findAll();

    @Insert("INSERT INTO labs(name, capacity, equipment_list, is_active, description) " +
            "VALUES(#{name}, #{capacity}, #{equipmentList}, #{isActive}, #{description})")
    void insert(Lab lab);

    @Delete("DELETE FROM labs WHERE id = #{id}")
    void deleteById(Long id);

    @Update("UPDATE labs SET is_active = #{isActive} WHERE id = #{id}")
    void updateStatus(Long id, Boolean isActive);
}
