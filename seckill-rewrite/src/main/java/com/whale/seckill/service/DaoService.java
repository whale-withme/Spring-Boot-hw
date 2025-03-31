package com.whale.seckill.service;

import java.util.Date;

import com.whale.seckill.exception.SeckillException;

public interface DaoService {
    void InsertPayorder(long seckillId, long userphone, Date date) throws SeckillException;

    void ReduceInventory(long seckillId, long userphone, Date nowtime) throws SeckillException;
}
