package com.morningtech.eth.server.enums;

/**
 * 自动转出status状态
 * @author xuchunlin
 * @version V1.0
 * @Title: EthTransactionNoStatus
 * @Package com.hucheng.common.enums
 * @Description: TODO
 * @date 2018/1/2 11:20
 */
public enum MyzTrasactionStatus {
    PENDING_AUDIT(0),//待审核
    SUCCESS_AUTO(1),//自动转出成功
    SUCCESS_HAND(2),//人工转出成功
    PENDING_HAND(3),//等待人工处理
    PENDING_AUTO(4),//等待自动处理
    FAILURE(-1);//撤销转出币

    private int status;

    MyzTrasactionStatus(int status) {
        this.status = status;
    }

    public int getStatus() {
        return status;
    }
}
