package com.morningtech.eth.server.email;

import com.morningtech.eth.server.util.SendMailUtils;
import com.morningtech.eth.server.util.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 负责发送email服务类
 * @author xuchunlin
 * @version V1.0
 * @Title: SenderMailServer
 * @Package com.hucheng.wallet.email
 * @Description: TODO
 * @date 2018/1/22 14:05
 */
@Component
public class SenderMailServer {
    @Value("${email.smtp.host}")
    private String smtphost;

    @Value("${email.smtp.port}")
    private Integer smtpport=25;

    @Value("${email.smtp.email}")
    private String formEmail;

    @Value("${email.smtp.password}")
    private String password;

    @Value("${eth.withdrawals.email}")
    private String withdrawalsEmail;

    @Value("${eth.withdrawals.email.cc}")
    private String ccAddress;//抄送人地址，多个逗号隔开


    public void send(String subJect,StringBuffer contentBuffer){
        send( subJect, contentBuffer.toString());
    }

    public void send(String subJect,String content){
        SendMailUtils.send(smtphost, smtpport, formEmail, password, withdrawalsEmail, subJect, content, getCcAddress());
    }

    public void send(String email,String subJect,String content){
        send(email, subJect, content,null);
    }

    public void send(String email,String subJect,String content,String[] cc){
        SendMailUtils.send(smtphost, smtpport, formEmail, password, email, subJect, content, cc);
    }

    public String[] getCcAddress() {
        if(StringUtils.isNullOrEmpty(ccAddress)){
            return new String[]{};
        }
        return ccAddress.split(",");
    }
}
