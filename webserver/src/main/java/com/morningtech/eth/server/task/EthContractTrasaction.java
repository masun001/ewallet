package com.morningtech.eth.server.task;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.morningtech.eth.server.entity.WalletUser;
import com.morningtech.eth.server.eth.EthWalletManage;
import com.morningtech.eth.server.eth.TransactionManage;
import com.morningtech.eth.server.eth.call.PoolingProcessor;
import com.morningtech.eth.server.eth.contract.ContractConfig;
import com.morningtech.eth.server.eth.contract.EthContract;
import com.morningtech.eth.server.eth.contract.GeneralContract;
import com.morningtech.eth.server.eth.enums.EnumContractCoinname;
import com.morningtech.eth.server.eth.enums.EnumTransStatus;
import com.morningtech.eth.server.eth.util.EthWalletUtils;
import com.morningtech.eth.server.util.DesUtils;
import com.morningtech.eth.server.util.ExceptionUtil;
import com.morningtech.eth.server.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.web3j.crypto.CipherException;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.tx.ChainId;
import org.web3j.tx.FastRawTransactionManager;
import org.web3j.tx.RawTransactionManager;
import org.web3j.tx.TransactionManager;
import org.web3j.tx.response.PollingTransactionReceiptProcessor;
import org.web3j.utils.Convert;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * =========提款到主账户====================
 *
 * 负责将用户的币转入到提款账户
 * @author xuchunlin
 * @version V1.0
 * @Title: EthContractTrasaction
 * @Package com.hucheng.wallet.eth.task
 * @Description: TODO
 * @date 2018/1/19 18:34
 */
@Component
public class EthContractTrasaction {
    public final static Logger logger= LoggerFactory.getLogger(EthContractTrasaction.class);

    @Autowired
    private EthWalletManage ethWalletManage;

    @Autowired
    private TransferSummaryCoinManage transferSummaryCoinManage;

    private HashBasedTable<String,String,GeneralContract> accountCoinContractMap=HashBasedTable.create();

    private Map<String,Credentials> credentialsMap= Maps.newConcurrentMap();

    private static  ExecutorService contractPoolExecutor = Executors.newCachedThreadPool();

    private int TRANSFER_COUNT=1;

    public static Map<String , TransactionManager> transactionManagerMap = Maps.newConcurrentMap();

    public synchronized TransactionManager getTransactionManager(String address, Credentials credentials) throws IOException, CipherException {
        TransactionManager transactionManager =   transactionManagerMap.get(address);
        if(transactionManager!=null){
            return transactionManager;
        }
        Web3j web3j = ethWalletManage.admin();
        transactionManager= new FastRawTransactionManager(web3j, credentials, ChainId.NONE, new PoolingProcessor(web3j, 15000, 40));
        transactionManagerMap.put(address, transactionManager);
        return transactionManager;
    }


