package com.morningtech.eth.server.task.bean;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author xuchunlin
 * @version V1.0
 * @Title: LackMentionCoin
 * @Package com.morningtech.eth.server.task.bean
 * @Description: TODO
 * @date 2018/6/8 17:32
 */
public class LackMentionCoin implements Serializable{
    private String coinname;//合约名称
    private BigDecimal ethValue=BigDecimal.ZERO;//缺少的以太坊数量
    private BigDecimal contractValue=BigDecimal.ZERO;//缺少的合约数量

    public String getCoinname() {
        return coinname;
    }

    public void setCoinname(String coinname) {
        this.coinname = coinname;
    }

    public BigDecimal getEthValue() {
        return ethValue;
    }

    public void setEthValue(BigDecimal ethValue) {
        this.ethValue = ethValue;
    }

    public BigDecimal getContractValue() {
        return contractValue;
    }

    public void setContractValue(BigDecimal contractValue) {
        this.contractValue = contractValue;
    }
}
