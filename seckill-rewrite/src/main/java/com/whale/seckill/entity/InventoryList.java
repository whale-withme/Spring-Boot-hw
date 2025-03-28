package com.whale.seckill.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventoryList {
    @JsonProperty("seckillId")
    private String seckillId;

    @JsonProperty("inventory")
    private int inventory;
}
