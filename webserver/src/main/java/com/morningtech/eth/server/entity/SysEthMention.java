package com.morningtech.eth.server.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 转出提币记录申请表
* @Description: TODO
* @author xuchunlin
* @date 2018/6/4 22:31
* @version V1.0
*/
public class SysEthMention implements Serializable {
    private Integer id;

    private String txid;

    private Integer userid;

    private String username;

    private String coinname;

    private String mentionaddress;

    private BigDecimal num;

    private Date addtime;

    private Date endtime;

    private Date locktime;

    private Integer status;

    private String hash;

    private Integer isdeleted;

    private String remark;

    private Integer islock;

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

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getCoinname() {
        return coinname;
    }

    public void setCoinname(String coinname) {
        this.coinname = coinname;
    }

    public BigDecimal getNum() {
        return num;
    }

    public void setNum(BigDecimal num) {
        this.num = num;
    }

    public Date getAddtime() {
        return addtime;
    }

    public void setAddtime(Date addtime) {
        this.addtime = addtime;
    }

    public Date getEndtime() {
        return endtime;
    }

    public void setEndtime(Date endtime) {
        this.endtime = endtime;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public Integer getIsdeleted() {
        return isdeleted;
    }

    public void setIsdeleted(Integer isdeleted) {
        this.isdeleted = isdeleted;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public Integer getIslock() {
        return islock;
    }

    public void setIslock(Integer islock) {
        this.islock = islock;
    }

    public String getMentionaddress() {
        return mentionaddress;
    }

    public void setMentionaddress(String mentionaddress) {
        this.mentionaddress = mentionaddress;
    }

    public String getTxid() {
        return txid;
    }

    public void setTxid(String txid) {
        this.txid = txid;
    }

    public Date getLocktime() {
        return locktime;
    }

    public void setLocktime(Date locktime) {
        this.locktime = locktime;
    }
}