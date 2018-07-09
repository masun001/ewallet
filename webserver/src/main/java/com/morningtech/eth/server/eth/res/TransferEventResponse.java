package com.morningtech.eth.server.eth.res;

import org.web3j.protocol.core.methods.response.Log;

import java.io.Serializable;
import java.math.BigInteger;

/**
 * 通用交易记录对象
 * @author xuchunlin
 * @version V1.0
 * @Title: TransferEventResponse
 * @Package com.hucheng.wallet.eth.coin
 * @Description: TODO
 * @date 2018/1/5 18:19
 */
public class TransferEventResponse implements Serializable{
    public Log log;

    public String from;

    public String to;

    public BigInteger value;

    public Log getLog() {
        return log;
    }

    public void setLog(Log log) {
        this.log = log;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public BigInteger getValue() {
        return value;
    }

    public void setValue(BigInteger value) {
        this.value = value;
    }
}
