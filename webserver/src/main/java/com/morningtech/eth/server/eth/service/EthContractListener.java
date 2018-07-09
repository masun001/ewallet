package com.morningtech.eth.server.eth.service;


import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.core.methods.response.Transaction;

class BaseEthListener {

   public void addBlock(EthBlock.Block block){}

   public void pendingTransaction(Transaction tx){}
}

public abstract class EthContractListener extends BaseEthListener{

    public abstract void transaction(Transaction tx);

}
