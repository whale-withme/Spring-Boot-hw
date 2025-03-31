package com.whale.seckill.exception;

import com.whale.seckill.enums.SeckillStateEnum;

public class SeckillException extends RuntimeException{
    private SeckillStateEnum seckillStateEnum;

    public SeckillException(SeckillStateEnum state){
        seckillStateEnum = state;
    }

    public SeckillException(String message){
        super(message);
    }

    public SeckillException(String message, Throwable cause){
        super(message, cause);
    }

    public SeckillStateEnum getExceptionState(){
        return seckillStateEnum;
    }
}
