package com.ruoyi.ruoyilab.service.impl;

import com.ruoyi.ruoyilab.mapper.JournalMapper;
import com.ruoyi.ruoyilab.mapper.UserMapper;
import com.ruoyi.ruoyilab.pojo.Journal;
import com.ruoyi.ruoyilab.pojo.Journal_time;
import com.ruoyi.ruoyilab.pojo.User;
import com.ruoyi.ruoyilab.service.JournalService;
import com.ruoyi.ruoyilab.vo.JournalVo;
import com.ruoyi.ruoyilab.vo.JournalaVo;
import com.ruoyi.ruoyilab.vo.UserVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author 陈帅彬
 * @date 2023/5/10 16:24
 */
@Service
@SuppressWarnings({"all"})
public class JournalServiceImpl implements JournalService {

    @Autowired
    private JournalMapper journalMapper;

    @Autowired
    private UserMapper userMapper;

    @Override
    public List<JournalaVo> selectjournal(String username) {
        List<JournalaVo> result = new ArrayList<>();
        List<Journal> journalList = journalMapper.selectjournal(username);
        User user = userMapper.selectname(username);
        String name = user.getName();
        for (Journal journal : journalList) {
            JournalaVo journalaVo = new JournalaVo();
            journalaVo.setId(journal.getId());
            journalaVo.setUsername(journal.getUsername());
            journalaVo.setName(name);
            journalaVo.setContent(journal.getContent());
            journalaVo.setCreate_time(journal.getCreate_time());
            journalaVo.setUpdate_time(journal.getUpdate_time());
            result.add(journalaVo);
        }
        return result;
    }

    @Override
    public List<JournalVo> selectlist() {
        List<JournalVo> result = new ArrayList<>();
        // 根据已经签到的人来查询他们的信息
        List<UserVo> users = userMapper.selectalluser();
        for (UserVo user : users) {
            Journal journal = journalMapper.selectlist(user.getUsername());
            JournalVo journalVo = new JournalVo();
            journalVo.setId(user.getId());
            journalVo.setName(user.getName());
            journalVo.setSex(user.getSex());
            journalVo.setUsername(user.getUsername());
            journalVo.setCreate_time(journal.getCreate_time());
            journalVo.setUpdate_time(journal.getUpdate_time());
            journalVo.setContent(journal.getContent());
            journalVo.setState(user.getState());
            result.add(journalVo);
        }
        return result;
    }

