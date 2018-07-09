package com.morningtech.eth.server.eth.service;


import org.web3j.protocol.core.RemoteCall;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.exceptions.TransactionException;
import org.web3j.tx.ClientTransactionManager;
import org.web3j.tx.Contract;
import org.web3j.tx.Transfer;
import org.web3j.utils.Convert;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * 以太坊及合约接口定义
 * @author xuchunlin
 * @version V1.0
 * @Title: EthInterface
 * @Package com.hucheng.api.eth.service
 * @Description: TODO
 * @date 2017/12/5 18:47
 */
public interface EthContractInterface {

    /**
     * 异步部署合约对象
     * @param type 合约对象
     * @param gasPrice 价格
     * @param gasLimit 最大值
     * @param binary 合约Bin
     * @param encodedConstructor 字符编码
     * @param value 初始化值
     * @param <T>
     * @return
     * @throws Exception
     */
    <T extends Contract> RemoteCall<T> deployAsync(Class<T> type, BigInteger gasPrice, BigInteger gasLimit, String binary, String encodedConstructor, BigInteger value) throws Exception;

    /**
     * 同步部署合约对象
     * @param type 合约对象
     * @param gasPrice 价格
     * @param gasLimit 最大值
     * @param binary 合约Bin
     * @param encodedConstructor 字符编码
     * @param value 初始化值
     * @param <T>
     * @return
     * @throws Exception
     */
    <T extends Contract> T deploy(Class<T> type, BigInteger gasPrice, BigInteger gasLimit, String binary, String encodedConstructor, BigInteger value) throws Exception;


    /**
     * 根据枚举异步获取合约对象
     * @param coinname
     * @param <T>
     * @return
     * @throws Exception
     */
     <T extends Contract> RemoteCall<T> deployAsync(String coinname) throws Exception;

    /**
     * 根据枚举获取合约对象
     * @param coinname
     * @param <T>
     * @return
     * @throws Exception
     */
     <T extends Contract> T deploy(String coinname) throws Exception;

    /**
     * 给账户设置金额
     * @param bigInteger
     */
    TransactionReceipt executeTrans(BigInteger bigInteger) throws TransactionException, IOException ;

    /**
     * 转入币到对方账户
     * @param to
     * @param value
     * @return
     * @throws IOException
     */
    EthSendTransaction sendTransaction(String to, BigInteger value) throws IOException;

    Transfer transfer();

    ClientTransactionManager clientTransactionManager();

    RemoteCall<TransactionReceipt>  sendTransaction(String to, BigDecimal value, Convert.Unit unit) throws IOException;

    RemoteCall<TransactionReceipt>  sendTransaction(String to, BigDecimal value, Convert.Unit unit, BigInteger gasPrice, BigInteger gasLimit) throws IOException;

    /**
     * 转入币到对方账户
     * @param to
     * @param value
     * @return
     * @throws IOException
     */
    EthSendTransaction sendTransaction(BigInteger gasPrice, BigInteger gasLimit, String to, BigInteger value) throws IOException;


}
