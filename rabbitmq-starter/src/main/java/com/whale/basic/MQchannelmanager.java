package com.whale.basic;

import org.springframework.stereotype.Component;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;

import com.rabbitmq.client.ConnectionFactory;

@Component
public class MQchannelmanager {
    static String host;
    static int port;
    static String username;
    static String passwd;

    public MQchannelmanager(String host, int port, String username, String passwd){
        this.host = host;
        this.port = port;
        this.username = username;
        this.passwd = passwd;
    }
    public Channel createChannel() throws Exception{
        final ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(host);
        factory.setPort(port);
        factory.setUsername(username);
        factory.setPassword(passwd);

        Connection connection = factory.newConnection();
        // 复用tcp连接，创建信道
        Channel channel = connection.createChannel();
        return channel;
    }

    public void bindExchange(Channel channel, String exchangeName, String queueName, String routingKey) throws Exception{
        channel.exchangeDeclare(exchangeName, "direct", true);      // 可以多个
        channel.queueDeclare(queueName, true, false, false, null);
        channel.queueBind(queueName, exchangeName, routingKey, null);
    }
}
