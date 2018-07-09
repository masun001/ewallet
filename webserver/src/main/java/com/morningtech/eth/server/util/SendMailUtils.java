package com.morningtech.eth.server.util;


import org.simplejavamail.email.Email;
import org.simplejavamail.email.EmailBuilder;
import org.simplejavamail.mailer.Mailer;
import org.simplejavamail.mailer.MailerBuilder;
import org.simplejavamail.mailer.config.TransportStrategy;

/**
 * @author xuchunlin
 * @version V1.0
 * @Title: SendMailUtils
 * @Package com.hucheng.util.email
 * @Description: TODO
 * @date 2018/1/13 11:45
 */
public class SendMailUtils {

//    public static void send(String toemail,String subject,String html){
//        send(toemail,subject,html,null);
//    }
//
//    public static void send(String toemail,String subject,String html,String ...ccAddress){
//        Email email=null;
//        if(ccAddress!=null && ccAddress.length>0){
//            email = EmailBuilder.startingBlank().from("noreply@notice.moduo.io").cc("通知人",ccAddress).to(toemail).withSubject(subject).appendTextHTML(html).buildEmail();
//        }else{
//            email = EmailBuilder.startingBlank().from("noreply@notice.moduo.io").to(toemail).withSubject(subject).appendTextHTML(html).buildEmail();
//        }
//
//        Mailer mailer = MailerBuilder.withSMTPServer("smtpdm.aliyun.com", 465)
//                .withSMTPServerUsername("903207003@qq.com")
//                .withSMTPServerPassword("")
////                .withTransportStrategy(TransportStrategy.SMTPS)
//                .withSessionTimeout(5* 1000)
//                .withDebugLogging(false)
//                .buildMailer();
//
//        mailer.sendMail(email,true);
//    }

    public static void send(String smtphost,int port,String emailAddress,String emailPswd,String toemail,String subject,String html,String ...ccAddress){
        try {
            Email email = null;
            if (ccAddress != null && ccAddress.length > 0) {
                email = EmailBuilder.startingBlank().from(emailAddress).cc("通知人", ccAddress).to(toemail).withSubject(subject).appendTextHTML(html).buildEmail();
            } else {
                email = EmailBuilder.startingBlank().from(emailAddress).to(toemail).withSubject(subject).appendTextHTML(html).buildEmail();
            }

            Mailer mailer = MailerBuilder.withSMTPServer(smtphost, port)
                    .withSMTPServerUsername(emailAddress)
                    .withSMTPServerPassword(emailPswd)
                .withTransportStrategy(TransportStrategy.SMTPS)
                    .withSessionTimeout(5 * 1000)
                    .withDebugLogging(false)
                    .buildMailer();

            mailer.sendMail(email, true);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

}
