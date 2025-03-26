package com.whale;

import com.rabbitmq.client.Channel;

import lombok.AllArgsConstructor;


@AllArgsConstructor
public class MQproducer {
    private Channel channel;

    public void send(String message, String exchangeName, String routingKey) throws Exception{
        channel.basicPublish(exchangeName, routingKey, null, message.getBytes());
    }
}
