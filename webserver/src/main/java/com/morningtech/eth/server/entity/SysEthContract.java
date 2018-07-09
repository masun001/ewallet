package com.morningtech.eth.server.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * @author xuchunlin
 * @version V1.0
 * @Title: SysEthContract
 * @Package com.morningtech.eth.server.entity
 * @Description: TODO
 * @date 2018/6/11 9:05
 */
public class SysEthContract implements Serializable{

    private Integer id;

    private String coinname;//合约编码

    private String contractBinary;//合约BIN字符串

    private String contractAddress;//合约地址

    private BigInteger gasPrice;//燃料价格

    private BigInteger gasLimit;//限制消耗的最大燃料数量

    private BigInteger decimals;//数量精确度，默认18位，特殊币种例如llt,只有8位

    private BigDecimal maxMentionLimit;//最大提币数量

    private Integer status;

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

    public String getContractBinary() {
        return contractBinary;
    }

    public void setContractBinary(String contractBinary) {
        this.contractBinary = contractBinary;
    }

    public String getContractAddress() {
        return contractAddress;
    }

    public void setContractAddress(String contractAddress) {
        this.contractAddress = contractAddress;
    }

    public BigInteger getGasPrice() {
        return gasPrice;
    }

    public void setGasPrice(BigInteger gasPrice) {
        this.gasPrice = gasPrice;
    }

    public BigInteger getGasLimit() {
        return gasLimit;
    }

    public void setGasLimit(BigInteger gasLimit) {
        this.gasLimit = gasLimit;
    }

    public BigInteger getDecimals() {
        return decimals;
    }

    public void setDecimals(BigInteger decimals) {
        this.decimals = decimals;
    }

    public BigDecimal getMaxMentionLimit() {
        return maxMentionLimit;
    }

    public void setMaxMentionLimit(BigDecimal maxMentionLimit) {
        this.maxMentionLimit = maxMentionLimit;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}
