package com.morningtech.eth.server.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 提币成功通知对象
 * @author xuchunlin
 * @version V1.0
 * @Title: MentionMsg
 * @Package com.morningtech.eth.server.entity
 * @Description: TODO
 * @date 2018/6/8 20:10
 */
public class MentionMsg implements Serializable{

    private String txid;//第三方提币唯一ID

    private String mentionaddress;

    private String coinname;//币种

    private BigDecimal num;

    private Long time;

    private Integer status;//0交易等待/1交易成功

    private String hash;

    public String getTxid() {
        return txid;
    }

    public void setTxid(String txid) {
        this.txid = txid;
    }

    public String getMentionaddress() {
        return mentionaddress;
    }

    public void setMentionaddress(String mentionaddress) {
        this.mentionaddress = mentionaddress;
    }

    public BigDecimal getNum() {
        return num;
    }

    public void setNum(BigDecimal num) {
        this.num = num;
    }

    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
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

    public String getCoinname() {
        return coinname;
    }

    public void setCoinname(String coinname) {
        this.coinname = coinname;
    }
}
