package com.whale.order.service.impl;

import com.whale.order.dto.OrderVO;
import com.whale.order.entity.OrderDetail;

public class OrderServiceImpl {
    public void createOrder(OrderVO orderVO){
        OrderDetail order = new OrderDetail().builder()
                                .accountId(orderVO.getAccountId())
                                .productId(orderVO.getProductId())
                                .addre
    }
}
