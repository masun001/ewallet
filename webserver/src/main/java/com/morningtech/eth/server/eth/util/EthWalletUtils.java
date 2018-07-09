package com.morningtech.eth.server.eth.util;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.morningtech.eth.server.util.StringUtils;
import org.web3j.crypto.*;
import org.web3j.utils.Numeric;

import java.io.IOException;
import java.io.InputStream;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Eth钱包工具类
 * @author xuchunlin
 * @version V1.0
 * @Title: MyWalletUtils
 * @Package com.hucheng.wallet.eth.util
 * @Description: TODO
 * @date 2018/1/24 10:28
 */
public class EthWalletUtils {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public EthWalletUtils() {
    }

    public Credentials credentials(String pswd, String keystorePath) throws IOException, CipherException {
        return  WalletUtils.loadCredentials(pswd, keystorePath);
    }

    public static synchronized Credentials loadCredentials(String password, byte[] bytes) throws IOException, CipherException {
        WalletFile walletFile = (WalletFile)objectMapper.readValue(bytes, WalletFile.class);
        return Credentials.create(Wallet.decrypt(password, walletFile));
    }

    public static Credentials loadCredentials(String password, InputStream inputStream) throws IOException, CipherException {
        WalletFile walletFile = (WalletFile)objectMapper.readValue(inputStream, WalletFile.class);
        return Credentials.create(Wallet.decrypt(password, walletFile));
    }

    private static String getWalletFileName(WalletFile walletFile) {
        DateTimeFormatter format = DateTimeFormatter.ofPattern("'UTC--'yyyy-MM-dd'T'HH-mm-ss.nVV'--'");
        ZonedDateTime now = ZonedDateTime.now(ZoneOffset.UTC);
        return now.format(format) + walletFile.getAddress() + ".json";
    }


    static {
        objectMapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public static  String hexToBigIntString(String value){
        try {
            if (!StringUtils.isNullOrEmpty(value) && Numeric.containsHexPrefix(value)) {
                String code = Numeric.toBigInt(value).toString();
                return code;
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }
}
