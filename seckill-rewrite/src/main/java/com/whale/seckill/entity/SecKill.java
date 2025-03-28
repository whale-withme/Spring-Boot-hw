package com.whale.seckill.entity;

import java.sql.Date;

import javax.annotation.Generated;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "seckill")
public class SecKill {
    @Id
    @Column(name = "seckill_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long seckillId;
    
    private String name;
    private int inventory;
    private Date start_time;
    private Date end_time;
    private Date create_time;
    private long version;
}
