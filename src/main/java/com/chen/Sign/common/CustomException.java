package com.chen.Sign.common;

/**
 * @author 陈帅彬
 * @date 2023/2/6 20:50
 */

/**
 * 自定义业务异常类
 */
public class CustomException extends RuntimeException {
    public CustomException(String message) {
        super(message);
    }
}
