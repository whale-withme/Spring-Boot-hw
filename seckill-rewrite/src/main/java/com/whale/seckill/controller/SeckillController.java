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
import com.whale.seckill.entity.InventoryList;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

@RestController
@RequestMapping("/seckill/rest")
public class SeckillController {

    @Resource(name = "initJedisPool")
    private JedisPool jedisPool;
    
    @GetMapping("/inventory/all")
    @ResponseBody
    public List<InventoryList> RestGetInventoryAll(){
        Jedis jedis = jedisPool.getResource();
        List<InventoryList> inventories = new ArrayList<>();  // list 是接口不能直接被实例化
        Set<String> SeckillIdSet = jedis.smembers(RedisKey.SECKILL_ID);

        for(String seckillid : SeckillIdSet){
            System.out.println(seckillid);
            String checkString = RedisPreKey.INVENTORY + seckillid;
            String Inventorystr = jedis.get(checkString);

            if(Inventorystr != null)
                inventories.add(new InventoryList(seckillid, Integer.parseInt(Inventorystr)));
        }
        return inventories;
    }

}
