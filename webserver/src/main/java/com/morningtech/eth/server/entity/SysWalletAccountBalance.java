package com.morningtech.eth.server.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 账户钱包表
 */
public class SysWalletAccountBalance implements Serializable{
    private Integer id;
    private String coinname;//币种
    private String address;//钱包地址
    private BigDecimal balance=BigDecimal.ZERO;//余额
    private Date lasttime;//最后获取时间

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

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public Date getLasttime() {
        return lasttime;
    }

    public void setLasttime(Date lasttime) {
        this.lasttime = lasttime;
    }
}