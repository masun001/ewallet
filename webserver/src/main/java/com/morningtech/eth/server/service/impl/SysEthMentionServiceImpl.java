package com.morningtech.eth.server.service.impl;

import com.morningtech.eth.server.dao.SysEthMentionMapperDao;
import com.morningtech.eth.server.entity.SysEthMention;
import com.morningtech.eth.server.service.SysEthMentionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author xuchunlin
 * @version V1.0
 * @Title: DandanAdminServiceImpl
 * @Package com.hucheng.service.impl
 * @Description: TODO
 * @date 2017/11/29 17:11
 */
@Service
public class SysEthMentionServiceImpl implements SysEthMentionService {

    @Autowired
    private SysEthMentionMapperDao sysEthMentionMapperDao;

    public List<SysEthMention> findPeddingTibList() throws Exception {
        return sysEthMentionMapperDao.findAll("findPeddingTibList",new SysEthMention());
    }

    @Override
    public List<SysEthMention> findTransactionPeddingList() throws Exception {
        return sysEthMentionMapperDao.findAll("findTransactionPeddingList",new SysEthMention());
    }

    public int updateSysEthMention(SysEthMention sysEthMention) throws Exception {
        return sysEthMentionMapperDao.update("updateByPrimaryKeySelective", sysEthMention);
    }

    public SysEthMention findSysEthMention(Integer id) throws Exception {
        return sysEthMentionMapperDao.find(id);
    }

    @Override
    public int updateSysEthMentionLock(SysEthMention sysEthMention) throws Exception {
        return sysEthMentionMapperDao.update("updateSysEthMentionLock" , sysEthMention);
    }

    @Override
    public int updateSysEthMentionUnLock(SysEthMention sysEthMention) throws Exception {
        return sysEthMentionMapperDao.update("updateSysEthMentionUnLock", sysEthMention);
    }

    @Override
    public int updateSysEthMentionReset(SysEthMention sysEthMention) throws Exception {
        return sysEthMentionMapperDao.update("updateSysEthMentionReset", sysEthMention);
    }
}
