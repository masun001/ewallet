package com.morningtech.eth.server.task;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.util.concurrent.*;
import com.morningtech.eth.server.WalletManage;
import com.morningtech.eth.server.email.SenderMailServer;
import com.morningtech.eth.server.entity.SysEthMention;
import com.morningtech.eth.server.eth.bean.PendingMyzc;
import com.morningtech.eth.server.redis.RedisService;
import com.morningtech.eth.server.task.bean.LackMentionCoin;
import com.morningtech.eth.server.util.StringUtils;
import com.morningtech.eth.server.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 自动转出功能管理
 * @author xuchunlin
 * @version V1.0
 * @Title: TransferAutoCoinManage
 * @Package com.hucheng.wallet.eth.task
 * @Description: TODO
 * @date 2018/1/21 12:46
 */
public class TransferMentionCoinManage {

    public final static Logger logger= LoggerFactory.getLogger(TransferMentionCoinManage.class);

    @Autowired
    private TransactionServer transactionServer;//DB接口管理

    @Autowired
    private RedisService redisService;

    private WalletManage walletManage;

    final static ListeningExecutorService mentionService = MoreExecutors.listeningDecorator(Executors.newCachedThreadPool());

    //负责从检测hash是否正确
    final static ScheduledExecutorService scheduledThreadPoolExecutor_checkHash = Executors.newSingleThreadScheduledExecutor();

    //缓存所有已交易hash
    private Map<String,PendingMyzc> hashTimeMap= Maps.newConcurrentMap();

    private Set<Integer> cacheIdSet= Sets.newConcurrentHashSet();

    private Set<String> peddingTransAddressSet= Sets.newConcurrentHashSet();

    private static Long lastSendMailTime=null;

    private ReentrantLock lock =new ReentrantLock(true);

    public void transTibiTaskStart(WalletManage walletManage){
        this.walletManage=walletManage;
        this.resetMentionTask();
        this.loadMentionTaskPeddingHash();//初始化加载未完成的交易hash,用于检测交易是否已经完成
    }

