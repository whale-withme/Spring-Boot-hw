package com.whale;

import com.rabbitmq.client.Channel;

public class MQconsumer {
    String queueName;

    public MQconsumer(String queueName){
        this.queueName = queueName;
    }

    public void receive(Channel channel) throws Exception{
        channel.basicConsume(queueName, false,(consumerTag, message) -> {
            String msg = new String(message.getBody());
            System.out.println("收到消息: " + msg);

            // 消息处理完成，手动确认
            channel.basicAck(message.getEnvelope().getDeliveryTag(), false);
        }, consumerTag -> {
            System.out.println("消费者取消监听：" + consumerTag);
        });
    }
}
