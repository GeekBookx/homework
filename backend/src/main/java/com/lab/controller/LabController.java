package com.lab.controller;

import com.lab.entity.Lab; // 需要你自己创建 Lab 实体类
import com.lab.mapper.LabMapper; // 需要你自己创建 LabMapper
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/labs")
public class LabController {

    @Autowired
    private LabMapper labMapper;

    // 1. 获取实验室列表 (用于预约页面下拉框 + 管理员列表)
    @GetMapping("/list")
    public List<Lab> getLabs() {
        // SQL: SELECT * FROM labs
        return labMapper.findAll();
    }

    // 2. 添加实验室 (管理员)
    @PostMapping("/add")
    public String addLab(@RequestBody Lab lab) {
        // SQL: INSERT INTO labs ...
        labMapper.insert(lab);
        return "实验室添加成功";
    }

    // 3. 删除实验室 (管理员)
    @DeleteMapping("/{id}")
    public String deleteLab(@PathVariable Long id) {
        // SQL: DELETE FROM labs WHERE id = #{id}
        labMapper.deleteById(id);
        return "实验室已删除";
    }

    // 4. 更新维护状态 (负责人/管理员)
    @PostMapping("/update/{id}")
    public String updateLabStatus(@PathVariable Long id, @RequestBody Lab lab) {
        // 题目要求：设置实验室维护期（暂停预约）
        // SQL: UPDATE labs SET is_active = #{isActive} WHERE id = #{id}
        labMapper.updateStatus(id, lab.getIsActive());
        return "状态更新成功";
    }
}
