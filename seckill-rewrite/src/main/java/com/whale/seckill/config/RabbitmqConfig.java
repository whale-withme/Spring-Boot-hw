package com.whale.seckill.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
// import com.rabbitmq.client.ConnectionFactory;

// 消息队列初始化，一个队列一个交换机

@Configuration
public class RabbitmqConfig {
    public String QUEUE_NAME = "seckill_queue1";
    public static String EXCHANGE_NAME = "seckill_exchange1";

    // 1. 定义JSON消息转换器
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    // 2. 配置消费者监听容器工厂
    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory,
            MessageConverter messageConverter) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter); // 使用JSON转换器
        return factory;
    }


    public String getQueueName(){ return QUEUE_NAME; }
    public String getExchangeName() { return EXCHANGE_NAME; }
    
    @Bean
    public Queue queueInit(){
        return new Queue(QUEUE_NAME);     // 定义队列名称
    }

    @Bean
    public FanoutExchange exchangeInit(){
        return new FanoutExchange(EXCHANGE_NAME);
    }

    @Bean
    public Binding bindingInit(){
        Queue seckillQueue = queueInit();
        FanoutExchange seckillExchange = exchangeInit();
        return BindingBuilder.bind(seckillQueue).to(seckillExchange);
    }
}
