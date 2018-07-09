package com.morningtech.eth.server.eth.enums;

/**
 * 交易状态枚举
 * @author xuchunlin
 * @version V1.0
 * @Title: EnumTransStatus
 * @Package com.hucheng.wallet.eth.enums
 * @Description: TODO
 * @date 2018/1/22 19:25
 */
public enum  EnumTransStatus {
    //0:默认，1正在转入eth矿工费，2正在转出，3转出成功，-1失败
    DEFAULT(0),
    PENDING_GAS(1),
    PENDING_TRANS(2),
    SUCCESS(3),
    FAILURE(-1);

    private int code;

    EnumTransStatus(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