    // 获取补签的开始时间和结束时间
    // 处理为 小时 和 分钟
    // 获取周时长和全部时长
    // 分割字符提取小时和分钟，转换为int类型
    // 将预处理数据加和
    // 再转换为String类型 进行存储
    @Override
    public int repair(String username, String starttime, String endtime) throws ParseException {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        // 处理为小时和分钟
        Map<String, Long> time = time(starttime, endtime);
        Long hour = time.get("hour");
        Long minute = time.get("minute");
        Long sumTime = hour * 60 + minute;
        // 将信息添加到journal表中
        String content = "补签";
        int i = journalMapper.addjournal(username, content, starttime, endtime, sumTime);
        // 获取周时长和全部时长
        Journal_time journal_time = journalMapper.selecttime(username);
        // 分割字符串转换类型
        String day_time = journal_time.getDay_time();
        String week_time = journal_time.getWeek_time();
        String all_time = journal_time.getAll_time();
        SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd");
        Date time1 = sf.parse(starttime);
        Boolean isweek = isweek(time1);
        Boolean isday = isday(time1);
        if (isweek) {
            if (isday) {
                // 天时长处理
                String day = timeconvert1(day_time, sumTime);
                // 周时长处理
                String week = timeconvert1(week_time, sumTime);
                // 总时长处理
                String all = timeconvert1(all_time, sumTime);
                // 总时长处理
                int result = journalMapper.repair(username, day, week, all);
                // 修改用户的状态值
                int result1 = userMapper.updatestate(username);
                return result;
            } else {
                // 周时长处理
                String week = timeconvert1(week_time, sumTime);
                // 总时长处理
                String all = timeconvert1(all_time, sumTime);
                // 总时长处理
                int result = journalMapper.repair(username, day_time, week, all);
                // 修改用户的状态值
                int result1 = userMapper.updatestate(username);
                return result;
            }
        } else {
            // 总时长处理
            String all = timeconvert1(all_time, sumTime);
            // 总时长处理
            int result = journalMapper.repair(username, day_time, week_time, all);
            // 修改用户的状态值
            int result1 = userMapper.updatestate(username);
            return result;
        }
//        if (isweek) {
//            // 获取周时长和全部时长
//            Journal_time journal_time = journalMapper.selecttime(username);
//            // 分割字符串转换类型
//            String week_time = journal_time.getWeek_time();
//            String all_time = journal_time.getAll_time();
//            // 周时长处理
//            String[] w = week_time.split("小时");
//            Long w1 = Long.parseLong(w[0]); // 周时长原来的小时
//            Long w2 = (w1 + hour) * 60;
//            Long w3 = Long.parseLong(w[1].split("分钟")[0]); // 周时长原来的分钟
//            Long w4 = w2 + w3 + minute; // 修改后的总分钟
//            Long w5 = w4 / 60; // 修改后的小时
//            Long w6 = w4 % 60; // 修改后的分钟
//            String w7 = String.valueOf(w5); // 周时长的小时
//            String w8 = String.valueOf(w6); // 周时长的分钟
//            String week = w7 + "小时" + w8 + "分钟"; // 周时长完全体
//            // 总时长处理
//            String[] a = all_time.split("小时");
//            Long a1 = Long.parseLong(a[0]); // 总时长原来的小时
//            Long a2 = (a1 + hour) * 60;
//            Long a3 = Long.parseLong(a[1].split("分钟")[0]); // 总时长原来的分钟
//            Long a4 = a2 + a3 + minute;
//            Long a5 = a4 / 60; //总时长的小时
//            Long a6 = a5 % 60; //总时长的分钟
//            String a7 = String.valueOf(a5);
//            String a8 = String.valueOf(a6);
//            String all = a7 + "小时" + a8 + "分钟"; // 总时长完全体
//            // 总时长处理
//            int result = journalMapper.repair(username, week, all);
//            return result;
//        } else {
//            // 获取周时长和全部时长
//            Journal_time journal_time = journalMapper.selecttime(username);
//            // 分割字符串转换类型
//            String week_time = journal_time.getWeek_time();
//            String all_time = journal_time.getAll_time();
//            // 总时长处理
//            String[] a = all_time.split("小时");
//            Long a1 = Long.parseLong(a[0]); // 总时长原来的小时
//            Long a2 = (a1 + hour) * 60;
//            Long a3 = Long.parseLong(a[1].split("分钟")[0]); // 总时长原来的分钟
//            Long a4 = a2 + a3 + minute;
//            Long a5 = a4 / 60; //总时长的小时
//            Long a6 = a5 % 60; //总时长的分钟
//            String a7 = String.valueOf(a5);
//            String a8 = String.valueOf(a6);
//            String all = a7 + "小时" + a8 + "分钟"; // 总时长完全体
//            // 总时长处理
//            int result = journalMapper.repair(username, week_time, all);
//            return result;
//        }
    }

