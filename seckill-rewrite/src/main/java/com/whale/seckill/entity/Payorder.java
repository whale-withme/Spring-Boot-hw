package com.whale.seckill.entity;

import java.sql.Date;

import com.whale.seckill.enums.SeckillStateEnum;

import lombok.Data;

@Data
public class Payorder {
    private long seckillId;
    private long userphone;
    private Date create_time;
    private SeckillStateEnum state;
}
