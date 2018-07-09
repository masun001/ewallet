package com.morningtech.eth.server.eth.res;

import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tx.response.Callback;

/**
 * @author xuchunlin
 * @version V1.0
 * @Title: MyCallback
 * @Package com.hucheng.wallet.eth.res
 * @Description: TODO
 * @date 2018/1/8 19:49
 */
public class TransactionPeddingCallback implements Callback {

    @Override
    public void accept(TransactionReceipt transactionReceipt) {

    }

    @Override
    public void exception(Exception e) {

    }
}
