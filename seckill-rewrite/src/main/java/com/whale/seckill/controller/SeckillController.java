package com.whale.seckill.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.annotation.Resource;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.whale.seckill.constant.RedisKey;
import com.whale.seckill.constant.RedisPreKey;
import com.whale.seckill.entity.Inventory;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

@RestController
@RequestMapping("/seckill/rest")
public class SeckillController {

    @Resource(name = "initJedisPool")
    private JedisPool jedisPool;
    
    @GetMapping("/inventory/all")
    @ResponseBody
    public List<Inventory> RestGetInventoryAll(){
        Jedis jedis = jedisPool.getResource();
        List<Inventory> inventories = new ArrayList<>();  // list 是接口不能直接被实例化
        Set<String> SeckillIdSet = jedis.smembers(RedisKey.SECKILL_ID);

        for(String seckillid : SeckillIdSet){
            String Inventorystr = jedis.get(seckillid);
            inventories.add(new Inventory(Long.parseLong(seckillid), Integer.parseInt(Inventorystr)));
        }
        return inventories;
    }

}
