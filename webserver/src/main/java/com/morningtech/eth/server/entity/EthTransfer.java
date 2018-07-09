package com.morningtech.eth.server.entity;

/**
 * @author xuchunlin
 * @version V1.0
 * @Title: EthTransfer
 * @Package com.morningtech.eth.server.entity
 * @Description: TODO
 * @date 2018/6/8 20:39
 */
public class EthTransfer extends  SysEthTransfer{

    private Long time;

    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
    }
}
