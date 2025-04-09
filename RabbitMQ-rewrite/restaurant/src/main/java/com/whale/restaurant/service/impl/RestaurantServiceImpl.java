package com.whale.restaurant.service.impl;

import org.springframework.amqp.rabbit.annotation.Argument;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;

import com.whale.restaurant.dto.OrderMessageDTO;
import com.whale.restaurant.service.RestaurantService;


public class RestaurantServiceImpl implements RestaurantService{
    @RabbitListener(
        bindings = {
            @QueueBinding(
                value = @Queue("queue.restaurant"),
                exchange = @Exchange("exchange.order.restaurant"),
                key = "key.restaurant",
                arguments = {
                    // queue.restaurant ttl 设置6s
                    @Argument(name = "x-message-ttl", value = "6000"),

                    // 绑定死信交换机
                    @Argument(name = "x-dead-letter-exchange", value = "dlx.exchange")
                }
            ),
            @QueueBinding(
                value = @Queue("queue.dlx"),
                exchange = @Exchange("dlx.exchange"),
                key = "#"
            )
        },
        queues = {"queue.restaurant"}       // 只监听此队列
    )
    public void handleOrderMessage(OrderMessageDTO orderMessageDTO){

    }
}