    /**
     * 提款所有账户到主账户
     * @param walletUsers
     */
    public void withdrawals(List<WalletUser> walletUsers){
        try {
            if(walletUsers.size()>0) {
                logger.debug("##############@@@@@@开始读取所有文件======================" + walletUsers.size() + "笔提款");
                 Map<String,Set<String>> accountCoinMap=new HashMap<>();
                for (WalletUser walletUser : walletUsers) {
                    String accountId =walletUser.getAccountId();
                    String coinname= walletUser.getCoinName();
                    if(accountCoinMap.containsKey(accountId)){
                        accountCoinMap.get(accountId).add(coinname);
                    }else{
                        Set<String> set= Sets.newHashSet();
                        set.add(coinname);
                        accountCoinMap.put(accountId, set);
                    }
                }
                for (WalletUser walletUser : walletUsers) {
                    if(walletUser.getAccountId().toLowerCase().equals(ethWalletManage.getConf().getEthWalletAddr().toLowerCase())){
                        continue;
                    }
                    //账户存在多种币归集，则eth延迟1分钟归集
                    String accountId =walletUser.getAccountId();
                    if(EnumContractCoinname.ETH.getCoinname().equals(walletUser.getCoinName()) && accountCoinMap.get(accountId).size()>1){
                        TimeUnit.SECONDS.sleep(60);
                    }
                    contractPoolExecutor.execute(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                GeneralContract contract = ethWalletManage.contract(walletUser.getCoinName());
                                ContractConfig config = contract.getContractConfig();
                                String coinname= config.getCoinname();
                                String tikAccountId = walletUser.getAccountId();
                                String password = DesUtils.decrypt(walletUser.getPassword());
                                String keyStorePath = ethWalletManage.getKeystorePath(walletUser.getKeystorePath());
                                logger.debug("得到地址：{} 的keystorePath:{}", tikAccountId, keyStorePath);
                                boolean isContractExits = false;
                                if (!accountCoinContractMap.contains(tikAccountId, coinname)) {
                                    Credentials credentials = null;
                                    if (!credentialsMap.containsKey(tikAccountId)) {
                                        long a = System.currentTimeMillis();
                                        // TODO: 2018/6/7 注释任务
                                         long b = System.currentTimeMillis();
                                        logger.debug("{}读取keystore完毕：{}......耗时：{}", ++TRANSFER_COUNT, tikAccountId, (b - a) / 1000);
                                        try {
                                            if (password!=null && keyStorePath != null) {
                                                credentials = ethWalletManage.credentials(password, keyStorePath);
                                                credentialsMap.put(tikAccountId, credentials);
                                            } else {
                                                throw new Exception("获取keystore为空！");
                                            }
                                        } catch (Exception e) {
                                            logger.error("【私钥获取错误1】：{} {}", config.getCoinname(), tikAccountId);
                                            e.printStackTrace();
                                        }
                                    } else {
                                        credentials = credentialsMap.get(tikAccountId);
                                    }
                                    try {
                                        if (credentials != null) {
                                            Web3j web3j = ethWalletManage.admin();
                                            TransactionManager clientTransactionManager=getTransactionManager(tikAccountId, credentials);
                                            GeneralContract abstractContract =null;
                                            if(coinname.equals(EnumContractCoinname.ETH.getCoinname())){
                                                config.setContractBinary(null);
                                                config.setContractAddress(tikAccountId);
                                                abstractContract = EthContract.load(config, tikAccountId, web3j, clientTransactionManager, config.getGasPrice(), config.getGasLimit());
                                            }else{
                                                String address = config.getContractAddress();//合约地址
                                                abstractContract = GeneralContract.load(config, address, web3j, clientTransactionManager, config.getGasPrice(), config.getGasLimit());
                                            }

                                            if (EnumContractCoinname.ETH.getCoinname().equals(config.getCoinname()) || abstractContract.isValid()) {
                                                logger.debug("合约：{}  地址：{}  获取成功！", config.getCoinname(), tikAccountId);
                                                accountCoinContractMap.put(tikAccountId, coinname, abstractContract);
                                                isContractExits = true;
                                            } else {
                                                logger.error("{}合约获取失败：{}", config.getCoinname(), tikAccountId);
                                            }
                                        } else {
                                            logger.error("【私钥获取错误2】：{} {}", config.getCoinname(), tikAccountId);
                                        }
                                    } catch (Exception e) {
                                        logger.error("{}合约或私钥获取失败：{} {}", config.getCoinname(), tikAccountId, ExceptionUtil.getMessage(e));
                                        e.printStackTrace();
                                    }
                                }else{
                                    isContractExits=true;
                                }
                                if (isContractExits) {
                                    /**
                                     * 执行提款操作
                                     */
                                    transferSummaryCoinManage.getWathdrawalsPoolExecutor().submit(new CoinContractTransferTask(transferSummaryCoinManage, walletUser));
                                }else{
                                    logger.error("{}合约获取失败2：{}", config.getCoinname(), tikAccountId);
                                }
                            } catch (Exception e) {
                                logger.error(ExceptionUtil.getMessage(e));
                                e.printStackTrace();
                            }
                        }
                    });
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public class CoinContractTransferTask implements Runnable{
        private TransferSummaryCoinManage transferSummaryCoinManage;

        private WalletUser walletUser;

        public CoinContractTransferTask(TransferSummaryCoinManage transferSummaryCoinManage, WalletUser walletUser){
            this.transferSummaryCoinManage = transferSummaryCoinManage;
            this.walletUser=walletUser;
        }

        @Override
        public void run() {
            this.executeSummaryToMain();
        }

        /**
         *归集账户
         */
        public void executeSummaryToMain(){
            String tikAccountId = walletUser.getAccountId();
            String withdrawalsAddress = transferSummaryCoinManage.getWithdrawalsAddress();
            try {
                logger.debug("【提款操作开始】{}  {}......",tikAccountId, Convert.fromWei(walletUser.getValueWei().toString(), Convert.Unit.ETHER));
                String coinname= walletUser.getCoinName();
                if (accountCoinContractMap.contains(tikAccountId, coinname)) {
                    BigInteger withdrawls_value = walletUser.getValueWei();//提取的数量
                    GeneralContract abstractContract = accountCoinContractMap.get(tikAccountId, coinname);
                    //账户余额
                    BigInteger walletWei = abstractContract.l_getBalance(tikAccountId);
                    logger.debug("{}合约{}地址提币数量为：{}", coinname, tikAccountId, walletWei);
                    //需提取的数量
                    BigInteger toValue = withdrawls_value;
                    if (walletWei.compareTo(withdrawls_value) <0) {//账户余额少于实际提款的余额
                        toValue = walletWei;
                    }
                    if(walletUser.getCoinName().equals(EnumContractCoinname.ETH.getCoinname())){
                        ContractConfig contractConfig = abstractContract.getContractConfig();
                        BigInteger gas = contractConfig.getGasPrice().multiply(contractConfig.getGasLimit());//gas矿工费所需
                        if(toValue.add(gas).compareTo(walletWei)== 1){
                            toValue = walletWei.subtract(gas);//保留本次eth归集所需的手续费以支付本次矿工费
                        }
                    }
                    if(toValue.compareTo(BigInteger.ZERO)>0) {//数量必须大于0.01才能转
                        ethWalletManage.unlockDefault(tikAccountId, walletUser.getPassword());
                        /**
                         * 已转未到账
                         */
                        logger.debug("【开始交易】==================提款:{},到地址：{}========数量：{}", tikAccountId, withdrawalsAddress, Convert.fromWei(toValue.toString(), Convert.Unit.ETHER));
                        abstractContract.l_transfer(withdrawalsAddress, toValue).observable().doOnError(err -> {
                            try {
                                logger.debug("---------------------归集失败，尝试重新归集 {} {}-------------------------", walletUser.getAccountId(), walletUser.getCoinName());
                                TimeUnit.SECONDS.sleep(5);
                                this.executeSummaryToMain();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }).subscribe(tx -> {
                            try {
                                transferSummaryCoinManage.getTransactionServer().updateEthTrasferOutTransactionHash(walletUser.getTransactionHash(), tx.getTransactionHash());
                                logger.debug("############提款到主账户交易转出HASH：{}", tx.getTransactionHash());
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }).unsubscribe();
                    }else{
                        transferSummaryCoinManage.getTransactionServer().updateEthTrasferStatus(walletUser.getTransactionHash(), EnumTransStatus.SUCCESS, "归集数量不足，");
                    }
                }else{
                    logger.error("未获得合约！");
                }
            } catch (Exception e) {
                logger.debug("---------------------归集失败，尝试重新归集 {} {}-------------------------", walletUser.getAccountId(), walletUser.getCoinName());
                try {
                    TimeUnit.SECONDS.sleep(5);
                    this.executeSummaryToMain();
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
                e.printStackTrace();
            }
        }

    }


}
