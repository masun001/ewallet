package com.morningtech.eth.server.eth.bean;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * @author xuchunlin
 * @version V1.0
 * @Title: AccountBalance
 * @Package com.morningtech.eth.server.eth.bean
 * @Description: TODO
 * @date 2018/6/24 12:56
 */
public class AccountBalance implements Serializable {

    private static final long serialVersionUID = 6448792120368005057L;

    private String coinname;//币种
    private String address;//钱包地址
    private BigDecimal balance=BigDecimal.ZERO;//余额
    private Date lasttime;//最后获取时间

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
