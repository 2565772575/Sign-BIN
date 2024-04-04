package com.chen.Sign.controller;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chen.Sign.common.Result;
import com.chen.Sign.mapper.JournalMapper;
import com.chen.Sign.mapper.Journal_timeMapper;
import com.chen.Sign.mapper.UserMapper;
import com.chen.Sign.pojo.Journal;
import com.chen.Sign.pojo.Journal_time;
import com.chen.Sign.pojo.User;
import com.chen.Sign.service.Journal_timeService;
import com.chen.Sign.service.impl.JournalServiceImpl;
import com.chen.Sign.utils.JWTUtils;
import com.chen.Sign.vo.Journal_timeVo;
import com.chen.Sign.vo.Journal_timepcVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;


import java.util.*;

// 签到模块
@RestController
@Slf4j
@SuppressWarnings({"all"})
@CrossOrigin
@RequestMapping("/journal")
@Component
public class JournalController {

    @Autowired
    Journal_timeService journal_timeService;

    @Autowired
    Journal_timeMapper journal_timeMapper;
    @Autowired
    JournalServiceImpl journalService;

    @Autowired
    JournalMapper journalMapper;

    @Autowired
    UserMapper userMapper;

    @Autowired
    RedisTemplate redisTemplate;

    // 签到开始2.0
    @PostMapping("/start")
    public Result startjournal(HttpServletRequest request) {
        String token = request.getHeader("token");
        DecodedJWT tokenInfo = JWTUtils.getTokenInfo(token);
        String id = tokenInfo.getClaim("id").asString();
        String username = tokenInfo.getClaim("username").asString();
        String create_time = request.getParameter("create_time");
        Journal journal = new Journal();
        journal.setUsername(username);
        journal.setCreate_time(create_time);
        journal.setUpdate_time("");
        journal.setContent("");
        journal.setState(1);
        int insert = journalMapper.insert(journal);
        if (insert > 0) {
            // 改变用户状态
            User user = userMapper.selectUserByName(username);
            user.setState(1);
            userMapper.updateById(user);
            return new Result(1, "开始签到成功", "");
        } else {
            return new Result(1, "开始签到失败", "");
        }

    }

