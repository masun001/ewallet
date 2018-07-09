package com.morningtech.eth.server.task;

import com.google.common.collect.Lists;
import com.morningtech.eth.server.entity.*;
import com.morningtech.eth.server.enums.MyzTrasactionStatus;
import com.morningtech.eth.server.eth.EthWalletManage;
import com.morningtech.eth.server.eth.contract.ContractConfig;
import com.morningtech.eth.server.eth.enums.EnumTransStatus;
import com.morningtech.eth.server.service.SysEthContractService;
import com.morningtech.eth.server.service.SysEthMentionService;
import com.morningtech.eth.server.service.SysEthTransferService;
import com.morningtech.eth.server.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.utils.Convert;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * 交易更新持久接口
 * @author xuchunlin
 * @version V1.0
 * @Title: TransactionServer
 * @Package com.hucheng.wallet.eth.task
 * @Description: TODO
 * @date 2018/1/11 19:06
 */
@Component
public class TransactionServer {
    public final static Logger logger= LoggerFactory.getLogger(TransactionServer.class);
    /**
     * 交易记录历史服务接口
     */
    @Autowired
    private SysEthTransferService sysEthTransferService;

    @Autowired
    private SysEthContractService sysEthContractService;
    /**
     * 转出申请服务接口
     */
    @Autowired
    private SysEthMentionService sysEthMentionService;

    @Autowired
    private EthWalletManage ethWalletManage;

    /**
     * 查询所有用户未提款的记录
     * @return
     * @throws Exception
     */
    public List<WalletUser> findSysEthTransferNoToMainList(Integer lastTransferId) throws Exception {
        return sysEthTransferService.findSysEthTransferNoToMainList(lastTransferId);
    }

    /**
     * 查询所有待提币任务
     * @return
     * @throws Exception
     */
    public List<SysEthMention> findPeddingTibList() throws Exception{
        return sysEthMentionService.findPeddingTibList();
    }

    public SysSummarizeTask findSysSummarizeTaskLastFinish() throws Exception {
        return sysEthTransferService.findSysSummarizeTaskLastFinish();
    }

    public SysSummarizeTask createSummarizeTask(Integer lastTransferId) throws Exception{
        return sysEthTransferService.saveSysSummarizeTask(lastTransferId);
    }

    public Integer findLastId() throws Exception{
       SysEthTransfer sysEthTransfer =  sysEthTransferService.findLastEthTransfer();
       if(sysEthTransfer!=null){
           return sysEthTransfer.getId();
       }
       return null;
    }

