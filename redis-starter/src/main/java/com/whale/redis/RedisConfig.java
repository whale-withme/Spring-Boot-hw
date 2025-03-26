package com.whale.redis;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

@Component // 类级别也需要被标记扫描，否则 bean一样不能被resource识别
public class RedisConfig {

    @Bean(name = "jedisPoolConfig")
    public JedisPoolConfig jedisPoolConfig(){
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        jedisPoolConfig.setMaxTotal(10);
        return jedisPoolConfig;
    }
    
    @Bean(name = "InitJedisPool")   
    public JedisPool InitJedisPool(@Qualifier("jedisPoolConfig") JedisPoolConfig poolConfig){
        return new JedisPool(poolConfig, "localhost", 6379);
    }
}