    // 根据日期返回所有用户的汇总
    @Override
    public List<Map<String, Object>> selecttimeall(String starttime, String endtime) {
        System.out.println("日期柱状图开始时间"+starttime);
        System.out.println("日期柱状图结束时间"+endtime);
        List<Map<String, Object>> result = new ArrayList<>();
        // 查询签到用户的签到信息并封装
        // 将结束日期修改
        String endtime1 = endtime + " 23:59:59";
        List<Journal> journalList = journalMapper.selecttimeall(starttime, endtime1);
        for (Journal journal : journalList) {
            Map<String, Object> map = new HashMap<>();
            String username = journal.getUsername();
            User user = userMapper.selectname(username);
            String name = user.getName();
            Long sum_time = journal.getSum_time();
            result.remove(username);
            Long hour = sum_time / 60;
            Long minute = sum_time % 60;
            Double minute2 = minute / 60.0;
            Double result2 = hour + minute2;
            map.put("name", name);
            map.put("time", result2);
            result.add(map);
        }
        // 查询没有签到信息的用户并添加签到时长为0
        List<User> userList = userMapper.selectnjname(starttime, endtime);
        for (User user : userList) {
            Map<String, Object> map = new HashMap<>();
            map.put("name", user.getName());
            map.put("time", 0.0);
            result.add(map);
        }
        return result;
    }

    /**
     * 1.在用户列表中添加状态值（判断是否正在签到，是否可以结签）√
     * 2.根据用户名找到最后一条签到数据--拿到开始签到时间（便于后续对签到时间表操作）
     * 3.根据前端传来的结束签到时间传入签到列表（签到列表中也应该加一个状态值，只显示签到完成的）中--修改签到表和用户表的状态值--签到表完成
     * 4.根据开始时间和结束时间，对数据进行处理
     * 5.对签到时长表进行操作--加上本次签到的时长
     *
     * @param endtime
     * @return
     */
    @Override
    public int endjournal(String endtime, String username) throws ParseException {
        // 编写sql语句查找最后一条正在签到数据
        Journal journal = journalMapper.selectend(username);
        String create_time = journal.getCreate_time();
        // 修改journal的数据
        Map<String, Long> time = time(create_time, endtime);
        Long hour = time.get("hour");
        Long minute = time.get("minute");
        Long sumTime = hour * 60 + minute;
        // 修改该用户未结签的数据
        String content = "结签";
        System.out.println(username);
        System.out.println(content);
        System.out.println(endtime);
        System.out.println(sumTime);
        int i = journalMapper.updatejournal(username, content, endtime, sumTime);
        // 获取周时长和全部时长
        Journal_time journal_time = journalMapper.selecttime(username);
        // 分割字符串转换类型
        String day_time = journal_time.getDay_time();
        String week_time = journal_time.getWeek_time();
        String all_time = journal_time.getAll_time();
        SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd");
        Date time1 = sf.parse(create_time);
        Boolean isweek = isweek(time1);
        Boolean isday = isday(time1);
        if (isweek) {
            if (isday) {
                // 天时长处理
                String day = timeconvert1(day_time, sumTime);
                // 周时长处理
                String week = timeconvert1(week_time, sumTime);
                // 总时长处理
                String all = timeconvert1(all_time, sumTime);
                // 总时长处理
                int result = journalMapper.repair(username, day, week, all);
                // 修改用户的状态值
                int result1 = userMapper.updatestate(username);
                return result;
            } else {
                // 周时长处理
                String week = timeconvert1(week_time, sumTime);
                // 总时长处理
                String all = timeconvert1(all_time, sumTime);
                // 总时长处理
                int result = journalMapper.repair(username, day_time, week, all);
                // 修改用户的状态值
                int result1 = userMapper.updatestate(username);
                return result;
            }
        } else {
            // 总时长处理
            String all = timeconvert1(all_time, sumTime);
            // 总时长处理
            int result = journalMapper.repair(username, day_time, week_time, all);
            // 修改用户的状态值
            int result1 = userMapper.updatestate(username);
            return result;
        }
    }

