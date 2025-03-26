package com.whale.rabbitmq;

import java.io.IOException;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;
import org.springframework.amqp.support.AmqpHeaders;

import com.rabbitmq.client.Channel;

@Component
public class MQconsumer {
    
    @RabbitListener(queues = "QUEUE", ackMode = "MANUAL")   // 监听指定的队列，消息确认机制为手动处理，防止消息未成功处理也被消费
    public void receive(String message, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
        try{
            System.out.println(message);
            channel.basicAck(deliveryTag, false);   // 手动确认消息ack
        }catch(Exception err){
            System.err.println(err);
            try{
                channel.basicNack(deliveryTag, false, true);
            }catch(IOException e){
                System.err.println(e);
            }
        }   
    }
}
