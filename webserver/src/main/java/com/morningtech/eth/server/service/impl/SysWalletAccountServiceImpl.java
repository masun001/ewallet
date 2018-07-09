package com.morningtech.eth.server.service.impl;


import com.morningtech.eth.server.dao.SysWalletAccountMapperDao;
import com.morningtech.eth.server.entity.SysWalletAccount;
import com.morningtech.eth.server.service.SysWalletAccountService;
import com.morningtech.eth.server.service.SysWalletAccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
* @Description: TODO
* @author xuchunlin
* @date 2018/1/3 11:03
* @version V1.0
*/
@Service
public class SysWalletAccountServiceImpl implements SysWalletAccountService {

    @Autowired
    private SysWalletAccountMapperDao dandanWalletAccountMapperDao;

    public List<SysWalletAccount> findList() throws Exception {
        return dandanWalletAccountMapperDao.findAll();
    }

    public int saveSysWalletAccount(SysWalletAccount dandanWalletAccount) throws Exception {
        return dandanWalletAccountMapperDao.insertSelective(dandanWalletAccount);
    }

    public SysWalletAccount findAccountByUserId(Integer userId) throws Exception{
        SysWalletAccount dandanWalletAccount=new SysWalletAccount();
        dandanWalletAccount.setUserid(userId);
        return dandanWalletAccountMapperDao.find("findAccountByUserId",dandanWalletAccount);
    }
}