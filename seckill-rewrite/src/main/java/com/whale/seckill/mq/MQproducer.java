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
import com.whale.seckill.enums.SeckillStateEnum;
import com.whale.seckill.exception.SeckillException;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;

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
        // try {
        //     // 直接发送 OrderMessage 对象，而非 JSON 字符串
        //     rabbitTemplate.convertAndSend(RabbitmqConfig.EXCHANGE_NAME, "", order);
        //     logger.info("订单发送至队列: " + rabbitmqConfig.QUEUE_NAME);
        // } catch (Exception e) {
        //     logger.error("订单发送异常", e);
        // }

        MessageProperties messageProperties = new MessageProperties();
        messageProperties.setExpiration("6000");

        String messagesString = null;
        ObjectMapper mapper = new ObjectMapper();
        try{
            messagesString = mapper.writeValueAsString(order);
        }catch(JsonProcessingException err){
            throw new SeckillException(SeckillStateEnum.JSON_ANALYSIS_FAILED);
        }
        
        Message message = new Message(messagesString.getBytes(), messageProperties);

        rabbitTemplate.send("order.exchange", "key.order", message);
        logger.info("发送订单消息至消息队列...");
    }
}
