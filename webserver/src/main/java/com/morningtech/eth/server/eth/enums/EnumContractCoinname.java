package com.morningtech.eth.server.eth.enums;

/**
 * @author xuchunlin
 * @version V1.0
 * @Title: EnumContractCoinname
 * @Package com.morningtech.eth.server.eth.enums
 * @Description: TODO
 * @date 2018/6/09 9:25
 */
public enum  EnumContractCoinname {
    ETH("eth");
    private String coinname;

    EnumContractCoinname(String coinname) {
        this.coinname = coinname;
    }

    public String getCoinname() {
        return coinname;
    }
}
