package com.morningtech.eth.server.service;


import com.morningtech.eth.server.entity.SysEthTransfer;
import com.morningtech.eth.server.entity.SysSummarizeTask;
import com.morningtech.eth.server.entity.WalletUser;

import java.util.List;

/**
 * @author xuchunlin
 * @version V1.0
 * @Title: DandanUserService
 * @Package com.hucheng.service
 * @Description: TODO
 * @date 2017/11/28 16:10
 */
public interface SysEthTransferService {

    /**
     * 保存交易记录
    * @Description: TODO
    * @author xuchunlin
    * @date 2018/1/2 10:32
    * @version V1.0
    */
    int saveSysEthTransfer(SysEthTransfer sysEthTransfer) throws Exception;

    int updateByTransfer(SysEthTransfer sysEthTransfer) throws Exception;

    int updateByTransactionHash(SysEthTransfer sysEthTransfer) throws Exception;

    void updateByTransactionHash(List<SysEthTransfer> sysEthTransfer) throws Exception;
    /**
     * 根据hash查询对应的记录
     * @param transactionHash
     * @return
     * @throws Exception
     */
    SysEthTransfer findSysEthTransferByTransactionHash(String transactionHash) throws Exception;

    /**
     * 查询所有自动到账未修改账户金额的记录，状态类型为2
     * @return
     * @throws Exception
     */
    List<SysEthTransfer> findSysEthTransferFinishList() throws Exception;

    /**
     * 查询类型成功入账单未转入到系统账户的记录
     * @return
     * @throws Exception
     */
    List<WalletUser> findSysEthTransferNoToMainList(Integer lastTransferId) throws  Exception;

    /**
     * 成功转入系统账户的记录执行账户金额修改和插入新的转入记录
     * @param id
     * @throws Exception
     */
    void executeTransferAutoAddCoin(Integer id) throws Exception;


    int updateByOutTransactionHash(SysEthTransfer sysEthTransfer) throws Exception;

    List<SysEthTransfer> findSysEthTransferOutPeddingList() throws Exception;

    SysEthTransfer findSummarzeGtId(Integer id, String coinname, String address ) throws Exception;

    int updateByOutTransactionHashById(SysEthTransfer sysEthTransfer) throws Exception;

    SysSummarizeTask findSysSummarizeTaskLastFinish() throws Exception;

    SysSummarizeTask saveSysSummarizeTask(Integer lastTransferId) throws Exception;

    SysEthTransfer findLastEthTransfer() throws Exception;

    int updateLastSummaryTaskFinish() throws Exception;

    List<SysEthTransfer> queryNoFinishSummaryTransferList(Integer lastTransferId) throws Exception;
}
