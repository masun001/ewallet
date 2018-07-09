package com.morningtech.eth.server.eth.bean;

import java.math.BigDecimal;

/**
 * 正在转出记录对象
 *
 * @author xuchunlin
 * @version V1.0
 * @Title: PendingMyzc
 * @Package com.hucheng.wallet.eth.entity
 * @Description: TODO
 * @date 2018/1/25 11:41
 */
public class PendingMyzc {

    private Integer myzcId;//转出记录ID

    private String transactionHash;//交易hash

    private String txid;//第三方提币唯一ID

    private Long time;//存储时间

    private String coinname;

    private String mentionaddress;

    private BigDecimal num;


    public Integer getMyzcId() {
        return myzcId;
    }

    public void setMyzcId(Integer myzcId) {
        this.myzcId = myzcId;
    }

    public String getTransactionHash() {
        return transactionHash;
    }

    public void setTransactionHash(String transactionHash) {
        this.transactionHash = transactionHash;
    }

    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
    }

    public String getTxid() {
        return txid;
    }

    public void setTxid(String txid) {
        this.txid = txid;
    }

    public String getCoinname() {
        return coinname;
    }

    public void setCoinname(String coinname) {
        this.coinname = coinname;
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
}
