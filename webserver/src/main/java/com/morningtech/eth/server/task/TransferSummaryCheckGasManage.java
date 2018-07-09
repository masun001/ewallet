package com.morningtech.eth.server.task;

import com.google.common.collect.Sets;
import com.morningtech.eth.server.entity.WithdrawalsTransactionUser;
import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * =========提款到主账户====================
 *
 *      监听账户eth是否满足提款数量，满足则进行归集到冷钱包
 * @author xuchunlin
 * @version V1.0
 * @Title: CheckGasAutoTransferManage
 * @Package com.hucheng.wallet.eth.task
 * @Description: TODO
 * @date 2018/1/19 18:01
 */
@Component
public class TransferSummaryCheckGasManage {
    //负责检测eth满足矿工费之后提款到主账户任务
    private ScheduledExecutorService scheduledCheckGasEthPoolExecutor = Executors.newSingleThreadScheduledExecutor();

    private TransferSummaryCoinManage transferSummaryCoinManage;
    //账户eth无法支付Gas的列表，加入监听
    private Set<WithdrawalsTransactionUser> noTramsferMainList= Sets.newConcurrentHashSet();

    private ReentrantLock lock =new ReentrantLock(true);

    public void start(TransferSummaryCoinManage transferSummaryCoinManage){
        this.transferSummaryCoinManage=transferSummaryCoinManage;
         Set<String> withdrwalsAccountIdSet=Sets.newHashSet();
        //调度提取所有用户币
        scheduledCheckGasEthPoolExecutor.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                if(noTramsferMainList!=null && noTramsferMainList.size()>0) {
                    lock.lock();
                    try {
                        Iterator iterator = noTramsferMainList.iterator();
                        while (iterator.hasNext()) {
                            WithdrawalsTransactionUser withdrawalsTransactionUser = (WithdrawalsTransactionUser) iterator.next();
                            try {
                                String accountId = withdrawalsTransactionUser.getAccountId();
                                BigInteger totalGas_unitwei = withdrawalsTransactionUser.getQue_eth_unitwei();

                                BigInteger eth_wei = transferSummaryCoinManage.getEthWalletManage().getBlanceEth_UnitWei(accountId);
                                //账户已有的eth数量少于矿工费用，需要主账户转移缺少的eth以支持转账
                                if (eth_wei.compareTo(totalGas_unitwei) >= 0) {
                                    iterator.remove();
                                    System.out.println("监听eth矿工费已满足：：：" + accountId + "    " + eth_wei);
                                    transferSummaryCoinManage.getEthContractTrasaction().withdrawals(withdrawalsTransactionUser.getWalletUserList());
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }finally {
                        lock.unlock();
                    }
                }
            }
        },0, 10, TimeUnit.SECONDS);
    }


    //添加一个账户到监听列表
    public synchronized  void addAccountListener(WithdrawalsTransactionUser withdrawalsTransactionUser){
        lock.lock();
        try {
            if (!noTramsferMainList.contains(withdrawalsTransactionUser.getAccountId())) {
                noTramsferMainList.add(withdrawalsTransactionUser);
            }
        }finally {
            lock.unlock();
        }
    }

}
