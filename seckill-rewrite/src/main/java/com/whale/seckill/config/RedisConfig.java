package com.whale.seckill.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

@Configuration
public class RedisConfig {
    
    @Bean(name = "initJedisConfig")
    public JedisPoolConfig InitJedisPoolConfig(){
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        jedisPoolConfig.setMaxTotal(50);
        return jedisPoolConfig;
    }

    @Bean(name = "initJedisPool")
    public JedisPool InitJedisPool(@Qualifier("initJedisConfig") JedisPoolConfig jedisPoolConfig){
        return new JedisPool(jedisPoolConfig, "localhost", 6379);
    }
}
