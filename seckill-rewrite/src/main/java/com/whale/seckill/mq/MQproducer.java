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
        String orderJson = new String("");
        try{
            orderJson = objectMapper.writeValueAsString(order);
        }catch(JsonProcessingException err){
            logger.error("订单json解析异常", err);
        }
        rabbitTemplate.convertAndSend(RabbitmqConfig.EXCHANGE_NAME, "", orderJson);
        logger.info("订单发送至队列" + rabbitmqConfig.QUEUE_NAME);
    }
}
