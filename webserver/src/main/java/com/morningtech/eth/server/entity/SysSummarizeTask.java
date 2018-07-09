package com.morningtech.eth.server.entity;

import java.io.Serializable;
import java.util.Date;

/**
 * @author xuchunlin
 * @version V1.0
 * @Title: SysSummarizeTask
 * @Package com.morningtech.eth.server.entity
 * @Description: TODO
 * @date 2018/6/29 16:07
 */
public class SysSummarizeTask implements Serializable{

    private Integer id;
    private Integer lastTransferId;
    private Date startTime;
    private Date endTime;
    private Integer isfinish;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getLastTransferId() {
        return lastTransferId;
    }

    public void setLastTransferId(Integer lastTransferId) {
        this.lastTransferId = lastTransferId;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public Integer getIsfinish() {
        return isfinish;
    }

    public void setIsfinish(Integer isfinish) {
        this.isfinish = isfinish;
    }
}
