package com.chen.Sign.controller;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chen.Sign.common.GuiguException;
import com.chen.Sign.common.Result;
import com.chen.Sign.mapper.ScheduleMapper;
import com.chen.Sign.pojo.Schedule;
import com.chen.Sign.service.impl.ScheduleServiceImpl;
import com.chen.Sign.utils.JWTUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;

// 任务栏模块
@RestController
@Slf4j
@RequestMapping("/schedule")
@SuppressWarnings({"all"})
public class ScheduleController {

    @Autowired
    ScheduleServiceImpl scheduleService;

    @Autowired
    ScheduleMapper scheduleMapper;

    // 添加任务
    @PostMapping("/add")
    public Result add(HttpServletRequest request) {
        String token = request.getHeader("token");
        DecodedJWT tokenInfo = JWTUtils.getTokenInfo(token);
        String uid = tokenInfo.getClaim("id").asString();
        String task = request.getParameter("task");
        String time = request.getParameter("time");
        if (task == null || time == null) {
            throw new GuiguException(0, "有必要参数未填写", "");
        }
        Schedule schedule = new Schedule();
        schedule.setUid(uid);
        schedule.setTask(task);
        System.out.println(task);
        schedule.setTime(time);
        schedule.setStatus(0);
        int insert = scheduleMapper.insert(schedule);
        return new Result(1, "添加成功", "");
    }

    // 查看任务pc端
    @GetMapping("/check")
    public Result check(HttpServletRequest request) {
        String current = request.getParameter("current");
        String size = request.getParameter("size");
        String token = request.getHeader("token");
        DecodedJWT tokenInfo = JWTUtils.getTokenInfo(token);
        String uid = tokenInfo.getClaim("id").asString();
        QueryWrapper<Schedule> queryWapper = new QueryWrapper<>();
        queryWapper.eq("uid", uid);
        List<Schedule> schedules = scheduleMapper.selectList(queryWapper);
        Page<Schedule> page = new Page<>(Integer.parseInt(current), Integer.parseInt(size));
        scheduleMapper.selectPage(page, queryWapper);
        HashMap<String, Object> map = new HashMap<>();
        // 获得数据
        List<Schedule> list = page.getRecords();
        map.put("datalist", list);
        // 总记录数
        map.put("total", page.getTotal());
        // 总页数
        map.put("pages", page.getPages());
        // 当前页
        map.put("currentpage", page.getCurrent());
        return new Result(1, "查看数据成功", map);
    }

    // 查看任务（小程序端）
    @GetMapping("/wxcheck")
    public Result wxcheck(HttpServletRequest request) {
        String token = request.getHeader("token");
        DecodedJWT tokenInfo = JWTUtils.getTokenInfo(token);
        String uid = tokenInfo.getClaim("id").asString();
        QueryWrapper<Schedule> queryWapper = new QueryWrapper<>();
        queryWapper.eq("uid", uid);
        List<Schedule> schedules = scheduleMapper.selectList(queryWapper);
        return new Result(1,"查看任务成功",schedules);
    }

    // 任务已完成
    @PutMapping("/complete/{id}")
    public Result complete(@PathVariable("id") Integer id, HttpServletRequest request) {
        UpdateWrapper<Schedule> wrapper = new UpdateWrapper<>();
        wrapper.eq("id",id).set("status",1);
        int update = scheduleMapper.update(null, wrapper);
        return new Result(1, "修改状态成功", "");
    }

    // 删除任务
    @DeleteMapping("/delete/{id}")
    public Result delete(@PathVariable("id") Integer id, HttpServletRequest request) {
        int i = scheduleMapper.deleteById(id);
        return new Result(1,"删除成功","");
    }

    // 修改任务
    @PutMapping("/modify/{id}")
    public Result modify(@PathVariable("id") Integer id, HttpServletRequest request) {
        String task = request.getParameter("task");
        String time = request.getParameter("time");
        if (task == null || time == null) {
            throw new GuiguException(0, "有必要参数未填写", "");
        }
        UpdateWrapper<Schedule> wrapper = new UpdateWrapper<>();
        wrapper.eq("id",id).set("task",task).set("time",time);
        int update = scheduleMapper.update(null, wrapper);
        return new Result(1,"修改任务成功","");
    }
}
