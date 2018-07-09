package com.morningtech.eth.server.entity;


import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * 辅助类，用于封装提款对象
 * @author xuchunlin
 * @version V1.0
 * @Title: WalletUser
 * @Package com.hucheng.common.view
 * @Description: TODO
 * @date 2018/1/11 18:21
 */
public class WalletUser implements Serializable{
    private Integer transId;//自动入账记录ID
    private String transactionHash;//交易hash
    private String coinName;//币种名称
    private BigDecimal valueEther;//账户余额，ether单位
    private BigInteger valueWei;//账户余额，单位wei
    private String accountId;//账户地址
    private String password;//账户加密密码
    private String keystorePath;//钱包文件地址
    private String tranStatus="";//提款状态

    public Integer getTransId() {
        return transId;
    }

    public void setTransId(Integer transId) {
        this.transId = transId;
    }

    public String getTransactionHash() {
        return transactionHash;
    }

    public void setTransactionHash(String transactionHash) {
        this.transactionHash = transactionHash;
    }

    public String getCoinName() {
        return coinName;
    }

    public void setCoinName(String coinName) {
        this.coinName = coinName;
    }

    public BigDecimal getValueEther() {
        return valueEther;
    }

    public void setValueEther(BigDecimal valueEther) {
        this.valueEther = valueEther;
    }

    public BigInteger getValueWei() {
        return valueWei;
    }

    public void setValueWei(BigInteger valueWei) {
        this.valueWei = valueWei;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getKeystorePath() {
        return keystorePath;
    }

    public void setKeystorePath(String keystorePath) {
        this.keystorePath = keystorePath;
    }

    public String getTranStatus() {
        return tranStatus;
    }

    public void setTranStatus(String tranStatus) {
        this.tranStatus = tranStatus;
    }
}
