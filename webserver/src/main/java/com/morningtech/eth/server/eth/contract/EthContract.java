package com.morningtech.eth.server.eth.contract;

import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.RemoteCall;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tx.TransactionManager;
import org.web3j.tx.Transfer;
import org.web3j.utils.Convert;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.ExecutionException;

/**
 * ETH Token对象，与合约不同单独处理
 * @author xuchunlin
 * @version V1.0
 * @Title: EthContract
 * @Package com.hucheng.api.eth.service.impl
 * @Description: TODO
 * @date 2017/12/18 12:02
 */
public class EthContract  extends GeneralContract{

    private Transfer transfer;

    private ContractConfig contractConfig=null;

    public EthContract(ContractConfig contractConfig,String contractAddress, Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        super(contractConfig, contractAddress, web3j, credentials, gasPrice, gasLimit);
        this.contractConfig = contractConfig;
    }

    public EthContract(ContractConfig contractConfig,String contractAddress, Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        super(contractConfig, contractAddress, web3j, transactionManager, gasPrice, gasLimit);
        this.contractConfig  =contractConfig;
    }

    public Transfer transfer(){
        if(transfer!=null){
            return transfer;
        }
       return  transfer=new Transfer(web3j, transactionManager);
    }

    public RemoteCall<TransactionReceipt> sendTransaction(String to, BigDecimal value, Convert.Unit unit) throws IOException {
        return sendTransaction(to, value , unit, contractConfig.getGasPrice(), contractConfig.getGasLimit());
    }

    public RemoteCall<TransactionReceipt> sendTransaction(String to, BigDecimal value, Convert.Unit unit,BigInteger gasPrice, BigInteger gasLimit) throws IOException {
        return transfer().sendFunds(to,  value , unit,gasPrice ,gasLimit);
    }

    @Override
    public BigInteger l_getBalance(String address) throws ExecutionException, InterruptedException {
        return web3j.ethGetBalance(address, DefaultBlockParameterName.LATEST).sendAsync().get().getBalance();
    }

    @Override
    public TransactionReceipt l_transferFrom(String from, String to, BigInteger value ) throws ExecutionException, InterruptedException {
        Function function = new Function(
                "transferFrom",
                Arrays.<Type>asList(new Address(from),
                        new Address(to),
                        new Uint256(value)),
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function).sendAsync().get();
    }

    @Override
    public RemoteCall<TransactionReceipt> l_transfer(String address,BigInteger value ) throws ExecutionException, InterruptedException, IOException {
        return sendTransaction(address,new BigDecimal(value), Convert.Unit.WEI);
    }


    public static EthContract load(ContractConfig contractConfig, String contractAddress, Web3j web3j, Credentials credentials,
                                       BigInteger gasPrice, BigInteger gasLimit) {
        return new EthContract(contractConfig, contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    public static EthContract load(ContractConfig contractConfig, String contractAddress, Web3j web3j,
                                       TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        return new EthContract(contractConfig, contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    public TransactionManager transactionManager(){
        return transactionManager;
    }
}
