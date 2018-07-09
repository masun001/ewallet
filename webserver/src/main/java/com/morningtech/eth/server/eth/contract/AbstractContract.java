package com.morningtech.eth.server.eth.contract;

 import com.morningtech.eth.server.eth.res.TransferEventResponse;
 import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.RemoteCall;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tx.Contract;
import org.web3j.tx.TransactionManager;
import rx.Observable;

import java.io.IOException;
import java.math.BigInteger;
import java.util.concurrent.ExecutionException;

/**
 * @author xuchunlin
 * @version V1.0
 * @Title: AbsContract
 * @Package com.hucheng.wallet.eth.contract
 * @Description: TODO
 * @date 2017/12/28 12:41
 */
public abstract class AbstractContract extends Contract{
    protected AbstractContract(String contractBinary, String contractAddress, Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        super(contractBinary, contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    protected AbstractContract(String contractBinary, String contractAddress, Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        super(contractBinary, contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    public abstract Observable<TransferEventResponse> transferEventObservable(DefaultBlockParameter startBlock,
                                                                              DefaultBlockParameter endBlock);

    public abstract BigInteger l_getBalance(String address) throws Exception;

    public abstract RemoteCall<TransactionReceipt> l_transfer(String address, BigInteger value, BigInteger gasPrice) throws ExecutionException, InterruptedException, IOException;

    public abstract TransactionReceipt l_transferFrom(String from,String to, BigInteger value, BigInteger gasPrice) throws ExecutionException, InterruptedException, IOException;
}
