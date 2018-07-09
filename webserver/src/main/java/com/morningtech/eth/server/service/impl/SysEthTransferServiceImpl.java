package com.morningtech.eth.server.service.impl;

import com.morningtech.eth.server.dao.SysEthTransferMapperDao;
import com.morningtech.eth.server.dao.SysEthTransferMapperDao;
import com.morningtech.eth.server.entity.SysEthTransfer;
import com.morningtech.eth.server.entity.SysSummarizeTask;
import com.morningtech.eth.server.entity.WalletUser;
import com.morningtech.eth.server.exception.CheckException;
import com.morningtech.eth.server.service.SysEthTransferService;
import com.morningtech.eth.server.service.SysEthTransferService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author xuchunlin
 * @version V1.0
 * @Title: DandanAdminServiceImpl
 * @Package com.hucheng.service.impl
 * @Description: TODO
 * @date 2017/11/29 17:11
 */
@Service
public class SysEthTransferServiceImpl implements SysEthTransferService {

    @Autowired
    private SysEthTransferMapperDao sysEthTransferMapperDao;

    public int saveSysEthTransfer(SysEthTransfer dandanEthTransfer) throws Exception {
        return sysEthTransferMapperDao.insertSelective(dandanEthTransfer);
    }

    public int updateByTransfer(SysEthTransfer dandanEthTransfer) throws Exception {
        return sysEthTransferMapperDao.update("updateByTransfer",dandanEthTransfer);
    }

    public int updateByTransactionHash(SysEthTransfer dandanEthTransfer) throws Exception {
        return sysEthTransferMapperDao.update("updateByTransactionHash",dandanEthTransfer);
    }

    public void updateByTransactionHash(List<SysEthTransfer> dandanEthTransfer) throws Exception {
        sysEthTransferMapperDao.update("updateBatchByTransactionHash",dandanEthTransfer);
    }

    public SysEthTransfer findSysEthTransferByTransactionHash(String transactionHash) throws Exception {
        SysEthTransfer dandanEthTransfer=new SysEthTransfer();
        dandanEthTransfer.setTransactionhash(transactionHash);
        return sysEthTransferMapperDao.find("findSysEthTransferByTransactionHash", dandanEthTransfer);
    }

    public List<SysEthTransfer> findSysEthTransferFinishList() throws Exception {
        return sysEthTransferMapperDao.findAll("findSysEthTransferFinishList",new SysEthTransfer());
    }

    public List<WalletUser> findSysEthTransferNoToMainList(Integer lastTransferId) throws Exception {
        Map map = new HashMap();
        map.put("lastTransferId", lastTransferId);
        return sysEthTransferMapperDao.findAll("findSysEthTransferNoToMainList",map);
    }

    public void executeTransferAutoAddCoin(Integer id) throws Exception {
        Map map=new HashMap();
        map.put("id",id);
        sysEthTransferMapperDao.find("executeTransferAutoAddCoin",map);
    }

    @Transactional
    @Override
    public int updateByOutTransactionHash(SysEthTransfer sysEthTransfer) throws Exception {
        return sysEthTransferMapperDao.update("updateByOutTransactionHash", sysEthTransfer);
    }

    @Override
    public List<SysEthTransfer> findSysEthTransferOutPeddingList() throws Exception {
        return sysEthTransferMapperDao.findAll("findSysEthTransferOutPeddingList",new SysEthTransfer());
    }

    @Override
    public SysEthTransfer findSummarzeGtId(Integer id, String coinname, String address ) throws Exception {
        SysEthTransfer sysEthTransfer  =new SysEthTransfer();
        sysEthTransfer.setId(id);
        sysEthTransfer.setCoinname(coinname);
        sysEthTransfer.setFrom(address);
        return sysEthTransferMapperDao.find("findSummarzeGtId", sysEthTransfer);
    }

    @Transactional
    @Override
    public int updateByOutTransactionHashById(SysEthTransfer sysEthTransfer) throws Exception {
        return sysEthTransferMapperDao.update("updateByOutTransactionHashById", sysEthTransfer);
    }

    @Override
    public SysSummarizeTask findSysSummarizeTaskLastFinish() throws Exception {
        return sysEthTransferMapperDao.find("findSysSummarizeTaskLastFinish",new SysSummarizeTask());
    }

    @Override
    public SysSummarizeTask saveSysSummarizeTask(Integer lastTransferId) throws Exception {
        SysSummarizeTask sysSummarizeTask = new SysSummarizeTask();
        sysSummarizeTask.setLastTransferId(lastTransferId);
        sysSummarizeTask.setStartTime(new Date());
        int row = sysEthTransferMapperDao.insert("saveSysSummarizeTask", sysSummarizeTask);
        if(row<=0){
            throw new CheckException("任务执行失败");
        }
        return sysSummarizeTask;
    }

    @Override
    public SysEthTransfer findLastEthTransfer() throws Exception {
        return sysEthTransferMapperDao.find("findLastEthTransfer",new SysEthTransfer());
    }

    @Override
    public int updateLastSummaryTaskFinish() throws Exception {
        return sysEthTransferMapperDao.update("updateLastSummaryTaskFinish",new SysEthTransfer());
    }

    @Override
    public List<SysEthTransfer> queryNoFinishSummaryTransferList(Integer lastTransferId) throws Exception {
        SysEthTransfer sysEthTransfer =new SysEthTransfer();
        sysEthTransfer.setId(lastTransferId);
        return sysEthTransferMapperDao.findAll("queryNoFinishSummaryTransferList", sysEthTransfer);
    }
}
