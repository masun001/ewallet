package com.morningtech.eth.server.service.impl;

import com.morningtech.eth.server.dao.SysEthContractMapperDao;
import com.morningtech.eth.server.entity.SysEthContract;
import com.morningtech.eth.server.service.SysEthContractService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author xuchunlin
 * @version V1.0
 * @Title: DandanUserService
 * @Package com.hucheng.service
 * @Description: TODO
 * @date 2017/11/28 16:10
 */
@Service
public class SysEthContractServiceImpl implements SysEthContractService {

    @Autowired
    private SysEthContractMapperDao sysEthContractMapperDao;

    @Override
    public List<SysEthContract> findEthAndContractList(Integer maxid) throws Exception {
        SysEthContract sysEthContract =new SysEthContract();
        if(maxid!=null)
        sysEthContract.setId(maxid);
        return sysEthContractMapperDao.findAll("findEthAndContractList", sysEthContract);
    }

    @Override
    public SysEthContract findEthContractConfig(String coinname) throws Exception {
        return sysEthContractMapperDao.find("findEthContractConfig", coinname);
    }
}