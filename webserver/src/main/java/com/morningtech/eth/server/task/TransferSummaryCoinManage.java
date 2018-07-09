package com.morningtech.eth.server.task;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.morningtech.eth.server.WalletManage;
import com.morningtech.eth.server.email.SenderMailServer;
import com.morningtech.eth.server.entity.SysEthTransfer;
import com.morningtech.eth.server.entity.SysSummarizeTask;
import com.morningtech.eth.server.entity.WalletUser;
import com.morningtech.eth.server.eth.EthWalletManage;
import com.morningtech.eth.server.exception.CheckException;
import com.morningtech.eth.server.util.StringUtils;
import com.morningtech.eth.server.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.*;

/**
 *  =========提款到主账户====================
 *
 * @author xuchunlin
 * @version V1.0
 * @Title: TransferSummaryCoinManage
 * @Package com.hucheng.wallet.eth.task
 * @Description: TODO
 * @date 2018/1/9 15:25
 */
@Component
public class TransferSummaryCoinManage {
    public final static Logger logger= LoggerFactory.getLogger(TransferSummaryCoinManage.class);

    @Autowired
    private TransactionServer transactionServer;//DB接口管理

    @Autowired
    private EthContractTrasaction ethContractTrasaction;//交易管理

    @Autowired
    private TransferSummaryCheckGasManage transferSummaryCheckGasManage;//检测管理

    private WalletManage walletManage; //钱包管理

    @Value("${eth.withdrawals.address}")
    private String withdrawalsAddress="";

    @Value("${eth.withdrawals.email}")
    private String withdrawalsEmail;

    @Value("${eth.withdrawals.email.cc}")
    private String ccAddress;//抄送人地址，多个逗号隔开

    @Value("${eth.withdrawals.checkethnum_min}")
    private Long wathdrawalsCheckEth_min=0L;//检测调度时间，分钟

    //负责从用户账户提币缺失eth检测任务
    private ExecutorService gasExecutor =Executors.newCachedThreadPool();

    //负责从检测hash是否正确
    final static ScheduledExecutorService scheduledThreadPoolExecutor_checkHash = Executors.newSingleThreadScheduledExecutor();

    //负责转ethr任务,单任务执行
    ExecutorService transferEthPoolExecutor = Executors.newSingleThreadExecutor();
    //负责提款线程池
    ExecutorService wathdrawalsPoolExecutor = Executors.newCachedThreadPool();

    private Set<String> transactionCacheSet= Sets.newConcurrentHashSet();//系统初次读取的未提款交易hash

    private Set<String> peddingSummarizeAccountSet=Sets.newConcurrentHashSet();//正在归集的账户

    private SysSummarizeTask sysSummarizeTask=null;

    public void transMainTaskStart(WalletManage walletManage){
        this.walletManage=walletManage;
        //服务启动、关闭最后一次任务，新启任务执行归集
        transactionServer.finishLastSummaryTask();
        //调度检测钱包是否满足矿工费
        transferSummaryCheckGasManage.start(this);
        //已产生交易等待完成的hash任务检测
        this.summaryHashCheck();
    }

    /**
     * 执行汇总到冷钱包任务
     */
    public synchronized void transferSummaryEthCheckTask() throws Exception{
        //上一次的归集任务完成之后才进行下一次的归集
        while(walletManage==null){
            TimeUnit.SECONDS.sleep(3);
        }
        SysSummarizeTask sysSummarizeTask = this.getTransactionServer().findSysSummarizeTaskLastFinish();
        if(sysSummarizeTask==null || sysSummarizeTask.getIsfinish()==1) {
            Integer lastTransferId = this.getTransactionServer().findLastId();
            if(lastTransferId!=null) {
                List<WalletUser> walletUserList = this.getTransactionServer().findSysEthTransferNoToMainList(lastTransferId);//查询得到所有可以归集的记录
                if (walletUserList != null && walletUserList.size() > 0) {
                    this.sysSummarizeTask = this.getTransactionServer().createSummarizeTask(lastTransferId);
                    gasExecutor.submit(new TransferSummaryGasTask(this, walletUserList));
                }else{
                    throw new CheckException("没有可归集的数据");
                }
            }else{
                throw new CheckException("没有可归集的数据");
            }
        }else{
          throw  new CheckException("上一次的归集任务未完成，请等待上一次完成之后再执行");
        }
    }

    public void checkSummaryFinish(){
        if(sysSummarizeTask!=null) {
            try {
                List<SysEthTransfer> sysEthTransferList=this.getTransactionServer().queryNoFinishSummaryTransferList(sysSummarizeTask.getLastTransferId());
                if(sysEthTransferList==null || sysEthTransferList.size()<=0){
                    transactionServer.finishLastSummaryTask();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 每10秒检查一次归集hash
     */
    public void summaryHashCheck(){
        scheduledThreadPoolExecutor_checkHash.scheduleAtFixedRate(new TransferSummaryHashCheckTask(this),
                0, 10, TimeUnit.SECONDS);
    }

    public TransactionServer getTransactionServer() {
        return transactionServer;
    }

    public WalletManage getWalletManage() {
        return walletManage;
    }

    public EthWalletManage getEthWalletManage() {
        return walletManage.getEthWalletManage();
    }

    public SenderMailServer getSenderMailServer(){
        return walletManage.getSenderMailServer();
    }

    public ExecutorService getTransferEthPoolExecutor() {
        return transferEthPoolExecutor;
    }

    public ExecutorService getWathdrawalsPoolExecutor() {
        return wathdrawalsPoolExecutor;
    }

    public TransferSummaryCheckGasManage getCheckGasAutoTransferManage() {
        return transferSummaryCheckGasManage;
    }

    public EthContractTrasaction getEthContractTrasaction() {
        return ethContractTrasaction;
    }

    public String getWithdrawalsAddress() {
        return withdrawalsAddress;
    }

    public String getWithdrawalsEmail() {
        return withdrawalsEmail;
    }

    public Set<String> getTransactionCacheSet() {
        return transactionCacheSet;
    }

    public Set<String> getPeddingSummarizeAccountSet() {
        return peddingSummarizeAccountSet;
    }

    public String[] getCcAddress() {
        if(StringUtils.isNullOrEmpty(ccAddress)){
            return new String[]{};
        }
        return ccAddress.split(",");
    }

    public synchronized void delCacheByTransactionHashs(String transaction){
        List<String> transactionHashList= Lists.newArrayList(transaction.split(","));
        transactionHashList.removeAll(Collections.singleton(null));//删除为空的内容
        for(String hash: transactionHashList){
            this.getTransactionCacheSet().remove(hash);
        }
    }

}
