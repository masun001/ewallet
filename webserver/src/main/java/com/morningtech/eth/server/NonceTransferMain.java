package com.morningtech.eth.server;

import com.morningtech.eth.server.eth.call.MyRawTransactionManager;
import com.morningtech.eth.server.eth.call.PoolingProcessor;
import com.morningtech.eth.server.eth.contract.ContractConfig;
import com.morningtech.eth.server.eth.contract.EthContract;
import com.morningtech.eth.server.eth.contract.GeneralContract;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.WalletUtils;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.admin.Admin;
import org.web3j.protocol.geth.Geth;
import org.web3j.protocol.http.HttpService;
import org.web3j.protocol.ipc.UnixIpcService;
import org.web3j.protocol.ipc.WindowsIpcService;
import org.web3j.tx.ChainId;
import org.web3j.tx.FastRawTransactionManager;
import org.web3j.tx.RawTransactionManager;
import org.web3j.tx.TransactionManager;
import org.web3j.utils.Convert;

import java.io.IOException;
import java.math.BigInteger;

/**
 * @author xuchunlin
 * @version V1.0
 * @Title: NonceTransferMain
 * @Package com.morningtech.eth.server
 * @Description: TODO
 * @date 2018/7/2 18:19
 */
public class NonceTransferMain {


    public static void main(String[] args) {
        String RPC_URL="http://192.168.123.225:8545";
        String walletAddr = "0x588cda518c819c73d3ce00dab83acca091ca7e04"; //钱包地址
        String walletPswd = "!Moduoio.2017";//钱包密码
        String walletKeystorePath="D:\\offline_wallet.json";//keystore路径
        BigInteger GAS_PRICE=  BigInteger.valueOf(100_000_000_000L);//100Gwei
        BigInteger GAS_LIMIT= BigInteger.valueOf(50_000);//limit
        BigInteger nonce=BigInteger.valueOf(40);//nonce
        String COIN_NAME="eth";//合约或eth代码
        String contractAddress="";
        try {
            Web3j web3j = Geth.build(new HttpService(RPC_URL));
            if(web3j.netListening().send().isListening()) {
                Credentials credentials = WalletUtils.loadCredentials(walletPswd, walletKeystorePath);
                TransactionManager clientTransactionManager = new MyRawTransactionManager(web3j, credentials, ChainId.NONE, new PoolingProcessor(web3j, 15000, 40), nonce);
                GeneralContract contractToken = null;
                if (COIN_NAME.equals("eth")) {
                    ContractConfig contractConfig = new ContractConfig();
                    contractConfig.setGasPrice(GAS_PRICE);
                    contractConfig.setGasLimit(GAS_LIMIT);
                    contractToken = EthContract.load(contractConfig, walletAddr, web3j, clientTransactionManager, GAS_PRICE, GAS_LIMIT);
                } else {
                    ContractConfig contractConfig = new ContractConfig();
                    contractConfig.setContractAddress(contractAddress);
                    contractConfig.setGasPrice(GAS_PRICE);
                    contractConfig.setGasLimit(GAS_LIMIT);
                    contractToken = GeneralContract.load(contractConfig, contractAddress, web3j, clientTransactionManager, GAS_PRICE, GAS_LIMIT);
                }
                if(COIN_NAME.equals("eth") || contractToken.isValid()) {
                    String toAddress = "0x588cda518c819c73d3ce00dab83acca091ca7e04"; //转出地址
                    String valueEther = "0.01";//转出数量 ether单位
                    contractToken.l_transfer("", Convert.toWei(valueEther, Convert.Unit.ETHER).toBigInteger()).observable().subscribe((tx) -> {
                        System.out.println("Hash:" + tx.getTransactionHash());
                    });
                }else{
                    System.out.println("合约部署失败！！！！！！！！！");
                }
            }

        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
