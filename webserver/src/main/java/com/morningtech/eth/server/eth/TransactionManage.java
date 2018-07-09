package com.morningtech.eth.server.eth;

 import com.google.common.base.Strings;
 import com.google.common.collect.Lists;
 import com.morningtech.eth.server.entity.SysEthTransfer;
import com.morningtech.eth.server.entity.SysWalletAccount;
 import com.morningtech.eth.server.entity.SysWalletAccountBalance;
 import com.morningtech.eth.server.enums.EnumCoin;
 import com.morningtech.eth.server.enums.EnumTransType;
 import com.morningtech.eth.server.enums.EthTransactionNoStatus;
 import com.morningtech.eth.server.eth.bean.AccountBalance;
 import com.morningtech.eth.server.eth.bean.AccountInfo;
 import com.morningtech.eth.server.eth.contract.ContractConfig;
 import com.morningtech.eth.server.eth.contract.GeneralContract;
 import com.morningtech.eth.server.eth.res.TransferEventResponse;
 import com.morningtech.eth.server.eth.util.EthWalletUtils;
 import com.morningtech.eth.server.redis.RedisService;
 import com.morningtech.eth.server.service.*;

 import com.morningtech.eth.server.service.SysEthTransferService;
 import com.morningtech.eth.server.service.SysWalletAccountService;
 import com.morningtech.eth.server.util.DesUtils;
 import com.morningtech.eth.server.util.ExceptionUtil;
 import com.morningtech.eth.server.util.MD5Utils;
 import com.morningtech.eth.server.util.StringUtils;
 import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.web3j.protocol.core.methods.response.Log;
import org.web3j.protocol.core.methods.response.Transaction;
 import org.web3j.protocol.core.methods.response.TransactionReceipt;
 import org.web3j.utils.Convert;
import org.web3j.utils.Numeric;

 import java.math.BigDecimal;
 import java.math.BigInteger;
 import java.util.*;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.ScheduledExecutorService;

/**
 * 交易持久化管理
 * @author xuchunlin
 * @version V1.0
 * @Title: TransactionManage
 * @Package com.hucheng.wallet.eth
 * @Description: TODO
 * @date 2018/1/2 11:01
 */
@Component
public class TransactionManage {

    public final static Logger logger= LoggerFactory.getLogger(TransactionManage.class);

    @Autowired
    private SysEthTransferService sysEthTransferService;

    @Autowired
    private SysWalletAccountService sysWalletAccountService;

    @Autowired
    private SysWalletAccountBalanceService sysWalletAccountBalanceService;

    @Autowired
    private EthWalletManage ethWalletManage;

    @Autowired
    private RedisService redisService;

    final static ExecutorService executorService = Executors.newCachedThreadPool();


    public void createTempTransactionNo(EnumCoin enumCoin, String from, String toAddress, String contactAddress, BigInteger value, BigInteger gasPrice, String transactionHash) throws Exception {
        SysEthTransfer sysEthTransfer=new SysEthTransfer();
        sysEthTransfer.setCoinname(enumCoin.name());
        sysEthTransfer.setContractaddress(contactAddress.toLowerCase());
        sysEthTransfer.setTo(toAddress);
        sysEthTransfer.setFrom(from);
        sysEthTransfer.setValue(value);
        sysEthTransfer.setEth(Convert.fromWei(value.toString(), Convert.Unit.ETHER));
        sysEthTransfer.setGasused(gasPrice);
        sysEthTransfer.setTransactionhash(transactionHash);//等待
        sysEthTransfer.setSystime(new Date());
        sysEthTransfer.setStatus(EthTransactionNoStatus.CREATE.getStatus());
        sysEthTransferService.saveSysEthTransfer(sysEthTransfer);
    }

