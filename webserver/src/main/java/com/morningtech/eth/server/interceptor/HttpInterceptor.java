package com.morningtech.eth.server.interceptor;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author xuchunlin
 * @version V1.0
 * @Title: HttpInterceptor
 * @Package com.hucheng.chinawkb.interceptor
 * @Description: 处理所有请求，获取请求信息，不做拦截
 * @date 2017/11/28 19:27
 */
public class HttpInterceptor extends HandlerInterceptorAdapter {

    public final static Logger logger= LoggerFactory.getLogger(HttpInterceptor.class);


    private String[] allowUrl=new String[]{"/public/","/authorize/"};

    public boolean isContainsIndexOf(String[] arrayUrl, String requestUrl){
        for(String url: allowUrl){
            if(requestUrl.indexOf(url)==0){
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        response.setCharacterEncoding("UTF-8");
        request.setCharacterEncoding("UTF-8");
        response.addHeader("x-frame-options","SAMEORIGIN");//允许相同域名页面的 frame 中展示

        String requestUrl = request.getRequestURI().replace(request.getContextPath(), "");
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        super.postHandle(request, response, handler, modelAndView);
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        super.afterCompletion(request, response, handler, ex);
    }

    @Override
    public void afterConcurrentHandlingStarted(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        super.afterConcurrentHandlingStarted(request, response, handler);
    }
}
