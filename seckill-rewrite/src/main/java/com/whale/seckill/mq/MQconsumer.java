package com.whale.seckill.mq;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import com.rabbitmq.client.Channel;
import com.whale.seckill.entity.OrderMessage;
import com.whale.seckill.enums.SeckillStateEnum;
import com.whale.seckill.exception.SeckillException;
import com.whale.seckill.service.DaoService;

@Component
@RabbitListener(queues = "seckill_queue1", ackMode = "MANUAL")
public class MQconsumer {
    @Autowired
    private DaoService daoService;

    @Autowired
    private static final Logger logger = LoggerFactory.getLogger(MQconsumer.class);

    @RabbitHandler
    public void HandleSeckillConsumer(@Payload OrderMessage order, Channel channel, Message message){
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
