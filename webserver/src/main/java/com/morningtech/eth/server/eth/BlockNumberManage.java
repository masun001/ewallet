package com.morningtech.eth.server.eth;


import com.morningtech.eth.server.util.PropertyUtils;
import com.morningtech.eth.server.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Eth BlockNumber本地文件更新与读取
 * @author xuchunlin
 * @version V1.0
 * @Title: BlockNumberManage
 * @Package com.hucheng.wallet.eth
 * @Description: TODO
 * @date 2018/1/8 12:11
 */
@Component
public class BlockNumberManage {

    public final static Logger logger= LoggerFactory.getLogger(BlockNumberManage.class);

    @Value("${eth.wallet.lastReadFile}")
    public static final String blockFile="/blocknumber.properties";

    ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();

    public void scheduleStart(EthWalletManage ethWalletManage){
        service.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                try {
                   BigInteger bigInteger= ethWalletManage.admin().ethBlockNumber().sendAsync().get().getBlockNumber();
                    logger.debug("更新最新监听区块到文件："+bigInteger.toString());
                   if(bigInteger!=null)
                   saveBlockNumber(bigInteger.toString());
                } catch (InterruptedException e) {
                    logger.error("更新最新区块到文件失败：{}", e.getMessage());
                } catch (ExecutionException e) {
                    logger.error("更新最新区块到文件失败：{}", e.getMessage());
                }
            }
        },60, 60, TimeUnit.SECONDS);
    }

    /**
     * 更新最新区块到文件
     * @param blockNumber
     */
    public void saveBlockNumber(String blockNumber){
        Properties p=new Properties();
        p.put("lastBlockNumber",blockNumber);
        PropertyUtils.write(blockFile,p);
    }

    /**
     * 读取文件ETH区块高度
     * @return
     */
    public BigInteger getEthBlockNumber(){
        try {
            Properties p= PropertyUtils.read(blockFile);
            if(p!=null) {
                String blockNumber = (String) p.get("lastBlockNumber");
                if(!StringUtils.isNullOrEmpty(blockNumber))
                    return new BigInteger(blockNumber).subtract(new BigInteger("900"));//减少误差;
            }
        } catch (IOException e) {
        }
        return BigInteger.valueOf(5779181);
    }

    /**
     * 获取合约区块高度
     * @return
     */
    public BigInteger getContractBlockNumber(){
        try {
            Properties p= PropertyUtils.read(blockFile);
            if(p!=null) {
                String blockNumber = (String) p.get("lastBlockNumber");
                if(!StringUtils.isNullOrEmpty(blockNumber))
                return new BigInteger(blockNumber).subtract(new BigInteger("5000"));//减少误差;
            }
        } catch (IOException e) {
        }
        return new BigInteger("5779181");
    }
}
