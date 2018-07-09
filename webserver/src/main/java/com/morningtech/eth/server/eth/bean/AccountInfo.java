package com.morningtech.eth.server.eth.bean;

import java.io.Serializable;
import java.util.Date;

/**
 * @author xuchunlin
 * @version V1.0
 * @Title: AccountInfo
 * @Package com.hucheng.wallet.eth.entity
 * @Description: TODO
 * @date 2017/12/29 16:05
 */
public class AccountInfo implements Serializable {
    private String accountId;

    private String password;

    private String keystorePath;

    private Integer userId;

    private Date createTime;

    private String remark;

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

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}
