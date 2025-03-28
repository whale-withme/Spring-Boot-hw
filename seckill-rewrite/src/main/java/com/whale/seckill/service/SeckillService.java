package com.whale.seckill.service;

import com.whale.seckill.entity.SeckillResult;

public interface SeckillService {

    public String getMD5(long seckillId);
    
    public SeckillResult hanlePreSeckill(long seckillId, long userphone, String md5);
}
