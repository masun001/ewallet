package com.morningtech.eth.server.service;

import com.morningtech.eth.server.entity.SysEthMention;

import java.util.List;

/**
 * @author xuchunlin
 * @version V1.0
 * @Title: DandanUserService
 * @Package com.hucheng.service
 * @Description: TODO
 * @date 2017/11/28 16:10
 */
public interface SysEthMentionService {

    /**
     * 查询等待自动入币的转出记录
     * @return
     * @throws Exception
     */
    List<SysEthMention> findPeddingTibList() throws Exception;

    /**
     * 查询钱包已打款到提币账户，但未完成交易的所有记录
     * @return
     * @throws Exception
     */
    List<SysEthMention> findTransactionPeddingList() throws Exception;

    /**
     * 修改转出表记录
     * @param sysEthMention
     * @return
     * @throws Exception
     */
    int updateSysEthMention(SysEthMention sysEthMention) throws Exception;

    /**
     * 根据ID查询转出记录
     * @param id
     * @return
     * @throws Exception
     */
    SysEthMention findSysEthMention(Integer id) throws Exception;

    int updateSysEthMentionLock(SysEthMention sysEthMention) throws Exception;

    int updateSysEthMentionUnLock(SysEthMention sysEthMention) throws Exception;

    int updateSysEthMentionReset(SysEthMention sysEthMention)  throws Exception;
}
