package com.whale.seckill.enums;

public enum SeckillStateEnum {
    SOLD_OUT(00, "已售完"),
    REPEAT_ORDER(01, "重复下单"),
    DATABASE_ERROR(03, "数据库相关异常"),
    MD5_ERROR(02, "商品md5验证错误"),
    LOCK_ERROR(04, "乐观锁异常"),
    DECR_INVENTORY_FAILED(05, "库存减少失败"),
    NULL_INFO(06, "无信息"),
    
    DECR_INVENTORY_SUCCESS(12, "库存减少成功"),
    ORDER_GENERATED(11, "订单生成"),
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
