package com.whale.seckill.enums;

public enum SeckillStateEnum {
    SOLD_OUT(00, "已售完"),
    REPEAT_ORDER(01, "重复下单"),
    MD5_ERROR(02, "商品md5验证错误"),

    PROCESSING(10, "订单已接收，处理中");


    private int state;
    private String stateInfo;

    public String getStateInfo() {  return stateInfo;  }
    public int getState()        {  return state;  }

    SeckillStateEnum(int state, String info){
        this.state = state;
        this.stateInfo = info;
    }
}
