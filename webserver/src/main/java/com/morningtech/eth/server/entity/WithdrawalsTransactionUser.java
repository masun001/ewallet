package com.morningtech.eth.server.entity;

import java.math.BigInteger;
import java.util.List;

/**
 * 提款账户
 * @author xuchunlin
 * @version V1.0
 * @Title: TransactionUser
 * @Package com.hucheng.wallet.eth.task
 * @Description: TODO
 * @date 2018/1/19 18:15
 */
public class WithdrawalsTransactionUser {
    private String accountId;

    private BigInteger que_eth_unitwei;//缺少的eth数量，单位 wei

    private List<WalletUser> walletUserList;//需要提款的币种及数量列表

    public WithdrawalsTransactionUser() {
    }

    public WithdrawalsTransactionUser(String accountId, BigInteger que_eth_unitwei, List<WalletUser> walletUserList) {
        this.accountId = accountId;
        this.que_eth_unitwei = que_eth_unitwei;
        this.walletUserList = walletUserList;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public BigInteger getQue_eth_unitwei() {
        return que_eth_unitwei;
    }

    public void setQue_eth_unitwei(BigInteger que_eth_unitwei) {
        this.que_eth_unitwei = que_eth_unitwei;
    }

    public List<WalletUser> getWalletUserList() {
        return walletUserList;
    }

    public void setWalletUserList(List<WalletUser> walletUserList) {
        this.walletUserList = walletUserList;
    }
}
