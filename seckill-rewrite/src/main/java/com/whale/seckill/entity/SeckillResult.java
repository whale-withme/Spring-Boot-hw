package com.whale.seckill.entity;

import com.whale.seckill.enums.SeckillStateEnum;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SeckillResult {
    private long seckillId;
    private long userphone;
    private SeckillStateEnum state;


}
