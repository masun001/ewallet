package com.morningtech.eth.server.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;

/**
 * 交易记录对象
* @Description: TODO
* @author xuchunlin
* @date 2018/6/4 22:30
* @version V1.0
*/
public class SysEthTransfer implements Serializable {
    private Integer id;

    private String coinname;

    private String from;

    private String to;

    private String contractaddress;

    private BigInteger value;

    private BigDecimal eth;

    private String blocknumber;

    private String transactionhash;

    private BigInteger gasused;

    private Date systime;

    private Date pendingtime;

    private Date transtime;

    private Date transmaintime;

    private Integer status;//0生成，1待交易，2转入成功，3已添加到账户，4提款成功，5，转出 ，-1失败

    private String transign;

    private int tranStatus=0;//0:默认，1正在转入eth矿工费，2正在转出，3转出成功，-1失败

    private String outTransactionHash;//转出hash

    private String remark;

    private String transtype;//C 充值或 G 归集即转出

    private Integer isdeleted;

    private BigDecimal gas;//总共消耗的燃料费，ether单位

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
        this.coinname = coinname == null ? null : coinname.trim();
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from == null ? null : from.trim();
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to == null ? null : to.trim();
    }

    public String getContractaddress() {
        return contractaddress;
    }

    public void setContractaddress(String contractaddress) {
        this.contractaddress = contractaddress == null ? null : contractaddress.trim();
    }

    public BigInteger getValue() {
        return value;
    }

    public void setValue(BigInteger value) {
        this.value = value;
    }

    public BigDecimal getEth() {
        return eth;
    }

    public void setEth(BigDecimal eth) {
        this.eth = eth;
    }

    public String getBlocknumber() {
        return blocknumber;
    }

    public void setBlocknumber(String blocknumber) {
        this.blocknumber = blocknumber;
    }

    public String getTransactionhash() {
        return transactionhash;
    }

    public void setTransactionhash(String transactionhash) {
        this.transactionhash = transactionhash == null ? null : transactionhash.trim();
    }

    public BigInteger getGasused() {
        return gasused;
    }

    public void setGasused(BigInteger gasused) {
        this.gasused = gasused;
    }

    public Date getSystime() {
        return systime;
    }

    public void setSystime(Date systime) {
        this.systime = systime;
    }

    public Date getPendingtime() {
        return pendingtime;
    }

    public void setPendingtime(Date pendingtime) {
        this.pendingtime = pendingtime;
    }

    public Date getTranstime() {
        return transtime;
    }

    public void setTranstime(Date transtime) {
        this.transtime = transtime;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getTransign() {
        return transign;
    }

    public void setTransign(String transign) {
        this.transign = transign == null ? null : transign.trim();
    }

    public Integer getIsdeleted() {
        return isdeleted;
    }

    public void setIsdeleted(Integer isdeleted) {
        this.isdeleted = isdeleted;
    }

    public Date getTransmaintime() {
        return transmaintime;
    }

    public void setTransmaintime(Date transmaintime) {
        this.transmaintime = transmaintime;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public int getTranStatus() {
        return tranStatus;
    }

    public void setTranStatus(int tranStatus) {
        this.tranStatus = tranStatus;
    }

    public String getOutTransactionHash() {
        return outTransactionHash;
    }

    public void setOutTransactionHash(String outTransactionHash) {
        this.outTransactionHash = outTransactionHash;
    }

    public String getTranstype() {
        return transtype;
    }

    public void setTranstype(String transtype) {
        this.transtype = transtype;
    }

    public BigDecimal getGas() {
        return gas;
    }

    public void setGas(BigDecimal gas) {
        this.gas = gas;
    }

    @Override
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append(from);
        buffer.append(",");
        buffer.append(to);
        buffer.append(",");
        buffer.append(value);
        buffer.append(",");
        buffer.append(transactionhash);
        return buffer.toString();
    }
}