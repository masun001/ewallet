package com.morningtech.eth.server.controller.eth;

import com.morningtech.eth.server.WalletManage;
import com.morningtech.eth.server.controller.BaseController;
import com.morningtech.eth.server.exception.CheckException;
import com.morningtech.eth.server.res.HttpResponse;
import com.morningtech.eth.server.util.StringUtils;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.io.Serializable;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author xuchunlin
 * @version V1.0
 * @Title: WalletController
 * @Package com.morningtech.eth.server.controller
 * @Description: TODO
 * @date 2018/6/8 9:18
 */
@Controller
public class WalletController extends BaseController{

    @Autowired
    private WalletManage walletManage;

    private static ReentrantLock lock = new ReentrantLock(true);

    /**
     * 批量创建钱包地址
     * @param num 需要创建的数量，默认10个，最大100
     * @param <T>
     * @return
     */
    @RequestMapping(value = "/eth/wallet/batchCreateWalletAddress", method = { RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public <T extends Serializable> HttpResponse<T> batchCreateWalletAddress(
            @RequestParam(required = false, defaultValue = "10", value = "num") Integer num
    ){
        if(num.compareTo(Integer.valueOf(100))==1){
            return new HttpResponse<>().check("创建数量不能超过100");
        }
        try {
            List<String> addressList = walletManage.batchCreateWalletAddress(num);
            return new HttpResponse().success(addressList);
        }catch (Exception e) {
            e.printStackTrace();
            return new HttpResponse().failed(String.format("创建失败,原因: %s", e.getMessage()));
        }
    }

    /**
     * 执行汇总到冷钱包任务一次
     * @param <T>
     * @return
     */
    @RequestMapping(value = "/eth/wallet/executeSummaryWithdrawalsAddress", method = { RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public <T extends Serializable> HttpResponse<T> executeSummaryWithdrawalsAddress( ){

            if(lock.tryLock()) {
                try {
                    if(walletManage!=null && walletManage.getTransferSummaryCoinManage()!=null) {
                        walletManage.getTransferSummaryCoinManage().transferSummaryEthCheckTask();
                        return new HttpResponse().success("截止当前所有账户汇总到冷钱包开启!......");
                    }else{
                        throw new Exception("walletmange为空");
                    }
                }catch (CheckException e){
                    return new HttpResponse().check(e.getMessage());
                }catch (Exception e) {
                    getLogger().debug(e.getMessage());
                    e.printStackTrace();
                    return new HttpResponse().failed(String.format("任务执行失败,原因: %s", e.getMessage()));
                }finally {
                    lock.unlock();
                }
            }else{
                return new HttpResponse().success("任务已执行!......");
            }

    }

    @RequestMapping(value = "/eth/wallet/queryBlockNumberTrasactionList", method = { RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public <T extends Serializable> HttpResponse<T> queryBlockNumberTrasactionList( ){
        try {
            walletManage.queryBlockNumberTrasactionList();
        } catch (Exception e) {
            e.printStackTrace();
            return new HttpResponse().failed("任务执行出错!......");
        }
        return new HttpResponse().success("任务已执行!......");
    }


    @RequestMapping(value = "/gas", method = {RequestMethod.GET})
    @ResponseBody
    public <T extends Serializable> HttpResponse<T> getGasUsed(String transHash){

        return new HttpResponse().success(walletManage.getEthWalletManage().queryTransactionReceipt(transHash));
    }
}
