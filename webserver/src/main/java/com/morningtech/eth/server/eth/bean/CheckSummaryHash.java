package com.morningtech.eth.server.eth.bean;

import java.math.BigDecimal;

/**
 * @author xuchunlin
 * @version V1.0
 * @Title: SummaryHash
 * @Package com.morningtech.eth.server.eth.bean
 * @Description: TODO
 * @date 2018/6/25 16:59
 */
public class CheckSummaryHash {
    private Integer id;
    private String coinname;
    private String outTransactionHash;

    public CheckSummaryHash() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getCoinname() {
        return coinname;
    }

    public void setCoinname(String coinname) {
        this.coinname = coinname;
    }

    public String getOutTransactionHash() {
        return outTransactionHash;
    }

    public void setOutTransactionHash(String outTransactionHash) {
        this.outTransactionHash = outTransactionHash;
    }
}
