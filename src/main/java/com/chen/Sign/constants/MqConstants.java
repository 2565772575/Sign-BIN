package com.chen.Sign.constants;

/**
 * @author 陈帅彬
 * @date 2024/3/12 21:03
 */
public class MqConstants {

    /**
     * 交换机
     */
    public final static String SIGN_EXCHANGE = "sign.topic";
    /**
     * 监听新增和修改的队列
     */
    public final static String SIGN_INSERT_QUEUE = "sign.insert.queue";
    /**
     * 监听删除的队列
     */
    public final static String SIGN_DELETE_QUEUE = "sign.delete.queue";
    /**
     * 新增和修改的Routingkey
     */
    public final static String SIGN_INSERT_KEY = "sign.insert";
    /**
     * 删除的Routingkey
     */
    public final static String SIGN_DELETE_KEY = "sign.delete";
}
