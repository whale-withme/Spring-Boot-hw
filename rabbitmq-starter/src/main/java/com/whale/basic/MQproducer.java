package com.whale.basic;

import org.springframework.stereotype.Component;

import com.rabbitmq.client.Channel;

import lombok.AllArgsConstructor;

@Component
@AllArgsConstructor
public class MQproducer {
    private Channel channel;

    public void send(String message, String exchangeName, String routingKey) throws Exception{
        channel.basicPublish(exchangeName, routingKey, null, message.getBytes());
    }
}
