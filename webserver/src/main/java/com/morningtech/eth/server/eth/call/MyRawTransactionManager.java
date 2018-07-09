package com.morningtech.eth.server.eth.call;

import org.web3j.crypto.Credentials;
import org.web3j.crypto.RawTransaction;
import org.web3j.crypto.TransactionEncoder;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
 import org.web3j.tx.TransactionManager;
import org.web3j.tx.response.TransactionReceiptProcessor;
import org.web3j.utils.Numeric;

import java.io.IOException;
import java.math.BigInteger;

/**
 * @author xuchunlin
 * @version V1.0
 * @Title: MyRawTransactionManager
 * @Package com.morningtech.eth.server.eth.call
 * @Description: TODO
 * @date 2018/6/27 18:10
 */
public class MyRawTransactionManager extends TransactionManager {
    private final Web3j web3j;
    final Credentials credentials;
    private final byte chainId;
    private BigInteger nonce;

    public MyRawTransactionManager(Web3j web3j, Credentials credentials, byte chainId, TransactionReceiptProcessor transactionReceiptProcessor, BigInteger nonce) {
        super(transactionReceiptProcessor, credentials.getAddress());
        this.web3j = web3j;
        this.credentials = credentials;
        this.chainId = chainId;
        this.nonce = nonce;
    }

    protected BigInteger getNonce() throws IOException {
         return this.nonce;
    }

    public EthSendTransaction sendTransaction(BigInteger gasPrice, BigInteger gasLimit, String to, String data, BigInteger value) throws IOException {
        RawTransaction rawTransaction = RawTransaction.createTransaction(getNonce(), gasPrice, gasLimit, to, value, data);
        return this.signAndSend(rawTransaction);
    }

    public EthSendTransaction signAndSend(RawTransaction rawTransaction) throws IOException {
        byte[] signedMessage;
        if (this.chainId > -1) {
            signedMessage = TransactionEncoder.signMessage(rawTransaction, this.chainId, this.credentials);
        } else {
            signedMessage = TransactionEncoder.signMessage(rawTransaction, this.credentials);
        }

        String hexValue = Numeric.toHexString(signedMessage);
        return (EthSendTransaction)this.web3j.ethSendRawTransaction(hexValue).send();
    }
}

