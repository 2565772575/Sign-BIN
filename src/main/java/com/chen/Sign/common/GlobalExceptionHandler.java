package com.chen.Sign.common;

import com.chen.Sign.common.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author 陈帅彬
 * @date 2023/2/6 20:32
 */
@ControllerAdvice
@SuppressWarnings({"all"})
@ResponseBody
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 异常处理方法
     *
     * @return
     */
//    @ExceptionHandler(NullPointerException.class)
//    public Result exceptionHandler(NullPointerException ex) {
//        log.error(ex.getMessage());
//        return new Result(0,"发生异常","空指针异常");
//    }

    // 自定义异常处理
    @ExceptionHandler(value = { GuiguException.class })
    @ResponseBody
    public Result error(GuiguException e) {
        e.printStackTrace();
        return new Result(e.getcode(),e.getMessage(),e.getdata());
    }
}