    public void createFinishTrasactionNo(String coinname,String from, String toAddress,String contactAddress, BigInteger value, BigInteger gasPrice,String blockNumber,String transactionHash,EthTransactionNoStatus ethTransactionNoStatus) throws Exception {
        Date nowdate=new Date();
        SysEthTransfer sysEthTransfer=new SysEthTransfer();
        sysEthTransfer.setCoinname(coinname);
        sysEthTransfer.setContractaddress(contactAddress==null?contactAddress:contactAddress.toLowerCase());
        sysEthTransfer.setTo(toAddress);
        sysEthTransfer.setFrom(from);
        sysEthTransfer.setValue(value);
        sysEthTransfer.setEth(Convert.fromWei(value.toString(), Convert.Unit.ETHER));

        sysEthTransfer.setBlocknumber(blockNumber);
        sysEthTransfer.setTransactionhash(transactionHash);//等待
        sysEthTransfer.setSystime(nowdate);
        if(ethTransactionNoStatus==EthTransactionNoStatus.PENDING){
            sysEthTransfer.setPendingtime(nowdate);
        }else if(ethTransactionNoStatus==EthTransactionNoStatus.SUCCESS){
            sysEthTransfer.setTranstime(nowdate);
        }
        EnumTransType enumTransType = getTransType(from, toAddress);
        sysEthTransfer.setTranstype(enumTransType.name());
        sysEthTransfer.setRemark(enumTransType.getRemark());
        if(enumTransType==EnumTransType.G || enumTransType==EnumTransType.T
                || enumTransType==EnumTransType.S){
            if (transactionHash != null) {
                TransactionReceipt transactionReceipt = ethWalletManage.queryTransactionReceipt(transactionHash);
                if (transactionReceipt != null) {
                    BigInteger bigInteger = transactionReceipt.getGasUsed();//得到消耗的gasUsd值 wei
                    sysEthTransfer.setGasused(bigInteger);
                    ContractConfig contractConfig = ethWalletManage.getContractConfig(coinname);
                    BigInteger _gasPrice = contractConfig.getGasPrice();//gwei转wei
                    BigDecimal gas = Convert.fromWei(bigInteger.multiply(_gasPrice).toString(), Convert.Unit.ETHER);
                    sysEthTransfer.setGas(gas);
                }
            }
        }
        sysEthTransfer.setStatus(ethTransactionNoStatus.getStatus());
        sysEthTransfer.setTransign(MD5Utils.MD5(sysEthTransfer.toString()));


        logger.debug("====新增===================================");
        sysEthTransferService.saveSysEthTransfer(sysEthTransfer);
        //发布到主题
        this.publishEthTransactionToRedis(sysEthTransfer);
        this.settingTransactionAndAccountBalance(toAddress, from);

    }

    public void pendingTransaction(String coinname,TransferEventResponse transferEventResponse) throws Exception {
        updateTransactionNo(coinname,transferEventResponse,EthTransactionNoStatus.PENDING);
    }

    public void finishTransaction(String coinname,TransferEventResponse transferEventResponse) throws Exception {
        updateTransactionNo(coinname,transferEventResponse,EthTransactionNoStatus.SUCCESS);
    }

    public void pendingTransaction(String coinname,Transaction transaction) throws Exception {
        TransferEventResponse tx=new TransferEventResponse();
        tx.to=transaction.getTo();
        tx.from=transaction.getFrom();
        tx.value=transaction.getValue();
        Log _log=new Log();
        _log.setTransactionHash(transaction.getHash());
        _log.setBlockNumber(Numeric.toHexStringWithPrefix(transaction.getBlockNumber()));
        tx.log=_log;
        updateTransactionNo(coinname,tx,EthTransactionNoStatus.PENDING);
    }

    public void finishTransaction(String coinname,Transaction transaction) throws Exception {
        TransferEventResponse tx=new TransferEventResponse();
        tx.to=transaction.getTo();
        tx.from=transaction.getFrom();
        tx.value=transaction.getValue();
        Log _log=new Log();
        _log.setTransactionHash(transaction.getHash());
        _log.setBlockNumber(Numeric.toHexStringWithPrefix(transaction.getBlockNumber()));
        tx.log=_log;
        updateTransactionNo(coinname,tx,EthTransactionNoStatus.SUCCESS);
    }

