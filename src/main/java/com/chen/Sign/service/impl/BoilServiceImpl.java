package com.chen.Sign.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chen.Sign.mapper.BoilMapper;
import com.chen.Sign.pojo.Boil;
import com.chen.Sign.service.BoilService;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author 陈帅彬
 * @date 2023/4/29 11:26
 */
@Service
@SuppressWarnings({"all"})
public class BoilServiceImpl extends ServiceImpl<BoilMapper, Boil> implements BoilService {

    @Autowired
    private BoilMapper boilMapper;

    public boolean updateviews(@Param("views") Integer views, @Param("id") Integer id) {
        int addviews = boilMapper.addviews(views, id);
        if (addviews > 0) {
            return true;
        } else {
            return false;
        }
    }
}
