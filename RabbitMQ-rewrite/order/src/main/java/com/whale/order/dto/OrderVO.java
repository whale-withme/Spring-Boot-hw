package com.whale.order.dto;

import lombok.Data;

@Data
public class OrderVO {
    private int accountId;
    private int productId;
    private String address;
}
