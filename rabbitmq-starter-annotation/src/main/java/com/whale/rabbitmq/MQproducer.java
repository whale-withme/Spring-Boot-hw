package com.whale.rabbitmq;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MQproducer {
    @Autowired
    private RabbitTemplate rabbitTemplate;

    public void send(String message, String exchange, String routingKey) {
        rabbitTemplate.convertAndSend(exchange, routingKey, message);  // 发送根据对象转换的消息
        System.out.println("Message sent: " + message);
    }
}
