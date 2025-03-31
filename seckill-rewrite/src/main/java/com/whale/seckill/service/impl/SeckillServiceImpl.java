package com.whale.seckill.service.impl;


import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.whale.seckill.constant.RedisPreKey;
import com.whale.seckill.entity.OrderMessage;
import com.whale.seckill.entity.SeckillResult;
import com.whale.seckill.enums.SeckillStateEnum;
import com.whale.seckill.mq.MQproducer;
import com.whale.seckill.service.SeckillService;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

@Service
public class SeckillServiceImpl implements SeckillService{

    @Autowired
    private MQproducer mqProducer;

    @Autowired
    private ObjectMapper objectMapper;
    
    @Resource(name = "initJedisPool")
    private JedisPool jedisPool;
    private static final Logger logger = LoggerFactory.getLogger(SeckillServiceImpl.class);
    private final String salt = "aksksks*&&^%%aaaa&^^%%*";

    @Override
    public String getMD5(long seckillId){
        String str = String.valueOf(seckillId) + salt;
        return DigestUtils.md5DigestAsHex(str.getBytes());
    }

    @Override
    public SeckillResult hanlePreSeckill(long seckillId, long userphone, String md5){
        Jedis jedis = jedisPool.getResource();
        String seckillIdstr = String.valueOf(seckillId);
        String inventoryRemainstr = jedis.get(RedisPreKey.INVENTORY + seckillId);
        String boughtOrder = String.valueOf(seckillId) + String.valueOf(userphone);   // 维护已购买商品+用户集合
        long inventoryRemain = 0;

        if(md5.equals(getMD5(seckillId)) != true)
            return new SeckillResult(seckillId, userphone, SeckillStateEnum.MD5_ERROR);

        try{
            inventoryRemain = Integer.parseInt(inventoryRemainstr);
        }catch(NumberFormatException err){
            logger.error("库存剩余量解析整数类型错误", err);
        }
        
        inventoryRemain =  jedis.decr(RedisPreKey.INVENTORY + seckillId);

        // 检查库存订单剩余量
        if(inventoryRemain < 0){
            logger.info(seckillId + "库存售完" + "，剩余库存：" + inventoryRemain);
            jedis.incr(RedisPreKey.INVENTORY + seckillId);  // rollback
            return new SeckillResult(seckillId, userphone, SeckillStateEnum.SOLD_OUT);
        }

        // 是否重复下订单
        if(jedis.sismember(RedisPreKey.BOUGHT_SET, boughtOrder) == true){
            logger.info("重复下单");
            jedis.incr(RedisPreKey.INVENTORY + seckillId);  // rollback
            return new SeckillResult(seckillId, userphone, SeckillStateEnum.REPEAT_ORDER);
        }

        // 维护秒杀状态
        String SeckillStatus = RedisPreKey.SECKILL_STATUS + seckillId + userphone;
        jedis.set(SeckillStatus, String.valueOf(SeckillStateEnum.PROCESSING));

        jedis.close();
        // 发送订单到消息队列
        OrderMessage order = new OrderMessage(seckillId, userphone);
        mqProducer.send(order);

        // 返回给用户
        return new SeckillResult(seckillId, userphone, SeckillStateEnum.PROCESSING);
    }
}
