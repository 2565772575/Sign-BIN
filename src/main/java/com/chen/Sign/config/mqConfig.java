package com.chen.Sign.config;

import com.chen.Sign.constants.MqConstants;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author 陈帅彬
 * @date 2024/3/13 18:37
 */
@Configuration
public class mqConfig {

    @Bean
    public TopicExchange topicExchange() {
        return new TopicExchange(MqConstants.SIGN_EXCHANGE,true,false);
    }

    @Bean
    public Queue insertQueue() {
        return new Queue(MqConstants.SIGN_INSERT_QUEUE,true);
    }

    @Bean
    public Queue deleteQueue() {
        return new Queue(MqConstants.SIGN_DELETE_QUEUE,true);
    }

    @Bean
    public Binding insertQueueBinging() {
        return BindingBuilder.bind(insertQueue()).to(topicExchange()).with(MqConstants.SIGN_INSERT_KEY);
    }

    @Bean
    public Binding deleteQueueBinging() {
        return BindingBuilder.bind(deleteQueue()).to(topicExchange()).with(MqConstants.SIGN_DELETE_KEY);
    }
}
