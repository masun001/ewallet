package com.morningtech.eth.server.eth;

import java.math.BigInteger;

/**
 * @author xuchunlin
 * @version V1.0
 * @Title: ContractConf
 * @Package com.hucheng.api.eth
 * @Description: TODO
 * @date 2017/12/19 12:02
 */
public class ContractConf {
    private String contractAddress="";

    private BigInteger gasPrice=BigInteger.valueOf(0);

    private BigInteger gasLimit=BigInteger.valueOf(0);

    private String binary="";

    private BigInteger maxTibNumber=BigInteger.valueOf(Long.MAX_VALUE);

    private String encodedConstructor="UTF-8";

    private BigInteger initialWeiValue=BigInteger.valueOf(0);

    public String getContractAddress() {
        return contractAddress;
    }

    public void setContractAddress(String contractAddress) {
        this.contractAddress = contractAddress;
    }

    public BigInteger getGasPrice() {
        return gasPrice;
    }

    public void setGasPrice(BigInteger gasPrice) {
        this.gasPrice = gasPrice;
    }

    public BigInteger getGasLimit() {
        return gasLimit;
    }

    public void setGasLimit(BigInteger gasLimit) {
        this.gasLimit = gasLimit;
    }

    public String getBinary() {
        return binary;
    }

    public void setBinary(String binary) {
        this.binary = binary;
    }

    public BigInteger getMaxTibNumber() {
        return maxTibNumber;
    }

    public void setMaxTibNumber(BigInteger maxTibNumber) {
        this.maxTibNumber = maxTibNumber;
    }

    public String getEncodedConstructor() {
        return encodedConstructor;
    }

    public void setEncodedConstructor(String encodedConstructor) {
        this.encodedConstructor = encodedConstructor;
    }

    public BigInteger getInitialWeiValue() {
        return initialWeiValue;
    }

    public void setInitialWeiValue(BigInteger initialWeiValue) {
        this.initialWeiValue = initialWeiValue;
    }
}
