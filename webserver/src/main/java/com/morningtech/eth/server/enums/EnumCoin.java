package com.morningtech.eth.server.enums;

import java.util.ArrayList;
import java.util.List;

/**
 * @author xuchunlin
 * @version V1.0
 * @Title: EnumCoin
 * @Package com.morningtech.eth.server.enums
 * @Description: TODO
 * @date 2018/6/7 22:18
 */
public enum EnumCoin {
    wkb,
    fct,
    rnt,
    usdt,
    dgc,
    eth,
    llt,
    bat,
    rdn,
    knc,
    kin,
    snt,
    wc,
    sdc,
    dcn,
    ust,
    dcc;

    EnumCoin() {
    }

    public static boolean is(String name) {
        List<String> list = new ArrayList();
        EnumCoin[] values = values();
        EnumCoin[] var3 = values;
        int var4 = values.length;
        for (int var5 = 0; var5 < var4; ++var5) {
            EnumCoin enumCoin = var3[var5];
            list.add(enumCoin.name());
        }
        return list.contains(name);
    }
}