package com.whale.seckill.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.whale.seckill.entity.PayOrder;

public interface PayOrderDao extends JpaRepository<PayOrder, Long>{
    
    boolean existsBySeckillIdAndUserphone(long seckillId, long userphone);
}