    public synchronized void  updateTransactionNo(String coinname, TransferEventResponse transferEventResponse, EthTransactionNoStatus ethTransactionNoStatus) throws Exception {

        String transactionHash=transferEventResponse.getLog().getTransactionHash();
        if(!StringUtils.isNullOrEmpty(transactionHash)) {
            SysEthTransfer $sysEthTransfer=sysEthTransferService.findSysEthTransferByTransactionHash(transactionHash);
            String from=transferEventResponse.getFrom();
            String to=transferEventResponse.getTo();
            String contractAddress=transferEventResponse.getLog().getAddress();
            BigInteger value=transferEventResponse.getValue();
            if($sysEthTransfer==null){
                if(!value.toString().equals(0)) {
                    logger.debug("插入新的交易记录...............");
                    String blockNumber = transferEventResponse.getLog().getBlockNumberRaw();
                    blockNumber=Numeric.toBigInt(blockNumber).toString();
                    createFinishTrasactionNo(coinname, from, to, contractAddress, value, null, blockNumber, transactionHash, ethTransactionNoStatus);
                }else{
                    logger.debug("交易额为0不进行入库！！！！！！！！");
                }
            }else {
                if($sysEthTransfer.getStatus().intValue()==0 || $sysEthTransfer.getStatus().intValue()==1 || $sysEthTransfer.getStatus().intValue()==2) {

                    if (($sysEthTransfer.getStatus().intValue() == 1 || $sysEthTransfer.getStatus().intValue() == 2)
                            && ethTransactionNoStatus == EthTransactionNoStatus.PENDING) {
                        logger.debug("记录已存在，不能修改1111！！！！！！！！");
                        return;
                    }
                    if ($sysEthTransfer.getStatus().intValue() == 2 && ethTransactionNoStatus == EthTransactionNoStatus.SUCCESS) {
                        logger.debug("记录已存在，不能修改2222！！！！！！！！");
                        return;
                    }
                    Date nowdate = new Date();
                    SysEthTransfer sysEthTransfer = new SysEthTransfer();
                    sysEthTransfer.setCoinname(coinname);
                    sysEthTransfer.setFrom(from);
                    sysEthTransfer.setTo(to);
                    sysEthTransfer.setValue(value);
                    sysEthTransfer.setEth(Convert.fromWei(value.toString(), Convert.Unit.ETHER));
                    String blockNumber = "";
                    if (transferEventResponse.getLog() != null && transferEventResponse.getLog().getBlockNumber() != null) {
                        blockNumber = transferEventResponse.getLog().getBlockNumber().toString();
                    }
                    sysEthTransfer.setBlocknumber(blockNumber);
                    sysEthTransfer.setTransactionhash(transactionHash);
                    sysEthTransfer.setStatus(ethTransactionNoStatus.getStatus());
                    if (ethTransactionNoStatus == EthTransactionNoStatus.PENDING) {
                        sysEthTransfer.setPendingtime(nowdate);
                    } else if (ethTransactionNoStatus == EthTransactionNoStatus.SUCCESS) {
                        sysEthTransfer.setTranstime(nowdate);
                        sysEthTransfer.setTransign(MD5Utils.MD5(sysEthTransfer.toString()));
                    }
                    EnumTransType enumTransType = getTransType(from, to);
                    sysEthTransfer.setTranstype(enumTransType.name());
                    sysEthTransfer.setRemark(enumTransType.getRemark());
                    //交易成功获取gasuse及矿工费
                    if(ethTransactionNoStatus == EthTransactionNoStatus.SUCCESS) {
                        if (enumTransType == EnumTransType.G || enumTransType == EnumTransType.T
                                || enumTransType == EnumTransType.S) {
                            if (transactionHash != null) {
                                TransactionReceipt transactionReceipt = ethWalletManage.queryTransactionReceipt(transactionHash);
                                if (transactionReceipt != null) {
                                    BigInteger bigInteger = transactionReceipt.getGasUsed();//得到消耗的gasUsd值 wei
                                    sysEthTransfer.setGasused(bigInteger);
                                    ContractConfig contractConfig = ethWalletManage.getContractConfig(coinname);
                                    BigInteger _gasPrice = contractConfig.getGasPrice();//gwei转wei
                                    BigDecimal gas = Convert.fromWei(bigInteger.multiply(_gasPrice).toString(), Convert.Unit.ETHER);
                                    sysEthTransfer.setGas(gas);
                                }
                            }
                        }
                    }
                    sysEthTransferService.updateByTransactionHash(sysEthTransfer);

                    //发布到主题
                    this.publishEthTransactionToRedis(sysEthTransfer);
                    this.settingTransactionAndAccountBalance(from, to);
                }
            }
        }else{
            logger.debug("transactionHash为空！！！！！！！！");
        }
    }

    public void saveAccount(AccountInfo accountInfo) throws Exception {
        SysWalletAccount dandanAalletAccount=new SysWalletAccount();
        dandanAalletAccount.setAccountid(accountInfo.getAccountId());
        dandanAalletAccount.setCreatetime(new Date());
        dandanAalletAccount.setKeystorepath(accountInfo.getKeystorePath());
        dandanAalletAccount.setPassword(accountInfo.getPassword());
        dandanAalletAccount.setUserid(accountInfo.getUserId());
        dandanAalletAccount.setRemark(accountInfo.getRemark());
        sysWalletAccountService.saveSysWalletAccount(dandanAalletAccount);
    }

