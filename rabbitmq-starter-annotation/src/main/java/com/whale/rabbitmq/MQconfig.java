package com.whale.rabbitmq;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;



@Configuration
public class MQconfig {
    @Bean // 可以定义多个队列，方法名不同
    public Queue queue(){
        return new Queue("QUEUE", true); // 开启持久化队列
    }

    @Bean
    public DirectExchange exchange(){
        return new DirectExchange("directExchange", true, false);
    }

    @Bean  // 针对只绑定给定的队列和交换机，使用路由键
    public Binding binding(Queue queue, DirectExchange exchange){
        return BindingBuilder.bind(queue()).to(exchange()).with("routingkey");
    }
}
