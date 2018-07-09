package com.morningtech.eth.server.eth.call;

/**
 * @author xuchunlin
 * @version V1.0
 * @Title: PendingCall
 * @Package com.hucheng.wallet.eth.call
 * @Description: TODO
 * @date 2018/1/5 17:51
 */
public interface PendingCall {

    void call(String trasactionHash);
}
