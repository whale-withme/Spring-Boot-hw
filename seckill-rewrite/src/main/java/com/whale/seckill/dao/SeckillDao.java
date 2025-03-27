package com.whale.seckill.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import com.whale.seckill.entity.SecKill;


public interface SeckillDao extends JpaRepository<SecKill, Long>{
    
}
