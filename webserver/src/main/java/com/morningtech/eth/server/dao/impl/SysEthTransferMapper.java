package com.morningtech.eth.server.dao.impl;


import com.morningtech.eth.server.dao.SysEthTransferMapperDao;
import com.morningtech.eth.server.entity.SysEthTransfer;
import org.springframework.stereotype.Service;

@Service
public class SysEthTransferMapper  extends BaseMapper<SysEthTransfer> implements SysEthTransferMapperDao {

    @Override
    public int insert(String statement, Object obj) throws Exception {
        return writeMapper.insert(statement, obj);
    }
}