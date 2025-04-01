package com.whale.dlx;


import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Channel;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.rabbitmq.client.Connection;

@Configuration
public class MQconfig {

    @Bean
    public Connection createConnection(){
        ConnectionFactory connectionFactory = new ConnectionFactory();
        Connection connection = null;
        try{
            connection = connectionFactory.newConnection();
        }catch(IOException | TimeoutException e){
            System.err.println(e);;
        }
        return connection;
    }

    @Bean
    public Channel creatChannel(@Autowired Connection connection){
        Channel channel = null;
        try{
            channel = connection.createChannel();
        }catch(IOException e){
            System.err.println(e);
        }
        return channel;
    }

    @Bean
    public void Config(@Autowired Channel channel){
        Map<String, Object> args = new HashMap<>();
        try{
            // 死信交换机
            channel.exchangeDeclare("dlx_exchange", "direct", true);
            channel.exchangeDeclare("normal_exchange", "fanout", true);

            // 正常队列配置死信队列参数，确定交换机和路由键，修改队列消息过期时间可以做成延迟队列
            args.put("x-message-ttl", 10000);
            args.put("x-dead-letter-exchange", "dlx_exchange");
            args.put("x-dead-letter-routing-key", "routingkey");
            channel.queueDeclare("noramal_queue", true, false, false, args);
            channel.queueBind("normal_queue", "normal_exchange", "");

            channel.queueDeclare("dlx_queue", true, false, false, null);
            channel.queueBind("dlx_queue", "normal_queue", "routingkey");
        }catch(IOException e){
            System.err.println(e);
        }
        
    }
}