    /**
     * 调度检测数据库提币任务，并执行从热钱包提币到相应的账户
     */
    public void scheduledTransferUserTask(){
        if(walletManage!=null && !walletManage.isEthAutoTib()){
            return;
        }
        List<Future> futureList = Lists.newArrayList();
        try {
            if(lock.tryLock()) {
                List<SysEthMention> sysEthMentionList = transactionServer.findPeddingTibList();
                if (sysEthMentionList.size() > 0) {
                    logger.info("【提币】检测到{}笔提币任务,开始执行任务......", sysEthMentionList.size());
                } else {
                    logger.debug("【提币】未检测到提币任务,停止本次执行任务......");
                    return;
                }

                for (final SysEthMention sysEthMention : sysEthMentionList) {

                     BigDecimal valueEther = sysEthMention.getNum();
                    if (valueEther.compareTo(BigDecimal.ZERO) > 0) {
                        //转出任务，单线程执行，防止存在问题可以及时补救
                        TimeUnit.SECONDS.sleep(3);
                        Future<LackMentionCoin> future = mentionService.submit(new TransferMentionTask(this, sysEthMention));
                        futureList.add(future);
                    }
                }
            }else{
                logger.debug("【提币】上一次提币任务正在执行...");
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            if(futureList.size()>0) {
                ListenableFuture[] futures = new ListenableFuture[futureList.size()];
                final ListenableFuture<List<LackMentionCoin>> allFutures = Futures.allAsList(futureList.toArray(futures));
                Futures.addCallback(allFutures, new FutureCallback<List<LackMentionCoin>>() {
                    @Override
                    public void onSuccess(@Nullable List<LackMentionCoin> result) {
                        try {
                            BigDecimal lackEthNum = BigDecimal.ZERO, lackContractNum = BigDecimal.ZERO;
                            Map<String, BigDecimal> coinLackMap = new HashMap<>();
                            //5分钟一次邮件
                            if (lastSendMailTime == null || Utils.timeoutNowTime(lastSendMailTime, 5, TimeUnit.MINUTES)) {
                                if (result != null && result.size() > 0) {
                                    for (LackMentionCoin lackMentionCoin : result) {
                                        String coinname = lackMentionCoin.getCoinname();
                                        BigDecimal num = coinLackMap.get(coinname);
                                        if (num == null) {
                                            coinLackMap.put(coinname, lackMentionCoin.getContractValue());
                                        } else {
                                            coinLackMap.put(coinname, num.add(lackMentionCoin.getContractValue()));
                                        }
                                        lackEthNum = lackEthNum.add(lackMentionCoin.getEthValue());
                                    }
                                    if (lackEthNum.compareTo(BigDecimal.ZERO) == 1 || coinLackMap.size() > 0) {
                                        String subJect = String.format("[提币]余额不足提醒");
                                        String content = "";
                                        if (lackEthNum.compareTo(BigDecimal.ZERO) == 1) {
                                            content += String.format("ETH缺少:%s <br/>", lackEthNum);
                                        }
                                        for (String key : coinLackMap.keySet()) {
                                            if (key != null && coinLackMap.containsKey(key))
                                                content += String.format("%s[提币]余额不足，至少打入数量：%s (单位ether)<br/>", key.toUpperCase(), coinLackMap.get(key));
                                        }
                                        lastSendMailTime = System.currentTimeMillis();
                                        if (!"".equals(content) && content != null) {
                                            getSenderMailServer().send(subJect, content);
                                        }
                                    }

                                }
                            }
                        }finally {
                            lock.unlock();
                         }
                    }

                    @Override
                    public void onFailure(Throwable t) {
                        try {
                            lock.unlock();
                        }finally {
                         }
                        t.printStackTrace();
                    }
                });
            }else{
                try {
                    lock.unlock();
                }finally {
                 }
            }
        }
     }

    /**
     * 调度检测交易hash是否完成/并修改提币任务状态
     */
    public void scheduledCheckTranstionHashStatusTask(){
        scheduledThreadPoolExecutor_checkHash.execute(new TransferMentionHashCheckTask(this));
    }

    /**
     * 加载数据库中所有等待的hash
     */
    public void loadMentionTaskPeddingHash(){
        try {
           List<SysEthMention> sysEthMentionList =  transactionServer.findTransactionPeddingList();
           for(SysEthMention sysEthMention : sysEthMentionList){
               Integer id= sysEthMention.getId();
               String hash = sysEthMention.getHash();
               this.saveCacheHash(id, hash, sysEthMention.getTxid(), sysEthMention.getAddtime().getTime(), sysEthMention.getCoinname(),
                       sysEthMention.getMentionaddress(), sysEthMention.getNum());
           }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 重启服务、重置已锁定未执行任务的数据
     */
    public void resetMentionTask(){
        try {
            transactionServer.updateSysEthMentionReset();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void saveCacheHash(Integer myzcId,String transactionHash, String txid,String coinname, String mentionaddress, BigDecimal num){
        this.saveCacheHash(myzcId, transactionHash , txid,null, coinname , mentionaddress, num);
    }

    public void saveCacheHash(Integer myzcId,String transactionHash,String txid, Long transTime, String coinname, String mentionaddress, BigDecimal num){
        if(!StringUtils.isNullOrEmpty(transactionHash)) {
            PendingMyzc pendingMyzc=new PendingMyzc();
            pendingMyzc.setTransactionHash(transactionHash);
            if(transTime==null) {
                pendingMyzc.setTime(System.currentTimeMillis());
            }else{
                pendingMyzc.setTime(transTime);
            }
            pendingMyzc.setCoinname(coinname);
            pendingMyzc.setTxid(txid);
            pendingMyzc.setMyzcId(myzcId);
            pendingMyzc.setMentionaddress(mentionaddress);
            pendingMyzc.setNum(num);
            this.getHashTimeMap().put(transactionHash,pendingMyzc);
        }
    }

    public SenderMailServer getSenderMailServer(){
        return walletManage.getSenderMailServer();
    }

    public WalletManage getWalletManage() {
        return walletManage;
    }

    public TransactionServer getTransactionServer() {
        return transactionServer;
    }

    public Map<String, PendingMyzc> getHashTimeMap() {
        return hashTimeMap;
    }

    public RedisService getRedisService() {
        return redisService;
    }
}
