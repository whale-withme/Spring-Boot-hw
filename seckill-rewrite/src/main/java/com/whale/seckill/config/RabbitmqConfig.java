package com.whale.seckill.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// 消息队列初始化，一个队列一个交换机

@Configuration
public class RabbitmqConfig {
    public String QUEUE_NAME = "seckill_queue1";
    public static String EXCHANGE_NAME = "seckill_exchange1";
    // private String ROUTING_KEY = "seckill_routingkey";
    public String getQueueName(){ return QUEUE_NAME; }
    public String getExchangeName() { return EXCHANGE_NAME; }
    // public String getRoutingKey()  {  return ROUTING_KEY; }
    
    @Bean
    public Queue queueInit(){
        return new Queue(QUEUE_NAME);     // 定义队列名称
    }

    @Bean
    public FanoutExchange exchangeInit(){
        return new FanoutExchange(EXCHANGE_NAME);
    }

    @Bean
    public Binding bindingInit(){
        Queue seckillQueue = queueInit();
        FanoutExchange seckillExchange = exchangeInit();
        return BindingBuilder.bind(seckillQueue).to(seckillExchange);
    }
}
