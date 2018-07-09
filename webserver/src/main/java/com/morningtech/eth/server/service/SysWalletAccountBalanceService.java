package com.morningtech.eth.server.service;

import com.morningtech.eth.server.entity.SysWalletAccount;
import com.morningtech.eth.server.entity.SysWalletAccountBalance;
import com.morningtech.eth.server.eth.bean.AccountBalance;

import java.util.List;

/**
 * @author maxiaodong
 *
 *
 */
public interface SysWalletAccountBalanceService {

    /**
     * 查询所有账户
     * @return
     * @throws Exception
     */
    List<SysWalletAccountBalance> findList() throws Exception;

    /**
     * 持久化新账户
     * @param dandanWalletAccount
     * @return
     * @throws Exception
     */
    int saveSysWalletAccountBalance(SysWalletAccountBalance dandanWalletAccount) throws Exception;

    /**
     * 根据userId查询账户信息
     * @param userId
     * @return
     * @throws Exception
     */
    SysWalletAccountBalance findAccountByUserId(Integer userId) throws Exception;

    void saveAccountBalance(List<SysWalletAccountBalance> balanceList) throws Exception;
}
