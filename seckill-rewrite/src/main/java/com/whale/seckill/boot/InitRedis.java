package com.whale.seckill.boot;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;

import com.whale.seckill.constant.RedisKey;
import com.whale.seckill.constant.RedisPreKey;
import com.whale.seckill.dao.SeckillDao;
import com.whale.seckill.entity.SecKill;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

public class InitRedis implements CommandLineRunner{
    
    @Autowired
    private SeckillDao seckillDao;

    @Resource(name = "initJedisPool")
    private JedisPool jedisPool;


    public void run(String ...args) throws Exception{
        List<SecKill> seckillList = seckillDao.findAll();
        Jedis jedis = jedisPool.getResource();
        jedis.flushDB();

        for(SecKill seckill : seckillList){
            String seckill_id = RedisKey.SECKILL_ID + seckill.getSeckillId();
            jedis.sadd(RedisKey.SECKILL_ID, seckill_id);

            String inventory = RedisPreKey.INVENTORY + seckill.getSeckillId();
            jedis.set(inventory, String.valueOf(seckill.getInventory()));
        }

        jedis.close();
    }
}
