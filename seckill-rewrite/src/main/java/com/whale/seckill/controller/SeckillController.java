package com.whale.seckill.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.annotation.Resource;

import org.hibernate.type.StringNVarcharType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.whale.seckill.constant.RedisKey;
import com.whale.seckill.constant.RedisPreKey;
import com.whale.seckill.entity.InventoryList;
import com.whale.seckill.entity.SeckillResult;
import com.whale.seckill.service.SeckillService;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

@RestController
@RequestMapping("/seckill/rest")
public class SeckillController {

    @Resource(name = "initJedisPool")
    private JedisPool jedisPool;

    @Autowired
    private SeckillService seckillService;
    
    @GetMapping("/inventory/all")
    @ResponseBody
    public List<InventoryList> RestGetInventoryAll(){
        Jedis jedis = jedisPool.getResource();
        List<InventoryList> inventories = new ArrayList<>();  // list 是接口不能直接被实例化
        Set<String> SeckillIdSet = jedis.smembers(RedisKey.SECKILL_ID);

        for(String seckillid : SeckillIdSet){
            // System.out.println(seckillid);
            String checkString = RedisPreKey.INVENTORY + seckillid;
            String Inventorystr = jedis.get(checkString);

            if(Inventorystr != null)
                inventories.add(new InventoryList(seckillid, Integer.parseInt(Inventorystr)));
        }
        return inventories;
    }

    @GetMapping("/excute/{seckillId}/{userphone}")
    @ResponseBody
    public SeckillResult ExcutePreMQ(@PathVariable("seckillId") long seckillId, @PathVariable("userphone") long userphone){
        String md5 = seckillService.getMD5(seckillId);
        SeckillResult seckillResult = seckillService.hanlePreSeckill(seckillId, userphone, md5);
        return seckillResult;
    }

    // @GetMapping("/inventory/getmd5")
    // @ResponseBody
    // public String getMD5(long seckillId){
    //     StringNVarcharType
    // }
}
