package com.morningtech.eth.server.task;

import com.alibaba.fastjson.JSON;
import com.morningtech.eth.server.WalletManage;
import com.morningtech.eth.server.entity.MentionMsg;
import com.morningtech.eth.server.entity.SysEthContract;
import com.morningtech.eth.server.entity.SysEthMention;
import com.morningtech.eth.server.eth.bean.PendingMyzc;
import com.morningtech.eth.server.eth.contract.ContractConfig;
import com.morningtech.eth.server.eth.contract.GeneralContract;
import com.morningtech.eth.server.eth.enums.EnumContractCoinname;
import com.morningtech.eth.server.task.bean.LackMentionCoin;
import com.morningtech.eth.server.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.web3j.protocol.core.RemoteCall;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.parity.methods.response.VMTrace;
import org.web3j.utils.Convert;
import org.web3j.utils.Numeric;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * 自动提币任务
 * @author xuchunlin
 * @version V1.0
 * @Title: TransactionMainTask
 * @Package com.hucheng.wallet.eth.task
 * @Description: TODO
 * @date 2018/1/8 18:11
 */
public class TransferMentionTask implements Callable<LackMentionCoin> {

    public final static Logger logger= LoggerFactory.getLogger(TransferMentionTask.class);

    private TransferMentionCoinManage transferMentionCoinManage;

    private WalletManage walletManage;

    private String mainAddress;

    private LackMentionCoin lackMentionCoin=new LackMentionCoin();

    private SysEthMention sysEthMention;//提币对象

    public TransferMentionTask(TransferMentionCoinManage transferMentionCoinManage, SysEthMention sysEthMention) {
       this.transferMentionCoinManage=transferMentionCoinManage;
       this.walletManage=transferMentionCoinManage.getWalletManage();
       this.mainAddress=this.walletManage.getEthWalletManage().getConf().getEthWalletAddr();
       this.sysEthMention = sysEthMention;
    }

    @Override
    public LackMentionCoin call() {
        try {
            transfer(sysEthMention);
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            return lackMentionCoin;
        }
    }

