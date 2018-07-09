package com.morningtech.eth.server.eth;

import com.morningtech.eth.server.util.PropertyUtils;
import com.morningtech.eth.server.util.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.math.BigInteger;
import java.util.*;

/**
 * @author xuchunlin
 * @version V1.0
 * @Title: Conf
 * @Package com.hucheng.api.eth
 * @Description: TODO
 * @date 2017/12/5 17:35
 */
@Component
public class Conf {
    enum EnumProtocol{
        http,socket,unknow
    };
    //协议类型/http or socket
    @Value("${web3j.protocol}")
    private String protocol="http";
    //http请求路径
    @Value("${eth.wallet.url}")
    public String ethWalletUrl="";
    //socket文件路径 rpc
    @Value("${eth.wallet.socketdir}")
    public String ethWalletSocketPath="";
    //钱包密码
    @Value("${eth.wallet.password}")
    private String ethWalletPswd="";
    //钱包keystore存放路径
    @Value("${eth.wallet.directory}")
    private String ethWalletDir="";
    //钱包地址
    @Value("${eth.wallet.addr}")
    private String ethWalletAddr="";
    //钱包文件路径
    @Value("${eth.wallet.path}")
    private String ethWalletPath="";
    //钱包币种
    private List<String> coinList=new ArrayList();

    private static Map<String,ContractConf> contractConfMap=new HashMap<>();

    // GAS价格  0.0011
    public static BigInteger GAS_PRICE = BigInteger.valueOf(20_000_000_000L);//10Gwei 20_000_000_000L
    // GAS上限
    public static BigInteger GAS_LIMIT = BigInteger.valueOf(50_000L);//4_300_000L
    //ETH 最大提取数量
    public static BigInteger MAX_TIBNUMBER=BigInteger.valueOf(5);
    // ETHER以太币
    public static BigInteger ETHER = new BigInteger("1000000000000000000");

    public boolean isHttp() {
       if(protocol.equals(EnumProtocol.http.name())){
           return true;
       }
       return false;
    }

    public String getEthWalletUrl() {
        return ethWalletUrl;
    }

    public String getEthWalletSocketPath() {
        return ethWalletSocketPath;
    }

    public String getEthWalletPswd() {
        return ethWalletPswd;
    }

    public String getEthWalletDir() {
        return ethWalletDir;
    }

    public String getEthWalletAddr() {
        return ethWalletAddr;
    }

    public String getEthWalletPath() {
        return ethWalletPath;
    }

    public static String IFNULL(Object obj ,String def){
        if(obj==null || obj.equals("")){
            return def;
        }
        return (String)obj;
    }

    public static ContractConf coinConf(String coin){
        try {
            if (!contractConfMap.containsKey(coin)) {
                ContractConf contractConf = new ContractConf();
                Properties properties = PropertyUtils.readRelative("/eth/" + coin + ".properties");
                contractConf.setContractAddress(IFNULL(properties.get("contractAddress"),""));
                contractConf.setGasPrice(new BigInteger(IFNULL(properties.get("gasPrice"),"0")));
                contractConf.setGasLimit(new BigInteger(IFNULL(properties.get("gasLimit"),"0")));
                contractConf.setBinary(IFNULL(properties.get("binary"),""));
                contractConf.setInitialWeiValue(new BigInteger(IFNULL(properties.get("maxTibNumber"),"0")));
                contractConf.setEncodedConstructor(IFNULL(properties.get("encodedConstructor"),"UTF-8"));
                contractConf.setInitialWeiValue(new BigInteger(IFNULL(properties.get("initialWeiValue"),"0")));
                contractConfMap.put(coin, contractConf);
                return contractConf;
            }else{
                return contractConfMap.get(coin);
            }
        }catch (IOException e){
            System.out.println("请配置币种"+coin.toUpperCase()+"合约配置文件！");
            e.printStackTrace();
        }catch (Exception e){
            System.out.println("读取"+coin.toUpperCase()+"合约配置文件失败！");
            e.printStackTrace();
        }
        return null;
    }

}
