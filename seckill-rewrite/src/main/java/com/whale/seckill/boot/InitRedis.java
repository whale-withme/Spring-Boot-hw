package com.whale.seckill.boot;

import java.util.List;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.whale.seckill.constant.RedisKey;
import com.whale.seckill.constant.RedisPreKey;
import com.whale.seckill.dao.SeckillDao;
import com.whale.seckill.entity.SecKill;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

@Component
public class InitRedis implements CommandLineRunner{
    private static final Logger logger = LoggerFactory.getLogger(InitRedis.class);
    @Autowired
    private SeckillDao seckillDao;

    @Resource(name = "initJedisPool")
    private JedisPool jedisPool;


    public void run(String ...args) throws Exception{
        List<SecKill> seckillList = seckillDao.findAll();
        Jedis jedis = jedisPool.getResource();
        jedis.flushDB();

        logger.info("Redis 初始化开始");
        for(SecKill seckill : seckillList){
            System.out.println(seckill.getSeckillId() + " " + seckill.getInventory());
            String seckill_id = String.valueOf(seckill.getSeckillId());
            jedis.sadd(RedisKey.SECKILL_ID, seckill_id);    // 丢掉了前缀

            String inventory = RedisPreKey.INVENTORY + seckill.getSeckillId();
            jedis.set(inventory, String.valueOf(seckill.getInventory()));
        }
        logger.info("Redis 初始化完毕");
        jedis.close();
    }
}
