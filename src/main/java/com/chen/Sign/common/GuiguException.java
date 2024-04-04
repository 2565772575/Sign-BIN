package com.chen.Sign.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author 陈帅彬
 * @date 2023/4/29 17:33
 */
@Data
@SuppressWarnings({"all"})
public class GuiguException extends RuntimeException {

    private Integer code;

    private String message;

    private Object data;

    public GuiguException() {
    }

    public Integer getcode() {
        return code;
    }

    public Object getdata() {
        return data;
    }

    public GuiguException(Integer code, String message, Object data) {
        this.message = message;
        this.code = code;
        this.data = data;
    }



}
