package com.whale.basic;

import java.io.IOException;

import org.springframework.stereotype.Component;

import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;

@Component
public class Consumer {
    private Channel channel;
    boolean autoack = false;

    public Consumer(Channel channel){
        this.channel = channel;
    }

    public void consume(String queueName) throws IOException{
        channel.basicConsume(queueName, autoack, new DefaultConsumer(channel) {
            @Override
             public void handleDelivery(String consumerTag,
                               Envelope envelope,
                               AMQP.BasicProperties properties,
                               byte[] body)throws IOException{
                String message = new String(body, "UTF-8");
                System.out.println(message + "已收到");

                long deliveryTag = envelope.getDeliveryTag();
                channel.basicAck(deliveryTag, autoack);
            }
        });
    }
}
