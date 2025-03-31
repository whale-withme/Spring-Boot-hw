package com.whale.seckill.mq;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.whale.seckill.config.RabbitmqConfig;
import com.whale.seckill.entity.OrderMessage;

@Component
public class MQproducer {

    private final RabbitmqConfig rabbitmqConfig;
    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private static final Logger logger = LoggerFactory.getLogger(MQproducer.class);

    MQproducer(RabbitmqConfig rabbitmqConfig) {
        this.rabbitmqConfig = rabbitmqConfig;
    }

    public void send(OrderMessage order){
        try {
            // 直接发送 OrderMessage 对象，而非 JSON 字符串
            rabbitTemplate.convertAndSend(RabbitmqConfig.EXCHANGE_NAME, "", order);
            logger.info("订单发送至队列: " + rabbitmqConfig.QUEUE_NAME);
        } catch (Exception e) {
            logger.error("订单发送异常", e);
        }
    }
}
