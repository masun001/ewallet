package com.morningtech.eth.server;

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.*;
import com.morningtech.eth.server.email.SenderMailServer;
import com.morningtech.eth.server.eth.EthWalletManage;
import com.morningtech.eth.server.eth.TransactionManage;
import com.morningtech.eth.server.eth.contract.ContractManage;
import com.morningtech.eth.server.task.TransferMentionCoinManage;
import com.morningtech.eth.server.task.TransferSummaryCoinManage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * 所有钱包管理服务，包括以太坊或比特币，以及其他币
 * @author xuchunlin
 * @version V1.0
 * @Title: NetWork
 * @Package com.hucheng.api.eth
 * @Description: TODO
 * @date 2017/12/19 15:45
 */
@Component
public class WalletManage {
    public final static Logger logger= LoggerFactory.getLogger(WalletManage.class);

    @Autowired
    private EthWalletManage ethWalletManage;

    @Autowired
    private ContractManage contractManage;

    @Autowired
    private TransactionManage transactionManage;

    @Autowired
    private TransferSummaryCoinManage transferSummaryCoinManage;

    @Autowired
    private TransferMentionCoinManage transferMentionCoinManage;

    @Autowired
    private SenderMailServer senderMailServer;

    @Value("${wallet.server.eth_rub}")
    private boolean ethsAutoincoin=true;//是否开启以太坊坊自动入币功能

    @Value("${wallet.server.eth_tikuan}")
    private boolean ethsAutoTikuan=true;//是否开启从用户地址自动提款

    @Value("${wallet.server.eth_tib}")
    private boolean ethAutoTib=true;//是否开启以太坊自动提取功能

    @Value("${wallet.server.eths.is_open_listener}")
    private boolean isEthsListener=true;//是否开启eth系列监听入库功能

    final static ListeningExecutorService executorService = MoreExecutors.listeningDecorator(Executors.newCachedThreadPool());

    final static ScheduledExecutorService scheduledThreadPoolExecutor =Executors.newSingleThreadScheduledExecutor();

    final static ListeningExecutorService createWalletService = MoreExecutors.listeningDecorator(Executors.newCachedThreadPool());

    /**
     * 初始化钱包服务，以太坊、玩客币、数字黄金等服务
     * @throws Exception
     */
    public void initCoin() throws Exception {
        contractManage.loadContracConfigs();
        //ETH服务启动
        if(ethsAutoincoin) {
            if(isEthsListener){
                logger.info("【2】启动以太坊钱包部署合约且开启监听自动入币出币功能!!!!!!!!!!!![YES]");
            }else {
                logger.info("【2】启动以太坊钱包部署合约且关闭监听自动入币出币功能!!!!!!!!!!!![NO]");
            }
            ethWalletManage.initCoin(this);
        }
        //用户币转入到指定地址任务启动
        if(ethsAutoTikuan) {
            logger.info("【3】启动用户以太坊钱包自动汇集到对应账户!!!!!!!!!!!!");
            transferSummaryCoinManage.transMainTaskStart(this);
        }
        if(ethAutoTib){
            logger.info("【4】启动以太坊钱包自动提币功能!!!!!!!!!!!!");
            transferMentionCoinManage.transTibiTaskStart(this);
        }

    }

    /**
     * 批量创建钱包地址
     * @param num
     * @return
     * @throws InterruptedException
     * @throws ExecutionException
     * @throws TimeoutException
     */
    public List<String> batchCreateWalletAddress(Integer num) throws InterruptedException, ExecutionException, TimeoutException {
        logger.debug("批量创建钱包地址：{} 个-----------------------------start-------------------", num);
        final List<Future> walletfutureList=new ArrayList<>();
        for(int i=0;i< num;i++){
            Future<String> future = createWalletService.submit(new Callable<String>() {
                @Override
                public String call() throws Exception {
                    return ethWalletManage.createAccounts();
                }
            });
            walletfutureList.add(future);
        }

        ListenableFuture[] futures = new ListenableFuture[walletfutureList.size()];
        final ListenableFuture<List<String>> allFutures = Futures.allAsList(walletfutureList.toArray(futures));
        ListenableFuture<List<List<String>>> addressList= Futures.successfulAsList(allFutures);
        List<List<String>> lists = addressList.get(10, TimeUnit.SECONDS);
        if(lists!=null && lists.size()>0){
            logger.debug("批量创建钱包地址：{} 个-----------------------------end-------------------", num);
            return lists.get(0);
        }
        return Lists.newArrayList();
    }

    public void queryBlockNumberTrasactionList() throws Exception{
        ethWalletManage.queryBlockNumberTrasactionList();
    }

    public EthWalletManage getEthWalletManage() {
        return ethWalletManage;
    }

    public TransactionManage getTransactionManage() {
        return transactionManage;
    }

    public SenderMailServer getSenderMailServer() {
        return senderMailServer;
    }


    public TransferSummaryCoinManage getTransferSummaryCoinManage() {
        return transferSummaryCoinManage;
    }

    public boolean isEthAutoTib() {
        return ethAutoTib;
    }

    public ListeningExecutorService getExecutorService() {
        return executorService;
    }

    public boolean isEthsListener() {
        return isEthsListener;
    }

    public ScheduledExecutorService getScheduledThreadPoolExecutor() {
        return scheduledThreadPoolExecutor;
    }

}
