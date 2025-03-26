package com.whale;

import com.rabbitmq.client.Channel;
import com.whale.*;
public class Main {
    public static void main(String[] args) throws Exception{
        MQchannelmanager manager = new MQchannelmanager("localhost", 5672, "guest", "guest");
        Channel channel = manager.createChannel();
        manager.bindExchange(channel, "EXCHANGE","QUEUE", "ROUTING");

        MQconsumer consumer = new MQconsumer("QUEUE");
        consumer.receive(channel);

        MQproducer producer = new MQproducer(channel);
        producer.send("hello, rabbitmq", "EXCHANGE", "ROUTING");
    }
}
