package com.whale.order.service.impl;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.whale.order.dao.OrderDao;
import com.whale.order.dto.OrderMessageDTO;
import com.whale.order.dto.OrderVO;
import com.whale.order.entity.OrderDetail;
import com.whale.order.enums.OrderStatusEnum;

@Service
public class OrderServiceImpl {
    @Autowired
    private static OrderDao orderDao;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    public void createOrder(OrderVO orderVO) throws JsonProcessingException{

        // 省略订单检查，直接保存到数据库
        OrderDetail order = new OrderDetail().builder()
                                .accountId(orderVO.getAccountId())
                                .productId(orderVO.getProductId())
                                .address(orderVO.getAddress())
                                .build();
        orderDao.save(order);

        // 发送消息给商家微服务校验
        OrderMessageDTO orderMessageDTO = new OrderMessageDTO().builder()
                                        .accountId(orderVO.getAccountId())
                                        .productId(orderVO.getProductId())
                                        .orderId(order.getId())
                                        .orderStatus(OrderStatusEnum.RESTAURANT_VERIFYING)
                                        .build();
        ObjectMapper mapper = new ObjectMapper();
        String messagesString = mapper.writeValueAsString(orderMessageDTO);     // 转换成json格式
        MessageProperties messageProperties = new MessageProperties();

        messageProperties.setExpiration("60000");
        Message message = new Message(messagesString.getBytes(), messageProperties);

        rabbitTemplate.send(
            "exchange.order.restaurant",
            "key.restaurant",
            message
        );
        
    }
}
