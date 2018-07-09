package com.morningtech.eth.server.eth;

import com.google.common.collect.Maps;
import com.morningtech.eth.server.eth.call.PendingCall;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Map;

/**
 * @author xuchunlin
 * @version V1.0
 * @Title: PendingHash
 * @Package com.hucheng.wallet.eth
 * @Description: TODO
 * @date 2018/1/5 17:40
 */
@Component
public class PendingHash {
    private String pendingTransactionHash;
    private Map<String,PendingCall> pendingCallMap= Maps.newConcurrentMap();

    public void add(String pendingTransactionHash){
        synchronized (this){
            this.pendingTransactionHash=pendingTransactionHash;
            Collection<PendingCall> pendingCalls= pendingCallMap.values();
            for(PendingCall pendingCall: pendingCalls){
                pendingCall.call(pendingTransactionHash);
            }
            pendingCallMap.clear();
        }
    }
    public PendingHash addCall(String name,PendingCall pendingCall){
        pendingCallMap.put(name, pendingCall);
        return  this;
    }

}

