package com.morningtech.eth.server.service.impl;

import com.morningtech.eth.server.dao.SysWalletAccountBalanceMapperDao;
import com.morningtech.eth.server.entity.SysWalletAccountBalance;
import com.morningtech.eth.server.eth.bean.AccountBalance;
import com.morningtech.eth.server.service.SysWalletAccountBalanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @program: ewallet
 * @description: ${description}
 * @author: Mark Ma
 * @create: 2018-06-24 14:16
 **/

@Service
public class SysWalletAccountBalanceServiceImpl implements SysWalletAccountBalanceService {


    @Autowired
    private SysWalletAccountBalanceMapperDao walletAccountBalanceMapperDao;

    @Override
    public List<SysWalletAccountBalance> findList() throws Exception {
        return null;
    }

    @Override
    public int saveSysWalletAccountBalance(SysWalletAccountBalance walletAccountBalance) throws Exception {
        return 0;
    }

    @Override
    public SysWalletAccountBalance findAccountByUserId(Integer userId) throws Exception {
        return null;
    }

    @Override
    public void saveAccountBalance(List<SysWalletAccountBalance> balanceList) throws Exception{

        for (SysWalletAccountBalance accountBalance:balanceList) {

            SysWalletAccountBalance resultAccontBalance = walletAccountBalanceMapperDao.find("findAccountBalance", accountBalance);
            if(resultAccontBalance == null){
//                add
                walletAccountBalanceMapperDao.insertSelective(accountBalance);
            }else{
                walletAccountBalanceMapperDao.update("updateAccountBalance", accountBalance);
            }

        }
    }
}
