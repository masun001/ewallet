//package com.morningtech.eth.server.task;
//
//import com.google.common.collect.HashBasedTable;
//import com.google.common.collect.Lists;
//import com.google.common.collect.Maps;
//import com.google.common.collect.Sets;
//import com.morningtech.eth.server.entity.SysSummarizeTask;
//import com.morningtech.eth.server.entity.WalletUser;
//import com.morningtech.eth.server.entity.WithdrawalsTransactionUser;
//import com.morningtech.eth.server.eth.EthWalletManage;
//import com.morningtech.eth.server.eth.contract.AbstractContract;
//import com.morningtech.eth.server.eth.contract.ContractConfig;
//import com.morningtech.eth.server.eth.contract.GeneralContract;
//import com.morningtech.eth.server.eth.enums.EnumContractCoinname;
//import com.morningtech.eth.server.eth.enums.EnumTransStatus;
//import com.morningtech.eth.server.util.ExceptionUtil;
//import com.morningtech.eth.server.util.StringUtils;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.web3j.utils.Convert;
//
//import java.math.BigDecimal;
//import java.math.BigInteger;
//import java.text.SimpleDateFormat;
//import java.util.*;
//import java.util.concurrent.TimeUnit;
//
///**
// *  * =========提款到主账户====================
// *
// * 平台子账号转移金额到主账号
// *      注意：每个账户的eth数量必须满足所有币提取手续费才进行提款操作
// * @author xuchunlin
// * @version V1.0
// * @Title: TransferSummaryEthCheckTask
// * @Package com.hucheng.wallet.eth.task
// * @Description: TODO
// * @date 2018/1/8 18:11
// */
//public class TransferSummaryEthCheckTask implements Runnable {
//
//    public final static Logger logger= LoggerFactory.getLogger(TransferSummaryEthCheckTask.class);
//
//    private TransferSummaryCoinManage transferSummaryCoinManage;
//
//    private EthWalletManage ethWalletManage;
//
//    private long  scheduleTime=5;//定时检测时间，分钟
//
//    private String withdrawalsAddress="";//主地址
//
//    private final BigInteger MAXGAS=BigInteger.valueOf(500_000_000_000_000_000L);//最大手续费限制 0.5 ether
//
//    final String TASK_TIME=( new SimpleDateFormat("yyyyMMddHHmmss")).format(new Date());
//
//    private int mail_sendEthQueCount=1;//缺少eth邮件发送次数标记
//    //账户=》币种=》数量
//    private HashBasedTable<String,String,BigDecimal> hashBasedTable= HashBasedTable.create();//已经开始转出的账号即数量
//
//    private Set<String> toUserEthSet=Sets.newConcurrentHashSet();//已经转入eth到用户地址记录
//
//    public TransferSummaryEthCheckTask(TransferSummaryCoinManage transferSummaryCoinManage,String withdrawalsAddress) {
//        this.transferSummaryCoinManage = transferSummaryCoinManage;
//        this.withdrawalsAddress=withdrawalsAddress;
//        this.ethWalletManage=transferSummaryCoinManage.getEthWalletManage();
//    }
//
//    public TransferSummaryEthCheckTask(TransferSummaryCoinManage transferSummaryCoinManage,String withdrawalsAddress, Long scheduleTime) {
//        this.transferSummaryCoinManage = transferSummaryCoinManage;
//        this.withdrawalsAddress=withdrawalsAddress;
//        this.ethWalletManage=transferSummaryCoinManage.getEthWalletManage();
//        if(scheduleTime!=null && scheduleTime.longValue()>0) {
//            this.scheduleTime = scheduleTime;
//        }
//    }
//
//    public TransactionServer getTransactionServer(){
//        return transferSummaryCoinManage.getTransactionServer();
//    }
//
//    @Override
//    public void run() {
//
//        if(withdrawalsAddress==null || "".equals(withdrawalsAddress)){
//            return;
//        }
//        logger.info("开始提款任务............................");
//        //币种单个手续费
//        Map<String, BigInteger> coinGasMap=Maps.newConcurrentMap();
//        //每个钱包地址绑定的各种合约币账户
//        Map<String,List<WalletUser>> accountWalletMap= Maps.newConcurrentMap();
//        boolean TO_TRANSFER_FLAG=false;
//        try {
//
//            EthWalletManage ethWalletManage=transferSummaryCoinManage.getEthWalletManage();
//
//            GeneralContract ethContract = ethWalletManage.contract(EnumContractCoinname.ETH.getCoinname());
//            ContractConfig ethConfig = ethContract.getContractConfig();
//            BigInteger ethGasWei= ethConfig.getGasPrice().multiply(ethConfig.getGasLimit());
//            //获取打款账户地址和eth余额
//            String mainAddress = ethWalletManage.getConf().getEthWalletAddr();//负责打款的账户以及部署的账户地址
//            //存储每个账户所需的总矿工费
//            Map<String,BigInteger> account_ether_unitwei= Maps.newConcurrentMap();
//            List<WalletUser> walletUserList=this.getTransactionServer().findSysEthTransferNoToMainList(null);
//            logger.debug("查询到满足提款数额的账号:{}个", walletUserList.size());
//            if(walletUserList==null || walletUserList.size()<=0){
//                return;
//            }
//            //eth GAS计算
//            coinGasMap.put(EnumContractCoinname.ETH.getCoinname(),ethGasWei);
//            /**
//             * ===================================================================
//             * 计算每个账户所需的总挖矿费
//             * ===================================================================
//             */
//            for (WalletUser walletUser:
//                    walletUserList ) {
//                String accountId=walletUser.getAccountId();
//                String transactionHashs=walletUser.getTransactionHash();
//                //判断hash串（多个）其中一个hash存在拉取过，则本次调度不进行处理
//                //控制方式：初次hash组：“AA,BB”，第二次hash组：“AA,BB,CC",则不处理，当AA，BB处理完，则本次新的hash组为“CC”，不存在则进行提款操作
//                if(!isCacheByTransactionHashs(transactionHashs)) {
//                    addCacheFromTrasactionHashs(transactionHashs);//hash加入到全局缓存中
//                    //获取币种合约枚举
//                    String coinname= walletUser.getCoinName();
//                    if (coinname!=null && !"".equals(coinname)) {
//                        if(walletUser.getTranStatus().indexOf("1")!=-1){
//                            toUserEthSet.add(accountId);//已经转过GAS到用户了
//                        }
//                        ContractConfig contractConfig=ethWalletManage.getContractConfig(coinname);
//                        //获取币种最大挖矿费用
//                        BigInteger maxGas = contractConfig.getGasPrice().multiply(contractConfig.getGasLimit());
//                        coinGasMap.put(walletUser.getCoinName(), maxGas);
//                        if (account_ether_unitwei.containsKey(accountId)) {
//                            BigInteger _gas = account_ether_unitwei.get(accountId);
//                            account_ether_unitwei.put(accountId, maxGas.add(_gas));//矿工费累加
//                        } else {
//                            account_ether_unitwei.put(accountId, maxGas);
//                        }
//
//                        if (!accountWalletMap.containsKey(accountId)) {
//                            accountWalletMap.put(accountId, Lists.newArrayList(walletUser));
//                        } else {
//                            accountWalletMap.get(accountId).add(walletUser);
//                        }
//                    }
//                }
//            }
//            /**
//             * ===================================================================
//             * 查询所有用户的eth剩余是否能否支付矿工费用，足够：则提款，不足够：计算缺少的eth数量
//             * ===================================================================
//             */
//            Map<String, BigInteger>  toAccountEth_unitwei=Maps.newConcurrentMap();
//            BigInteger totalEth_unitwei=BigInteger.valueOf(0);//总共需要打入平台用户账户的eth数量，单位wei
//            Set<String> accountIdSet= account_ether_unitwei.keySet();
//            List<WalletUser> transferWalletUserList=Lists.newArrayList();
//            for(String accountId : accountIdSet){
//                BigInteger totalGas_unitwei=account_ether_unitwei.get(accountId);
//               /**
//                 * 查询用户账号eth数量是否满足矿工费用
//                 */
//                BigInteger eth_wei=ethWalletManage.getBlanceEth_UnitWei(accountId);
//                //账户已有的eth数量少于矿工费用，需要主账户转移缺少的eth以支持转账
//                if(eth_wei.compareTo(totalGas_unitwei)==-1){
//                    BigInteger que_eth_unitwei= totalGas_unitwei.subtract(eth_wei);//缺少的eth数量
//                    totalEth_unitwei=totalEth_unitwei.add(que_eth_unitwei);
//                    toAccountEth_unitwei.put(accountId, que_eth_unitwei);
//                    // TODO: 2018/1/19 启动一个后台监听任务，如果矿工费满足，则进行提款
//                    WithdrawalsTransactionUser withdrawalsTransactionUser=new WithdrawalsTransactionUser(accountId,totalGas_unitwei, accountWalletMap.get(accountId));
//                    /**
//                     * 加入检测矿工费检测任务，一旦满足矿工费则开始归集
//                     */
//                    transferSummaryCoinManage.getCheckGasAutoTransferManage().addAccountListener(withdrawalsTransactionUser);
//                }else{
//                    //足够支付，不需要向账户转入eth
//                    //将账户所有币转入到固定地址
//                    List<WalletUser> walletUsers=accountWalletMap.get(accountId);
//                    for(WalletUser walletUser: walletUsers){
//                        BigInteger withdrawls_value=walletUser.getValueWei();//提取的数量
//                        hashBasedTable.put(walletUser.getAccountId(),walletUser.getCoinName(),Convert.fromWei(withdrawls_value.toString(), Convert.Unit.ETHER) );
//
//                    }
//                    transferWalletUserList.addAll(walletUsers);
//                }
//            }
//            if(transferWalletUserList.size()>0){
//                transferSummaryCoinManage.getEthContractTrasaction().withdrawals(transferWalletUserList);
//            }
//            logger.info("===============================================");
//            logger.info("==============准备检测矿工费====================");
//            logger.info("===============================================");
//            /**
//             * ===================================================
//             * 转入手续费eth到用户账号
//             * 循环监听缺少的数量账户是否已补缺，如未补缺，一直等待。如以填补，则为每个账户打入eth矿工费。一旦打入到账，则进行提款。
//             * ===================================================
//             */
//            //邮件内容
//            StringBuffer mailBuffer=new StringBuffer();
//            while(!TO_TRANSFER_FLAG) {
//
//                BigInteger eth_wei = ethWalletManage.getBlanceEth_UnitWei(mainAddress);
//                if (eth_wei.compareTo(totalEth_unitwei) == -1) {//账户余额不足，给老大发送邮件
//                    int walletSize=accountWalletMap.size();//提取的地址总数数量
//                    //转入到所有账户所需的eth数量
//                    BigInteger trasferAllWei=ethGasWei.multiply(BigInteger.valueOf(walletSize));
//                    BigInteger getEth_unitwei = totalEth_unitwei.subtract(eth_wei);//本次提取所有币，用户地址所有币提取账户欠缺矿工费数量
//                    //需要打入到提款账户的总数量
//                    getEth_unitwei=getEth_unitwei.add(trasferAllWei);
//
//
//                    logger.info("任务时间：{}，打款账户：{}，缺少eth数量：{}，请将此账户打入缺少的eth以够提款支付矿工费！", TASK_TIME, mainAddress, Convert.fromWei(getEth_unitwei.toString(), Convert.Unit.ETHER));
//
//                    String subJect="提款账户ETH余额不足提醒";
//                   if(mail_sendEthQueCount>=10){//停止本次任务
//                        TO_TRANSFER_FLAG=true;
//                       subJect="最后一次提醒("+mail_sendEthQueCount+")[提款账户ETH余额不足提醒]";
//                       logger.debug("本次归集任务等待打入GAS停止！！！！！！！！！！！！！！！！");
//                    }else if(mail_sendEthQueCount>1 && mail_sendEthQueCount<10){
//                        subJect="重复提醒("+mail_sendEthQueCount+")[提款账户ETH余额不足提醒]";
//                    }
//                    mailBuffer.append(String.format("<p>任务号：%s <br/> 代理提款账户：<B style='color:red'>%s</B> <br/>  剩余eth:%s (ether)<br/> 缺少eth：%s(ehter)<br/> 以上缺少的eth数量用于支付所有提款账户的总矿工费用,请务必在%s分钟内打入到代理提款账户中。（注意：不同邮件同一任务号请忽略）</p>"
//                            , TASK_TIME, mainAddress,Convert.fromWei(eth_wei.toString(), Convert.Unit.ETHER), Convert.fromWei(getEth_unitwei.toString(), Convert.Unit.ETHER), scheduleTime));
//                    mailBuffer.append("<div style='outline:solid 1px #000;padding:10px;margin:10px;'>");
//                    mailBuffer.append(gasDetailHtml(coinGasMap));//矿工费介绍
//                    mailBuffer.append(getTibRecordReport(walletUserList));//提款详情列表
//                    mailBuffer.append("</div>");
//                    //通知老大转入币到部署账户
//                    transferSummaryCoinManage.getSenderMailServer().send(subJect,mailBuffer);
////                    SendMailUtils.send(email, subJect, mailBuffer.toString(), tos);
//                    mail_sendEthQueCount++;
//                } else {//给每个用户转eth
//                    Set<String> toAddressIdSet = toAccountEth_unitwei.keySet();
//                    if(toAddressIdSet.size()>0) {
//                        for (String toAddressId : toAddressIdSet) {
//                            if(TO_TRANSFER_FLAG){
//                                break;
//                            }
//                            BigInteger value = toAccountEth_unitwei.get(toAddressId);
//                            if(!toUserEthSet.contains(toAddressId)) {
//                                toUserEthSet.add(toAddressId);
//                                logger.debug("====================转ETH到地址：{}========数量：{}",toAddressId, Convert.fromWei(value.toString(), Convert.Unit.ETHER));
//                                transferSummaryCoinManage.getTransferEthPoolExecutor().submit(new Runnable() {
//                                    @Override
//                                    public void run() {
//                                        try {
//                                            transferSummaryCoinManage.getTransactionServer().updateEthTrasferStatus(accountWalletMap.get(toAddressId), EnumTransStatus.PENDING_GAS,"正在打入Gas");
//                                            logger.info("向账户{}打入{}(ether)", toAddressId, Convert.fromWei(value.toString(), Convert.Unit.ETHER));
//                                            ethWalletManage.unlockMain();
//                                            ethContract.l_transfer(toAddressId, value).observable().doOnError(err->{
//                                                logger.error("转出eth矿工费到用户交易失败！！！！！！！！！！");
//                                                toUserEthSet.remove(toAddressId);
//                                            }).subscribe(tx->{
//                                                logger.debug("转出eth矿工费到用户交易hash：{}",tx.getTransactionHash());
//                                            });
//                                        } catch (Exception e) {
//                                            toUserEthSet.remove(toAddressId);
//                                            e.printStackTrace();
//                                        }
//                                    }
//                                });
//                            }
//                            TimeUnit.SECONDS.sleep(1);
//                        }
//                    }else{
//                        logger.info("未有缺少ETH矿工费的账户！");
//                        TO_TRANSFER_FLAG=true;
//                    }
//                }
//                TimeUnit.MINUTES.sleep(5);//每5分钟检测一次热钱包余额是否满足全部手续费数量
//            }
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//
//
//    public Set<String> accountSet(List<WalletUser> walletUserList){
//        Set<String> acccountSet= Sets.newHashSet();
//        for(WalletUser walletUser: walletUserList){
//            acccountSet.add(walletUser.getAccountId());
//        }
//        return acccountSet;
//    }
//
//    /**
//     * 获取提币记录统计详情信息
//     * @param walletUserList
//     * @return
//     */
//    public String getTibRecordReport(List<WalletUser> walletUserList){
//        StringBuffer buffer=new StringBuffer();
//        StringBuffer buffer_detail=new StringBuffer();
//        Map<String, BigDecimal> coinEthMap = Maps.newConcurrentMap();//每一种币对应的数量'
//
//        BigDecimal valueEtherSum=new BigDecimal(0);
//        buffer.append("<div style='font-size:12px'>");
//
//        if(true) {
//            buffer_detail.append("<div><B>提币明细</B>");
//            int count = 1;
//            for (WalletUser walletUser : walletUserList) {
//                String accountId = walletUser.getAccountId();
//                String coinname = walletUser.getCoinName();
//                BigDecimal valether = walletUser.getValueEther();
//                valueEtherSum=valueEtherSum.add(valether);
//                if(hashBasedTable.contains(accountId, coinname)){
//                    BigDecimal valueEther=hashBasedTable.get(accountId, coinname);//已经转出的账户
//                    buffer_detail.append(String.format("<p>%s<span style='color:red'>已提</span>提取地址：%s  |  币种:%s  |  提取数量:%s (ether) 已提数量：%s</p>", count, accountId, coinname, valether.toString(),valueEther.toString()));
//                }else {
//                    buffer_detail.append(String.format("<p>%s提取地址：%s  |  币种:%s  |  提取数量:%s (ether)</p>", count, accountId, coinname, valether.toString()));
//                }
//                if (!coinEthMap.containsKey(coinname)) {
//                    coinEthMap.put(coinname, valether);
//                } else {
//                    BigDecimal add_valether = coinEthMap.get(coinname);
//                    coinEthMap.put(coinname, add_valether.add(valether));
//                }
//                count++;
//            }
//            buffer_detail.append("</div>");
//        }
//        buffer.append("<div><B>提币统计</B>");
//        buffer.append(String.format("<p>提取总次数：%s</p>",walletUserList.size()));
//        buffer.append(String.format("<p>提取地址总数量：%s （排除重复）</p>",accountSet(walletUserList).size()));
//        buffer.append(String.format("<p>提取总数量：%s </p>",valueEtherSum));
//
//        Set<String> coinSet=coinEthMap.keySet();
//        Iterator<String> iterator= coinSet.iterator();
//        while(iterator.hasNext()){
//            String coinname=iterator.next();
//            buffer.append(String.format("<p>币种：%s  总提取数量：%s</p>",coinname, coinEthMap.get(coinname).toString()));
//        }
//        buffer.append("</div>");
//        buffer.append(buffer_detail.toString());
//        buffer.append("</div>");
//        return buffer.toString();
//    }
//
//    public String gasDetailHtml(Map<String, BigInteger> coinGasMap){
//        StringBuffer buffer=new StringBuffer();
//        buffer.append("<div style='font-size:12px'><B>单笔币种最高手续费明细</B>");
//        Set<String> coinSet=coinGasMap.keySet();
//        Iterator<String> iterator= coinSet.iterator();
//        while(iterator.hasNext()){
//            String coinname=iterator.next();
//            BigInteger gasWei=coinGasMap.get(coinname);
//            buffer.append(String.format("<p>币种：%s, Gas：%s (ether)</p>",coinname, Convert.fromWei(gasWei.toString(), Convert.Unit.ETHER)));
//        }
//        buffer.append("</div>");
//        return buffer.toString();
//    }
//
//
//    /**
//     * 添加交易hash到主缓存
//     * @param transactions
//     */
//    public synchronized void addCacheFromTrasactionHashs(String transactions){
//        if(transactions.indexOf(",")!=-1){
//            List transactionHashList=Lists.newArrayList(transactions.split(","));
//            transactionHashList.removeAll(Collections.singleton(null));//删除为空的内容
//            transferSummaryCoinManage.getTransactionCacheSet().addAll(transactionHashList);
//        }else{
//            transferSummaryCoinManage.getTransactionCacheSet().add(transactions);
//        }
//    }
//
//    /**
//     * 字符串hash ，多个逗号隔开的，其中一个存在则返回true,都不存在返回false
//     * @param transactions
//     * @return
//     */
//    public boolean isCacheByTransactionHashs(String transactions){
//        if(StringUtils.isNullOrEmpty(transactions)){
//            return false;
//        }
//        if(transactions.indexOf(",")==-1) {
//            return transferSummaryCoinManage.getTransactionCacheSet().contains(transactions);
//        }else{
//            List<String> transactionHashList=Lists.newArrayList(transactions.split(","));
//            transactionHashList.removeAll(Collections.singleton(null));//删除为空的内容
//            for(String hash: transactionHashList){
//                if(transferSummaryCoinManage.getTransactionCacheSet().contains(hash)){
//                    return true;
//                }
//            }
//            return false;
//        }
//    }
//
//
//    public synchronized boolean delCacheByTransactionHash(String transaction){
//        return transferSummaryCoinManage.getTransactionCacheSet().remove(transaction);
//    }
//}