    public void transfer(final SysEthMention sysEthMention) throws Exception{
        String txid= sysEthMention.getTxid();
        String coinname= sysEthMention.getCoinname();
        if(coinname!=null && !"".equals(coinname)){
            String  mentionaddress=sysEthMention.getMentionaddress();//用户提币钱包地址
            BigDecimal valueEther=sysEthMention.getNum();//用户提币数量（ehter单位)
            BigInteger value= Convert.toWei(valueEther, Convert.Unit.ETHER).toBigInteger();//转换成(wei单位)
            int id=sysEthMention.getId();//提币任务ID
            String toAddress=mentionaddress;//to地址
            try {
                if(StringUtils.isNullOrEmpty(mentionaddress)){
                    throw  new Exception("转出地址为空,提币ID："+id);
                }
                GeneralContract contract = walletManage.getEthWalletManage().contract(coinname);//根据提币币种获取相应的合约token对象
                if(contract!=null) {
                    ContractConfig contractConfig = contract.getContractConfig();//获取合约对象的配置信息，其中包括gasprice和gaslimit 矿工费信息
                    BigInteger mainCoin_Wei=contract.l_getBalance(mainAddress);//获得热钱包剩余的数量
                    BigInteger maxGas = contractConfig.getGasPrice().multiply(contractConfig.getGasLimit());//计算矿工费用
                    BigInteger mainEth_WEI= walletManage.getEthWalletManage().getBlanceEth_UnitWei(mainAddress);//获取热钱包的以太坊数量/用户支付矿工费

                    boolean contractIsLack=false,ethIsLack=false;
                    if(mainCoin_Wei.compareTo(value)<0){//余额不足
                        BigInteger que=value.subtract(mainCoin_Wei);//计算缺少的数量(wei单位)
                        BigDecimal eth=Convert.fromWei(que.toString(), Convert.Unit.ETHER);//得到缺少的数量(ether单位)
                        lackMentionCoin.setCoinname(sysEthMention.getCoinname());
                        lackMentionCoin.setContractValue(eth);//提币缺少数量
                        contractIsLack=true;
                    }
                    if(mainEth_WEI.compareTo(maxGas)<0){
                        BigInteger que=maxGas.subtract(mainEth_WEI);//计算缺少的矿工费(wei单位)
                        BigDecimal eth=Convert.fromWei(que.toString(), Convert.Unit.ETHER);//所需矿工费(ether单位)
                        lackMentionCoin.setEthValue(eth);//提币缺少矿工费
                        ethIsLack=true;
                    }
                    if(!contractIsLack && !ethIsLack ){
                        logger.debug("---------------------满足，开始提币---------------------------------------------------------------------");
                        //最大提币数量（wei单位)
                        SysEthContract sysEthContract= transferMentionCoinManage.getTransactionServer().findEthContractConfig(coinname);

                        BigInteger maxMentionValue=BigInteger.valueOf(99999999999999L);
                        if(sysEthContract!=null){
                            maxMentionValue =  Convert.toWei(sysEthContract.getMaxMentionLimit(), Convert.Unit.ETHER).toBigInteger();
                        }
                        if(value.compareTo(maxMentionValue)>0){
                            BigDecimal eth=Convert.fromWei(value.toString(), Convert.Unit.ETHER);
                            String subJect=String.format("%s[提币]提币金额太大",coinname.toUpperCase());
                            String content=String.format("%s[提币]提币金额太大，提币数量：%s (ether)",coinname.toUpperCase(),eth);
                            transferMentionCoinManage.getSenderMailServer().send(subJect,content);
                            transferMentionCoinManage.getTransactionServer().lock(id, "提币数量太大");//提币数量太大，锁住记录，进行人工处理
                        }else {
                            logger.debug("解锁热钱包账户-----------开始-------------");
                            //开始热钱包提币操作
                            walletManage.getEthWalletManage().unlockMain();//解锁热钱包账户
                            logger.debug("解锁热钱包账户------------解锁成功----------");
                            try {
                                transferMentionCoinManage.getTransactionServer().lock(id, "正在提币");//锁定记录，如果任务重复，此处会抛出异常阻止任务执行
                            }catch (Exception e){
                                logger.debug("提币已经开始,停止重复提币！！！！！");
                                return;
                            }
                            try {
                                RemoteCall<TransactionReceipt> receiptRemoteCall = contract.l_transfer(toAddress, value);
                                logger.debug("::::::::::::::::::::::::::::::::::::到达开始提币操作::::::::::::::::::::::::::::::::::::::::::::::::::::::");
                                receiptRemoteCall.observable().doOnError(err -> {
                                    try {
                                        //提币失败，修改提币记录状态为失败
                                        transferMentionCoinManage.getTransactionServer().transactionMyzcFailure(id, null, "失败:" + err.getMessage());
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }).subscribe((TransactionReceipt tx) -> {
                                    logger.info("------提币转出状态：----{}", tx.getStatus());
                                    try {
                                        //将hash放入到检测hash任务中
                                        transferMentionCoinManage.saveCacheHash(id, tx.getTransactionHash(), txid, coinname, sysEthMention.getMentionaddress(), valueEther);
                                        logger.info("#wallet# 转账到地址transfer：{}, 金额（eth):{}", toAddress, valueEther);
                                        String status = tx.getStatus();
                                        if (status.equals("Pending")) {
                                            //修改提币任务为交易等待状态
                                            String transactionHash = tx.getTransactionHash();
                                            transferMentionCoinManage.getTransactionServer().transactionMyzcPedding(id, transactionHash);
                                            //提币操作通知/状态交易等待中， 发布到redis
                                            MentionMsg mentionMsg = new MentionMsg();
                                            mentionMsg.setHash(transactionHash);
                                            mentionMsg.setMentionaddress(sysEthMention.getMentionaddress());
                                            mentionMsg.setNum(valueEther);
                                            mentionMsg.setStatus(0);
                                            mentionMsg.setTime(System.currentTimeMillis());
                                            mentionMsg.setTxid(sysEthMention.getTxid());
                                            mentionMsg.setCoinname(coinname);
                                            transferMentionCoinManage.getRedisService().publishMentionRecord(mentionMsg);
                                        } else {
                                            logger.debug("重试提币！！！！！！！！1");
                                            transferMentionCoinManage.getTransactionServer().unlock(id, "重试提币");
                                        }
                                    } catch (Exception e) {
                                        try {
                                            logger.debug("重试提币！！！！！！！！2");
                                            transferMentionCoinManage.getTransactionServer().unlock(id, "重试提币");
                                        } catch (Exception e1) {
                                        }
                                        e.printStackTrace();
                                    }
                                }).unsubscribe();//发布完则取消订阅
                            }catch (Exception e){
                                logger.error("转出到{}，eth:{},错误：{}",mentionaddress, value,e.getMessage());
                                transferMentionCoinManage.getTransactionServer().transactionMyzcFailure(id,null, "失败:"+e.getMessage());
                                e.printStackTrace();
                            }
                        }
                    }
                }else{
                    logger.error("{}合约未部署成功，无法提币", coinname);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }



}
