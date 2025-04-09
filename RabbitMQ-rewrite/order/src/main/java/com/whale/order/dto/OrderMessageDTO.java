package com.whale.order.dto;

import java.math.BigDecimal;

import com.whale.order.enums.OrderStatusEnum;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderMessageDTO {
    /**
     * 订单 ID
     */
    private Integer orderId;
    /**
     * 订单状态
     */
    private OrderStatusEnum orderStatus;

    /**
     * 订单价格
     */
    private BigDecimal price;

    /**
     * 骑手 ID
     */
    private Integer deliverymanId;

    /**
     * 产品 ID
     */
    private Integer productId;

    /**
     * 用户 ID
     */
    private Integer accountId;

    /**
     * 结算 ID
     */
    private Integer settlementId;

    /**
     * 积分结算 ID
     */
    private Integer rewardId;

    /**
     * 积分奖励数量
     */
    private Integer rewardAmount;

    /**
     * 确认
     */
    private Boolean confirmed;

}