    // 签到结束2.0
    @PostMapping("/end")
    public Result endjournal(HttpServletRequest request) throws ParseException {
        String token = request.getHeader("token");
        DecodedJWT tokenInfo = JWTUtils.getTokenInfo(token);
        String id = tokenInfo.getClaim("id").asString();
        String username = tokenInfo.getClaim("username").asString();
        String update_time = request.getParameter("update_time");
        String content = request.getParameter("content");
        // 查询正在签到的数据
        LambdaQueryWrapper<Journal> queryWrapper3 = new LambdaQueryWrapper<>();
        queryWrapper3.eq(Journal::getUsername, username).eq(Journal::getState, 1);
        Journal journal = journalMapper.selectOne(queryWrapper3);
        String create_time = journal.getCreate_time();
        // 将数据插入到数据库中
        journal.setContent(content);
        journal.setUpdate_time(update_time);
        journal.setState(0);
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date createtime = df.parse(create_time);
        Date updatetime = df.parse(update_time);
        // 计算本周时间、总时间、本次时间
        Long begin = createtime.getTime();
        Long end = updatetime.getTime();
        // 得到毫秒级别的差值·----------------------------------------------------------
        long timeLag = end - begin;
        //天
        long day = timeLag / (24 * 60 * 60 * 1000);
        //小时
        long hour = (timeLag / (60 * 60 * 1000) - day * 24);
        //分钟
        long minute = ((timeLag / (60 * 1000)) - day * 24 * 60 - hour * 60);
        //秒，顺便说一下，1秒 = 1000毫秒
        long s = (timeLag / 1000 - day * 24 * 60 * 60 - hour * 60 * 60 - minute * 60);
        Long sum_time = hour * 60 + minute;
        journal.setSum_time(sum_time);
        int i = journalMapper.updateById(journal);
        Long sumTimeday = Long.valueOf(0);
        Long sumTimeweek = Long.valueOf(0);
        Long sumTimeall = Long.valueOf(0);
        String timedesc = null;
        List<Journal> mapDay = journalService.selectByNameDay(username);
        List<Journal> mapWeek = journalService.selectByNameWeek(username);
        List<Journal> mapAll = journalService.selectByNameAll(username);
        // 遍历一天内的时长
        for (Journal journal1 : mapDay) {
            Long sum_time1 = journal1.getSum_time();
            sumTimeday += sum_time1;
        }
        String daytime = (sumTimeday / 60) + "小时" + (sumTimeday % 60) + "分钟";
        // 遍历一周内的时长
        for (Journal journal1 : mapWeek) {
            Long sum_time1 = journal1.getSum_time();
            sumTimeweek += sum_time1;
        }
        String weektime = (sumTimeweek / 60) + "小时" + (sumTimeweek % 60) + "分钟";
        // 遍历所有时长
        for (Journal journal1 : mapAll) {
            Long sum_time1 = journal1.getSum_time();
            sumTimeall += sum_time1;
        }
        String alltime = (sumTimeall / 60) + "小时" + (sumTimeall % 60) + "分钟";
        // 1：检查是否已存在数据
        LambdaQueryWrapper<Journal_time> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Journal_time::getUsername, username);
        Journal_time journal_time1 = journal_timeMapper.selectOne(queryWrapper);
        Integer continuous = journal_time1.getContinuous();
        Integer addupday = journal_time1.getAddupday();
        Integer status = journal_time1.getStatus();
        // 3：若存在数据，则进行数据的更新
        if (status != 1) {
            // 判断昨天是否签到
            // 1.获取昨天的时间
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DATE, -1);
            Date d = cal.getTime();
            SimpleDateFormat sp = new SimpleDateFormat("yyyy-MM-dd");
            String zt = sp.format(d);
            // 2.查询昨天是否有签到数据
            LambdaQueryWrapper<Journal> queryWrapper1 = new LambdaQueryWrapper<>();
            queryWrapper1.like(Journal::getCreate_time, zt);
            List<Journal> journalList = journalMapper.selectList(queryWrapper1);
            if (journalList.size() > 0) { // 昨天签到了
                LambdaUpdateWrapper<Journal_time> updateWrapper = new LambdaUpdateWrapper<>();
                updateWrapper.eq(Journal_time::getUsername, username)
                        .set(Journal_time::getAll_time, alltime)
                        .set(Journal_time::getDay_time, daytime)
                        .set(Journal_time::getWeek_time, weektime)
                        .set(Journal_time::getContinuous, ++continuous)
                        .set(Journal_time::getAddupday, ++addupday)
                        .set(Journal_time::getStatus, 1);
                int update = journal_timeMapper.update(journal_time1, updateWrapper);
            } else { // 昨天未签到
                LambdaUpdateWrapper<Journal_time> updateWrapper = new LambdaUpdateWrapper<>();
                updateWrapper.eq(Journal_time::getUsername, username)
                        .set(Journal_time::getAll_time, alltime)
                        .set(Journal_time::getDay_time, daytime)
                        .set(Journal_time::getWeek_time, weektime)
                        .set(Journal_time::getContinuous, 1)
                        .set(Journal_time::getAddupday, ++addupday)
                        .set(Journal_time::getStatus, 1);
                int update = journal_timeMapper.update(journal_time1, updateWrapper);
            }
        } else {
            LambdaUpdateWrapper<Journal_time> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(Journal_time::getUsername, username)
                    .set(Journal_time::getAll_time, alltime)
                    .set(Journal_time::getDay_time, daytime)
                    .set(Journal_time::getWeek_time, weektime);
            int update = journal_timeMapper.update(journal_time1, updateWrapper);
        }
        if (i > 0) {
            // 改变用户状态
            User user = userMapper.selectUserByName(username);
            user.setState(0);
            userMapper.updateById(user);
            return new Result(1, "结束签到成功", "");
        } else {
            return new Result(1, "结束签到失败", "");
        }
    }

    // 签到结束
    @PostMapping("/recordendwx")
    public Result recordwx(HttpServletRequest request) throws ParseException {
        Result result = new Result();
        String token = request.getHeader("token");
        DecodedJWT tokenInfo = JWTUtils.getTokenInfo(token);
        String id = tokenInfo.getClaim("id").asString();
        String username = tokenInfo.getClaim("username").asString();
        System.out.println("id = " + id);
        System.out.println("重点重点--->username = " + username);
        String content = request.getParameter("content");
        String create_time = request.getParameter("create_time");
        String update_time = request.getParameter("update_time");
        System.out.println("测试时间：------------------------------------------------------》");
        System.out.println("create_time = " + create_time);
        System.out.println("update_time = " + update_time);
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date createtime = df.parse(create_time);
        Date updatetime = df.parse(update_time);
        Journal journal = new Journal();
        journal.setUsername(username);
        journal.setContent(content);
        journal.setCreate_time(create_time);
        journal.setUpdate_time(update_time);
        // 代码优化
        // 计算本周时间、总时间、本次时间
        Long begin = createtime.getTime();
        Long end = updatetime.getTime();
        // 得到毫秒级别的差值·----------------------------------------------------------
        long timeLag = end - begin;
        //天
        long day = timeLag / (24 * 60 * 60 * 1000);
        //小时
        long hour = (timeLag / (60 * 60 * 1000) - day * 24);
        //分钟
        long minute = ((timeLag / (60 * 1000)) - day * 24 * 60 - hour * 60);
        //秒，顺便说一下，1秒 = 1000毫秒
        long s = (timeLag / 1000 - day * 24 * 60 * 60 - hour * 60 * 60 - minute * 60);
        Long sum_time = hour * 60 + minute;
        journal.setSum_time(sum_time);
        int insert = journalMapper.insert(journal);
        Long sumTimeday = Long.valueOf(0);
        Long sumTimeweek = Long.valueOf(0);
        Long sumTimeall = Long.valueOf(0);
        String timedesc = null;
        List<Journal> mapDay = journalService.selectByNameDay(username);
        List<Journal> mapWeek = journalService.selectByNameWeek(username);
        List<Journal> mapAll = journalService.selectByNameAll(username);
        // 遍历一天内的时长
        for (Journal journal1 : mapDay) {
            Long sum_time1 = journal1.getSum_time();
            sumTimeday += sum_time1;
        }
        String daytime = (sumTimeday / 60) + "小时" + (sumTimeday % 60) + "分钟";
        // 遍历一周内的时长
        for (Journal journal1 : mapWeek) {
            Long sum_time1 = journal1.getSum_time();
            sumTimeweek += sum_time1;
        }
        String weektime = (sumTimeweek / 60) + "小时" + (sumTimeweek % 60) + "分钟";
        // 遍历所有时长
        for (Journal journal1 : mapAll) {
            Long sum_time1 = journal1.getSum_time();
            sumTimeall += sum_time1;
        }
        String alltime = (sumTimeall / 60) + "小时" + (sumTimeall % 60) + "分钟";

//        // 统计时间的保存和修改
//        Journal_time journal_time = new Journal_time();
//        // 1：检查是否已存在数据
        LambdaQueryWrapper<Journal_time> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Journal_time::getUsername, username);
        Journal_time journal_time1 = journal_timeMapper.selectOne(queryWrapper);
        Integer continuous = journal_time1.getContinuous();
        Integer addupday = journal_time1.getAddupday();
        Integer status = journal_time1.getStatus();
        // 3：若存在数据，则进行数据的更新
        if (status != 1) {
            LambdaUpdateWrapper<Journal_time> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(Journal_time::getUsername, username)
                    .set(Journal_time::getAll_time, alltime)
                    .set(Journal_time::getDay_time, daytime)
                    .set(Journal_time::getWeek_time, weektime)
                    .set(Journal_time::getContinuous, ++continuous)
                    .set(Journal_time::getAddupday, ++addupday)
                    .set(Journal_time::getStatus, 1);
            journal_timeMapper.update(journal_time1, updateWrapper);
        } else {
            LambdaUpdateWrapper<Journal_time> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(Journal_time::getUsername, username)
                    .set(Journal_time::getAll_time, alltime)
                    .set(Journal_time::getDay_time, daytime)
                    .set(Journal_time::getWeek_time, weektime);
            journal_timeMapper.update(journal_time1, updateWrapper);
        }
        // 代码优化
        HashMap<String, String> map = new HashMap<>();
        if (insert > 0) {
            return result.insertContentok(map);
        } else {
            return result.insertContentno(map);
        }
    }

    // 返回签到数据（pc端）
    @GetMapping("/takeout")
    public Result takeOut(Integer page, Integer pageSize, HttpServletRequest request) {
        System.out.println(page);
        System.out.println(pageSize);
        String token = request.getHeader("token");
        DecodedJWT tokenInfo = JWTUtils.getTokenInfo(token);
        String id = tokenInfo.getClaim("id").asString();
        String username = tokenInfo.getClaim("username").asString();
        Page<Journal> pageInfo = new Page<>(page, pageSize);
        LambdaQueryWrapper<Journal> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.eq(Journal::getUsername, username);
        journalService.page(pageInfo, queryWrapper);
        LambdaQueryWrapper<Journal_time> queryWrapper1 = new LambdaQueryWrapper<>();
        queryWrapper1.eq(Journal_time::getUsername, username);
        Journal_time journal_time = journal_timeMapper.selectOne(queryWrapper1);
        LambdaQueryWrapper<Journal_time> queryWrapper6 = new LambdaQueryWrapper<>();
        queryWrapper6.orderByDesc(Journal_time::getWeek_time);
        List<Journal_time> journal_times = journal_timeMapper.selectList(queryWrapper6);
        int rank = 1;
        Journal_timepcVo journal_timepcVo = new Journal_timepcVo();
        for (Journal_time journal_time1 : journal_times) {
            String username1 = journal_time1.getUsername();
            if (username1.equals(username)) {
                String result = "第" + rank + "名";
                journal_timepcVo.setWeek_time_desc(result);
            }
            rank++;
        }
        // -------------
        journal_timepcVo.setPageInfo(pageInfo);
        journal_timepcVo.setUsername(username);
        journal_timepcVo.setWeek_time(journal_time.getWeek_time());
        journal_timepcVo.setDay_time(journal_time.getDay_time());
        journal_timepcVo.setAll_time(journal_time.getAll_time());
        return new Result(1, "查找成功", journal_timepcVo);
    }

    //返回签到数据（小程序端）
    @CrossOrigin
    @GetMapping("/wxtakeout")
    public Result wxtakeOut(HttpServletRequest request) {
        String token = request.getHeader("token");
        DecodedJWT tokenInfo = JWTUtils.getTokenInfo(token);
        String id = tokenInfo.getClaim("id").asString();
        String username = tokenInfo.getClaim("username").asString();
        LambdaQueryWrapper<Journal> queryWrapper = new LambdaQueryWrapper<>();
        LambdaQueryWrapper<Journal_time> queryWrapper1 = new LambdaQueryWrapper<>();
        queryWrapper.eq(Journal::getUsername, username).eq(Journal::getState, 0).orderByDesc(Journal::getId);
        queryWrapper1.eq(Journal_time::getUsername, username);
        List<Journal> journals = journalMapper.selectList(queryWrapper);
        Journal_time journal_time = journal_timeMapper.selectOne(queryWrapper1);
        // -------------
        LambdaQueryWrapper<Journal_time> queryWrapper6 = new LambdaQueryWrapper<>();
        queryWrapper6.orderByDesc(Journal_time::getWeek_time);
        List<Journal_time> journal_times = journal_timeMapper.selectList(queryWrapper6);
        int rank = 1;
        Journal_timeVo journal_timeVo = new Journal_timeVo();
        for (Journal_time journal_time1 : journal_times) {
            String username1 = journal_time1.getUsername();
            if (username1.equals(username)) {
                String result = "第" + rank + "名";
                journal_timeVo.setWeek_time_desc(result);
            }
            rank++;
        }
        // -------------
        journal_timeVo.setJournalList(journals);
        journal_timeVo.setUsername(username);
        journal_timeVo.setWeek_time(journal_time.getWeek_time());
        journal_timeVo.setDay_time(journal_time.getDay_time());
        journal_timeVo.setAll_time(journal_time.getAll_time());
        // 判断用户是否正在签到
        User user = userMapper.selectUserByName(username);
        Integer state = user.getState();
        journal_timeVo.setState(state);
        System.out.println(journal_timeVo);
        return new Result(1, "返回签到数据成功", journal_timeVo);
    }

    // 查看连续签到天数和累计签到天数
    @GetMapping("/continue")
    public Result continuous(HttpServletRequest request) {
        String token = request.getHeader("token");
        DecodedJWT tokenInfo = JWTUtils.getTokenInfo(token);
        String id = tokenInfo.getClaim("id").asString();
        String username = tokenInfo.getClaim("username").asString();
        Map<String, Object> map = new HashMap<>();
        // 查找对应用的连续签到天数和累计签到天数
        LambdaQueryWrapper<Journal_time> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Journal_time::getUsername, username);
        Journal_time journal_time = journal_timeMapper.selectOne(queryWrapper);
        // 判断状态值
        Integer status = journal_time.getStatus();
        if (status != 0) { // 状态值为1
            map.put("Continuous", journal_time.getContinuous());
            map.put("Addupday", journal_time.getAddupday());
            return new Result(1, "连续签到天数返回成功", map);
        } else { // 状态值为0
            // 判断昨天是否签到
            // 1.获取昨天的时间
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DATE, -1);
            Date d = cal.getTime();
            SimpleDateFormat sp = new SimpleDateFormat("yyyy-MM-dd");
            String zt = sp.format(d);
            // 2.查询昨天是否有签到数据
            LambdaQueryWrapper<Journal> queryWrapper1 = new LambdaQueryWrapper<>();
            queryWrapper1.like(Journal::getCreate_time, zt);
            List<Journal> journalList = journalMapper.selectList(queryWrapper1);
            if (journalList.size()>0) { // 昨天已签到
                map.put("Continuous", journal_time.getContinuous());
                map.put("Addupday", journal_time.getAddupday());
                return new Result(1, "连续签到天数返回成功", map);
            } else { // 昨天未签到
                journal_time.setContinuous(1);
                journal_timeMapper.updateById(journal_time);
                map.put("Continuous", journal_time.getContinuous());
                map.put("Addupday", journal_time.getAddupday());
                return new Result(1, "连续签到天数返回成功", map);
            }
        }
    }

    // 每天定时将状态值清零
    @Scheduled(cron = "0 0 0 * * *")
    public void timestatus() {
        List<Journal_time> journal_times = journal_timeMapper.selectList(null);
        for (Journal_time journal_time : journal_times) {
            Integer status = journal_time.getStatus();
            if (status != 1) {
                journal_time.setContinuous(0);
            }
            journal_time.setStatus(0);
            journal_timeMapper.updateById(journal_time);
        }
    }

    // 返回周一到周日的签到时长
    @GetMapping("/oneweek")
    public Result oneweek(HttpServletRequest request, String date) {
        String token = request.getHeader("token");
        DecodedJWT tokenInfo = JWTUtils.getTokenInfo(token);
        String id = tokenInfo.getClaim("id").asString();
        String username = tokenInfo.getClaim("username").asString();
        String[] g = date.split(",");
        Double data[] = new Double[g.length];
        for (int i = 0; i < g.length; i++) {
            String s = g[i];
            LambdaQueryWrapper<Journal> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.like(Journal::getCreate_time, s).eq(Journal::getUsername,username).eq(Journal::getState,0);
            List<Journal> journals = journalMapper.selectList(queryWrapper);
            Long alltime = 0L;
            if (journals.size() == 0) {
                data[i] = 0.0;
            } else {
                for (Journal journal : journals) {
                    Long sum_time = journal.getSum_time();
                    alltime += sum_time;
                }
                Long hour = alltime / 60;
                Long minute = alltime % 60;
                Double minute2 = minute / 60.0;
                Double result = hour + minute2;
                data[i] = result;
            }
        }
        return new Result(1, "返回签到时长成功", data);
    }

    // 日历签到情况--1：签到 0：未签到
    @PostMapping("/calendar")
    public Result calendar(HttpServletRequest request) {
        String token = request.getHeader("token");
        DecodedJWT tokenInfo = JWTUtils.getTokenInfo(token);
        String id = tokenInfo.getClaim("id").asString();
        String username = tokenInfo.getClaim("username").asString();
        String date = request.getParameter("date");
        String[] g = date.split(",");
        Integer data[] = new Integer[g.length];
        for (int i = 0; i < g.length; i++) {
            LambdaQueryWrapper<Journal> queryWrapper = new LambdaQueryWrapper<>();
            String s = g[i];
            queryWrapper.like(Journal::getCreate_time, s);
            List<Journal> journals = journalMapper.selectList(queryWrapper);
            if (journals.size() != 0) {
                data[i] = 1;
            } else {
                data[i] = 0;
            }
        }
        return new Result(1, "日历签到数据返回成功", data);
    }

    /**
     * 每周日将签到时间数据清空
     */
    @Scheduled(cron = "0 0 0 * * SUN")
    public void journaltimeout() {
        List<Journal_time> journal_times = journal_timeMapper.selectList(null);
        for (Journal_time journal_time : journal_times) {
            journal_time.setDay_time("0小时0分钟");
            journal_time.setWeek_time("0小时0分钟");
            journal_time.setAll_time("0小时0分钟");
            journal_time.setWeek_time_desc("第0名");
        }
    }

    /**
     * 连续签到天数的计算
     * 一:签到的时候
     * 1.签到结束的时候查看状态值
     * 2.状态值为1 只改变签到时长
     * 3.状态值为0 查看昨天是否签到
     * 4.昨天签到了 连续天数+1 总天数+1
     * 5.昨天没签到 连续天数变为1 总天数+1
     * 二:点击查看连续签到天数的时候
     * 1.查看状态值
     * 2.状态值为1 数据直接返回
     * 3.状态值为0 判断昨天是否签到 昨天签到：数据直接返回 昨天未签到：连续签到修改为0
     */

}









