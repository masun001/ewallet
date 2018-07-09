package com.morningtech.eth.server.redis;

import com.alibaba.fastjson.JSON;
import com.morningtech.eth.server.entity.MentionMsg;
import com.morningtech.eth.server.entity.SysEthTransfer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @author xuchunlin
 * @version V1.0
 * @Title: RedisService
 * @Package com.morningtech.eth.server.redis
 * @Description: TODO
 * @date 2018/6/8 19:57
 */
@Component
public class RedisService {
    private static final Logger logger= LoggerFactory.getLogger(RedisService.class);

    @Resource(name = "redisTemplate")
    private RedisTemplate redisTemplate;

    @Value("${redis.eth.transactiontopic}")
    private String ethTransactionTopicName;

    @Value("${redis.eth.mentiontopic}")
    private String mentionTopicName;

    public void publishEthTransaction(SysEthTransfer sysEthTransfer){
        redisTemplate.convertAndSend(ethTransactionTopicName, JSON.toJSONString(sysEthTransfer) );
    }

    public void publishMentionRecord(MentionMsg mentionMsg){
        redisTemplate.convertAndSend(mentionTopicName, JSON.toJSONString(mentionMsg));
    }



}
