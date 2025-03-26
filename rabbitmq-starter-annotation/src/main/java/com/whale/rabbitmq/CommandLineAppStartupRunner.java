package com.whale.rabbitmq;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;


@Component
public class CommandLineAppStartupRunner implements CommandLineRunner {

    @Autowired
    private MQproducer producer;

    @Autowired
    private MQconsumer consumer;

    @Override
    public void run(String... args) throws Exception {
        // 发送消息
        producer.send("Hello RabbitMQ", "directExchange", "routingKey");
    }
}
