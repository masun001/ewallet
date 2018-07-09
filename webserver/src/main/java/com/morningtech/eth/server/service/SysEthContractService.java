package com.morningtech.eth.server.service;

import com.morningtech.eth.server.entity.SysEthContract;
import com.morningtech.eth.server.entity.SysEthMention;

import java.util.List;

/**
 * @author xuchunlin
 * @version V1.0
 * @Title: DandanUserService
 * @Package com.hucheng.service
 * @Description: TODO
 * @date 2017/11/28 16:10
 */
public interface SysEthContractService {

    /**
     * 查询所有合约对象
     * @param  maxid  查询id大于maxid的所有记录
     * @return
     * @throws Exception
     */
    List<SysEthContract> findEthAndContractList(Integer maxid) throws Exception;

    /**
     * 根据coinname查询合约配置
     * @param coinname
     * @return
     * @throws Exception
     */
    SysEthContract findEthContractConfig(String coinname) throws Exception;
}
