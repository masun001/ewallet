package com.morningtech.eth.server.task;

import com.google.common.collect.Sets;
import com.morningtech.eth.server.WalletManage;
import com.morningtech.eth.server.entity.MentionMsg;
import com.morningtech.eth.server.eth.bean.PendingMyzc;
import com.morningtech.eth.server.eth.contract.GeneralContract;
import com.morningtech.eth.server.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tx.FastRawTransactionManager;
import org.web3j.tx.TransactionManager;
import org.web3j.utils.Numeric;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * 自动提币任务
 * @author xuchunlin
 * @version V1.0
 * @Title: TransactionMainTask
 * @Package com.hucheng.wallet.eth.task
 * @Description: TODO
 * @date 2018/1/8 18:11
 */
public class TransferMentionHashCheckTask implements Runnable {

    public final static Logger logger= LoggerFactory.getLogger(TransferMentionHashCheckTask.class);

    private TransferMentionCoinManage transferMentionCoinManage;

    private WalletManage walletManage;

    public TransferMentionHashCheckTask(TransferMentionCoinManage transferMentionCoinManage) {
       this.transferMentionCoinManage=transferMentionCoinManage;
       this.walletManage=transferMentionCoinManage.getWalletManage();
    }

    @Override
    public void run() {
        try {
           Set<PendingMyzc> trasactionSet=this.getTransactions();
           if(trasactionSet.size()>0){
               logger.debug("检测到存在{}个hash,进行下一步检测任务...", trasactionSet.size());
           }else{
               logger.debug("未检测到提币交易hash,停止本次检测任务...", trasactionSet.size());
               return;
           }
           for(PendingMyzc pendingMyzc: trasactionSet){
               String transactionHash=pendingMyzc.getTransactionHash();
               Integer myzcId=pendingMyzc.getMyzcId();
               //根据hash 查询交易状态
               TransactionReceipt transactionReceip=  walletManage.getEthWalletManage().queryTransactionReceipt(transactionHash);
               if(transactionReceip!=null){
                  String status= transactionReceip.getStatus();
                  if(!StringUtils.isNullOrEmpty(status) && Numeric.containsHexPrefix(status)){
                      String code=Numeric.toBigInt(status).toString();
                      if("0".equals(code)){//失败
                          this.delCacheTranshash(transactionHash);
                          try {
                              GeneralContract contract = walletManage.getEthWalletManage().contract(pendingMyzc.getCoinname());
                              if (contract != null) {
                                  TransactionManager transactionManager = contract.transactionManager();
                                  if (transactionManager instanceof FastRawTransactionManager) {
                                      FastRawTransactionManager rawTransactionManager = (FastRawTransactionManager) transactionManager;
                                      rawTransactionManager.resetNonce();
                                  }
                              }
                          }catch (Exception e){
                          }
                          transferMentionCoinManage.getTransactionServer().transactionMyzcFailure(myzcId, null,"检测hash交易失败");
                      }else if("1".equals(code)){//成功
                          this.delCacheTranshash(transactionHash);
                          transferMentionCoinManage.getTransactionServer().transactionMyzcSuccess(myzcId, null);

                          MentionMsg mentionMsg =new MentionMsg();
                          mentionMsg.setHash(transactionHash);
                          mentionMsg.setStatus(1);
                          mentionMsg.setTime(System.currentTimeMillis());
                          mentionMsg.setTxid(pendingMyzc.getTxid());
                          mentionMsg.setCoinname(pendingMyzc.getCoinname());
                          mentionMsg.setMentionaddress(pendingMyzc.getMentionaddress());
                          mentionMsg.setNum(pendingMyzc.getNum());
                          transferMentionCoinManage.getRedisService().publishMentionRecord(mentionMsg);
                      }
                  }
               }
           }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取超过三分钟的提币转出记录
     * @return
     */
    public Set<PendingMyzc> getTransactions(){
        Set<PendingMyzc> hashList= Sets.newConcurrentHashSet();
       Map<String,PendingMyzc> transHashMap=transferMentionCoinManage.getHashTimeMap();
       Set<String> stringSet= transHashMap.keySet();
       Iterator<String> iterator= stringSet.iterator();
       while (iterator.hasNext()){
           String hash=iterator.next();
           PendingMyzc pendingMyzc=transHashMap.get(hash);
           hashList.add(pendingMyzc);
       }
       return hashList;
    }

    public void delCacheTranshash(String transHash){
        transferMentionCoinManage.getHashTimeMap().remove(transHash);
    }


}
