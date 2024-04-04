package com.chen.Sign.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chen.Sign.pojo.Journal;
import com.chen.Sign.mapper.JournalMapper;
import com.chen.Sign.service.JournalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@SuppressWarnings({"all"})
public class JournalServiceImpl extends ServiceImpl<JournalMapper, Journal> implements JournalService {

    @Autowired
    JournalMapper journalMapper;

    @Autowired
    JournalService journalService;

    public boolean insertContent(Journal journal) {
        int x = journalMapper.insertJournal(journal);
        if (x > 0) {
            return true;
        } else {
            return false;
        }
    }

    public Map<String, Object> selectByName(String username) {
        Map<String, Object> map = journalMapper.selectByName(username);
        return map;
    }

    public List<Journal> selectByNameDay(String username) {
//        Map<String, Object> map = journalMapper.selectByNameDay(username);
        List<Journal> list = journalMapper.selectByNameDay(username);
        System.out.println(list);
        return list;
    }

    public List<Journal> selectByNameWeek(String username) {
        List<Journal> list = journalMapper.selectByNameWeek(username);
        return list;
    }

    public List<Journal> selectByNameAll(String username) {
        List<Journal> list = journalMapper.selectByNameAll(username);
        return list;
    }


}