    // 删除签到数据
    @Override
    public int deletejournal(Integer id) throws ParseException {
        // 通过id查找数据拿到Journal
        Journal journal = journalMapper.selectid(id);
        // 通过id进行删除
        int i = journalMapper.deleteid(id);
        // 对journal_time表进行操作，如果创建时间是本周则去除本周和总时长 如果不是本周则去除总时长
        String create_time = journal.getCreate_time();
        Long sum_time = journal.getSum_time();
        String username = journal.getUsername();
        Journal_time journal_time = journalMapper.selecttime(username);
        // 分割字符串转换类型
        String day_time = journal_time.getDay_time();
        String week_time = journal_time.getWeek_time();
        String all_time = journal_time.getAll_time();
        // 判断是不是本周
        // 1.将create_time转换为Date类型
        SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd");
        Date time = sf.parse(create_time);
        Boolean isweek = isweek(time);
        Boolean isday = isday(time);
        // 是本周时间
        if (isweek) {
            if (isday) {
                // 天时长处理
                String day = timeconvert(day_time, sum_time);
                // 周时长处理
                String week = timeconvert(week_time, sum_time);
                // 总时长处理
                String all = timeconvert(all_time, sum_time);
                int result = journalMapper.repair(username, day, week, all);
                // 返回结果
                return result;
            } else {
                // 周时长处理
                String week = timeconvert(week_time, sum_time);
                // 总时长处理
                String all = timeconvert(all_time, sum_time);
                int result = journalMapper.repair(username, day_time, week, all);
                // 返回结果
                return result;
            }
        } else {
            // 不是本周时间
            String all = timeconvert(all_time, sum_time);
            int result = journalMapper.repair(username, day_time, week_time, all);
            // 返回结果
            return result;
        }
    }

    private Map<String, Long> time(String starttime, String endtime) {
        HashMap<String, Long> map = new HashMap<>();
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date createtime = null;
        Date updatetime = null;
        try {
            createtime = df.parse(starttime);
            updatetime = df.parse(endtime);
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
            map.put("hour", hour);
            map.put("minute", minute);
            return map;
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    private Boolean isweek(Date time) {
        Calendar calendar = Calendar.getInstance();
        int currentWeek = calendar.get(Calendar.WEEK_OF_YEAR + 1);
        calendar.setTime(time);
        int paramWeek = calendar.get(Calendar.WEEK_OF_YEAR + 1);
        if (paramWeek == currentWeek) {
            return true;
        }
        return false;
    }

    private Boolean isday(Date time) {
        Calendar calendar = Calendar.getInstance();
        int currentWeek = calendar.get(Calendar.DAY_OF_WEEK);
        calendar.setTime(time);
        int paramWeek = calendar.get(Calendar.DAY_OF_WEEK);
        if (paramWeek == currentWeek) {
            return true;
        }
        return false;
    }

    private String timeconvert(String time, Long sum_time) {
        String[] a = time.split("小时");
        Long a1 = Long.parseLong(a[0]); // 时长原来的小时
        Long a2 = a1 * 60;
        Long a3 = Long.parseLong(a[1].split("分钟")[0]); // 时长原来的分钟
        Long a4 = a2 + a3 - sum_time;
        Long a5 = a4 / 60; //时长的小时
        Long a6 = a4 % 60; //时长的分钟
        String a7 = String.valueOf(a5);
        String a8 = String.valueOf(a6);
        String all = a7 + "小时" + a8 + "分钟"; // 时长完全体
        return all;
    }

    private String timeconvert1(String time, Long sum_time) {
        String[] a = time.split("小时");
        Long a1 = Long.parseLong(a[0]); // 时长原来的小时
        Long a2 = a1 * 60;
        Long a3 = Long.parseLong(a[1].split("分钟")[0]); // 时长原来的分钟
        Long a4 = a2 + a3 + sum_time;
        Long a5 = a4 / 60; //时长的小时
        Long a6 = a4 % 60; //时长的分钟
        String a7 = String.valueOf(a5);
        String a8 = String.valueOf(a6);
        String all = a7 + "小时" + a8 + "分钟"; // 时长完全体
        return all;
    }


}
