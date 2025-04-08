package com.whale.redis.dao;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.whale.redis.RedisEnums;
import com.whale.redis.User;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.whale.redis.RedisConfig;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

@Service
public class RedisDaoImpl {
    
    @Resource(name = "InitJedisPool") // 指定装配
    private JedisPool jedisPool;

    public User getUserById(String id){
        Jedis jedis = null;
        try{
            jedis = jedisPool.getResource();
            String name = jedis.get(id);
            if(name != null)
                return new User(name, id);
            else
                return new User(String.valueOf(RedisEnums.EMPTY_VALUE), id);
        }
        catch(Exception e){
            System.err.println(e);
        }
        finally{
            if(jedis != null){
                jedis.close();
            }
        }
        return new User(null, id);
    }

    public String addUser(User user){
        Jedis jedis = null;
        try{
            jedis = jedisPool.getResource();
            jedis.setnx(user.getId(), user.getName());
            return RedisEnums.SUCCESS.toString();
        }
        catch(Exception err){
            System.err.println(err);
            return RedisEnums.SET_FAILED.toString();
        }
        finally{
            if(jedis != null)
                jedis.close();
        }
    }

    public String zaddUser(String zset, User user, Double score){
        ObjectMapper objectMapper = new ObjectMapper();
        String useString = null;
        try{
            useString = objectMapper.writeValueAsString(user); // redis zset只能存储字符串
        }catch(JsonProcessingException err){
            System.err.println(err);
            return RedisEnums.JSONPARSE_FAILED.toString();
        }
        
        Jedis jedis = null;
        String result = RedisEnums.ZADD_SUCCESS.toString();
        try{
            jedis = jedisPool.getResource();
            jedis.zadd(zset, score, useString);
        }
        catch(Exception err){
            result = RedisEnums.ZADD_FAILED.toString();
            System.err.println(err);
        }
        finally{
            if(jedis != null)
                jedis.close();
        }
        return result;
    }


}
