package com.chen.Sign.mq;

import com.chen.Sign.service.BlogService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author 陈帅彬
 * @date 2024/3/13 18:25
 */
public class BlogListener {

    @Autowired
    BlogService blogService;


    @RabbitListener(queues = "sign.insert.queue")
    public void listenInsertQueue(Integer id) {
        blogService.insertById(id);
    }

    @RabbitListener(queues = "sign.delete.queue")
    public void listenDeleteQueue(Integer id) {
        blogService.deleteById(id);
    }
}
