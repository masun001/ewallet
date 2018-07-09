package com.morningtech.eth.server.eth.call;

import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.EthGetTransactionReceipt;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.exceptions.TransactionException;
import org.web3j.tx.response.TransactionReceiptProcessor;

import java.io.IOException;
import java.util.Optional;

/**
 * @author xuchunlin
 * @version V1.0
 * @Title: PoolingProcessor
 * @Package com.hucheng.wallet.eth.call
 * @Description: TODO
 * @date 2018/1/21 23:22
 */
public class PoolingProcessor extends TransactionReceiptProcessor {
    private final Web3j web3j;
    private final long sleepDuration;
    private final int attempts;

    public PoolingProcessor(Web3j web3j, long sleepDuration, int attempts) {
        super(web3j);
        this.web3j=web3j;
        this.sleepDuration = sleepDuration;
        this.attempts = attempts;
    }

    public TransactionReceipt waitForTransactionReceipt(String transactionHash) throws IOException, TransactionException {
        TransactionReceipt transactionReceipt=new TransactionReceipt();
        transactionReceipt.setTransactionHash(transactionHash);
        transactionReceipt.setStatus("Pending");
        return transactionReceipt;
    }

    private TransactionReceipt getTransactionReceipt(String transactionHash, long sleepDuration, int attempts) throws IOException, TransactionException {
        Optional<TransactionReceipt> receiptOptional = null;

        for(int i = 0; i < attempts; ++i) {
            if(receiptOptional!=null) {
                if (receiptOptional.isPresent()) {
                    return (TransactionReceipt) receiptOptional.get();
                }
            }
            try {
                Thread.sleep(sleepDuration);
            } catch (InterruptedException var8) {
                throw new TransactionException(var8);
            }

            receiptOptional = this.sendTransactionReceiptRequest(transactionHash);
        }

        throw new TransactionException("Transaction receipt was not generated after " + sleepDuration * (long)attempts / 1000L + " seconds for transaction: " + transactionHash);
    }

    Optional<TransactionReceipt> sendTransactionReceiptRequest(String transactionHash){
        try {
            EthGetTransactionReceipt transactionReceipt = this.web3j.ethGetTransactionReceipt(transactionHash).send();
            if (transactionReceipt.hasError()) {
                System.out.println("hash:::"+transactionHash);
                throw new TransactionException("Error processing request: " + transactionReceipt.getError().getMessage());
            } else {
                return transactionReceipt.getTransactionReceipt();
            }
        }catch (Exception e){
            return null;
        }
    }
}
