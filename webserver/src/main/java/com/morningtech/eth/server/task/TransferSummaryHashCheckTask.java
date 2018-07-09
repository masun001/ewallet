package com.morningtech.eth.server.task;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.morningtech.eth.server.WalletManage;
import com.morningtech.eth.server.entity.MentionMsg;
import com.morningtech.eth.server.entity.SysEthTransfer;
import com.morningtech.eth.server.eth.bean.PendingMyzc;
import com.morningtech.eth.server.eth.contract.ContractConfig;
import com.morningtech.eth.server.eth.enums.EnumTransStatus;
import com.morningtech.eth.server.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.utils.Convert;
import org.web3j.utils.Numeric;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Iterator;
import java.util.List;
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
public class TransferSummaryHashCheckTask implements Runnable {

    public final static Logger logger= LoggerFactory.getLogger(TransferSummaryHashCheckTask.class);

    private TransferSummaryCoinManage transferSummaryCoinManage;

    private WalletManage walletManage;

    public TransferSummaryHashCheckTask(TransferSummaryCoinManage transferSummaryCoinManage) {
       this.transferSummaryCoinManage=transferSummaryCoinManage;
       this.walletManage = transferSummaryCoinManage.getWalletManage();
    }

    @Override
    public void run() {
        try {
            transferSummaryCoinManage.checkSummaryFinish();
            List<SysEthTransfer> sysEthTransferList=transferSummaryCoinManage.getTransactionServer().queryPeddingOutTransactionHashs();
           if(sysEthTransferList!=null && sysEthTransferList.size()>0){
               logger.debug("归集检测到存在{}个hash,进行下一步检测任务...", sysEthTransferList.size());
           }else{
               logger.debug("归集未检测到提币交易hash,停止本次检测任务...", sysEthTransferList.size());
               return;
           }
           Map<String, BigDecimal> hashSuccessSet= Maps.newConcurrentMap();
            Set<String> hashFailureSet=Sets.newHashSet();
           for(final SysEthTransfer sysEthTransfer: sysEthTransferList){
               final String transactionHash=sysEthTransfer.getOutTransactionHash();
               final String coinname = sysEthTransfer.getCoinname();
               final String address = sysEthTransfer.getTo();
               final Integer id = sysEthTransfer.getId();

               if(hashFailureSet.contains(transactionHash)){
                    continue;
               }else if(hashSuccessSet.containsKey(transactionHash)){
                    continue;
               }
              try {
                  //根据hash 查询交易状态
                  TransactionReceipt transactionReceip = walletManage.getEthWalletManage().queryTransactionReceipt(transactionHash);
                  if (transactionReceip != null) {
                      String status = transactionReceip.getStatus();
                      if (!StringUtils.isNullOrEmpty(status) && Numeric.containsHexPrefix(status)) {
                          String code = Numeric.toBigInt(status).toString();
                          if ("0".equals(code)) {//失败
                              hashFailureSet.add(transactionHash);
                              transferSummaryCoinManage.getTransactionCacheSet().remove(transactionHash);
                              transferSummaryCoinManage.getTransactionServer().updateEthTrasferStatus(transactionHash, EnumTransStatus.DEFAULT, "");
                          } else if ("1".equals(code)) {//成功
                              hashSuccessSet.put(transactionHash, BigDecimal.ZERO);
                              transferSummaryCoinManage.getTransactionServer().updateTransferOutTransactionSuccess(transactionHash, BigDecimal.ZERO);
                          }
                      }
                  }
              }finally {
                   try {
                       SysEthTransfer sysEthTransfer1 = transferSummaryCoinManage.getTransactionServer().findSummarzeGtId(id, coinname, address);
                       if(sysEthTransfer1!=null){
                           transferSummaryCoinManage.getTransactionServer().updateSummarizeFinish(id, sysEthTransfer1.getTransactionhash());
                       }
                   }catch (Exception e){
                       e.printStackTrace();
                   }
              }
           }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
