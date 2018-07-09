package com.morningtech.eth.server.enums;

/**
 * @author xuchunlin
 * @version V1.0
 * @Title: EnumTransType
 * @Package com.morningtech.eth.server.enums
 * @Description: TODO
 * @date 2018/6/24 12:23
 */
public enum EnumTransType {
    C("充值"),//子账号充值
    G("归集"),//子账号归集
    I("热钱包入币"),//热钱包入币
    T("提币"),//热钱包提币
    S("矿工费"),//热钱包矿工费
    U("未知类型");//充值、G 归集、未知
    private String remark;

    EnumTransType(String remark) {
        this.remark = remark;
    }

    public String getRemark() {
        return remark;
    }
}
