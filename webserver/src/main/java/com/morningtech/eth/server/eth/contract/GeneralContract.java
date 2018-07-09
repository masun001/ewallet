package com.morningtech.eth.server.eth.contract;

import com.morningtech.eth.server.eth.EthWalletManage;
import com.morningtech.eth.server.eth.res.TransferEventResponse;
import org.slf4j.LoggerFactory;
import org.web3j.abi.EventEncoder;
import org.web3j.abi.EventValues;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Event;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.RemoteCall;
import org.web3j.protocol.core.methods.request.EthFilter;
import org.web3j.protocol.core.methods.response.Log;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tx.Contract;
import org.web3j.tx.TransactionManager;
import org.web3j.utils.Convert;
import rx.Observable;
import rx.functions.Func1;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;

/**
 * 通用合约构建对象
 * @author xuchunlin
 * @version V1.0
 * @Title: GeneralContract
 * @Package com.morningtech.eth.server.eth.contract
 * @Description: TODO
 * @date 2018/6/10 20:03
 */
public class GeneralContract extends Contract{

    private ContractConfig contractConfig=null;

    public final static org.slf4j.Logger logger= LoggerFactory.getLogger(GeneralContract.class);

    public GeneralContract(ContractConfig contractConfig, String contractAddress, Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        super(contractConfig.getContractBinary(), contractAddress, web3j, transactionManager, gasPrice, gasLimit);
        this.contractConfig = contractConfig;
    }

    public GeneralContract(ContractConfig contractConfig, String contractAddress, Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        super(contractConfig.getContractBinary(), contractAddress, web3j, credentials, gasPrice, gasLimit);
        this.contractConfig = contractConfig;
    }

    public TransactionReceipt l_transferFrom(String from, String to, BigInteger value) throws ExecutionException, InterruptedException, IOException {
//        BigInteger decimals= contractConfig.getDecimals();
//        value=value.multiply(BigInteger.valueOf(10).pow(BigInteger.valueOf(18).subtract(decimals).intValue()));
        return transferFrom(from,to,value).sendAsync().get();
    }

    public BigInteger l_getBalance(String address) throws ExecutionException, InterruptedException {
        BigInteger decimals= contractConfig.getDecimals();
        return  balanceOf(address).sendAsync().get().multiply(BigInteger.valueOf(10).pow(BigInteger.valueOf(18).subtract(decimals).intValue()));
    }

    public RemoteCall<TransactionReceipt> l_transfer(String address, BigInteger value) throws ExecutionException, InterruptedException, IOException {
//        value=Convert.fromWei(value.toString(),Convert.Unit.ETHER).toBigInteger();
        logger.debug("l_transfer : value:{}", value);
        return transfer(address,value);
    }


    public RemoteCall<TransactionReceipt> transferFrom(String _from, String _to,
                                                       BigInteger _value) {
        Function function = new Function(
                "transferFrom",
                Arrays.<Type>asList(new Address(_from),
                        new Address(_to),
                        new Uint256(_value)),
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteCall<BigInteger> balanceOf(String _owner) {
        Function function = new Function("balanceOf",
                Arrays.<Type>asList(new Address(_owner)),
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        return executeRemoteCallSingleValueReturn(function, java.math.BigInteger.class);
    }

    public RemoteCall<TransactionReceipt> transfer(String _to, BigInteger _value) {
        Function function = new Function(
                "transfer",
                Arrays.<Type>asList(new Address(_to),
                        new Uint256(_value)),
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteCall<BigInteger> decimals() {
        Function function = new Function("decimals",
                Arrays.<Type>asList(),
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        return executeRemoteCallSingleValueReturn(function, BigInteger.class);
    }

    public Observable<TransferEventResponse> transferEventObservable(DefaultBlockParameter startBlock,
                                                                     DefaultBlockParameter endBlock) {
        final Event event = new Event("Transfer",
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}, new TypeReference<Address>() {}),
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(event));
        return web3j.ethLogObservable(filter).map(new Func1<Log, TransferEventResponse>() {
            @Override
            public TransferEventResponse call(Log log) {
                EventValues eventValues = extractEventParameters(event, log);
                TransferEventResponse typedResponse = new TransferEventResponse();
                typedResponse.log = log;
                typedResponse.from = (String) eventValues.getIndexedValues().get(0).getValue();
                typedResponse.to = (String) eventValues.getIndexedValues().get(1).getValue();
                BigInteger decimals= contractConfig.getDecimals();

                BigInteger _value=  (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
                typedResponse.value = _value.multiply(BigInteger.valueOf(10).pow(BigInteger.valueOf(18).subtract(decimals).intValue()));
                return typedResponse;
            }
        });
    }

    public RemoteCall<BigInteger> totalSupply() {
        Function function = new Function("totalSupply",
                Arrays.<Type>asList(),
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        return executeRemoteCallSingleValueReturn(function, BigInteger.class);
    }

    public RemoteCall<BigInteger> allowance(String _owner, String _spender) {
        Function function = new Function("allowance",
                Arrays.<Type>asList(new Address(_owner),
                        new Address(_spender)),
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        return executeRemoteCallSingleValueReturn(function, BigInteger.class);
    }

    public static GeneralContract load(ContractConfig contractConfig, String contractAddress, Web3j web3j, Credentials credentials,
                                       BigInteger gasPrice, BigInteger gasLimit) {
        return new GeneralContract(contractConfig, contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    public static GeneralContract load(ContractConfig contractConfig, String contractAddress, Web3j web3j,
                                       TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        return new GeneralContract(contractConfig, contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    public ContractConfig getContractConfig() {
        return contractConfig;
    }

    public TransactionManager transactionManager(){
        return transactionManager;
    }
}
