package com.whale.seckill.enums;

public enum PayOrderStateEnum{
    REPEAT_ORDER(0, "订单重复"),
    UNPAID(1, "订单未支付");

    private int state;
    private String stateInfo;

    public int getState()   {  return state;}
    public String getInfo() {  return stateInfo;}

    PayOrderStateEnum(int code, String info){
        state = code;
        stateInfo = info;
    }
}
