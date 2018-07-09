package com.morningtech.eth.server.service;

import com.morningtech.eth.server.entity.SysWalletAccount;

import java.util.List;

/**
 * @author xuchunlin
 * @version V1.0
 * @Title: DandanWalletAccountService
 * @Package com.hucheng.service
 * @Description: TODO
 * @date 2017/11/28 16:10
 */
public interface SysWalletAccountService {

    /**
     * 查询所有账户
     * @return
     * @throws Exception
     */
    List<SysWalletAccount> findList() throws Exception;

    /**
     * 持久化新账户
     * @param dandanWalletAccount
     * @return
     * @throws Exception
     */
    int saveSysWalletAccount(SysWalletAccount dandanWalletAccount) throws Exception;

    /**
     * 根据userId查询账户信息
     * @param userId
     * @return
     * @throws Exception
     */
    SysWalletAccount findAccountByUserId(Integer userId) throws Exception;
}
