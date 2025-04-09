package com.whale.order.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import com.whale.order.entity.OrderDetail;

public interface OrderDao extends JpaRepository<OrderDetail, Integer>{
    
}
