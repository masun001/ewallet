package com.morningtech.eth.server;

 import com.morningtech.eth.server.entity.EthTransfer;
 import com.morningtech.eth.server.entity.MentionMsg;
 import com.morningtech.eth.server.entity.SysEthTransfer;
 import com.morningtech.eth.server.redis.RedisService;
 import com.morningtech.eth.server.util.StringUtils;
 import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
 import java.math.BigDecimal;
 import java.math.BigInteger;

/**
 * @author xuchunlin
 * @version V1.0
 * @Title: WalletService
 * @Package com.hucheng.wallet
 * @Description: TODO
 * @date 2017/12/27 11:39
 */
public class WalletService {

    public final static Logger logger= LoggerFactory.getLogger(WalletService.class);

    @Autowired
    private WalletManage walletManage;

    @Autowired
    private RedisService redisService;

    /**
     * 初始化钱包服务
     */
    public void init(){
        System.setProperty("user.timezone","Asia/Shanghai");
        try {
            logger.debug("Start WalletService........");
            walletManage.initCoin();//初始化所有钱包

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void test(){
            MentionMsg mentionMsg =new MentionMsg();
            mentionMsg.
                    setHash("0x8c127a82cc388217bc12944690234f941920ea1273641a5bf24919e2bdee6405");
            mentionMsg.setMentionaddress("0x39d3dfbb35551c1d4f696b57e514b524d880390b");
            mentionMsg.setNum(BigDecimal.valueOf(100).setScale(8));
            mentionMsg.setStatus(0);
            mentionMsg.setCoinname("dcc");
            mentionMsg.setTime(System.currentTimeMillis());
            mentionMsg.setTxid("690234f941920ea1273641a");
            redisService.publishMentionRecord(mentionMsg);


            EthTransfer transfer=new EthTransfer();
            transfer.setTransactionhash("0x8c127a82cc388217bc12944690234f941920ea1273641a5bf24919e2bdee6405");
            transfer.setEth(BigDecimal.valueOf(100.22).setScale(8));
            transfer.setFrom("0x39d3dfbb35551c1d4f696b57e514b524d880390b");
            transfer.setTo("0xdc30a5baf62f3ce8d7e74a272bc139fbb7a0cab4");
            transfer.setValue(BigInteger.valueOf(1800000000000000000L));
            transfer.setCoinname("dcc");
            transfer.setStatus(0);
            transfer.setTime(System.currentTimeMillis()/1000);
            redisService.publishEthTransaction(transfer);
    }
}
