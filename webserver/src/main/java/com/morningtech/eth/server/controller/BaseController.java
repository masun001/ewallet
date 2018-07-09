package com.morningtech.eth.server.controller;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;

import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author xuchunlin
 * @version V1.0
 * @Title: BaseController
 * @Package com.hucheng.chinawkb.controller
 * @Description: TODO
 * @date 2017/11/28 16:08
 */
public class BaseController {

    @Autowired
    protected HttpServletRequest request;


    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(String.class, new StringEscapeEditor(false, false, false));
        binder.registerCustomEditor(Date.class, new CustomDateEditor(
                new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"), false));
    }

//    protected String client_ip(){
//        return (String)request.getAttribute(Constant.REQ_IP);
//    }
//
//    protected String client_userAgent(){
//        return (String)request.getAttribute(Constant.REQ_UA);
//    }
//
//    protected String client_lang(){
//        return (String)request.getAttribute(Constant.REQ_LANG);
//    }
//
//    protected String client_osType(){
//        return (String)request.getAttribute(Constant.REQ_OSTYPE);
//    }
//
//    protected String client_version(){
//        return (String)request.getAttribute(Constant.REQ_APPVERSION);
//    }

    /**
     * 获取指定类的日志记录对象
     *
     * @param clazz
     * @return
     */
    protected static Logger getLogger(Class clazz) {
        return LoggerFactory.getLogger(clazz);
    }

    /**
     * 获取Base的日志对象
     * @return
     */
    protected Logger getLogger() {
        return LoggerFactory.getLogger(BaseController.class);
    }

}
