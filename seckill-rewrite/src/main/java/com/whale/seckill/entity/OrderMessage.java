package com.whale.seckill.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderMessage {
    
    @JsonProperty("seckillId")
    private long seckillId;

    @JsonProperty("userPhone")
    private long userPhone;
}
