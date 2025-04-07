package com.whale.seckill.dao;

import java.util.Date;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.whale.seckill.entity.SecKill;


public interface SeckillDao extends JpaRepository<SecKill, Long>{
    @Query("select s.inventory from SecKill s where seckillId =: id")
    Integer findInventoryById(@Param("id") Long seckillId);

    @Query("select s.startTime from Seckill s where seckillId =: id")
    Date findStarttimeById(@Param("id") long seckillId);

    @Query("select s.endTime from Seckill s where seckillId =: id")
    Date findEndtimeById(@Param("id") long seckillId);

    @Modifying
    // 注意这里的名字都是实体类名和其中的字段名，映射关系在实体类中已经指明
    @Query("update SecKill s set s.inventory = (s.inventory-1), s.version = (s.version+1) where s.seckillId = :id and s.version = :ver and s.inventory > 0")
    int reduceInventory(@Param("id") long seckillId, @Param("ver") long version);
}