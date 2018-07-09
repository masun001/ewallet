package com.morningtech.eth.server.eth;


import com.alibaba.fastjson.JSON;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.morningtech.eth.server.WalletManage;
import com.morningtech.eth.server.eth.bean.AccountInfo;
import com.morningtech.eth.server.eth.call.MyRawTransactionManager;
import com.morningtech.eth.server.eth.call.PoolingProcessor;
import com.morningtech.eth.server.eth.contract.ContractConfig;
import com.morningtech.eth.server.eth.contract.ContractManage;
import com.morningtech.eth.server.eth.contract.EthContract;
import com.morningtech.eth.server.eth.contract.GeneralContract;
import com.morningtech.eth.server.eth.enums.EnumContractCoinname;
import com.morningtech.eth.server.eth.util.EthWalletUtils;
import com.morningtech.eth.server.util.DesUtils;
import com.morningtech.eth.server.util.StringUtils;
import jnr.ffi.provider.converters.EnumSetConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.web3j.abi.datatypes.Int;
import org.web3j.crypto.CipherException;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.WalletUtils;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.admin.Admin;
import org.web3j.protocol.admin.methods.response.PersonalUnlockAccount;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.Transaction;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.geth.Geth;
import org.web3j.protocol.http.HttpService;
import org.web3j.protocol.ipc.UnixIpcService;
import org.web3j.protocol.ipc.WindowsIpcService;
import org.web3j.tx.*;
import org.web3j.utils.Async;
import org.web3j.utils.Convert;
import org.web3j.utils.Numeric;
import rx.Subscription;

import javax.annotation.Nullable;
import javax.annotation.PreDestroy;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 以太坊钱包管理服务
 * @author xuchunlin
 * @version V1.0
 * @Title: NetWork
 * @Package com.hucheng.api.eth
 * @Description: TODO
 * @date 2017/12/19 15:45
 */
@Component
public class EthWalletManage {

    public final static Logger logger= LoggerFactory.getLogger(EthWalletManage.class);

    @Autowired
    private Conf conf;

    @Autowired
    private BlockNumberManage blockNumberManage;

    @Autowired
    private ContractManage contractManage;

    private Admin web3j=null;

    private WalletManage walletManage=null;

    private Map<String,GeneralContract> contractMap= Maps.newConcurrentMap();

    private Map<String,AccountInfo> accountInfoMap= Maps.newConcurrentMap();

    private Map<String, List<Subscription>> subscriptionMap= Maps.newConcurrentMap();

    private String catchEthBlockNumber="";//拉取历史交易所在块

    private static ReentrantLock lock =new ReentrantLock(true);

    private static Credentials credentials =null;

    private static ScheduledFuture scheduledFuture=null;

    private static TransactionManager clientTransactionManager=null;

    public Web3j web3j(){
        return Web3j.build(new HttpService(conf.getEthWalletUrl()),5000L, Async.defaultExecutorService());
    }

    public Admin admin() {
        synchronized (this){
            if(isConnection()){
                return web3j;
            }else{
                String osType= System.getProperty("os.name").toLowerCase();
                if(conf.isHttp()){
                    web3j= Geth.build(new HttpService(conf.getEthWalletUrl()));
                    logger.debug("使用rpc http模式访问...");
                }else {
                    if (osType.indexOf("windows") >= 0) {
                        web3j=  Geth.build(new WindowsIpcService(conf.getEthWalletSocketPath()));
                        logger.debug("[Windows]使用rpc IPC模式访问...");
                    } else {
                        web3j=  Geth.build(new UnixIpcService(conf.getEthWalletSocketPath()));
                        logger.debug("[Unix]使用rpc IPC模式访问...");
                    }
                }
                logger.debug("connection::::::"+isConnection());
                return  web3j;
            }
        }
    }

