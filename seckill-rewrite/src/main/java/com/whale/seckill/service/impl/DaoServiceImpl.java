package com.whale.seckill.service.impl;

import java.util.Date;

import javax.annotation.Resource;
import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.whale.seckill.constant.RedisPreKey;
import com.whale.seckill.dao.PayOrderDao;
import com.whale.seckill.dao.SeckillDao;
import com.whale.seckill.entity.PayOrder;
import com.whale.seckill.entity.SecKill;
import com.whale.seckill.enums.PayOrderStateEnum;
import com.whale.seckill.enums.SeckillStateEnum;
import com.whale.seckill.exception.SeckillException;
import com.whale.seckill.service.DaoService;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

@Service
public class DaoServiceImpl implements DaoService{
    @Autowired
    private SeckillDao seckillDao;

    @Resource(name = "initJedisPool")
    private JedisPool jedisPool;
    
    @Autowired
    private PayOrderDao payOrderDao;

    @Autowired
    private static final Logger logger = LoggerFactory.getLogger(DaoServiceImpl.class);

    @Override
    @Transactional
    public void InsertPayorder(long seckillId, long userphone, Date date) throws SeckillException{
        // int inventoryRemain;
        // try{
        //     inventoryRemain = seckillDao.findInventoryById(seckillId);
        //     if(inventoryRemain <= 0){
        //         logger.error("订单接受处理，数据库商品库存不足");
        //         throw new SeckillException(SeckillStateEnum.SOLD_OUT);
        //     }
        // }catch(SeckillException err){
        //     logger.error(err.toString());
        // }

        // 订单重复检查
        Jedis jedis = jedisPool.getResource();
        String seckillState = RedisPreKey.SECKILL_STATUS + seckillId + userphone;

        PayOrder order = new PayOrder(seckillId, userphone, date, PayOrderStateEnum.UNPAID);
        if(payOrderDao.existsBySeckillIdAndUserphone(seckillId, userphone)){
            logger.error("订单重复秒杀");
            jedis.set(seckillState, SeckillStateEnum.REPEAT_ORDER.toString());
            throw new SeckillException(SeckillStateEnum.REPEAT_ORDER);
        }

        payOrderDao.save(order);
        // 维护已购买集合
        jedis.sadd(RedisPreKey.BOUGHT_SET, String.valueOf(seckillId) + String.valueOf(userphone));
        jedis.set(seckillState, SeckillStateEnum.ORDER_GENERATED.toString());
        jedis.close();
    }

    @Transactional
    @Override
    public void ReduceInventory(long seckillId, long userphone, Date nowtime) throws SeckillException{
        SecKill seckill = seckillDao.findById(seckillId).orElseThrow(() -> new SeckillException(SeckillStateEnum.DATABASE_ERROR));
        Jedis jedis = jedisPool.getResource();

        if(seckill.getInventory() <= 0)
            throw new SeckillException(SeckillStateEnum.SOLD_OUT);

        try{
            int updatecnt = seckillDao.reduceInventory(seckill.getSeckillId(), seckill.getVersion());
            jedis.set(RedisPreKey.SECKILL_STATUS + seckillId + userphone, SeckillStateEnum.DECR_INVENTORY_SUCCESS.toString());
            InsertPayorder(seckill.getSeckillId(), userphone, nowtime);
        }catch(SeckillException err){
            jedis.set(RedisPreKey.SECKILL_STATUS + seckillId + userphone, SeckillStateEnum.DECR_INVENTORY_FAILED.toString());
            if(err.getExceptionState() == SeckillStateEnum.REPEAT_ORDER)
                throw err;
            else
                throw new SeckillException(SeckillStateEnum.LOCK_ERROR);
        }
        jedis.close();
    }
}
