package com.whale.seckill.mq;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Argument;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Payload;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import com.whale.seckill.entity.OrderMessage;
import com.whale.seckill.exception.SeckillException;
import com.whale.seckill.service.DaoService;


public class Consumer {
    
    // @Autowired
    // private RabbitTemplate rabbitTemplate;

    @Autowired
    private DaoService daoService;

    @Autowired
    private final Logger logger = LoggerFactory.getLogger(Consumer.class);

    @Autowired
    private ObjectMapper mapper;

    @RabbitListener(bindings = {
            @QueueBinding(
                value = @Queue("queue.order"),
                exchange = @Exchange("order.exchange"),
                key = "key.order",
                arguments = {
                    @Argument(name = "x-message-ttl", value = "6000"),
                    @Argument(name = "x-dead-letter-exchange", value = "exchange.dlx")
                }
            )
        },
        ackMode = "MANUAL",
        queues = {"queue.order"}
    )
    public void handleSeckillOrderMessage(@Payload Message message, Channel channel) throws JsonMappingException, JsonProcessingException{
        String messString = message.getBody().toString();
        OrderMessage order = mapper.readValue(messString, OrderMessage.class);
        
        // 异常类处理，可能要设计单独的seckillException
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        Date nowtime = new Date();
        long seckillId = order.getSeckillId();
        long userphone = order.getUserPhone();
        try{
            daoService.ReduceInventory(seckillId, userphone, nowtime);
            channel.basicAck(deliveryTag, false);
            logger.info("库存已减少，订单已创建");
        }catch(SeckillException err){
            logger.error(err.getExceptionState().getStateInfo());

            try{
                channel.basicNack(deliveryTag, false, false);
            }catch(Exception NackE){
                logger.error(NackE.toString());
            }
        }
        catch(Exception err){
            logger.error("消息ack失败，尝试重新处理");

            try{
                channel.basicNack(deliveryTag, false, false);
            }catch(Exception NackE){
                logger.error(NackE.toString());
            }
        }
    }
}
