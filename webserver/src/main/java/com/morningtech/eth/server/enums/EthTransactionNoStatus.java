package com.morningtech.eth.server.enums;

/**
 * 以太坊入币记录状态表
 * @author xuchunlin
 * @version V1.0
 * @Title: EthTransactionNoStatus
 * @Package com.hucheng.common.enums
 * @Description: TODO
 * @date 2018/1/2 11:20
 */
public enum EthTransactionNoStatus {
    CREATE(0),//创建订单
    PENDING(1),//正在处理
    SUCCESS(2),//交易成功
    ADDCOIN(3),//添加财产到账户
    TANSMAIN(4),//财产转移到主账户
    OUT_TRNSFER(5),//转出记录
    FAILURE(-1);//交易失败

    private int status;

    EthTransactionNoStatus(int status) {
        this.status = status;
    }

    public int getStatus() {
        return status;
    }
}
