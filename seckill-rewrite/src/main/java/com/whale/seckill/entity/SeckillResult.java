package com.whale.seckill.entity;

import com.whale.seckill.enums.SeckillStateEnum;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SeckillResult {
    private long seckillId;
    private long userphone;
    private int state;
    private String stateInfo;

    public SeckillResult(long seckillid, long userphone, SeckillStateEnum code){
        this.seckillId = seckillid;
        this.userphone = userphone;
        this.state = code.getState();
        this.stateInfo = code.getStateInfo();
    }
}