    /**
     * 更新所有任务完成
     */
    public void finishLastSummaryTask()  {
        try {
            sysEthTransferService.updateLastSummaryTaskFinish();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<SysEthTransfer> queryNoFinishSummaryTransferList(Integer lastTransId) throws Exception {
        return sysEthTransferService.queryNoFinishSummaryTransferList( lastTransId);
    }

    /**
     * 更新交易记录归集状态
     * @param walletUserList
     * @param enumTransStatus
     * @param remark
     * @throws Exception
     */
    public void updateEthTrasferStatus(List<WalletUser> walletUserList, EnumTransStatus enumTransStatus,String remark) throws Exception{
        StringBuffer buffer=new StringBuffer();
        for(WalletUser walletUser: walletUserList){
            buffer.append(walletUser.getTransactionHash()+",");
        }
        updateEthTrasferStatus(buffer.toString(),enumTransStatus, remark);
    }
    /**
     * 已转未到账
     * @return
     */
    public void updateEthTrasferStatus(String transactionHashs, EnumTransStatus enumTransStatus,String remark) throws Exception {
        if(StringUtils.isNullOrEmpty(transactionHashs)){
            throw  new Exception("交易hash为空，无法修改记录！");
        }
        System.out.println("========更新:"+transactionHashs);
        List<String> transactionHashList= Lists.newArrayList(transactionHashs.split(","));
        transactionHashList.removeAll(Collections.singleton(null));//删除为空的内容
        List<SysEthTransfer> dandanEthTransferList=new ArrayList<>();
        for(String hash: transactionHashList) {
            if(!StringUtils.isNullOrEmpty(hash)) {
                SysEthTransfer dandanEthTransfer = new SysEthTransfer();
                dandanEthTransfer.setTransactionhash(hash);
                dandanEthTransfer.setTranStatus(enumTransStatus.getCode());////0:默认，1正在转入eth矿工费，2正在转出，3转出成功，-1失败
                dandanEthTransfer.setRemark(remark);
                dandanEthTransferList.add(dandanEthTransfer);
            }
        }
        if(dandanEthTransferList.size()>0){
            sysEthTransferService.updateByTransactionHash(dandanEthTransferList);
        }
    }

    /**
     * 归集hash设置
     */
    public void updateEthTrasferOutTransactionHash(String transactionHashs,String outTransactionhash) throws Exception {
        List<String> transactionHashList= Lists.newArrayList(transactionHashs.split(","));
        transactionHashList.removeAll(Collections.singleton(null));//删除为空的内容
        List<SysEthTransfer> dandanEthTransferList=new ArrayList<>();
        Date nowDate = new Date();
        for(String hash: transactionHashList) {
            if(!StringUtils.isNullOrEmpty(hash)) {
                SysEthTransfer dandanEthTransfer = new SysEthTransfer();
                dandanEthTransfer.setTransactionhash(hash);
                dandanEthTransfer.setOutTransactionHash(outTransactionhash);//绑定转出hash到转入记录
                dandanEthTransfer.setRemark("正在归集，等待任务完成!");
                dandanEthTransfer.setTranStatus(EnumTransStatus.PENDING_TRANS.getCode());
                dandanEthTransfer.setTransmaintime(nowDate);//开始归集时间
                dandanEthTransferList.add(dandanEthTransfer);

            }
        }
        if(dandanEthTransferList.size()>0){
            sysEthTransferService.updateByTransactionHash(dandanEthTransferList);
        }
    }

    /**
     * 转出hash绑定
     * @param transactionHashs
     * @throws Exception
     */
    public void updateEthTrasferOutTransactionHash(String transactionHashs,String outTransactionhash, String coinname) throws Exception {
        if(StringUtils.isNullOrEmpty(transactionHashs)){
            throw  new Exception("交易hash为空，无法修改记录！");
        }
        BigDecimal gas =BigDecimal.ZERO;
        if (outTransactionhash != null) {
            TransactionReceipt transactionReceipt = ethWalletManage.queryTransactionReceipt(outTransactionhash);
            if (transactionReceipt != null) {
                BigInteger bigInteger = transactionReceipt.getGasUsed();//得到消耗的gasUsd值 wei
                ContractConfig contractConfig = ethWalletManage.getContractConfig(coinname);
                BigInteger gasPrice = contractConfig.getGasPrice();//gwei转wei
                gas = Convert.fromWei(bigInteger.multiply(gasPrice).toString(), Convert.Unit.ETHER);
            }
        }
        // TODO: 2018/1/19  修改交易的状态为等待提款确认
        List<String> transactionHashList= Lists.newArrayList(transactionHashs.split(","));
        transactionHashList.removeAll(Collections.singleton(null));//删除为空的内容
        List<SysEthTransfer> dandanEthTransferList=new ArrayList<>();
        Date nowDate = new Date();
        for(String hash: transactionHashList) {
            if(!StringUtils.isNullOrEmpty(hash)) {
                SysEthTransfer dandanEthTransfer = new SysEthTransfer();
                dandanEthTransfer.setTransactionhash(hash);
                dandanEthTransfer.setOutTransactionHash(outTransactionhash);//绑定转出hash到转入记录
                dandanEthTransfer.setRemark("提款成功!");
                dandanEthTransfer.setTranStatus(EnumTransStatus.SUCCESS.getCode());
                dandanEthTransfer.setTransmaintime(nowDate);//归集时间
                dandanEthTransfer.setGas(gas);
                dandanEthTransferList.add(dandanEthTransfer);

            }
        }
        if(dandanEthTransferList.size()>0){
            sysEthTransferService.updateByTransactionHash(dandanEthTransferList);
        }
    }

    /**
     * 自动转出失败
     * @param id 记录ID
     * @return
     */
    public void transactionMyzcFailure(Integer id,String transactionHash, String remark) throws Exception {
        if(StringUtils.isNullOrEmpty(id) ){
            throw new Exception("ID为空！");
        }
        SysEthMention dandanMyzc=new SysEthMention();
        dandanMyzc.setId(id);
        dandanMyzc.setStatus(MyzTrasactionStatus.PENDING_HAND.getStatus());
        dandanMyzc.setEndtime(new Date());
        dandanMyzc.setHash(transactionHash);
        dandanMyzc.setIslock(1);
        dandanMyzc.setRemark(remark);
        this.updateSysEthMention(dandanMyzc);
    }

    public void transactionMyzcSuccess(Integer id,String trasactionHash) throws Exception{
        if(StringUtils.isNullOrEmpty(id)){
            throw new Exception("ID为空！");
        }
        SysEthMention dandanMyzc=new SysEthMention();
        dandanMyzc.setId(id);
        dandanMyzc.setHash(trasactionHash);
        dandanMyzc.setStatus(MyzTrasactionStatus.SUCCESS_AUTO.getStatus());
        dandanMyzc.setEndtime(new Date());
        dandanMyzc.setIslock(1);
        dandanMyzc.setRemark("提币成功");
        this.updateSysEthMention(dandanMyzc);
    }

    public void transactionMyzcPedding(Integer id,String trasactionHash) throws Exception{
        if(StringUtils.isNullOrEmpty(id) || StringUtils.isNullOrEmpty(trasactionHash) ){
            throw new Exception("ID或transactionHash为空！");
        }
        SysEthMention dandanMyzc=new SysEthMention();
        dandanMyzc.setId(id);
        dandanMyzc.setHash(trasactionHash);
        dandanMyzc.setEndtime(new Date());
        dandanMyzc.setRemark("等待提币完成");
        dandanMyzc.setIslock(1);
        this.updateSysEthMention(dandanMyzc);
    }

    public SysEthContract findEthContractConfig(String coinname){
        try {
            return sysEthContractService.findEthContractConfig(coinname);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    /**
     * 查询钱包已打款到提币账户，但未完成交易的所有记录
     * @return
     * @throws Exception
     */
    public List<SysEthMention> findTransactionPeddingList() throws Exception {
        return sysEthMentionService.findTransactionPeddingList();
    }

    public void lock(Integer id ) throws Exception{
        lock(id, null);
    }

    public synchronized void lock(Integer id, String remark) throws Exception{
        if(StringUtils.isNullOrEmpty(id)){
            throw new Exception("ID为空！");
        }
        SysEthMention dandanMyzc=new SysEthMention();
        dandanMyzc.setId(id);
        dandanMyzc.setIslock(1);
        dandanMyzc.setLocktime(new Date());
        if(remark!=null){
            dandanMyzc.setRemark(remark);
        }
        int row= sysEthMentionService.updateSysEthMentionLock(dandanMyzc);
        if(row<=0){
            throw new Exception("并发问题，任务已经被锁定，取消此次任务");
        }
    }

    public void unlock(Integer id, String remark) throws Exception{
        SysEthMention dandanMyzc=new SysEthMention();
        dandanMyzc.setId(id);
        dandanMyzc.setIslock(0);
        if(remark!=null){
            dandanMyzc.setRemark(remark);
        }
       sysEthMentionService.updateSysEthMentionUnLock(dandanMyzc);
    }

    public synchronized  int updateSysEthMention(SysEthMention dandanMyzc) throws Exception{
       return sysEthMentionService.updateSysEthMention(dandanMyzc);
    }

    public int updateSysEthMentionReset() throws Exception{
        return sysEthMentionService.updateSysEthMentionReset(new SysEthMention());
    }


    public List<SysEthTransfer> queryPeddingOutTransactionHashs(){
        try {
            return sysEthTransferService.findSysEthTransferOutPeddingList();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void updateTransferOutTransactionFailure(String outTransactionHash) {
        SysEthTransfer sysEthTransfer=new SysEthTransfer();
        sysEthTransfer.setOutTransactionHash(outTransactionHash);
        sysEthTransfer.setTranStatus(EnumTransStatus.FAILURE.getCode());
        sysEthTransfer.setRemark("归集失败");
        try {
            sysEthTransferService.updateByOutTransactionHash(sysEthTransfer);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void updateTransferOutTransactionSuccess(String outTransactionHash, BigDecimal gas){
        SysEthTransfer sysEthTransfer=new SysEthTransfer();
        sysEthTransfer.setOutTransactionHash(outTransactionHash);
        sysEthTransfer.setGas(gas);
        sysEthTransfer.setRemark("归集成功");
        sysEthTransfer.setTranStatus(EnumTransStatus.SUCCESS.getCode());
        try {
            sysEthTransferService.updateByOutTransactionHash(sysEthTransfer);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public SysEthTransfer findSummarzeGtId(Integer id, String coinname, String address) throws Exception{
        return sysEthTransferService.findSummarzeGtId(id, coinname, address);
    }

    public void updateSummarizeFinish(Integer id, String transactionHash) throws Exception {
        SysEthTransfer _sysEthTransfer = new SysEthTransfer();
        _sysEthTransfer.setId(id);
        _sysEthTransfer.setOutTransactionHash(transactionHash);
        _sysEthTransfer.setRemark("归集成功");
        _sysEthTransfer.setTranStatus(EnumTransStatus.SUCCESS.getCode());
        try {
            sysEthTransferService.updateByOutTransactionHashById(_sysEthTransfer);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
