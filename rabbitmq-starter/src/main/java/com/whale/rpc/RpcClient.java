package com.whale.rpc;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;


public class RpcClient {
    
    private String replyQueue;
    private String requetsQueue;
    private Connection connection;
    private Channel channel;

    public RpcClient() throws IOException, TimeoutException{    // 抛出异常，否则函数内部 try...catch
        ConnectionFactory connectionFactory = new ConnectionFactory();
        connection = connectionFactory.newConnection();
        channel = connection.createChannel();

        replyQueue = channel.queueDeclare().getQueue();
        requetsQueue = "rpc_queue";
    }

    // 远程调用：参数消息，返回结果

    public String call(String message) throws IOException {
        String corrId = UUID.randomUUID().toString();
        final String[] responseHolder = new String[1];  // 用数组存储可变的 response

        BasicProperties properties = new BasicProperties.Builder()
                .correlationId(corrId)
                .replyTo(replyQueue)
                .build();

        // 发送请求消息
        channel.basicPublish("", requetsQueue, properties, message.getBytes(StandardCharsets.UTF_8));

        // 监听回复队列
        channel.basicConsume(replyQueue, true, (consumerTag, delivery) -> {
            String replyCorrId = delivery.getProperties().getCorrelationId();
            if (corrId.equals(replyCorrId)) {
                responseHolder[0] = new String(delivery.getBody(), StandardCharsets.UTF_8);
            }
        }, consumerTag -> {});

        // 轮询等待响应（非阻塞式回调）
        while (responseHolder[0] == null) {
            try {
                Thread.sleep(10); // 轮询等待，避免 CPU 高占用
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IOException("Interrupted while waiting for response", e);
            }
        }

        return responseHolder[0];
    }

}
