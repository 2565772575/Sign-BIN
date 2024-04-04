package com.ruoyi.ruoyilab.controller;

import com.ruoyi.common.annotation.DataScope;
import com.ruoyi.ruoyilab.common.R;
import com.ruoyi.ruoyilab.pojo.End;
import com.ruoyi.ruoyilab.pojo.Repair;
import com.ruoyi.ruoyilab.service.JournalService;
import com.ruoyi.ruoyilab.vo.JournalVo;
import com.ruoyi.ruoyilab.vo.JournalaVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.util.List;
import java.util.Map;

/**
 * @author 陈帅彬
 * @date 2023/5/10 16:24
 */
@Slf4j
@RestController
@SuppressWarnings({"all"})
@RequestMapping("/journal")
public class JournalController {

    @Autowired
    private JournalService journalService;

    // 详情--显示用户的所有签到信息
    @GetMapping("/uall")
    public R ujouranlall(String username) {
        if (username != null) {
            List<JournalaVo> result = journalService.selectjournal(username);
            return R.success("返回用户所有签到信息成功", result);
        } else {
            return R.error("未传username参数");
        }
    }

    // 签到列表--显示所有用户的信息及最新的一条签到信息--姓名，性别，学号，签到时间，结束时间，学习记录，操作
    @GetMapping("/list")
    public R list() {
        List<JournalVo> result = journalService.selectlist();
        return R.success("返回签到列表成功", result);
    }

    // 补签--
    @PutMapping("")
    public R repair(@RequestBody Repair repair) throws ParseException {
        String username = repair.getUsername();
        String starttime = repair.getStarttime();
        String endtime = repair.getEndtime();
        System.out.println("starttime->" + starttime);
        System.out.println("endtime->" + endtime);
        System.out.println("username->" + username);
        int result = journalService.repair(username, starttime, endtime);
        if (result > 0) {
            return R.success("补签成功", "");
        }
        return R.error("补签失败");
    }

    // 时长详情--柱状图显示用户这段时间的签到时长
    @GetMapping("/weekall")
    public R utimeall(String starttime, String endtime) {
        List<Map<String, Object>> result = journalService.selecttimeall(starttime, endtime);
        return R.success("返回时长汇总成功", result);
    }

    // 结签
    @PutMapping("/end")
    public R end(@RequestBody End end) throws ParseException {
        String username = end.getUsername();
        String endtime = end.getEndtime();
        System.out.println("endtime->" + endtime);
        System.out.println("username->" + username);
        int result = journalService.endjournal(endtime, username);
        if (result > 0) {
            return R.success("结签成功", "");
        }
        return R.error("结签失败");
    }

    // 删除签到数据
    @DeleteMapping("/{id}")
    public R delete(@PathVariable("id") Integer id) throws ParseException {
        int result = journalService.deletejournal(id);
        if (result > 0) {
            return R.success("删除成功", "");
        }
        return R.error("删除失败");
    }

}
