package com.morningtech.eth.server.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 账户钱包表
 */
public class SysWalletAccount implements Serializable{
    private Integer id;

    private Integer userid;

    private String accountid;

    private String password;

    private String keystorepath;

    private Date createtime;

    private String remark;

    private BigDecimal balance=BigDecimal.ZERO;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getUserid() {
        return userid;
    }

    public void setUserid(Integer userid) {
        this.userid = userid;
    }

    public String getAccountid() {
        return accountid;
    }

    public void setAccountid(String accountid) {
        this.accountid = accountid == null ? null : accountid.trim();
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password == null ? null : password.trim();
    }

    public String getKeystorepath() {
        return keystorepath;
    }

    public void setKeystorepath(String keystorepath) {
        this.keystorepath = keystorepath == null ? null : keystorepath.trim();
    }

    public Date getCreatetime() {
        return createtime;
    }

    public void setCreatetime(Date createtime) {
        this.createtime = createtime;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark == null ? null : remark.trim();
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }
}