    public List<AccountInfo> getAccountInfoList() throws Exception {
        List<AccountInfo> accountInfoList=new ArrayList<>();
        List<SysWalletAccount> walletAccounts= sysWalletAccountService.findList();
        for (SysWalletAccount d:walletAccounts ) {
            AccountInfo accountInfo=new AccountInfo();
            accountInfo.setUserId(d.getUserid());
            accountInfo.setPassword(DesUtils.decrypt(d.getPassword()));
            accountInfo.setKeystorePath(d.getKeystorepath());
            accountInfo.setRemark(d.getRemark());
            accountInfo.setCreateTime(d.getCreatetime());
            accountInfo.setAccountId(d.getAccountid());
            accountInfoList.add(accountInfo);
        }
        return accountInfoList;
    }

    public AccountInfo findAccountByUserId(Integer userId) throws Exception {
        SysWalletAccount d=sysWalletAccountService.findAccountByUserId(userId);
        if(d!=null) {
            AccountInfo accountInfo = new AccountInfo();
            accountInfo.setUserId(d.getUserid());
            accountInfo.setPassword(DesUtils.decrypt(d.getPassword()));
            accountInfo.setKeystorePath(d.getKeystorepath());
            accountInfo.setRemark(d.getRemark());
            accountInfo.setCreateTime(d.getCreatetime());
            accountInfo.setAccountId(d.getAccountid());
            return accountInfo;
        }
        return null;
    }


    public void updateByTransactionHash(SysEthTransfer sysEthTransfer) throws Exception {
        sysEthTransferService.updateByTransactionHash(sysEthTransfer);
    }

    public void settingTransactionAndAccountBalance(final String... addresss){
        executorService.submit(() -> {
            try {
                //存储或更新账户余额
                List<SysWalletAccountBalance> balanceList= Lists.newArrayList();
                //获取余额
                Set<String> set = ethWalletManage.getContractManage().getCoinList();
                Iterator<String> iterator = set.iterator();
                while (iterator.hasNext()) {
                    String coinname = iterator.next();
                    GeneralContract generalContract = ethWalletManage.contract(coinname);
                    if (generalContract != null) {
                        Date nowDate =new Date();

                        for(String address  : addresss) {
                            BigInteger valueWei = generalContract.l_getBalance(address);
                            BigDecimal valueEther = Convert.fromWei(valueWei.toString(), Convert.Unit.ETHER);
                            valueEther = valueEther.setScale(8, BigDecimal.ROUND_UP);
                            logger.debug("{}账户余额：{}(ether)      {}(wei)  decimals:{}", Strings.padEnd(coinname, 6, ' '), Strings.padEnd(valueEther.toString(), 20, ' '), Strings.padEnd(valueWei.toString(), 40, ' '),
                                    generalContract.getContractConfig().getDecimals());

                            SysWalletAccountBalance accountBalance = new SysWalletAccountBalance();
                            accountBalance.setCoinname(coinname);
                            accountBalance.setAddress(address);
                            accountBalance.setBalance(valueEther);
                            accountBalance.setLasttime(nowDate);
                            balanceList.add(accountBalance);
                        }
                    }
                }

                sysWalletAccountBalanceService.saveAccountBalance(balanceList);

            }catch (Exception e){
                e.printStackTrace();
                logger.error("settingTransactionAndAccountBalance:" + ExceptionUtil.getMessage(e));

            }
        });

    }

    public void publishEthTransactionToRedis(SysEthTransfer sysEthTransfer){
        String walletAddr =ethWalletManage.getConf().getEthWalletAddr().toLowerCase();//热钱包地址
        String from = sysEthTransfer.getFrom().toLowerCase();
        String to = sysEthTransfer.getTo().toLowerCase();
        if(!walletAddr.equals(from) && !walletAddr.equals(to)) {//热钱包相关转入转出不推送
            redisService.publishEthTransaction(sysEthTransfer);
        }
    }

    public EnumTransType getTransType(String from, String to){
        String walletAddr =ethWalletManage.getConf().getEthWalletAddr().toLowerCase();//热钱包地址
        from = from.toLowerCase();
        to = to.toLowerCase();
        if(ethWalletManage.isSysAccount(to) && !ethWalletManage.isSysAccount(from)
                && !to.equals(walletAddr)){
            return EnumTransType.C;
        }else if(to.equals(walletAddr)){
            return EnumTransType.I;
        }else if (from.equals(walletAddr) && !ethWalletManage.isSysAccount(to)){
            return EnumTransType.T;
        }else if (from.equals(walletAddr) && ethWalletManage.isSysAccount(to)){
            return EnumTransType.S;
        }else if(ethWalletManage.isSysAccount(from) && !from.equals(walletAddr)){
            return EnumTransType.G;
        }
        return EnumTransType.U;
    }
}
