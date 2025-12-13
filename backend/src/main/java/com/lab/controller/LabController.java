package com.lab.controller;

import com.lab.entity.Lab;
import com.lab.mapper.LabMapper;
import com.lab.mapper.ReservationMapper; // 引入 ReservationMapper
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/labs")
public class LabController {

    @Autowired
    private LabMapper labMapper;

    @Autowired
    private ReservationMapper reservationMapper; // 注入预约Mapper用于统计

    // 1. 获取所有实验室列表 (基础接口)
    @GetMapping("/list")
    public List<Lab> getLabs() {
        return labMapper.findAll();
    }

    // 2. [新增] 获取某时段实验室可用容量
    @GetMapping("/available")
    public List<Map<String, Object>> getAvailableLabs(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        
        List<Lab> allLabs = labMapper.findAll();
        List<Map<String, Object>> result = new ArrayList<>();

        for (Lab lab : allLabs) {
            // 查询该时间段已占用的数量
            int used = reservationMapper.countOverlapping(lab.getId(), start, end);
            // 计算剩余容量 (不小于0)
            int remaining = Math.max(0, lab.getCapacity() - used);

            Map<String, Object> map = new HashMap<>();
            map.put("id", lab.getId());
            map.put("name", lab.getName());
            map.put("capacity", lab.getCapacity());
            map.put("remaining", remaining); // 返回剩余量
            map.put("isActive", lab.getIsActive());
            
            result.add(map);
        }
        return result;
    }

    // 3. 添加实验室
    @PostMapping("/add")
    public String addLab(@RequestBody Lab lab) {
        labMapper.insert(lab);
        return "实验室添加成功";
    }

    // 4. 删除实验室
    @DeleteMapping("/{id}")
    public String deleteLab(@PathVariable Long id) {
        labMapper.deleteById(id);
        return "实验室已删除";
    }

    // 5. 更新状态
    @PostMapping("/update/{id}")
    public String updateLabStatus(@PathVariable Long id, @RequestBody Lab lab) {
        labMapper.updateStatus(id, lab.getIsActive());
        return "状态更新成功";
    }
}
