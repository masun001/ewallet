package com.morningtech.eth.server.eth.contract;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.morningtech.eth.server.WalletService;
import com.morningtech.eth.server.entity.SysEthContract;
import com.morningtech.eth.server.service.SysEthContractService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.web3j.utils.Convert;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * 合约管理对象
 * @author xuchunlin
 * @version V1.0
 * @Title: ContractManage
 * @Package com.morningtech.eth.server.eth.contract
 * @Description: TODO
 * @date 2018/6/10 20:29
 */
@Component
public class ContractManage{
    /**
     * 部署的配置列表
     */
    public static Map<String,ContractConfig> contractConfigListMap= Maps.newConcurrentMap();

    private Boolean initCoin=true;

    @Autowired
    private SysEthContractService sysEthContractService;

    private Integer lastId=null;

    private List<String> failureSet= Lists.newArrayList();

    private Map<String, BigInteger> coinGasMap=new HashMap();//合约每笔gas

    /**
     * 加载新的合约列表
     * @return
     */
    public Set<String> loadContracConfigs(){
       Set<String> coinnameSet= Sets.newHashSet();
       List<SysEthContract> sysEthContractList;
        try {
//            contractConfigListMap.clear();
            sysEthContractList=  sysEthContractService.findEthAndContractList(lastId);//查询所有合约对象
            if(sysEthContractList!=null && sysEthContractList.size()>0) {
                for (SysEthContract sysEthContract : sysEthContractList) {
                        String coinname = sysEthContract.getCoinname();
                        ContractConfig contractConfig = new ContractConfig();
                        BeanUtils.copyProperties(sysEthContract, contractConfig);
                        if (sysEthContract.getGasPrice() != null) {
                            contractConfig.setGasPrice(Convert.toWei(new BigDecimal(sysEthContract.getGasPrice().toString()), Convert.Unit.GWEI).toBigInteger());
                        }
                        coinGasMap.put(coinname, contractConfig.getGasPrice().multiply(contractConfig.getGasLimit()));
                        contractConfigListMap.put(coinname, contractConfig);
                        coinnameSet.add(coinname);
                }
                lastId = sysEthContractList.get(sysEthContractList.size() - 1).getId();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        coinnameSet.addAll(failureSet);
        failureSet.clear();
        return coinnameSet;
    }

    public void addFailureCoinname(String coinname){
        try {
            SysEthContract sysEthContract = sysEthContractService.findEthContractConfig(coinname);
            //开启则重新尝试部署合约
            if (sysEthContract != null) {
                ContractConfig contractConfig = new ContractConfig();
                BeanUtils.copyProperties(sysEthContract, contractConfig);
                if (sysEthContract.getGasPrice() != null) {
                    contractConfig.setGasPrice(Convert.toWei(new BigDecimal(sysEthContract.getGasPrice().toString()), Convert.Unit.GWEI).toBigInteger());
                }
                contractConfigListMap.put(coinname, contractConfig);
                failureSet.add(coinname);
            }
        }catch (Exception e){}
    }

    public Set<String> getCoinList(){
        return contractConfigListMap.keySet();
    }

    public ContractConfig getContrarctConfig(String coinname){
        return contractConfigListMap.get(coinname);
    }

    public BigInteger getGas(String coinname){
        return coinGasMap.get(coinname);
    }

    public void setInitCoin(Boolean initCoin) {
        this.initCoin = initCoin;
    }
}