    public synchronized TransactionManager getTransactionManager(Credentials credentials) throws IOException, CipherException {
        if(clientTransactionManager!=null){
            return clientTransactionManager;
        }
        clientTransactionManager= new FastRawTransactionManager(web3j, credentials, ChainId.NONE, new PoolingProcessor(web3j, 15000, 40));
        return clientTransactionManager;
    }
    /**
     * 判断rpc是否连接成功
     * @return
     */
    public boolean isConnection(){
        try {
            if(web3j==null){
                return false;
            }
            return web3j.netListening().send().isListening();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 获取凭证
     * @return
     * @throws IOException
     * @throws CipherException
     */
    public Credentials credentials() throws IOException, CipherException {
        if(credentials!=null){
            return credentials;
        }
        return credentials = credentials(conf.getEthWalletPswd(), conf.getEthWalletPath());
    }

    public Credentials credentials(String pswd, String keystorePath) throws IOException, CipherException {
        return  WalletUtils.loadCredentials(pswd, keystorePath);
    }

    /**
     * 创建以太坊钱包，返回地址
     */
    public String createAccounts() throws Exception {
        String password=com.morningtech.eth.server.util.UUID.randomShortUUID();
        String keystoreDir= conf.getEthWalletDir();
        String fileName=WalletUtils.generateLightNewWalletFile(password, new File(keystoreDir));
        String accountId="";
        if(!StringUtils.isNullOrEmpty(fileName)){
            accountId= Numeric.prependHexPrefix(fileName.split("--")[2].replace(".json",""));
        }
        if(!accountInfoMap.containsKey(accountId)) {
            AccountInfo accountInfo = new AccountInfo();
            accountInfo.setAccountId(accountId);
            accountInfo.setPassword(DesUtils.encrypt(password));
            accountInfo.setKeystorePath(fileName);

            walletManage.getTransactionManage().saveAccount(accountInfo);
            accountInfoMap.put(accountId, accountInfo);
             logger.info("#wallet#  创建新账户：{}，文件：{}" , accountId, fileName);
        }else{
            logger.debug("已存在的账户:{}",accountId);
        }
        return accountId;
    }

    public boolean isSysAccount(String address){
        return accountInfoMap.containsKey(address);
    }
    /**
     * 获取私钥文件全路径
     * @param fileName
     * @return
     */
    public String getKeystorePath(String fileName){
        return String.format("%s%s%s",conf.getEthWalletDir(),File.separator,fileName);
    }

    /**
     * 解锁主账户
     * @throws ExecutionException
     * @throws InterruptedException
     */
    public void unlockMain() throws Exception{
        unlockDefault(conf.getEthWalletAddr(),conf.getEthWalletPswd());
    }

    /**
     * 解锁单个账户,默认1分钟
     * @param address
     * @param password
     * @throws ExecutionException
     * @throws InterruptedException
     */
    public void unlockDefault(String address, String password) throws Exception{
        unlock(address,password,BigInteger.valueOf(1000*60*1));
    }

    /**
     * 解锁账户，并设置持续的时间
     * @param address
     * @param password
     * @param duration
     * @throws ExecutionException
     * @throws InterruptedException
     */
    public void unlock(String address, String password,BigInteger duration) throws Exception{
        admin().personalUnlockAccount(address,password,duration).send();
    }

    /**
     * 解锁主账户
     * @throws ExecutionException
     * @throws InterruptedException
     * @throws IOException
     */
    public void lockMain(){
        lock(conf.getEthWalletAddr());
    }

    /**
     * 解锁任意账户
     * @param address
     * @throws ExecutionException
     * @throws InterruptedException
     * @throws IOException
     */
    public void lock(String address){
        Admin admin=admin();
        if(admin instanceof Geth) {
           ((Geth) admin).personalLockAccount(address).sendAsync();
        }
    }

    /**
     * 获取钱包下的kestore下所有钱包地址
     * @return
     * @throws IOException
     */
    public List<String> getAccountlist() throws IOException {
        return  admin().personalListAccounts().send().getAccountIds();
    }

    /**
     * 初始化加载所有账户到内存中
     */
    public void loadAccounts(){
        try {
            String mainAddress=getConf().getEthWalletAddr().trim().toLowerCase();
            if(!accountInfoMap.containsKey(mainAddress)){
                logger.debug("####################部署主账户地址：{}",getConf().getEthWalletAddr());
                accountInfoMap.put(mainAddress,new AccountInfo());
            }
            List<AccountInfo> accountInfoList= walletManage.getTransactionManage().getAccountInfoList();
            for (AccountInfo accountInfo: accountInfoList) {
                accountInfoMap.put(accountInfo.getAccountId(), accountInfo);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 查询hash详细交易信息
     am transactionHash
     * @return
     */
    public TransactionReceipt queryTransactionReceipt(String transactionHash){
        try {
            Optional<TransactionReceipt> ethGetTransactionReceipt = this.web3j.ethGetTransactionReceipt(transactionHash).send().getTransactionReceipt();
            if(ethGetTransactionReceipt.isPresent()){
                TransactionReceipt transactionReceipt= ethGetTransactionReceipt.get();
                return transactionReceipt;
            }
        }catch (IOException e){
            logger.error("获取hash信息错误：{}",e.getMessage());
        }
        return null;
    }

    /**
     * 火币网的地址用于测试：0x3f5CE5FBFe3E9af3971dD833D26bA9b5C936f0bE
     * 初始化合约以及钱包
     * @throws Exception
     */
    public void initCoin(WalletManage walletManage) throws Exception {
        this.walletManage=walletManage;
        this.loadAccounts();
        this.web3j=this.admin();//初始化钱包连接，监听
        if(isConnection()){
            blockNumberManage.scheduleStart(this);
            if(walletManage.isEthsListener()) {//是否开启自动入币出币
                this.triggerEthListener();//以太币事物监听
            }
        }else{
            throw new Exception("服务连接失败!!!!!!!!!!!!");
        }
        this.web3j.ethGetTransactionByHash("0xb98b10c1aed0327252ecd39161c4e521a744f9bd1b081513054600bb8bf767d2").sendAsync().thenAcceptAsync((tx)->{
           Optional<Transaction> transaction =  tx.getTransaction();
           Transaction transaction1 = transaction.get();
            System.out.println(transaction1.getGasPrice());
        });
        logger.debug("获取到上次读取最新合约块blockNumber:{}",blockNumberManage.getContractBlockNumber().toString());
        logger.debug("获取到上次读取最新ETH块blockNumber:{}",blockNumberManage.getEthBlockNumber().toString());

        Set<String> coinList = contractManage.getCoinList();
        this.loadEthContracts(coinList,false);
    }

    /**
     * 加载ETH及所有配置的合约
     * @param coinList
     * @param isInit
     * @throws Exception
     */
    public void loadEthContracts(Set<String> coinList, boolean isInit) throws Exception{
        if(coinList==null || coinList.size()<=0){
            return;
        }
        lock.lock();
        try {
            final List<Future> futureList = Lists.newArrayList();
            final String walletAddr = getConf().getEthWalletAddr();
            TransactionManager clientTransactionManager = getTransactionManager(credentials());
            for (final String coinname : coinList) {
                    if (isInit) {
                        logger.debug("新增初始化合约：{}", coinname.toUpperCase());
                    } else {
                        logger.debug("初始化合约：{}", coinname.toUpperCase());
                    }
                    if (contractMap.containsKey(coinname)) {
                        continue;
                    }

                    final ContractConfig contractConfig = this.getContractConfig(coinname);
                    if (EnumContractCoinname.ETH.getCoinname().equals(coinname.toLowerCase())) {
                        contractConfig.setContractBinary(null);
                        contractConfig.setContractAddress(walletAddr);

                        EthContract ethContract = EthContract.load(contractConfig, walletAddr, web3j, clientTransactionManager, contractConfig.getGasPrice(), contractConfig.getGasLimit());
                        contractMap.put(coinname, ethContract);
                        logger.debug("{}部署完毕！验证：{}，热钱包账户余额：{}", coinname, true, 0);
                    } else {
                        ListenableFuture<Integer> future = walletManage.getExecutorService().submit(new Callable<Integer>() {
                            @Override
                            public Integer call() {
                                String contractAddress = contractConfig.getContractAddress();
                                try {
//                                    TransactionManager clientTransactionManager = new FastRawTransactionManager(web3j, credentials, ChainId.MAINNET, new PoolingProcessor(web3j, 15000, 40));
                                    GeneralContract abstractContract = GeneralContract.load(contractConfig, contractAddress, web3j, clientTransactionManager, contractConfig.getGasPrice(), contractConfig.getGasLimit());
                                    if (abstractContract.isValid()) {
                                        BigInteger decimals = abstractContract.decimals().send();
                                        if (decimals != null) {
                                            abstractContract.getContractConfig().setDecimals(decimals);
                                        }
                                        if (walletManage.isEthsListener()) {//是否开启自动入币出币
                                            Subscription subscription1= abstractContract.transferEventObservable(DefaultBlockParameterName.PENDING, DefaultBlockParameterName.PENDING)
//                                                .filter(tx->jms().sendPending(ethCoinEnums,tx))//将区块所有的交易存入队列中，形成一个强大的交易数据库
                                                    .filter(tx -> isSysAccounts(tx.from, tx.to, tx.value)).subscribe(tx -> {
                                                logger.debug("{}新的等待事物：{}", coinname, JSON.toJSONString(tx));
                                                try {
                                                    walletManage.getTransactionManage().pendingTransaction(coinname, tx);
                                                } catch (Exception e) {
                                                    logger.error(e.getMessage());
                                                    e.printStackTrace();
                                                }
                                            });
                                            Subscription subscription2= abstractContract.transferEventObservable(DefaultBlockParameter.valueOf(blockNumberManage.getContractBlockNumber()), DefaultBlockParameterName.LATEST)
//                                                .filter(tx->jms().sendSuccess(ethCoinEnums,tx))
                                                    .filter(tx -> isSysAccounts(tx.from, tx.to, tx.value)).subscribe(tx -> {
                                                logger.debug("{}完成的事物：{}", coinname, JSON.toJSONString(tx));
                                                try {
                                                    walletManage.getTransactionManage().finishTransaction(coinname, tx);
                                                } catch (Exception e) {
                                                    logger.error(e.getMessage());
                                                    e.printStackTrace();
                                                }
                                            });
                                            bindSubscription(coinname, subscription1, subscription2);
                                        }

                                        logger.debug("{}合约部署完毕,验证：{}，热钱包账户余额：{}", coinname, abstractContract.isValid(), weiUnit(abstractContract.l_getBalance(walletAddr)));
                                        contractMap.put(coinname, abstractContract);
                                    } else {
                                        contractManage.addFailureCoinname(coinname);
                                        logger.error("{}合约部署失败！合约地址：{}", coinname, contractAddress);
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    contractManage.addFailureCoinname(coinname);
                                    logger.error("{}合约部署失败！合约地址：{}", coinname, contractAddress);
                                }
                                return 1;
                            }
                        });
                        futureList.add(future);
                    }
                }
                final String startEthBlock = blockNumberManage.getEthBlockNumber().toString();
                final String startContractBlock = blockNumberManage.getContractBlockNumber().toString();

                ListenableFuture[] futures = new ListenableFuture[futureList.size()];
                final ListenableFuture<List<Integer>> allFutures = Futures.allAsList(futureList.toArray(futures));
                Futures.addCallback(allFutures, new FutureCallback<List<Integer>>() {
                    @Override
                    public void onSuccess(@Nullable List<Integer> result) {
                        logger.debug("ETH以及合约部署完成!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                        scheduleShowWalletBanlance(startEthBlock, startContractBlock);
                    }

                    @Override
                    public void onFailure(Throwable t) {
                        logger.error("ETH以及合约部署失败!!!!error!!!!!error!!!!!!!!!!error!!!!!!!!!!!!error!!!!!!!!!!!!!!!!!!!error!!!!!!!!!!!");
                    }
                });
        }finally {
            try {
                lock.unlock();
            }catch (Exception e){}
        }
    }

    /**
     * 调度显示热钱包账户余额
     * @param startEthBlock
     * @param startContractBlock
     */
    public void scheduleShowWalletBanlance(String startEthBlock, String startContractBlock){
        if(scheduledFuture==null || scheduledFuture.isCancelled() || scheduledFuture.isDone()) {
            scheduledFuture = walletManage.getScheduledThreadPoolExecutor().scheduleAtFixedRate(new Runnable() {
                @Override
                public void run() {
                    try {
                        Set<String> coinList = contractManage.loadContracConfigs();
                        loadEthContracts(coinList, true);
                        logger.debug("读取ETH历史所在区块：{}", catchEthBlockNumber);
                        logger.debug("系统检测ETH起始块：{} ，合约起始块：{}，当前钱包最新块blockNumber：{}", startEthBlock, startContractBlock, web3j.ethBlockNumber().sendAsync().get().getBlockNumber());
                        logger.debug("监听用户自动入币总数：{} 个", accountInfoMap.size());
                        Set<String> set = contractManage.getCoinList();
                        Iterator<String> iterator = set.iterator();
                        while (iterator.hasNext()) {
                            String coinname = iterator.next();
                            GeneralContract generalContract = contractMap.get(coinname);
                            if (generalContract != null) {
                                BigInteger valueWei = generalContract.l_getBalance(getConf().getEthWalletAddr());
                                BigDecimal valueEther = Convert.fromWei(valueWei.toString(), Convert.Unit.ETHER);
                                valueEther = valueEther.setScale(8, BigDecimal.ROUND_UP);
                                logger.debug("{}账户余额：{}(ether)      {}(wei)  decimals:{}", Strings.padEnd(coinname, 6, ' '), Strings.padEnd(valueEther.toString(), 20, ' '), Strings.padEnd(valueWei.toString(), 40, ' '),
                                        generalContract.getContractConfig().getDecimals());
                            }

                        }
                        logger.debug("---------------------------------------------------------");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }, 10, 10, TimeUnit.SECONDS);
        }
    }


    public void queryBlockNumberTrasactionList() throws Exception{
            Set<String> set = contractManage.getCoinList();
            Iterator<String> iterator = set.iterator();
            while (iterator.hasNext()) {
                String coinname = iterator.next();
                GeneralContract generalContract = contractMap.get(coinname);
                if (generalContract != null) {
                    if(!coinname.equals(EnumContractCoinname.ETH.getCoinname())){
                        generalContract.transferEventObservable(DefaultBlockParameter.valueOf(blockNumberManage.getContractBlockNumber()), DefaultBlockParameterName.LATEST)
                                .filter(tx -> isSysAccounts(tx.from, tx.to, tx.value)).subscribe(tx -> {
                            logger.debug("{}完成的事物：{}", coinname, JSON.toJSONString(tx));
                            try {
                                walletManage.getTransactionManage().finishTransaction(coinname, tx);
                            } catch (Exception e) {
                                logger.error(e.getMessage());
                                e.printStackTrace();
                            }
                        }).unsubscribe();
                    }else{
                        web3j.catchUpToLatestAndSubscribeToNewTransactionsObservable(DefaultBlockParameter.valueOf(blockNumberManage.getEthBlockNumber()))
                                .filter(tx->isSysAccounts(tx.getFrom(),tx.getTo(),tx.getValue(),tx.getBlockNumber())).asObservable().subscribe(tx -> {
                            logger.debug("获取ETH完成交易事物，当前块：{}  详细：{}", tx.getBlockNumber().toString(), JSON.toJSONString(tx));
                            try {
                                walletManage.getTransactionManage().finishTransaction(EnumContractCoinname.ETH.getCoinname(), tx);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }).unsubscribe();
                    }
                }
            }

    }

    /**
     * 系统正常停止，消耗线程
     */
    @PreDestroy
    public void dectory(){
        if(!walletManage.getScheduledThreadPoolExecutor().isShutdown()) {
            logger.debug("停止线程！！！！！！");
            walletManage.getScheduledThreadPoolExecutor().shutdown();
        }
    }

    /**
     * 获取钱包eth币数量，单位wei
     * @param address
     * @return
     * @throws ExecutionException
     * @throws InterruptedException
     */
    public BigInteger getBlanceEth_UnitWei(String address) throws ExecutionException, InterruptedException {
        return web3j.ethGetBalance(address, DefaultBlockParameterName.LATEST).sendAsync().get().getBalance();
    }

    public BigInteger getGasPrice() throws IOException {
        return web3j.ethGasPrice().send().getGasPrice();
    }

    /**
     * eth币监听事件
     */
    public void triggerEthListener(){
        //监听待处理事务
        Subscription subscription1= web3j.pendingTransactionObservable()
                .filter(tx->isSysAccounts(tx.getFrom(),tx.getTo(),tx.getValue())).asObservable().subscribe(tx -> {
            logger.debug("eth监听等待事物-tx:{}",JSON.toJSONString(tx));
            try {
                walletManage.getTransactionManage().pendingTransaction(EnumContractCoinname.ETH.getCoinname(), tx);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        //监听正在完成的事物
        Subscription subscription2= web3j.transactionObservable()
                .filter(tx->isSysAccounts(tx.getFrom(),tx.getTo(),tx.getValue())).asObservable().subscribe(tx -> {
            logger.debug("ETH最新完成交易事物:{}", JSON.toJSONString(tx));
            try {
                walletManage.getTransactionManage().finishTransaction(EnumContractCoinname.ETH.getCoinname(),tx);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        //拉取截止最新事物的所有记录
        Subscription subscription3 = web3j.catchUpToLatestAndSubscribeToNewTransactionsObservable(DefaultBlockParameter.valueOf(blockNumberManage.getEthBlockNumber()))
                .filter(tx->isSysAccounts(tx.getFrom(),tx.getTo(),tx.getValue(),tx.getBlockNumber())).asObservable().subscribe(tx -> {
            logger.debug("获取ETH完成交易事物，当前块：{}  详细：{}", tx.getBlockNumber().toString(), JSON.toJSONString(tx));
            try {
                walletManage.getTransactionManage().finishTransaction(EnumContractCoinname.ETH.getCoinname(), tx);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        this.bindSubscription(EnumContractCoinname.ETH.getCoinname(), subscription1, subscription2, subscription3 );
    }

    /**
     * ETH及合约订阅功能绑定
     * @param coinname
     * @param subscription
     */
    public void bindSubscription(String coinname,Subscription... subscription){
        List<Subscription> subscriptionList = subscriptionMap.get(coinname);
        if(subscriptionList==null){
            List<Subscription> _sublist = Lists.newArrayList();
            _sublist.addAll(Lists.newArrayList(subscription));
            subscriptionMap.put(coinname, _sublist);
        }else{
            subscriptionList.addAll(Lists.newArrayList(subscription));
        }
    }

    /**
     * 取消合约监听/同时涉及取消提币
     * @param coinname
     */
    public void cancelContract(String coinname){
        contractMap.remove(coinname);
        List<Subscription> subscriptionList = subscriptionMap.get(coinname);
        if(subscriptionList!=null && subscriptionList.size()>0){
            for(Subscription subscription: subscriptionList){
                if(!subscription.isUnsubscribed()){
                    subscription.unsubscribe();
                }
            }
        }
    }

    /**
     * 判断交易是否系统内的账户
     * @return
     */
    public synchronized boolean isSysAccounts(String from, String to, BigInteger value,BigInteger blockNumber){
        return isSysAccounts(from, to, value);
    }

    public BigDecimal ethUnit(BigInteger wei){
        BigDecimal bigDecimal=Convert.fromWei(new BigDecimal(wei.intValue()), Convert.Unit.ETHER);
        return bigDecimal.setScale(8,BigDecimal.ROUND_HALF_UP);
    }

    public BigInteger weiUnit(BigInteger wei){
        return wei;
    }

    public boolean isSysAccounts(String from, String to, BigInteger value){
        try {
            if (!StringUtils.isNullOrEmpty(from)) {
                from = from.toLowerCase();
            }
            if (!StringUtils.isNullOrEmpty(to)) {
                to = to.toLowerCase();
            }
            try {
                if (value != null && value.compareTo(BigInteger.valueOf(0)) <= 0) {
                    return false;
                }
            }catch (Exception e){}
            return accountInfoMap.containsKey(from) || accountInfoMap.containsKey(to);
        }catch (Exception e){
            return false;
        }
    }

    public Conf getConf() {
        return conf;
    }

    public GeneralContract contract(String  coinname){
        return contractMap.get(coinname);
    }

    public ContractConfig getContractConfig(String coinname){
        return contractManage.getContrarctConfig(coinname);
    }

    public ContractManage getContractManage() {
        return contractManage;
    }
}
