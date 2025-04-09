package com.whale.order.entity;

import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import com.whale.order.enums.OrderStatusEnum;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "order_detail")
public class OrderDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;     // 订单id自动生成

    private OrderStatusEnum status;
    private String address;
    @Column(name = "account_id")
    private int accountId;
    @Column(name = "product_id")
    private int productId;
    @Column(name = "deliveryman_id")
    private int deliverymanId;
    private int settlementId;
    @Column(name = "reward_id")
    private int reward;
    private BigDecimal price;
    private Date date; 
}
