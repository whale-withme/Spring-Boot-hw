package com.whale.seckill.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import com.whale.seckill.enums.PayOrderStateEnum;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
// 增加约束，约束的是数据库中的列名
@Table(
    name = "pay_order",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_seckillid_userphone",
            columnNames = {"seckill_id", "user_phone"}
        )
    }
)
public class PayOrder {
    @Id
    @Column(name = "seckill_id")
    private long seckillId;

    @Column(name = "user_phone")
    private long userphone;

    private Date create_time;
    private PayOrderStateEnum state;

    // public PayOrder(long seckillId, long userphone, Date time, PayOrderStateEnum state){

    // }
}
